package common;

import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
//import org.neo4j.graphdb.GraphDatabaseService;

//import com.javamex.classmexer.MemoryUtil;
//import com.javamex.classmexer.MemoryUtil.VisibilityFilter;

import common.JavaHdfsLR.DataPoint;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import candidateMatches.CandidatePairs;
import fimEntityResolution.BitMatrix;
import fimEntityResolution.FIRunnable;
import fimEntityResolution.FrequentItem;
import fimEntityResolution.GDS_NG;
import fimEntityResolution.ParsedFrequentItemSet;
import fimEntityResolution.Record;
import fimEntityResolution.StringSimTools;
import fimEntityResolution.Utilities;
import fimEntityResolution.interfaces.BitSetIF;
import fimEntityResolution.interfaces.IFRecord;
import fimEntityResolution.pools.BitMatrixPool;
import fimEntityResolution.pools.FIRunnablePool;


public class App {
	private final static String ItemsetExpression = "([0-9\\s]+)\\(([0-9]+)\\)$";

	//public static boolean DEBUG = false;
	//public static boolean WRITE_ALL_ERRORS = false;
	static Map<Integer,Record> records;
	final static int minSup=3;
	private static double NG_LIMIT = 3;
	public static int DB_SIZE;
	public static String NEW_LINE = System.getProperty("line.separator");
	//public static Map<Integer, FrequentItem> globalItemsMap;
	public static Map<Integer, Record> globalRecords;
	private static int minRecordLength = Integer.MAX_VALUE;
	//public static GraphDatabaseService recordDB;
	//private static final String RECORD_DB_PATH = "target/records-db";
	public static AtomicInteger nonFIs = new AtomicInteger(0);
	public static AtomicInteger numOfFIs = new AtomicInteger(0);
	public static AtomicLong timeSpentCalcScore = new AtomicLong(0);
	private static AtomicLong timeSpentUpdatingCoverage = new AtomicLong(0);
	public static AtomicInteger[] clusterScores = new AtomicInteger[21];	
	private static AtomicInteger numOfBMs = new AtomicInteger(0);
	public static AtomicInteger numOfGDs = new AtomicInteger(0);
	private static AtomicInteger numSet = new AtomicInteger(0);
	public static double scoreThreshold;
	
	private static void resetAtomicIntegerArr(AtomicInteger[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new AtomicInteger(0);
		}
	}

	public static void main(String[] args){
	
		//============================EXMAPLE=================================================================
		//		String logFile = "C:/workspace/spark-0.8.1-incubating/README.md"; // Should be some file on your system
		//		JavaSparkContext sc = new JavaSparkContext("local[5]", "App",
		//				"C:/workspace/spark-0.8.1-incubating/README.md", new String[]{"target/SparkTest-1.0-SNAPSHOT.jar"});
		//		JavaRDD<String> logData = sc.textFile(logFile).cache();
		//		long numAs = logData.filter(new Function<String, Boolean>() {
		//			public Boolean call(String s) { return s.contains("a"); }
		//		}).count();
		//
		//		long numBs = logData.filter(new Function<String, Boolean>() {
		//			public Boolean call(String s) { return s.contains("b"); }
		//		}).count();
		//
		//		System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);
		//=========================END OF EXAMPLE==============================================================

		/* Pseudo code:
		 * 1. Load file
		 * 2. Partition file into blocks according to number of processors 
		 * 3. On each processor:
		 * 	3.1 Check support condition 
		 * 	3.2 Check ClusterJaccard score
		 * 	3.3 Check sparse neighborhood condition
		 * 4. Accumulate the blocks
		 * 5. Derive CandidatePairs from blocks
		 */
		//0. Load records files:
		records = Utilities.readRecords("/MFIBlocksImp/exampleInputOutputFiles/DS_800_200_3_1_3_po_clean_numeric.txt",
				"/MFIBlocksImp/exampleInputOutputFiles/DS_800_200_3_1_3_po_clean_NoSW.txt",null);
		Utilities.parseLexiconFile("/MFIBlocksImp/exampleInputOutputFiles/DS_800_200_3_1_3_po_clean_lexicon_3grams.txt");
		//1. Load file
		String fmiFile = "/MFIBlocksImp/MFIs6674994509185988344.tmp";
		JavaSparkContext sc = new JavaSparkContext("local[5]", "App",
				"$SPARK_HOME", new String[]{"target/SparkTest-1.0-SNAPSHOT.jar"});
		JavaRDD<String> fmiSets = sc.textFile(fmiFile).cache();
		//2. Partition file into blocks
		//TODO: check how the distribution works
		JavaRDD<ParsedFrequentItemSet> parsedFIs = fmiSets.map(new ParseFILine());
		JavaRDD<ParsedFrequentItemSet> checkedFIs=parsedFIs.filter(new ExctractBlocks(Utilities.globalItemsMap,
				scoreThreshold, records,minSup, NG_LIMIT)).cache();
		long trueBlocks=checkedFIs.count();
		System.out.print("True blocks: "+trueBlocks);
		//JavaRDD<ParsedFrequentItemSet> 
		CandidatePairs candidatePairs=new CandidatePairs();
	}
	
	
	
