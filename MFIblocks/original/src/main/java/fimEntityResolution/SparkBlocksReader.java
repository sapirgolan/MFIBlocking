package fimEntityResolution;

import java.util.List;
import java.util.Map;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import candidateMatches.CandidatePairs;
import fimEntityResolution.FrequentItem;
import fimEntityResolution.Record;
import fimEntityResolution.StringSimTools;
import fimEntityResolution.Utilities;
import fimEntityResolution.interfaces.BitSetIF;
import fimEntityResolution.interfaces.IFRecord;
import fimEntityResolution.pools.BitMatrixPool;
/***
 * Reads frequent item-sets created by fpgrow and marks candidate blocks according to 3 parameters:
 * support condition, ClusterJaccard score, sparse neighborhood condition.
 * Uses SPARK as a local RDD.
 * @author Jonathan Svirsky
 *
 */
public class SparkBlocksReader {
	
	private final static String ItemsetExpression = "([0-9\\s]+)\\(([0-9]+)\\)$";
	public static int DB_SIZE;
	public static String NEW_LINE = System.getProperty("line.separator");
	static CandidatePairs candidatePairs;
	
	/**
	 * Imported from Utilities
	 * @param arr
	 */
	private static void resetAtomicIntegerArr(AtomicInteger[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new AtomicInteger(0);
		}
	}
	//TODO: change to distributed environment (add option to switch between environments)
	//TODO: check if distribution influences CandidatePairs object in this class
	/**
	 * Replaces Utilities.readFIs, uses SPARK.
	 * @param frequentItemsetFile
	 * @param globalItemsMap
	 * @param scoreThreshold
	 * @param records
	 * @param minSup
	 * @param NG_PARAM
	 * @return CandidatePairs object
	 */
	public static CandidatePairs readFIs(String frequentItemsetFile,
			Map<Integer, FrequentItem> globalItemsMap, double scoreThreshold,
			Map<Integer, Record> records, int minSup, double NG_PARAM){
		
		resetAtomicIntegerArr(Utilities.clusterScores);	
		StringSimTools.numOfMFIs.set(0);
		StringSimTools.numOfRoughMFIs.set(0);
		StringSimTools.timeInGetClose.set(0);
		StringSimTools.timeInRough.set(0);
		//timeSpentUpdatingCoverage.set(0);
		BitMatrixPool.getInstance().restart();
		//System.out.println("About to execute JavaSparkContext");
		JavaSparkContext sc = new JavaSparkContext("local[8]", "App",
				"$SPARK_HOME", new String[]{"target/SparkTest-1.0-SNAPSHOT.jar"});
		JavaRDD<String> fmiSets = sc.textFile(frequentItemsetFile).cache();		
		int maxSize = (int) Math.floor(minSup*NG_PARAM);
		candidatePairs = new CandidatePairs(maxSize);
		JavaRDD<CandidateBlock> parsedFIs = fmiSets.map(new ParseFILine());
		//long parsedFisNum=parsedFIs.count();
		//System.out.println("parsedFIs: "+parsedFisNum);
		//Strings: FI, NONFI, SCOREPRUNED, TOOLARGE
		JavaPairRDD<String,Integer> temp=parsedFIs.map(new ExctractBlocks(globalItemsMap,scoreThreshold, 
				records,minSup, NG_PARAM, candidatePairs));

		JavaPairRDD<String, Integer> counts = temp.reduceByKey(new Function2<Integer, Integer, Integer>() {
		      public Integer call(Integer i1, Integer i2) {
		        return i1 + i2;
		      }
		    });
		List<Tuple2<String, Integer>> output =new ArrayList<Tuple2<String,Integer>>();
		output=counts.collect();
	    for (Tuple2 tuple : output) {
	      System.out.println(tuple._1 + ": " + tuple._2);
	    }
		return candidatePairs;
	}
	
	static class ExctractBlocks extends PairFunction<CandidateBlock, String, Integer> {

		Map<Integer, FrequentItem> globalItemsMap;
		double scoreThreshold;
		Map<Integer, Record> records; 
		int minSup;
		double NG_PARAM;
		//CandidatePairs candidatePairs;

		public ExctractBlocks(Map<Integer, FrequentItem> globalItemsMap,
				double scoreThreshold, Map<Integer, Record> records,
				int minSup, double nG_PARAM,CandidatePairs candidatePairs) {
			super();
			this.globalItemsMap = globalItemsMap;
			this.scoreThreshold = scoreThreshold;
			this.records = records;
			this.minSup = minSup;
			NG_PARAM = nG_PARAM;
			//this.candidatePairs=candidatePairs;
		}
		
		@Override
		public Tuple2<String, Integer> call(CandidateBlock candidateBlock) throws Exception {
			
			// 3.1 support condition 
			if (candidateBlock.supportSize > minSup * NG_PARAM) {
				//tooLarge++;
				return new Tuple2<String, Integer>("TOOLARGE", 1);
			}

			// 3.2 ClusterJaccard score
			List<Integer> currentItemSet = candidateBlock.items;
			double maxClusterScore = StringSimTools.MaxScore(candidateBlock.supportSize, currentItemSet, Utilities.minRecordLength);
			if (maxClusterScore < 0.1 * scoreThreshold) {
				//scorePruned++;
				return new Tuple2<String, Integer>("SCOREPRUNED", 1);
			}
			
			// 3.3 sparse neighborhood condition			
			BitSetIF support = null;
			support = Utilities.getItemsetSupport(currentItemSet);
			if (support.getCardinality() <  minSup) {
				//SparkBlocksReader.nonFIs.incrementAndGet();
				return new Tuple2<String, Integer>("NONFI", 1); // must be the case that the item appears minSup times
																// but in a number of records < minsup
			}			
			List<IFRecord> FISupportRecords = support.getRecords();
			//long start = System.currentTimeMillis();
			double currClusterScore = StringSimTools.softTFIDF(
					FISupportRecords, currentItemSet, scoreThreshold);
			FISupportRecords = null;
			//Utilities.timeSpentCalcScore.addAndGet(System.currentTimeMillis() - start);
			Utilities.clusterScores[cellForCluster(currClusterScore)].incrementAndGet();
			if (currClusterScore > scoreThreshold) {
				//SparkBlocksReader.numOfFIs.incrementAndGet();
				support.markPairs(candidatePairs,currClusterScore);
				return new Tuple2<String, Integer>("FI", 1);
			}
			//JS:Non of conditions passed and wasn't classified.
			return new Tuple2<String, Integer>("NOTHING", 1);
		}
		/**
		 * Imported from Utilities.
		 * @param score
		 * @return
		 */
		private static int cellForCluster(final double score) {
			return (int) Math.ceil(score / 0.05);
		}
		
	}
	
	/***
	 * Parser extends Function<String, CandidateBlock> for parsing lines from the text files in SPARK.
	 * @author Jonathan Svirsky
	 *
	 */
	static class ParseFILine extends Function<String, CandidateBlock> {

		public CandidateBlock call(String line) {
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
			CandidateBlock pFI=new CandidateBlock(retVal, supportSize);
			return pFI;
		}
	}
}