package fimEntityResolution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.NotSupportedException;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;
import candidateMatches.CandidatePairs;
import fimEntityResolution.bitsets.EWAH_BitSet;
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
	//must be defined on other nodes:
	//public static final String LEXICON_FILE="C:/workspace/mfiblocking/MFIBlocking/MFIblocks/original/exampleInputOutputFiles/DS_800_200_3_1_3_po_clean_lexicon_3grams.txt";
	
	//TODO: change to distributed environment (add option to switch between environments)
	
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
	public static CandidatePairs readFIs(FrequentItemsetContext itemsetContext){
		
		MfiContext context = itemsetContext.getMfiContext();
		int minSup = itemsetContext.getMinimumSupport();
		double NG_PARAM = itemsetContext.getNeiborhoodGrowthLimit();
		
		//StringSimToolsLocal.globalItemsMap = Utilities.parseLexiconFile(context .getLexiconFile());
		//StringSimToolsLocal.globalRecords = Utilities.readRecords(context);

		resetAtomicIntegerArr(Utilities.clusterScores);	
		StringSimTools.numOfMFIs.set(0);
		StringSimTools.numOfRoughMFIs.set(0);
		StringSimTools.timeInGetClose.set(0);
		StringSimTools.timeInRough.set(0);
		BitMatrixPool.getInstance().restart();
	
		int maxSize = (int) Math.floor(minSup *NG_PARAM );
		candidatePairs = new CandidatePairs(maxSize);
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		int numOfCores = runtime.availableProcessors();
		JavaRDD<String> fmiSets = BottomUp.sc.textFile(itemsetContext.getFrequentItemssetFilePath(), numOfCores*3); //JS: Spark tuning: minSplits=numOfCores*3
		JavaRDD<CandidateBlock> parsedBlocks = fmiSets.map(new ParseFILine());
		JavaPairRDD<CandidateBlock,Double> blocksWithScores = parsedBlocks.mapToPair( new CalculateScores(context.getLexiconFile(),
				context.getRecordsFile(), context.getOriginalFile(), itemsetContext.getMinBlockingThreshold(), minSup, NG_PARAM));
		JavaPairRDD<CandidateBlock,Double> trueBlocks=blocksWithScores.filter(new TrueBlocks());
		trueBlocks.count(); //JS:there is no need for the result, but we have to perform ACTION in order to apply our mapping
							//and make distributed calculations in Spark
		return candidatePairs;
	}
	/**
	 * Imported from Utilities.resetAtomicIntegerArr
	 * @param arr
	 */
	private static void resetAtomicIntegerArr(AtomicInteger[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new AtomicInteger(0);
		}
	}
	/**
	 * TrueBlocks extends Function<Tuple2<CandidateBlock, Double>, Boolean>
	 * for filtering blocks that holds our 3 conditions.
	 * -100.0: tooLarge in original	
	 * -200.0: scorePruned
	 * -300.0: nonFIs (support.getCardinality() <  minSup) 
	 * -400.0: not classified
	 *
	 */
	static class TrueBlocks implements Function<Tuple2<CandidateBlock, Double>, Boolean>{

		@Override
		public Boolean call(Tuple2<CandidateBlock, Double> tuple)
				throws Exception {
			if (tuple._2!=-100.0 && tuple._2!=-200.0 && tuple._2!=-300.0 &&tuple._2!=-400.0)
				return true;
			return false;
		}	
	}

	static class CalculateScores implements PairFunction<CandidateBlock, CandidateBlock, Double>{

		double scoreThreshold;
		int minSup;
		double NG_PARAM;

		public CalculateScores(String lexiconFile, String recordsFile,String origRecordsFile,
				double scoreThreshold, int minSup, double nG_PARAM){
			this.scoreThreshold=scoreThreshold;
			this.minSup=minSup;
			this.NG_PARAM=nG_PARAM;
			
		}
		@Override
		public Tuple2<CandidateBlock, Double> call(CandidateBlock candidateBlock)
				throws Exception {
			// 3.1 support condition 
			if (candidateBlock.supportSize > minSup * NG_PARAM) {
				//tooLarge++;
				return new Tuple2<CandidateBlock, Double>(candidateBlock, -100.0);
			}

			// 3.2 ClusterJaccard score
			List<Integer> currentItemSet = candidateBlock.items;
			double maxClusterScore = StringSimTools.MaxScore(candidateBlock.supportSize, currentItemSet, RecordSet.minRecordLength);
			if (maxClusterScore < 0.1 * scoreThreshold) {
				//scorePruned++;
				return new Tuple2<CandidateBlock, Double>(candidateBlock, -200.0);
			}
			// 3.3 sparse neighborhood condition
			BitSetIF support = null;
			support = getItemsetSupport(currentItemSet);
			if (support.getCardinality() <  minSup) {
				//SparkBlocksReader.nonFIs.incrementAndGet();
				return new Tuple2<CandidateBlock, Double>(candidateBlock, -300.0); // must be the case that the item appears minSup times
				// but in a number of records < minsup
			}
			
			List<IFRecord> FISupportRecords = support.getRecords();
			double currClusterScore = StringSimTools.softTFIDF(
					FISupportRecords, currentItemSet, scoreThreshold);
			FISupportRecords = null;
			Utilities.clusterScores[cellForCluster(currClusterScore)].incrementAndGet();
			if (currClusterScore > scoreThreshold) {
				//SparkBlocksReader.numOfFIs.incrementAndGet();
				support.markPairs(candidatePairs,currClusterScore);
				return new Tuple2<CandidateBlock, Double>(candidateBlock, currClusterScore);
			}
			//JS:Non of conditions passed and wasn't classified.
			return new Tuple2<CandidateBlock, Double>(candidateBlock, -400.0);
		}
		/**
		 * Utilities.getItemsetSupport
		 * @param items
		 * @return
		 */
		public static BitSetIF getItemsetSupport(List<Integer> items) {
			try {
				long start = System.currentTimeMillis();
				BitSetIF retVal = new EWAH_BitSet();
				try {
					retVal = retVal.or(Utilities.globalItemsMap.get(items.get(0))
							.getSupport());
					for (int i = 1; i < items.size(); i++) {
						int item = items.get(i);
						BitSetIF itemSupport = Utilities.globalItemsMap.get(item)
								.getSupport();
						retVal = retVal.and(itemSupport);
					}
				} catch (NotSupportedException e) {
					e.printStackTrace();
					return null;
				}
				if (retVal.getCardinality() <= 0) {
					System.out
							.println("DEBUG: itemset with support retVal.getCardinality()"
									+ retVal.getCardinality());
				}
				//time_in_supp_calc.addAndGet(System.currentTimeMillis() - start);
				return retVal;
			} catch (Exception e) {
				System.out.println("cought exception " + e);
				e.printStackTrace();
				return null;
			}

		}
		/**
		 * Imported from Utilities.cellForCluster
		 * @param score
		 * @return
		 */
		private static int cellForCluster(final double score) {
			return (int) Math.ceil(score / 0.05);
		}
	}

	/***
	 * Parser extends Function<String, CandidateBlock> for parsing lines from the text files in SPARK.
	 */
	static class ParseFILine implements Function<String, CandidateBlock> {

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