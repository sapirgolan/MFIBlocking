package fimEntityResolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.spark.api.java.JavaSparkContext;
import org.neo4j.graphdb.GraphDatabaseService;

import candidateMatches.CandidatePairs;

import com.javamex.classmexer.MemoryUtil;
import com.javamex.classmexer.MemoryUtil.VisibilityFilter;

import dnl.utils.text.table.TextTable;
import fimEntityResolution.entityResulution.EntityResolutionFactory;
import fimEntityResolution.entityResulution.EntityResulutionComparisonType;
import fimEntityResolution.entityResulution.ExecuteJaccardComparisons;
import fimEntityResolution.entityResulution.IComparison;
import fimEntityResolution.pools.BitMatrixPool;
import fimEntityResolution.statistics.BlockingResultsSummary;
import fimEntityResolution.statistics.BlockingRunResult;
import fimEntityResolution.statistics.DuplicateBusinessLayer;
import fimEntityResolution.statistics.StatisticMeasuremnts;

public class BottomUp {
	
	public static JavaSparkContext sc;
	public static int DEDUP_MIN_SUP = 2;
	private final static String FI_DIR = "FIs";
	private static double NG_LIMIT = 3;
	private static double lastUsedBlockingThreshold;
	public enum Alg{
		CFI,
		MFI
	}
	public enum Configuration{
		SPARK,DEFAULT
	}
	