	static class ExctractBlocks extends Function<ParsedFrequentItemSet,Boolean> {

		Map<Integer, FrequentItem> globalItemsMap;
		double scoreThreshold;
		Map<Integer, Record> records; 
		int minSup;
		double NG_PARAM;

		public ExctractBlocks(Map<Integer, FrequentItem> globalItemsMap,
				double scoreThreshold, Map<Integer, Record> records,
				int minSup, double nG_PARAM) {
			super();
			this.globalItemsMap = globalItemsMap;
			this.scoreThreshold = scoreThreshold;
			this.records = records;
			this.minSup = minSup;
			NG_PARAM = nG_PARAM;
		}
		
		@Override
		public Boolean call(ParsedFrequentItemSet freqItemSet) throws Exception {
			//original:
				//=============================================================================================
			double tooLarge = 0;
			double scorePruned = 0;
			App.scoreThreshold = this.scoreThreshold;
			// reset all parameters
			nonFIs.set(0);
			numOfFIs.set(0);
			timeSpentCalcScore.set(0);
			numOfBMs.set(0);
			numSet.set(0);
			//time_in_supp_calc.set(0);
			resetAtomicIntegerArr(clusterScores);	
			//ConcurrentHashMap<Integer, BitMatrix> coverageIndex = new ConcurrentHashMap<Integer, BitMatrix>(21);

			int maxSize = (int) Math.floor(minSup*NG_PARAM);

			//CandidatePairs candidatePairs = new CandidatePairs(maxSize);
			StringSimTools.numOfMFIs.set(0);
			StringSimTools.numOfRoughMFIs.set(0);
			StringSimTools.timeInGetClose.set(0);
			StringSimTools.timeInRough.set(0);
			timeSpentUpdatingCoverage.set(0);
			//BitMatrixPool.getInstance().restart();
			//FIRunnablePool.getInstance().restart();

			// 3.1 support condition 
			if (freqItemSet.supportSize > minSup * NG_PARAM) {
				//tooLarge++;
				return false;
			}
			// 3.2 ClusterJaccard score
			List<Integer> currentItemSet = freqItemSet.items;
			//TODO: check it
			double maxClusterScore = StringSimTools.MaxScore(
					freqItemSet.supportSize, currentItemSet, minRecordLength);

			if (maxClusterScore < 0.1 * App.scoreThreshold) {
				//scorePruned++;
				return false;
			}
			// 3.3 sparse neighborhood condition
			//FIRunnable FIR = FIRunnablePool.getInstance().getRunnable(
			//		currentItemSet, minSup, records, NG_PARAM,coverageIndex,candidatePairs);
			//executorService.execute(FIR);
			
			//FROM========FIRunnable.java=========================================
			BitSetIF support = null;
			support = Utilities.getItemsetSupport(currentItemSet);
			if (support.getCardinality() <  minSup) {
				App.nonFIs.incrementAndGet();					
				return false; // must be the case that the item appears minSup times
						// but in a number of records < minsup
			}			
			List<IFRecord> FISupportRecords = support.getRecords();
			//long start = System.currentTimeMillis();
			double currClusterScore = StringSimTools.softTFIDF(
					FISupportRecords, currentItemSet, App.scoreThreshold);
			FISupportRecords = null;
			//Utilities.timeSpentCalcScore.addAndGet(System.currentTimeMillis() - start);

			App.clusterScores[cellForCluster(currClusterScore)].incrementAndGet();

			if (currClusterScore > App.scoreThreshold) {
				App.numOfFIs.incrementAndGet();
				//support.markPairs(candidatePairs,currClusterScore);			
				return true;
			}
			//FROM========FIRunnable.java==========END===================================
			//TODO: Statistics
			//===========================================================================
			//numOfLines++;
			//			if (numOfLines % 100000 == 0) {
			//				System.out.println("Read " + numOfLines + " FIs");
			//				System.out.println("queue size: " + LQ.size());
			//				System.out.println("GDS_NG.memAn.getFreePercent(): "
			//						+ GDS_NG.getMem().getFreePercent());
			//				System.out.println("GDS_NG.memAn.getFree(): "
			//						+ GDS_NG.getMem().getFree());
			//				System.out.println("GDS_NG.memAn.getActualFree(): "
			//						+ GDS_NG.getMem().getActualFree());
			//				System.out.println("GDS_NG.memAn.getTotal(): "
			//						+ GDS_NG.getMem().getTotal());
			//
			//				System.out.println("memory statuses");
			//				System.out.println("DEBUG: size of coverageIndex "
			//						+ MemoryUtil.deepMemoryUsageOfAll(coverageIndex
			//								.values(), VisibilityFilter.ALL)
			//						/ Math.pow(2, 30) + " GB");
			//				System.out.println("DEBUG: size of BitMatrixPool "
			//						+ MemoryUtil.deepMemoryUsageOf(BitMatrixPool
			//								.getInstance(), VisibilityFilter.ALL)
			//						/ Math.pow(2, 30) + " GB");
			//				System.out.println("DEBUG: size of FIRunnablePool "
			//						+ MemoryUtil.deepMemoryUsageOf(FIRunnablePool
			//								.getInstance(), VisibilityFilter.ALL)
			//						/ Math.pow(2, 30) + " GB");
			//				System.gc();
			//			}
			//==================================================================================
			
			//Unreachable:
			return false;
		}
		private static int cellForCluster(final double score) {
			return (int) Math.ceil(score / 0.05);
		}
	}
		

