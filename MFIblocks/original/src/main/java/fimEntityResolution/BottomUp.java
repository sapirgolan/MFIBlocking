package fimEntityResolution;

import dnl.utils.text.table.TextTable;
import fimEntityResolution.entityResulution.EntityResolutionFactory;
import fimEntityResolution.entityResulution.EntityResulutionComparisonType;
import fimEntityResolution.entityResulution.IComparison;
import fimEntityResolution.statistics.*;
import fimEntityResolution.statistics.Timer;
import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.data.structure.BitMatrix;
import il.ac.technion.ie.model.*;
import il.ac.technion.ie.output.writers.Writer;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPotential;
import il.ac.technion.ie.potential.model.SharedMatrix;
import il.ac.technion.ie.potential.service.PotentialService;
import il.ac.technion.ie.potential.service.iPotentialService;
import il.ac.technion.ie.search.core.SearchEngine;
import il.ac.technion.ie.search.module.ComparisonInteraction;
import il.ac.technion.ie.service.BlockService;
import il.ac.technion.ie.service.iBlockService;
import il.ac.technion.ie.types.Alg;
import il.ac.technion.ie.types.MFISetsCheckConfiguration;
import il.ac.technion.ie.utils.FrequentItemsetContext;
import il.ac.technion.ie.utils.Utilities;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class BottomUp {
	
    static final Logger logger = Logger.getLogger(BottomUp.class);
	private final static String FI_DIR = "FIs";
	private final static String TEMP_RECORD_DIR = "TEMP_RECORD_DIR";
	private final static File TempDir = new File(TEMP_RECORD_DIR);
	private final static double MAX_SUPP_CONST = 1.0;//0.005;
    public static BitSet coveredRecords= null;
	public static String srcFile = null;
	private static double NG_LIMIT = 3;
	private static double lastUsedBlockingThreshold;
	private static MfiContext context;

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
		9. Blocks output format [S,Path], [B], [N] (S-statistics+blocks with a path of original CSV, B-only blocks, N-no print)
		10.Size of the first dataset (optional, only for 2 and more dataset files)
	 */
	public static void main(String[] args){
		context = readArguments(args);
		//StringSimToolsLocal.init(context);
		enterPerformanceModeIfNeeded( context.isInPerformanceMode() );
		
		createSparkContext( context.getConfig() );
		
		System.out.println("Entered Main");	
		String currDir = new File(".").getAbsolutePath();
		System.out.println("Working dir: " + currDir);	
		System.out.println("args.length : " + args.length);
		System.out.println("Main srcFile : " + srcFile);
		long start = System.currentTimeMillis();
		RecordSet.readRecords(context);
		int numOfRecords = RecordSet.DB_SIZE;
		System.out.println("After reading records numOfRecords=" + numOfRecords);
		System.out.println("Time to read records " + (System.currentTimeMillis()-start)/1000.0 + " seconds");
		//System.out.println("DEBUG: Size of records: " + MemoryUtil.deepMemoryUsageOfAll(RecordSet.values.values(), VisibilityFilter.ALL)/Math.pow(2,30) + " GB");
		start = System.currentTimeMillis();
		Utilities.parseLexiconFile(context.getLexiconFile(),context.getPrntFormat());
		System.out.println("Time to read items (lexicon) " + (System.currentTimeMillis()-start)/1000.0 + " seconds");
		//System.out.println("DEBUG: Size of lexicon: " + MemoryUtil.deepMemoryUsageOfAll(Utilities.globalItemsMap.values(), VisibilityFilter.ALL)/Math.pow(2,30) + " GB");
				
		start = System.currentTimeMillis();
		mfiBlocksCore();
		System.out.println("Total time for algorithm " + (System.currentTimeMillis()-start)/1000.0 + " seconds");	
	}

	private static void createSparkContext(MFISetsCheckConfiguration config) {
		if (config.equals(MFISetsCheckConfiguration.SPARK)) {
			//System.setProperty("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
			//System.setProperty("spark.kryo.registrator", "fimEntityResolution.MyRegistrator");
			System.setProperty("spark.executor.memory", "2g");
			System.setProperty("hadoop.home.dir", "C:\\workspace\\winutils\\");
			//System.getProperty("spark.akka.askTimeout","50000");
			Runtime runtime = Runtime.getRuntime();
			runtime.gc();
		}
	}
	
	private static void enterPerformanceModeIfNeeded(boolean inPerformanceMode) {
		if( inPerformanceMode ){
			System.out.println("You have started the application in profiling mode for performce");
			System.out.println("Start your profiler and then hit any key on the console");
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

    /**
     * The args input looks like the following:
     * SPARK [Configuration]
     * datasets/lexicon.txt [LexiconFile]
     * datasets/ids.txt [RecordsFile]
     * 0,0.05,0.1,0.15,0.75,0.8,0.95,1 [MinBlockingThresholds]
     * datasets/matching.txt [MatchFile]
     * datasets/NoSW.txt [OrigRecordsFileWithoutCommas]
     * <PATH_TO_FILE>/originalRecordsFile.csv [OriginalRecordsPath]
     * 2 [MinSup]
     * 1.5,2,3,5,10,20 [NG]
     * N [PrintFormat]
     *
     * @param args
     * @return
     */
    private static MfiContext readArguments(String[] args) {
		MfiContext context = new MfiContext();
		context.setConfiguration(args[0]);
		context.setLexiconFile(args[1]);
		context.setRecordsFile(args[2]);
		context.setMinBlockingThresholds(args[3]);
		context.setMatchFile(args[4]);
        context.setOrigRecordsFileWithoutCommas(args[5]);
        context.setOriginalRecordsPath(args[6]);
        context.setDatasetName(args[7]);
        context.setMinSup(args[8]);
        context.setAlgorithm(Alg.MFI);
        context.setNGs(args[9]);
        context.setFirstDbSize(args);
        context.setPerformanceFlag(args);
        context.setPrintFormat(args[10]);
        return context;
    }

	/**
	 * Core of the MFIBlocks algorithm
	 */
	public static void mfiBlocksCore() {

		int recordsSize = RecordSet.size;
		System.out.println("order of minsups used: " + Arrays.toString(context.getMinSup()));
		List<BlockingRunResult> blockingRunResults = new ArrayList<>();
		//iterate for each neighborhood grow value that was set in input
		double[] neighborhoodGrowth = context.getNeighborhoodGrowth();
		SearchEngine engine = createAndInitSearchEngine(context.getRecordsFile());
		IComparison comparison = EntityResolutionFactory.createComparison(EntityResulutionComparisonType.Jaccard, engine);
		for(double neighborhoodGrow: neighborhoodGrowth){
			NG_LIMIT = neighborhoodGrow;

			double[] minBlockingThresholds = context.getMinBlockingThresholds();
			for (double minBlockingThreshold : minBlockingThresholds) { // test for each minimum blocking threshold
				coveredRecords = new BitSet(recordsSize+1);
				coveredRecords.set(0,true); // no such record
                logger.info("running iterative " + context.getAlgName() + "s with minimum blocking threshold " + minBlockingThreshold +
                        " and NGLimit: " + NG_LIMIT);
				System.out.println("running iterative " + context.getAlgName() + "s with minimum blocking threshold " + minBlockingThreshold +
						" and NGLimit: " + NG_LIMIT);
                Timer timer = new Timer();
                //obtain all the clusters that has the minimum score
				CandidatePairs algorithmObtainedPairs = getClustersToUse(context, minBlockingThreshold);
                timer.startActionTimeMeassurment();

                List<Block> algorithmBlocks = findBlocks(algorithmObtainedPairs, true, recordsSize);
                Writer.printNeighborsAndBlocks(algorithmObtainedPairs, context, algorithmBlocks);
                Map<Integer, List<BlockDescriptor>> blocksAmbiguousRepresentatives = findBlocksAmbiguousRepresentatives(algorithmBlocks, context);
                Writer.printAmbiguousRepresentatives(blocksAmbiguousRepresentatives, context);

                //Fetching and printing local potential from algorithmBlocks
                iPotentialService potentialService = new PotentialService();
                List<BlockPotential> localPotential = potentialService.getLocalPotential(algorithmBlocks);
                AdjustedMatrix adjustedMatrix = potentialService.getAdjustedMatrix(algorithmBlocks);
				List<SharedMatrix> sharedMatrices = potentialService.getSharedMatrices(algorithmBlocks);
				Writer.printBlockPotential(localPotential, adjustedMatrix, sharedMatrices, context);
                long writeBlocksDuration = timer.getActionTimeDuration();

                timer.startActionTimeMeassurment();
                TrueClusters trueClusters = new TrueClusters();
				trueClusters.findClustersAssingments(context.getMatchFile());

                List<Block> trueBlocks = findBlocks(trueClusters.getGroundTruthCandidatePairs(), false, recordsSize);

                NonBinaryResults nonBinaryResults = new NonBinaryResults(algorithmBlocks, trueBlocks);
                ExperimentResult experimentResult = new ExperimentResult(trueClusters, algorithmObtainedPairs, recordsSize);

                StatisticMeasurements results = experimentResult.calculate();
                long totalMaxRecallCalculationDuration = timer.getActionTimeDuration();

                long timeOfERComparison = comparison.measureComparisonExecution(algorithmObtainedPairs);
                double executionTime = calcExecutionTime(timer.getStartTime(), totalMaxRecallCalculationDuration, writeBlocksDuration);

                BlockingResultContext resultContext = new BlockingResultContext(results, nonBinaryResults,
                        minBlockingThreshold, lastUsedBlockingThreshold, NG_LIMIT,
						executionTime, Utilities.convertToSeconds(timeOfERComparison));

				BlockingRunResult blockingRR = new BlockingRunResult(resultContext);
				blockingRunResults.add(blockingRR);
				
				System.out.println("");
				System.out.println("");
			}
		}
			
		if(!blockingRunResults.isEmpty()){
			printExperimentMeasurments(blockingRunResults);
			String resultsString = writeBlockingRR(blockingRunResults);
			System.out.println();
			System.out.println(resultsString);
		}
		else{
			System.out.println("Under current configuration, no clustering were achieved!!");
		}		
	}

    private static Map<Integer, List<BlockDescriptor>> findBlocksAmbiguousRepresentatives(List<Block> algorithmBlocks, MfiContext context) {
        iBlockService blockService = new BlockService();
        Map<Integer, List<BlockDescriptor>> ambiguousRepresentatives = blockService.findAmbiguousRepresentatives(algorithmBlocks, context);
        return ambiguousRepresentatives;
    }

    /**
     *
     * @param candidatePairs
     * @param isAlgorithmResults - whether or not to calc probabilities on given CandidatePairs
     * @param recordsSize
     * @return
     */
    private static List<Block> findBlocks(CandidatePairs candidatePairs, boolean isAlgorithmResults, int recordsSize) {
        iBlockService blockService = new BlockService();
        List<Block> blocks = blockService.getBlocks(candidatePairs, recordsSize);
        if (isAlgorithmResults) {
            blockService.calcProbOnBlocks(blocks, context);
        } else {
            blockService.setTrueMatch(blocks);
        }
        return blocks;
    }

    private static double calcExecutionTime(long start,
			long totalMaxRecallCalculationDuration, long writeBlocksDuration) {
		long totalRunTime = System.currentTimeMillis() - start;
		totalRunTime = reduceIrrelevantTimes(totalRunTime, totalMaxRecallCalculationDuration, writeBlocksDuration);
		double totalRunTimeSeconds = Utilities.convertToSeconds(totalRunTime); 
		return totalRunTimeSeconds;
	}

	private static long reduceIrrelevantTimes(long totalRunTime,
                                              long totalMaxRecallCalculationDuration, long writeBlocksDuration) {
		return (totalRunTime - (totalMaxRecallCalculationDuration + writeBlocksDuration));
	}


	private static SearchEngine createAndInitSearchEngine(String recordsFile) {
		SearchEngine engine = new SearchEngine(new ComparisonInteraction());
		engine.addRecords(recordsFile);
		return engine;
	}

	private static void printExperimentMeasurments( List<BlockingRunResult> blockingRunResults) {
		String[] columnNames = {}; 
		if (!blockingRunResults.isEmpty()) {
			columnNames = BlockingRunResult.getColumnsNames();
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

	private static CandidatePairs getClustersToUse(MfiContext context, double minBlockingThreshold){
		coveredRecords.set(0,true); // no such record
		
		int[] minimumSupports = context.getMinSup();
		double[] usedThresholds = new double[minimumSupports.length];
		File mfiDir = new File(FI_DIR);
		if(!mfiDir.exists()){
			if(!mfiDir.mkdir()) {
                logger.error("Failed to create directory " + mfiDir.getAbsolutePath());
                System.out.println("Failed to create directory " + mfiDir.getAbsolutePath());
            }
		}
		
		CandidatePairs allResults = new CandidatePairs(); //unlimited

        for (int i = (minimumSupports.length - 1); i >= 0 && coveredRecords.cardinality() < RecordSet.size; i--) {
            /*
		        array is sorted in ascending order begin with largest minSup
		        continue until all records have been covered OR we have completed running over all minSups
		     */
			long start = System.currentTimeMillis();
			//TODO: check content of file
			File uncoveredRecordsFile = createRecordFileFromRecords(coveredRecords, minimumSupports[i]);
            logProgress("Time to createRecordFileFromRecords" + Double.toString((double) (System.currentTimeMillis() - start) / 1000.0));

			start = System.currentTimeMillis();
			File mfiFile = Utilities.RunMFIAlg(minimumSupports[i], uncoveredRecordsFile.getAbsolutePath(), mfiDir);
			/*File mfiFile = Utilities.RunPFPGrowth(minimumSupports[i],
						RecordSet.size-coveredRecords.cardinality(),uncoveredRecordsFile.getAbsolutePath(), mfiDir);*/
            logProgress("Time to run MFI with minsup=" + minimumSupports[i] +
                    " on table of size " + (RecordSet.size - coveredRecords.cardinality()) +
                    " is " + Double.toString((double) (System.currentTimeMillis() - start) / 1000.0));
		
			start = System.currentTimeMillis();

			FrequentItemsetContext itemsetContext = createFrequentItemsetContext( mfiFile.getAbsolutePath(), minBlockingThreshold, minimumSupports[i], context);
			CandidatePairs candidatePairs;
			if (MFISetsCheckConfiguration.SPARK.equals(context.getConfig())) {
				candidatePairs = SparkBlocksReader.readFIs(itemsetContext);
			} else {
				candidatePairs = Utilities.readFIs(itemsetContext);
			}
            logProgress("Time to read MFIs: " + Double.toString((double) (System.currentTimeMillis() - start) / 1000.0) + " seconds");

			start = System.currentTimeMillis();
			BitMatrix coverageMatrix = candidatePairs.exportToBitMatrix();
			updateCoveredRecords(coveredRecords, coverageMatrix.getCoveredRows());
            logProgress("Time to updateCoveredRecords " + Double.toString((double) (System.currentTimeMillis() - start) / 1000.0) + " seconds");
			start = System.currentTimeMillis();				
			updateCandidatePairs(allResults,candidatePairs );
            logProgress("Time to updateBlockingEfficiency " + Double.toString((double) (System.currentTimeMillis() - start) / 1000.0) + " seconds");
			usedThresholds[i] = candidatePairs.getMinThresh();

			lastUsedBlockingThreshold = candidatePairs.getMinThresh();
            logProgress("lastUsedBlockingThreshold: " + lastUsedBlockingThreshold);

            logProgress("Number of covered records after running with Minsup=" +
                    minimumSupports[i] + " is " + coveredRecords.cardinality() + " out of " + RecordSet.size);
			
		}

        logProgress("Minsups used " + Arrays.toString(minimumSupports));
        logProgress("Total number of covered records under minimum blocking threshold " + minBlockingThreshold +
                " and minsups " + Arrays.toString(minimumSupports) + " is: " + coveredRecords.cardinality() + " out of " + RecordSet.size +
                " which are: " + 100 * (coveredRecords.cardinality() / RecordSet.size) + "%");

        logProgress("After adding uncovered records: Total number of covered records under blocking threshold " + minBlockingThreshold +
                " and minsups " + Arrays.toString(minimumSupports) + " is: " + coveredRecords.cardinality() + " out of " + RecordSet.size +
                " which are: " + 100 * (coveredRecords.cardinality() / RecordSet.size) + "%");
		int firstDbSize = context.getFirstDbSize();
		if (firstDbSize>0) {
			allResults=removePairsSameSet(allResults,firstDbSize);
		}
		
		return allResults;
	}

    /**
     * This method is usage until logger will be productive in code
     * @param message
     */
    private static void logProgress(String message) {
        logger.info(message);
        System.out.println(message);
    }

    private static FrequentItemsetContext createFrequentItemsetContext(
			String absolutePath, double minBlockingThreshold, int minimumSupport,
			MfiContext mfiContext) {
		FrequentItemsetContext itemsetContext = new FrequentItemsetContext();
		itemsetContext.setAbsolutePath(absolutePath);
		itemsetContext.setMinBlockingThreshold(minBlockingThreshold);
		itemsetContext.setMinimumSupport(minimumSupport);
		itemsetContext.setMfiContext(mfiContext);
		itemsetContext.setGolbalItemsMap(Utilities.globalItemsMap);
		itemsetContext.setNeighborhoodGrowthLimit(NG_LIMIT);
		
		return itemsetContext;
	}
	
	private static CandidatePairs removePairsSameSet(CandidatePairs actualCPs, int firstDbSize) {
		System.out.println("Excluding pairs if records from the same set..");
		CandidatePairs updatedPairs=new CandidatePairs();
		long start=System.currentTimeMillis();
		for (Entry<Integer,RecordMatches> entry: actualCPs.getAllMatches().entrySet()) { //run over all records
			for (CandidateMatch cm : entry.getValue().getCandidateMatches()) { //for each record, check out its match
				if ( 	(entry.getKey()>firstDbSize && cm.getRecordId()>firstDbSize) ||
						(entry.getKey()<firstDbSize && cm.getRecordId()<firstDbSize)) 
					continue;
//				if  (entry.getKey()>firstDbSize || cm.getRecordId()>firstDbSize) //for CDDB experiemtns (also add 500 to the command inputs)
//					continue;
				else
                {
					updatedPairs.setPair(entry.getKey(), cm.getRecordId(), actualCPs.getMinThresh());
                }
			}
		}
		System.out.println("Time exclude pairs : " + Double.toString((double)(System.currentTimeMillis() - start)/1000.0) + " seconds");
		return updatedPairs;
	}

	private static void updateCoveredRecords(BitSet coveredRecords, BitSet coveredRows){
		coveredRecords.or(coveredRows);
	}
	
	private static File createRecordFileFromRecords(BitSet coveredRecords, int minSup){		
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
		
		Map<Integer,Integer> appItems = appitems(coveredRecords, minSup);
		
		BufferedWriter writer = null;
		int numOfWrittenLines=0;
		try {
			outputFle.delete();
			outputFle.createNewFile();
			writer = new BufferedWriter(new FileWriter(outputFle));
			
			for(int i=coveredRecords.nextClearBit(0); i>=0 && i <= RecordSet.size ; i=coveredRecords.nextClearBit(i+1)){
				Record currRecord = RecordSet.values.get(i);
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
	
	private static Map<Integer,Integer> appitems(BitSet coveredRecords, int minSup){
		Map<Integer,Integer> retVal = new HashMap<>();
		for( int i=coveredRecords.nextClearBit(0); i>=0 && i <= RecordSet.size ; i=coveredRecords.nextClearBit(i+1) ){
			Record currRecord = RecordSet.values.get(i);		
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
		double DBSize = RecordSet.size - coveredRecords.cardinality();
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
	
	private static void updateCandidatePairs(CandidatePairs allResults, final CandidatePairs coveragePairs){	
		allResults.addAll(coveragePairs);
		
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

}