	public static String srcFile = null;
	/**
	 * The Main of the MFIBlocking Algorithm
	 * @param args
	 * The parameters are as follows:
	 *  0. Cofiguration - SPARK or DEFAULT
		1. Path to the lexicon file created in the previous stage (out parameter 6).
		2. The dataset of item ids created in the previous stage (out parameter 2).
		3. The set of min_th parameters you would like the algorithm to run on. This parameter is optional and 0 can be provided as the only parameter effectively eliminating the threshold.
		4. Path to the generated match file (out parameter 3).
		5. path to the debug file containing the items themselves (out parameter 7)
		6. The set of min supports to use.
		7. Must be set to MFI
		8. The set of p parameters to use as the Neighberhood Growth constraints.
	 */
	public static void main(String[] args){
		Configuration config = Configuration.valueOf(args[0]);
		if (config.equals(Configuration.SPARK)) {
			System.setProperty("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
			System.setProperty("spark.kryo.registrator", "fimEntityResolution.MyRegistrator");
			System.setProperty("spark.executor.memory", "5g");
			//System.getProperty("spark.akka.askTimeout","50000");
			Runtime runtime = Runtime.getRuntime();
			runtime.gc();
			int numOfCores = runtime.availableProcessors();
			sc=new JavaSparkContext("local["+numOfCores+"]", "App",
					"$SPARK_HOME", new String[]{"target/original-0.0.1.jar"});
			//System.out.println("spark.akka.askTimeout = " + System.getProperty("spark.akka.askTimeout"));
		}
		System.out.println("Entered Main");	
		String currDir = new File(".").getAbsolutePath();
		System.out.println("Working dir: " + currDir);	
		String lexiconFile = args[1];
		StringSimToolsLocal.LEXICON_FILE=lexiconFile;
		String recordsFile = args[2];
		StringSimToolsLocal.RECORDS_FILE=recordsFile;
		String minBlockingThresholds = args[3];				
		double[] dMinBlockingThresholds = getThresholds(minBlockingThresholds);
		String matchFile = args[4];
		String origRecordsFile = args[5];
		StringSimToolsLocal.ORIGRECORDS_FILE=origRecordsFile;
		int[] minSups = getInts(args[6]);
		Alg alg = Alg.valueOf(args[7]);	
		double[] NGs = getDoubles(args[8]);		
		if(args.length > 9 && args[9] != null){
			if ("perf".equalsIgnoreCase(args[9])) {
				System.out.println("You have started the application in profiling mode for performce");
				System.out.println("Start your profiler and then hit any key on the console");
				try {
					System.in.read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				srcFile = args[8];
				srcFile = args[9];
			}
		}
		
		System.out.println("args.length : " + args.length);
		System.out.println("Main srcFile : " + srcFile);
		long start = System.currentTimeMillis();
		Map<Integer,Record> records = Utilities.readRecords(recordsFile,origRecordsFile,srcFile);
		int numOfrecords = Utilities.DB_SIZE;
		System.out.println("After reading records numOfrecords=" + numOfrecords);
		System.out.println("Time to read records " + (System.currentTimeMillis()-start)/1000.0 + " seconds");
		System.out.println("DEBUG: Size of recods: " + MemoryUtil.deepMemoryUsageOfAll(records.values(), VisibilityFilter.ALL)/Math.pow(2,30) + " GB");
		start = System.currentTimeMillis();
		Utilities.parseLexiconFile(lexiconFile);
		System.out.println("Time to read items (lexicon) " + (System.currentTimeMillis()-start)/1000.0 + " seconds");
		System.out.println("DEBUG: Size of lexicon: " + MemoryUtil.deepMemoryUsageOfAll(Utilities.globalItemsMap.values(), VisibilityFilter.ALL)/Math.pow(2,30) + " GB");
				
		start = System.currentTimeMillis();
		mfiBlocksCore(config,records,matchFile,minSups,dMinBlockingThresholds,alg,NGs,lexiconFile,recordsFile,origRecordsFile);
		System.out.println("Total time for algorithm " + (System.currentTimeMillis()-start)/1000.0 + " seconds");	
	}
	
	public static BitSet coveredRecords= null;
	private static int FP = 0;	
	private static int numComparisons = 0;
	/**
	 * Core of the MFIBlocks algorithm
	 * @param records
	 * @param matchFile
	 * @param minSups
	 * @param minBlockingThresholds
	 * @param alg
	 * @param NGs
	 */
	public static void mfiBlocksCore(Configuration config,Map<Integer,Record> records, String matchFile,int[] minSups, 
			double[] minBlockingThresholds, Alg alg, double[] NGs, String lexiconFile,
			String recordsFile,String origRecordsFile){
		Arrays.sort(minSups);
		System.out.println("order of minsups used: " + Arrays.toString(minSups));
		List<BlockingRunResult> blockingRunResults= new ArrayList<BlockingRunResult>();
		//iterate for each neighborhood grow value that was set in input 
		for(double neiborhoodGrow: NGs){
			NG_LIMIT = neiborhoodGrow;
		
			for (double minBlockingThreshold : minBlockingThresholds) { // test for each minimum blocking threshold
				coveredRecords = new BitSet(records.size()+1);
				coveredRecords.set(0,true); // no such record
				FP = 0;
				numComparisons = 0;
				//clearTrueClusters(trueClusters);
				System.out.println("running iterative " + alg.toString() + "s with minimum blocking threshold " + minBlockingThreshold +
						" and NGLimit: " + NG_LIMIT);			
				long start = System.currentTimeMillis();
				//obtain all the clusters that has the minimum score
				CandidatePairs algorithmObtainedPairs = getClustersToUse(config,records,minSups,minBlockingThreshold,lexiconFile,recordsFile,origRecordsFile);
				long actionStart = System.currentTimeMillis();
				writeCandidatePairs(algorithmObtainedPairs);
				long writeBlocksDuration = System.currentTimeMillis() - actionStart;
				
				actionStart = System.currentTimeMillis();
				TrueClusters trueClusters = new TrueClusters(Utilities.DB_SIZE, matchFile);
				System.out.println("DEBUG: Size of trueClusters: " + MemoryUtil.deepMemoryUsageOf(trueClusters, VisibilityFilter.ALL)/Math.pow(2,30) + " GB");				
				StatisticMeasuremnts results = calculateFinalResults(trueClusters, algorithmObtainedPairs, records.size());
				long totalMaxRecallCalculationDuration = System.currentTimeMillis() - actionStart;
				IComparison comparison = EntityResolutionFactory.createComparison(EntityResulutionComparisonType.Jaccard);
				long timeOfComparison = comparison.measureComparisonExecution(trueClusters.getGroundTruthCandidatePairs(), algorithmObtainedPairs);
				BlockingRunResult blockingRR = new BlockingRunResult(results, minBlockingThreshold, lastUsedBlockingThreshold,
						NG_LIMIT,(double)(System.currentTimeMillis()-start-totalMaxRecallCalculationDuration-writeBlocksDuration)/1000.0);
				blockingRunResults.add(blockingRR);
				
				System.out.println("");
				System.out.println("");
			}
		}
			
		if(blockingRunResults != null && blockingRunResults.size() > 0){
			printExperimentMeasurments(blockingRunResults);
			String resultsString = writeBlockingRR(blockingRunResults);
			System.out.println();
			System.out.println(resultsString);
		}
		else{
			System.out.println("Under current configuration, no clustering were achienved!!");
		}		
	}
	
	/**
	 * the method write the blocking output to a file for later usage
	 * @param cps
	 */
	private static void writeCandidatePairs(CandidatePairs cps) {
		ResultWriter resultWriter = new ResultWriter();
		File outputFile = resultWriter.createOutputFile();
		try {
			resultWriter.writeBlocks(outputFile, cps);
		} catch (IOException e) {
			System.err.println("***Failed to write blocks***");
			e.printStackTrace();
			return;
		}
		System.out.println("Outfile was written to: " + outputFile.getAbsolutePath());
	}


	private static void printExperimentMeasurments( List<BlockingRunResult> blockingRunResults) {
		String[] columnNames = {}; 
		if (blockingRunResults.size()>0) {
			columnNames = blockingRunResults.get(0).getCoulmnsName();
		}
		Object[][] rows = new Object [blockingRunResults.size()][columnNames.length];
		int index = 0;
		
		for (BlockingRunResult blockingRunResult : blockingRunResults) {
			Object[] row = blockingRunResult.getValues();
			rows[index] = row;
			index++;
		}
		
		TextTable textTable = new TextTable(columnNames, rows);
		textTable.setAddRowNumbering(true);
		textTable.printTable();
	}

	private static double[] getThresholds(String strDoubles){
		String[] thresholds = strDoubles.split(",");
		double[] dThresholds = new double[thresholds.length];
		for(int i=0 ; i < thresholds.length ; i++ ){
			dThresholds[i] = Double.parseDouble(thresholds[i].trim());
		}
		return dThresholds;
	}
	
	private static int[] getInts(String strInts){
		String[] intStrs = strInts.trim().split(",");
		int[] ints = new int[intStrs.length];
		for(int i=0 ; i < intStrs.length ; i++ ){
			ints[i] = Integer.parseInt(intStrs[i].trim());
		}
		return ints;
	}
	
	private static double[] getDoubles(String strDbs){
		String[] dbStrs = strDbs.trim().split(",");
		double[] dbs = new double[dbStrs.length];
		for(int i=0 ; i < dbStrs.length ; i++ ){
			dbs[i] = Double.parseDouble(dbStrs[i].trim());
		}
		return dbs;
	}
	
	
	public final static double THRESH_STEP = 0.05;
	private final static int HIGH_VAL = (int) (1/THRESH_STEP+1);
	
	private static int getSNIndex(Map<Integer, BitMatrix> coverageMap, Map<Integer,Record> records, double ngLimit, double minThresh){
		int begIndex = Utilities.getIntForThresh(minThresh);
		System.out.println("entered  getSNIndex minThresh= " + minThresh + " beggining from " + begIndex);	
		System.out.println("coverageMap.size() =" + coverageMap.size());
		
		for(int i= begIndex ; i < 1/THRESH_STEP+1 ; i++){
			if(coverageMap.containsKey(i) && coverageMap.get(i) != null){
				if(checkSNConstraint(coverageMap.get(i),records,ngLimit)){		
					System.out.println("about to return " + i);
					System.out.println("(coverageMap.get(i) == null) " + (coverageMap.get(i) == null));
					return i;
				}
			}
		}
		
		System.out.println("returnin coverageMap.size() " + 1/THRESH_STEP+1);
		return (int) (1/THRESH_STEP+1);
	}
	
	private static int getSNIndexDB(Map<Integer, GDS_NG> coverageMap, double ngLimit, double minThresh){
		int begIndex = Utilities.getIntForThresh(minThresh);
		System.out.println("entered  getSNIndex minThresh= " + minThresh + " beggining from " + begIndex);	
		System.out.println("coverageMap.size() =" + coverageMap.size());
		for(int i= begIndex ; i < 1/THRESH_STEP+1 ; i++){
			if(coverageMap.containsKey(i) && coverageMap.get(i) != null){
				if(checkSNConstraintDB(coverageMap.get(i),ngLimit)){		
					System.out.println("about to return " + i);
					System.out.println("(coverageMap.get(i) == null) " + (coverageMap.get(i) == null));
					return i;
				}
			}
		}
		
		System.out.println("returnin coverageMap.size() " + 1/THRESH_STEP+1);
		return (int) (1/THRESH_STEP+1);
	}
		
	private static CandidatePairs getClustersToUse(Configuration config,Map<Integer,Record> records,int[] minSups, double minBlockingThreshold, String lexiconFile,String recordsFile, String origRecordsFile){
		Arrays.sort(minSups);
		coveredRecords.set(0,true); // no such record
		double[] usedThresholds = new double[minSups.length];		
		File mfiDir = new File(FI_DIR);
		if(!mfiDir.exists()){
			if(!mfiDir.mkdir())
				System.out.println("Failed to create directory " + mfiDir.getAbsolutePath());
		}
		CandidatePairs allResults = new CandidatePairs(); //unlimited
		
		for(int i=(minSups.length - 1) ; i >=0  && coveredRecords.cardinality() < records.size(); i--){ // array is sorted in ascending order -
			//begin with largest minSup
			//continue until all records have been covered OR we have completed running over all minSups			
			long start = System.currentTimeMillis();
			//TODO: check content of file
			File uncoveredRecordsFile = createRecordFileFromRecords(coveredRecords,records, minSups[i]);	
			System.out.println("Time to createRecordFileFromRecords" +Double.toString((double)(System.currentTimeMillis()-start)/1000.0));
				
			start = System.currentTimeMillis();
			File mfiFile = Utilities.RunMFIAlg(minSups[i], uncoveredRecordsFile.getAbsolutePath(), mfiDir);
			System.out.println("Time to run MFI with minsup="+minSups[i] +
					" on table of size " + (records.size()-coveredRecords.cardinality()) + 
					" is " + Double.toString((double)(System.currentTimeMillis()-start)/1000.0));
		
			start = System.currentTimeMillis();
			//coverageMap = Utilities.readFIs(mfiFile.getAbsolutePath(),Utilities.globalItemsMap, minBlockingThreshold,records,minSups[i],NG_LIMIT);
			CandidatePairs candidatePairs=null;
			if (config.equals(Configuration.SPARK)){	
				candidatePairs = SparkBlocksReader.readFIs(mfiFile.getAbsolutePath(),Utilities.globalItemsMap, minBlockingThreshold,records,minSups[i],NG_LIMIT,lexiconFile,recordsFile,origRecordsFile);
			}
			else {
				candidatePairs = Utilities.readFIs(mfiFile.getAbsolutePath(),Utilities.globalItemsMap, minBlockingThreshold,records,minSups[i],NG_LIMIT);
			}
			System.out.println("Time to read MFIs: " + Double.toString((double)(System.currentTimeMillis() - start)/1000.0) + " seconds");
			
					
			start = System.currentTimeMillis();
			BitMatrix coveragematrix = candidatePairs.exportToBitMatrix();
			updateCoveredRecords(coveredRecords,coveragematrix.getCoveredRows());
			System.out.println("Time to updateCoveredRecords " + Double.toString((double)(System.currentTimeMillis() - start)/1000.0) + " seconds");				
			start = System.currentTimeMillis();				
			updateCandidatePairs(allResults,candidatePairs );
			coveragematrix = null; 
			System.out.println("Time to updateBlockingEfficiency " + Double.toString((double)(System.currentTimeMillis() - start)/1000.0) + " seconds");
			usedThresholds[i] = candidatePairs.getMinThresh();

			lastUsedBlockingThreshold = candidatePairs.getMinThresh();
			candidatePairs = null;
			System.out.println("lastUsedBlockingThreshold: " + lastUsedBlockingThreshold);
				
			
			System.out.println("Number of covered records after running with Minsup=" +
					minSups[i] +  " is " + coveredRecords.cardinality() + " out of " + records.size());
			
			System.out.println("memory statuses:");
			System.out.println("DEBUG: Size of coveredRecords: " + MemoryUtil.deepMemoryUsageOf(coveredRecords,VisibilityFilter.ALL)/Math.pow(2,30) + " GB");
			//System.out.println("DEBUG: Size of resultMatrix: " + MemoryUtil.deepMemoryUsageOf(resultMatrix,VisibilityFilter.ALL)/Math.pow(2,30) + " GB");
			System.out.println("DEBUG: Size of allResults: " + allResults.memoryUsage() + " GB");
			
				
		}
		System.out.println("Minsups used " + Arrays.toString(minSups));
		System.out.println("Total number of covered records under minimum blocking threshold " + minBlockingThreshold + 
				" and minsups " + Arrays.toString(minSups) + " is: " + coveredRecords.cardinality() + " out of " + records.size() + 
				" which are: " + 100*(coveredRecords.cardinality()/records.size()) + "%");		

		System.out.println("After adding uncovered records: Total number of covered records under blocking threshold " + minBlockingThreshold + 
				" and minsups " + Arrays.toString(minSups) + " is: " + coveredRecords.cardinality() + " out of " + records.size() + 
				" which are: " + 100*(coveredRecords.cardinality()/records.size()) + "%");
		
		return allResults;
		
	}
	
	private static GDS_NG getClustersToUseDB(Map<Integer,Record> records,int[] minSups, double minBlockingThreshold){
		Arrays.sort(minSups);
	//	int totalNumOfClusters = 0;				
		coveredRecords.set(0,true); // no such record
		double[] usedThresholds = new double[minSups.length];		
		File mfiDir = new File(FI_DIR);
		if(!mfiDir.exists()){
			if(!mfiDir.mkdir())
				System.out.println("Failed to create directory " + mfiDir.getAbsolutePath());
		}
		GDS_NG resultMatrix = new GDS_NG();
		for(int i=(minSups.length - 1) ; i >=0  && coveredRecords.cardinality() < Utilities.DB_SIZE; i--){ // array is sorted in ascending order -
			resultMatrix.setNGLimit(NG_LIMIT*minSups[i]);
			//begin with largest minSup
			//continue until all records have been covered OR we have completed running over all minSups			
			Map<Integer, GDS_NG> coverageMapDB = null;			
			long start = System.currentTimeMillis();
			//File uncoveredRecordsFile = createRecordFileFromRecordsDB(coveredRecords,recordsDB, minSups[i]);
			File uncoveredRecordsFile = createRecordFileFromRecords(coveredRecords,records, minSups[i]);
			System.out.println("Time to createRecordFileFromRecords" +Double.toString((double)(System.currentTimeMillis()-start)/1000.0));
				
			start = System.currentTimeMillis();
			File mfiFile = Utilities.RunMFIAlg(minSups[i], uncoveredRecordsFile.getAbsolutePath(), mfiDir);
			System.out.println("Time to run MFI with minsup="+minSups[i] +
					" on table of size " + (Utilities.DB_SIZE-coveredRecords.cardinality()) + 
					" is " + Double.toString((double)(System.currentTimeMillis()-start)/1000.0));
		
			start = System.currentTimeMillis();
			//coverageMapDB = Utilities.readFIsDB(mfiFile.getAbsolutePath(),Utilities.globalItemsMap, minBlockingThreshold,recordsDB,minSups[i],NG_LIMIT);
			coverageMapDB = Utilities.readFIsDB(mfiFile.getAbsolutePath(),Utilities.globalItemsMap, minBlockingThreshold,records,minSups[i],NG_LIMIT);
			System.out.println("Time to read MFIs: " + Double.toString((double)(System.currentTimeMillis() - start)/1000.0) + " seconds");
			
					
			start = System.currentTimeMillis();
			int SNIndex = getSNIndexDB(coverageMapDB, minSups[i]*NG_LIMIT,minBlockingThreshold);
			
			System.out.println("index returned: " + SNIndex + " corresponds to " + (SNIndex-1)*0.05 + " < score <= " + SNIndex*0.05);			
		//	List<Cluster> currRoundClusters = uniteFromIndex(SNIndex,clusterMap);
			System.out.println("Time to get clustering which maintains NG(t)<=" + NG_LIMIT + "*minsup is: " + 
					Double.toString((double)(System.currentTimeMillis() - start)/1000.0) + " seconds");
			
			
			if(SNIndex <= 20){
				start = System.currentTimeMillis();
				GDS_NG coveragematrix = coverageMapDB.get(SNIndex);
				updateCoveredRecords(coveredRecords,coveragematrix.getCoveredRows());
				System.out.println("Time to updateCoveredRecords " + Double.toString((double)(System.currentTimeMillis() - start)/1000.0) + " seconds");				
				start = System.currentTimeMillis();				
				updateResultMatrix(resultMatrix,coveragematrix.exportToBM());				
				coveragematrix = null; coverageMapDB = null;
				System.out.println("Time to updateBlockingEfficiency " + Double.toString((double)(System.currentTimeMillis() - start)/1000.0) + " seconds");
				usedThresholds[i]=(SNIndex-1)*0.05;
				lastUsedBlockingThreshold = (SNIndex-1)*0.05;
				System.gc();
			}
			else{				
					System.out.println("No threshold exists such that the NG contraint NG < " + NG_LIMIT*minSups[i] +
							" is satisfied for minsup = " + minSups[i]);
					lastUsedBlockingThreshold = usedThresholds[i] = 1.0;								
			}
		
			
			System.out.println("Number of covered records after running with Minsup=" +
					minSups[i] +  " is " + coveredRecords.cardinality() + " out of " + Utilities.DB_SIZE);
		}
		System.out.println("Minsups used " + Arrays.toString(minSups));
		System.out.println("Total number of covered records under minimum blocking threshold " + minBlockingThreshold + 
				" and minsups " + Arrays.toString(minSups) + " is: " + coveredRecords.cardinality() + " out of " + Utilities.DB_SIZE + 
				" which are: " + 100*(coveredRecords.cardinality()/Utilities.DB_SIZE) + "%");		

		System.out.println("After adding uncovered records: Total number of covered records under blocking threshold " + minBlockingThreshold + 
				" and minsups " + Arrays.toString(minSups) + " is: " + coveredRecords.cardinality() + " out of " + Utilities.DB_SIZE + 
				" which are: " + 100*(coveredRecords.cardinality()/Utilities.DB_SIZE) + "%");
		
			
		return resultMatrix;
		
	}

	private static void checkInBMs(Map<Integer, BitMatrix> coverageMap){
		for (BitMatrix bm : coverageMap.values()) {
			if(bm != null){ // already returned
				BitMatrixPool.getInstance().returnMatrix(bm);
			}
		}
	}
	
	private static boolean checkSNConstraint(BitMatrix coverageMatrix,Map<Integer,Record> records, double ngLimit){
		return (coverageMatrix.getMaxNG() <= ngLimit);	
	}
	private static boolean checkSNConstraintDB(GDS_NG coverageMatrix, double ngLimit){
		return (coverageMatrix.getMaxNG() <= ngLimit);	
	}
	
	private static void updateCoveredRecords(BitSet coveredRecords, BitSet coveredRows){
		coveredRecords.or(coveredRows);
	}
	
	private final static String TEMP_RECORD_DIR = "TEMP_RECORD_DIR";
	private final static File TempDir = new File(TEMP_RECORD_DIR);
	
	private static File createRecordFileFromRecords(BitSet coveredRecords, Map<Integer,Record> records, int minSup){		
		File outputFle = null;
		System.out.println("Directory TempDir= " + TempDir + " TempDir.getAbsolutePath()" + TempDir.getAbsolutePath());
		try {
			if(!TempDir.exists()){
				if(!TempDir.mkdir()) {						
					System.out.println("failed to create directory " + TempDir.getAbsolutePath());
				}
			}
			outputFle = File.createTempFile("records", null, TempDir);
			System.out.println("retVal= " + outputFle + " retVal.getAbsolutePath()=" + outputFle.getAbsolutePath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
		outputFle.deleteOnExit();
		if(outputFle.exists()){
			System.out.println("File " + outputFle.getAbsolutePath() + " exists right after deleteOnExit");
		}
		
		Map<Integer,Integer> appItems = appitems(coveredRecords, records, minSup);
		
		BufferedWriter writer = null;
		int numOfWrittenLines=0;
		try {
			outputFle.delete();
			outputFle.createNewFile();
			writer = new BufferedWriter(new FileWriter(outputFle));
			
			for(int i=coveredRecords.nextClearBit(0); i>=0 && i <= records.size() ; i=coveredRecords.nextClearBit(i+1)){
				Record currRecord = records.get(i);				
				String toWrite = currRecord.getNumericline(appItems.keySet());
				writer.write(toWrite);
				writer.newLine();
				numOfWrittenLines++;
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			try {
				System.out.println("Number of records written: " + numOfWrittenLines);
				writer.flush();
				writer.close();		
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return outputFle;			
	}
	
	private static File createRecordFileFromRecordsDB(BitSet coveredRecords, GraphDatabaseService records, int minSup){		
		File retVal = null;
		System.out.println("Directory TempDir= " + TempDir + " TempDir.getAbsolutePath()" + TempDir.getAbsolutePath());
		try {
			if(!TempDir.exists()){
				if(!TempDir.mkdir())						
					System.out.println("failed to create directory " + TempDir.getAbsolutePath());				
			}
			
			retVal = File.createTempFile("records", null, TempDir);
			System.out.println("retVal= " + retVal + " retVal.getAbsolutePath()=" + retVal.getAbsolutePath());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		retVal.deleteOnExit();
		if(retVal.exists()){
			System.out.println("File " + retVal.getAbsolutePath() + " exists right after deleteOnExit");
		}
		
		Map<Integer,Integer> appItems = appitemsDB(coveredRecords, records, minSup);
		
		BufferedWriter writer = null;
		int written=0;
		try {
			retVal.delete();
			retVal.createNewFile();
			writer = new BufferedWriter(new FileWriter(retVal));
			
			for(int i=coveredRecords.nextClearBit(0); i>=0 && i <= Utilities.DB_SIZE ; i=coveredRecords.nextClearBit(i+1)){
				DBRecord currRecord = new DBRecord(i);				
				String toWrite = currRecord.getNumericline(appItems.keySet());
				writer.write(toWrite);
				writer.newLine();
				written++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				System.out.println("Number of records written: " + written);
				writer.flush();
				writer.close();		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retVal;			
	}
	
	
	private final static double MAX_SUPP_CONST = 1.0;//0.005;
	private static Map<Integer,Integer> appitems(BitSet coveredRecords, Map<Integer,Record> records, int minSup){
		Map<Integer,Integer> retVal = new HashMap<Integer, Integer>();
		for( int i=coveredRecords.nextClearBit(0); i>=0 && i <= records.size() ; i=coveredRecords.nextClearBit(i+1) ){
			Record currRecord = records.get(i);		
			Set<Integer> recordItems = currRecord.getItemsToFrequency().keySet();
			for (Integer recorditem : recordItems) {
				int itemSuppSize = 1;
				if(retVal.containsKey(recorditem)){
					itemSuppSize = retVal.get(recorditem) + 1;
				}
				retVal.put(recorditem, itemSuppSize);
			}
		}
		int origSize =  retVal.size();
		System.out.println("Number of items before pruning too frequent items: " + origSize);
		double DBSize = records.size() - coveredRecords.cardinality();
		if(DBSize > 10000){			
			double removal = ((double)minSup)*DBSize*MAX_SUPP_CONST;
			Iterator<Entry<Integer,Integer>> retValIterator = retVal.entrySet().iterator();
			while(retValIterator.hasNext()){
				Entry<Integer,Integer> currItem =  retValIterator.next();
				if(currItem.getValue() > removal){
					retValIterator.remove();
				}
			}
		}
		System.out.println("Number of items AFTER pruning too frequent items: " + retVal.size());
		System.out.println("A total of : " + (origSize-retVal.size()) + " items were pruned");
		return retVal;
	}
	
	private static Map<Integer,Integer> appitemsDB(BitSet coveredRecords, GraphDatabaseService records, int minSup){
		Map<Integer,Integer> retVal = new HashMap<Integer, Integer>();
		for(int i=coveredRecords.nextClearBit(0); i>=0 && i <= Utilities.DB_SIZE ; i=coveredRecords.nextClearBit(i+1)){
			DBRecord currRecord = new DBRecord(i);
			Set<Integer> recordItems = currRecord.getItemsToFrequency().keySet();
			for (Integer recorditem : recordItems) {
				int itemSuppSize = 1;
				if(retVal.containsKey(recorditem)){
					itemSuppSize = retVal.get(recorditem) + 1;
				}
				retVal.put(recorditem, itemSuppSize);
			}
		}
		int origSize =  retVal.size();
		System.out.println("Number of items before pruning too frequent items: " + origSize);
		double DBSize = Utilities.DB_SIZE  - coveredRecords.cardinality();
		if(DBSize > 10000){			
			double removal = ((double)minSup)*DBSize*MAX_SUPP_CONST;
			Iterator<Entry<Integer,Integer>> retValIterator = retVal.entrySet().iterator();
			while(retValIterator.hasNext()){
				Entry<Integer,Integer> currItem =  retValIterator.next();
				if(currItem.getValue() > removal){
					retValIterator.remove();
				}
			}
		}
		System.out.println("Number of items AFTER pruning too frequent items: " + retVal.size());
		System.out.println("A total of : " + (origSize-retVal.size()) + " items were pruned");
		return retVal;
	}
	
	private static int sameSource = 0;
	private static int numSet = 0;
	private static void updateResultMatrix(GDS_NG resultMatrix, final BitMatrix coverageMatrix){	
		//ResultMatrix.or(coverageMatrix);
		resultMatrix.writeToDB(coverageMatrix);
	}
	
	private static void updateCandidatePairs(CandidatePairs allResults, final CandidatePairs coveragePairs){	
		allResults.addAll(coveragePairs);
	}
	
	private static void updateResultMatrix(BitMatrix resultMatrix, final BitMatrix coverageMatrix){
		resultMatrix.or(coverageMatrix);
	}
	
	public static void comparePairSets(Set<Pair> prev, Set<Pair> next){
		for (Pair pair : prev) {
			if(!next.contains(pair)){
				System.out.println("Pair " + pair.toString() + " doesn't appear in next!!");
			}
		}
		
		for (Pair pair : next) {
			if(!prev.contains(pair)){
				System.out.println("Pair " + pair.toString() + " doesn't appear in prev!!");
			}
		}
	}
	
	
	public static boolean sameSource(long r1_l, long r2_l){
		int r1 = new Long(r1_l).intValue();
		int r2 = new Long(r2_l).intValue();
		String src1 = Utilities.globalRecords.get(r1).getSrc();
		String src2 = Utilities.globalRecords.get(r2).getSrc();		
		if(src1 == null || src2 == null){ //if null then assume different sources		
			return false;
		}
		return (src1.equalsIgnoreCase(src2));
	}
	
	
	private static double[] calculateFinalResults(BitMatrix GroundTruth,BitMatrix ResultMatrix,int numOfRecords)
	{
		long start = System.currentTimeMillis();
		long numRecords = (long)numOfRecords;
		double[] TPFP = BitMatrix.TrueAndFalsePositives(GroundTruth, ResultMatrix);
		double TP = TPFP[0];		
		double FP = TPFP[1];
		double FN = BitMatrix.FalseNegatives(GroundTruth, ResultMatrix);
		
		
		double precision = TP/(TP+FP);
		double recall = TP/(TP+FN);
		double pr_f_measure = (2*precision*recall)/(precision+recall);
		
		double totalComparisons = ((numRecords * (numRecords - 1))*0.5);	
		double RR = Math.max(0.0, (1.0-((TP+FP)/totalComparisons)));		
		System.out.println("num of same source pairs: " + sameSource);
	//	System.out.println(" ResultMatrix.numOfSet() " + ResultMatrix.numOfSet());
		System.out.println("TP = " + TP +", FP= " + FP + ", FN="+ FN  + " totalComparisons= " + totalComparisons);
		System.out.println("recall = " + recall +", precision= " + precision + ", f-measure="+ pr_f_measure + " RR= " + RR);	
		double[] retVal = new double[4];
		retVal[0] = recall;
		retVal[1] = precision;
		retVal[2]=  pr_f_measure;
		retVal[3]=  RR;	
		System.out.println("time to calculateFinalResults: " + Double.toString((double)(System.currentTimeMillis()-start)/1000.0));
		return retVal;
	}
	
	/**
	 * Calculate output measurements: F-measure, Precision (PC), Recall 
	 * @param groundTruth
	 * @param resultMatrix
	 * @param numOfRecords
	 * @return
	 */
	private static StatisticMeasuremnts calculateFinalResults(TrueClusters groundTruth,CandidatePairs resultMatrix, int numOfRecords)
	{
		long start = System.currentTimeMillis();
		long numRecords = (long)numOfRecords;
		//calculate TP and FP
		double[] TPFP = groundTruth.getGroundTruthCandidatePairs().calcTrueAndFalsePositives(groundTruth.getGroundTruthCandidatePairs(), resultMatrix);
		double truePositive = TPFP[0];		
		double falsePositive = TPFP[1];
		double falseNegative =TPFP[2];
		//double falseNegative = CandidatePairs.FalseNegatives(groundTruth.groundTruthCandidatePairs(), resultMatrix);
		
		DuplicateBusinessLayer duplicateBusinessLayer = new DuplicateBusinessLayer(groundTruth.getGroundTruthCandidatePairs(),resultMatrix);
		//double totalDuplicates = duplicateBusinessLayer.getNumberOfDuplicatesInDataset();
		double totalDuplicates = groundTruth.getCardinality();
		double comparisonsMadeTPFP = truePositive + falsePositive;
		int comparisonsCouldHaveMade = duplicateBusinessLayer.getNumberOfComparisons();
		//int duplicatesFound = duplicateBusinessLayer.getNumberOfDuplicatesFound();
		int duplicatesFound = (int)comparisonsMadeTPFP;
		//double precision = duplicatesFound/(comparisonsMadeTPFP);
		double precision = truePositive/(truePositive+falsePositive);
		double recall = truePositive/(truePositive+falseNegative);
		//double recall = duplicatesFound/totalDuplicates;
		double pr_f_measure = (2*precision*recall)/(precision+recall);	
		double totalComparisonsAvailable = ((numRecords * (numRecords - 1))*0.5);	
		double reductionRatio = Math.max(0.0, (1.0-((comparisonsMadeTPFP)/totalComparisonsAvailable)));		
		System.out.println("num of same source pairs: " + sameSource);
		System.out.println("TP = " + truePositive +", FP= " + falsePositive + ", FN="+ falseNegative  + " totalComparisons= " + totalComparisonsAvailable);
		System.out.println("recall = " + recall +", precision= " + precision + ", f-measure="+ pr_f_measure + " RR= " + reductionRatio);
		StatisticMeasuremnts statisticMeasuremnts = new StatisticMeasuremnts();
		statisticMeasuremnts.setRecall(recall);
		statisticMeasuremnts.setPrecision(precision);
		statisticMeasuremnts.setFMeasure(pr_f_measure);
		statisticMeasuremnts.setRR(reductionRatio);
		
		statisticMeasuremnts.setDuplicatesFound(duplicatesFound);
		statisticMeasuremnts.setTotalDuplicates(totalDuplicates);
		statisticMeasuremnts.setComparisonsMade(comparisonsMadeTPFP);
		statisticMeasuremnts.setComparisonsCouldHaveMake(comparisonsCouldHaveMade);
		System.out.println("time to calculateFinalResults: " + Double.toString((double)(System.currentTimeMillis()-start)/1000.0));
		return statisticMeasuremnts;
	}
	
	
	private static Map<Integer,Cluster> getClustermap(Collection<Cluster> clustCollection){
		Map<Integer,Cluster> retVal = new HashMap<Integer, Cluster>(clustCollection.size());
		for (Cluster cluster : clustCollection) {
			retVal.put(cluster.getId(), cluster);
		}
		return retVal;	
	}

	public static String writeBlockingRR(Collection<BlockingRunResult> runResults){
		StringBuilder sb = new StringBuilder();
		//calculate average, Min & Max for all runs
		BlockingResultsSummary brs = new BlockingResultsSummary(runResults);
		sb.append(Utilities.NEW_LINE);
		sb.append(brs.getSummary()).append(Utilities.NEW_LINE);		
		sb.append(Utilities.NEW_LINE).append(Utilities.NEW_LINE);
		return sb.toString();
	}
	
	public static String writeRunResults(Collection<RunResult> runResults, double[] blockingThresholds, int[] minSups){
		StringBuilder sb = new StringBuilder();
		sb.append("blocking_thresh").append("\t").append("\t").append("dedup_thresh").append("\t").append("\t").append("precision")
		.append("\t").append("\t").append("recall").append("\t").append("\t").append("f-measure").append("\t").append("\t").
		append("time").append("\t").append("\t").append("max_recall")
		.append("\t").append("\t").append("CFIThresh").append(Utilities.NEW_LINE);
		for (RunResult runResult : runResults) {
			sb.append(runResult.toString()).append(Utilities.NEW_LINE);
		}
		
		sb.append(Utilities.NEW_LINE).append(Utilities.NEW_LINE);
		//write summary
		sb.append(maxRecallByBlockingThresh(runResults,blockingThresholds,minSups)).append(Utilities.NEW_LINE).append(Utilities.NEW_LINE);
		sb.append(timeByBlockingThresh(runResults,blockingThresholds,minSups));
		return sb.toString();
	}
	
	private static String maxRecallByBlockingThresh(Collection<RunResult> runResults, double[] blockingThresholds, int[] minSups){
		StringBuilder sb = new StringBuilder();
		sb.append("\t");
		for (int minSup : minSups) {
			sb.append(minSup).append("\t");
		}
		sb.append(Utilities.NEW_LINE);
		for (double blockingThresh : blockingThresholds) {
			sb.append(blockingThresh).append("\t");
			for (int minSup : minSups) {				
				for (RunResult rr : runResults) {
					if(rr.blockingThresh==blockingThresh && rr.cfiThresh==(double)minSup/(double)Utilities.globalRecords.size()){
						sb.append(rr.maxRecall).append("\t");
						break;
					}
				}
			}
			sb.append(Utilities.NEW_LINE);
		}
		return sb.toString();
	}
	
	private static String timeByBlockingThresh(Collection<RunResult> runResults, double[] blockingThresholds, int[] minSups){
		StringBuilder sb = new StringBuilder();
		sb.append("\t");
		for (int minSup : minSups) {
			sb.append(minSup).append("\t");
		}
		sb.append(Utilities.NEW_LINE);
		
		for (double blockingThresh : blockingThresholds) {
			sb.append(blockingThresh).append("\t");
			for (int minSup : minSups) {
				double timeToWrite = Integer.MAX_VALUE;
				for (RunResult rr : runResults) {					
					if(rr.blockingThresh==blockingThresh && rr.cfiThresh==(double)minSup/(double)Utilities.globalRecords.size()){
						timeToWrite = Math.min(timeToWrite, rr.timeToRunInSec);
					}
				}
				sb.append(timeToWrite).append("\t");
			}
			sb.append(Utilities.NEW_LINE);
		}
		return sb.toString();
	}
	
	public class RunResult{
		double precision;
		double recall;
		double f_measure;
		double blockingThresh;
		double internalThresh;
		long timeToRunInSec;
		double maxRecall;
		double cfiThresh;
		
		public RunResult(double[] score, double blockingThresh, double internalThresh, long timeToRunInSec, double maxRecall,
				double cfiThresh){
			this.precision = score[0];
			this.recall = score[1];
			this.f_measure = score[2];
			this.blockingThresh = blockingThresh;
			this.internalThresh = internalThresh;
			this.timeToRunInSec = timeToRunInSec;
			this.maxRecall = maxRecall;
			this.cfiThresh = cfiThresh;
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append(blockingThresh).append("\t")
			  .append(internalThresh).append("\t")
			  .append(precision).append("\t")
			  .append(recall).append("\t")
			  .append(f_measure).append("\t")
			  .append(timeToRunInSec).append("\t")
			  .append(maxRecall).append("\t")
			  .append(cfiThresh);
			return sb.toString();
		}
	}
	
}