	static class ParseFILine extends Function<String, ParsedFrequentItemSet> {

		public ParsedFrequentItemSet call(String line) {
			if (line == null)
				return null;
			if (line.startsWith("(")) // the empty FI - ignore it
				return null;
			line = line.trim();
			List<Integer> retVal = new ArrayList<Integer>();
			Pattern ISPatters = Pattern.compile(ItemsetExpression);
			Matcher fiMatcher = ISPatters.matcher(line);
			boolean matchFound = fiMatcher.find();
			if (!matchFound) {
				System.out.println("no match found in " + line);
			}
			String itemsAsString = fiMatcher.group(1).trim();
			String[] items = itemsAsString.split(" ");
			for (String strItem : items) {
				retVal.add(Integer.parseInt(strItem));
			}
			int supportSize = Integer.parseInt(fiMatcher.group(2).trim());
			ParsedFrequentItemSet pFI=new ParsedFrequentItemSet(retVal, supportSize);
			return pFI;
		}
	}



	//  private static ParsedFrequentItemSet parseFILine(String line) {
	//		line = line.trim();
	//		List<Integer> retVal = new ArrayList<Integer>();
	//		Pattern ISPatters = Pattern.compile(ItemsetExpression);
	//		Matcher fiMatcher = ISPatters.matcher(line);
	//		boolean matchFound = fiMatcher.find();
	//		if (!matchFound) {
	//			System.out.println("no match found in " + line);
	//		}
	//		String itemsAsString = fiMatcher.group(1).trim();
	//		String[] items = itemsAsString.split(" ");
	//		for (String strItem : items) {
	//			retVal.add(Integer.parseInt(strItem));
	//		}
	//		int supportSize = Integer.parseInt(fiMatcher.group(2).trim());
	//		ParsedFrequentItemSet pFI = new ParsedFrequentItemSet(retVal, supportSize);
	//		return pFI;
	//	}
	//

}