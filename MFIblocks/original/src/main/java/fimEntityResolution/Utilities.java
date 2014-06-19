package fimEntityResolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.NotSupportedException;

import org.enerj.core.SparseBitSet;
import org.enerj.core.SparseBitSet.Iterator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.util.FileUtils;

import candidateMatches.CandidatePairs;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;
import com.javamex.classmexer.MemoryUtil;
import com.javamex.classmexer.MemoryUtil.VisibilityFilter;

import fimEntityResolution.bitsets.EWAH_BitSet;
import fimEntityResolution.bitsets.EWAH_BitSet_Factory;
import fimEntityResolution.bitsets.Java_BitSet_Factory;
import fimEntityResolution.bitsets.SingleBSFactory;
import fimEntityResolution.interfaces.BitSetFactory;
import fimEntityResolution.interfaces.BitSetIF;
import fimEntityResolution.interfaces.IFRecord;
import fimEntityResolution.pools.BitMatrixPool;
import fimEntityResolution.pools.FIRunnableDBPool;
import fimEntityResolution.pools.FIRunnablePool;
import fimEntityResolution.pools.GDSPool;
import fimEntityResolution.pools.LimitedPool;

public class Utilities {

	public static boolean DEBUG = false;
	public static String NEW_LINE = System.getProperty("line.separator");
	public static Map<Integer, FrequentItem> globalItemsMap;
	public static boolean WRITE_ALL_ERRORS = false;
	public static GraphDatabaseService recordDB;
	private static final String RECORD_DB_PATH = "target/records-db";
	private final static String lexiconItemExpression = "(\\S+),(0.\\d+),\\{(.+)\\}";
	private final static String ItemsetExpression = "([0-9\\s]+)\\(([0-9]+)\\)$";

	

	private static void clearRecordDb() {
		try {
			FileUtils.deleteRecursively(new File(RECORD_DB_PATH));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	/**
	 * Will read records to a neo4j DB
	 * 
	 * @param numericRecordsFile
	 * @param origRecordsFile
	 * @param srcFile
	 * @return
	 */
	public static GraphDatabaseService readRecordsToDB(
			String numericRecordsFile, String origRecordsFile, String srcFile) {
		clearRecordDb();
		recordDB = new EmbeddedGraphDatabase(RECORD_DB_PATH);
		registerShutdownHook(recordDB);
		Transaction tx = recordDB.beginTx();

		try {
			BufferedReader recordsFileReader = new BufferedReader(
					new FileReader(new File(numericRecordsFile)));
			BufferedReader origRecordsFileReader = new BufferedReader(
					new FileReader(new File(origRecordsFile)));
			BufferedReader srcFileReader = null;
			if (srcFile != null && srcFile.length() > 0) {
				srcFileReader = new BufferedReader(new FileReader(new File(
						srcFile)));
			}
			System.out.println("readRecords: srcFile = " + srcFile);
			/*
			 * BufferedReader origRecordsFileReader = new BufferedReader (new
			 * InputStreamReader(new FileInputStream(origRecordsFile),
			 * "UTF16"));
			 */

			String numericLine = "";
			String recordLine = "";
			Pattern ws = Pattern.compile("[\\s]+");
			int recordIndex = 1;
			while (numericLine != null) {
				try {
					numericLine = recordsFileReader.readLine();
					if (numericLine == null) {
						break;
					}
					numericLine = numericLine.trim();
					recordLine = origRecordsFileReader.readLine().trim();
					String src = null;
					if (srcFileReader != null) {
						src = srcFileReader.readLine().trim();
					}

					DBRecord r = new DBRecord(recordIndex, recordLine);
					r.setSrc(src); // in the worst case this is null
					String[] words = ws.split(numericLine);
					if (numericLine.length() > 0) { // very special case when
													// there is an empty line
						for (String word : words) {
							int item = Integer.parseInt(word);
							r.addItem(item);
						}
					}
					RecordSet.minRecordLength = (r.getSize() < RecordSet.minRecordLength) ? r
							.getSize() : RecordSet.minRecordLength;
					recordIndex++;
				} catch (Exception e) {
					System.out.println("Exception while reading line "
							+ recordIndex + ":" + numericLine);
					System.out.println(e);
					System.out.println(e.getStackTrace());
					break;
				}
			}
			recordsFileReader.close();
			System.out.println("Num of records read: " + (recordIndex - 1));
			tx.success();
			RecordSet.DB_SIZE = recordIndex - 1;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			tx.finish();
		}
		return recordDB;

	}

	public static Map<Integer, FrequentItem> parseLexiconFile(String lexiconFile) {
		globalItemsMap = new HashMap<Integer, FrequentItem>();
		Pattern lexiconPattern = Pattern.compile(lexiconItemExpression);
		Properties itemsProps = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(lexiconFile);
			itemsProps.load(fis);
			fis.close();
			for (Map.Entry<Object, Object> itemEntry : itemsProps.entrySet()) {
				String itemId = (String) itemEntry.getKey();
				String itemVal = (String) itemEntry.getValue();
				Matcher fiMatcher = lexiconPattern.matcher(itemVal);
				boolean matched = fiMatcher.find();
				assert matched : "failed to match " + itemVal + " to "
						+ lexiconItemExpression;
				if (!matched) {
					System.out.println("failed to match " + itemVal + " to "
							+ lexiconItemExpression);
				}
				String wordVal = fiMatcher.group(1);
				String weightsStr = fiMatcher.group(2);
				String supportAsString = fiMatcher.group(3);
				String[] supportStrings = supportAsString.split(", ");
				FrequentItem item;
				if (supportStrings.length > 1) {
					item = new FrequentItem(Integer.parseInt(itemId), wordVal,
							Double.parseDouble(weightsStr), EWAH_BitSet_Factory
									.getInstance());
					// item = new FrequentItem_ewah(Integer.parseInt(itemId),
					// wordVal,Double.parseDouble(weightsStr));
				} else {
					// item = new
					// SingleSupportFrequentItem(Integer.parseInt(itemId),
					// wordVal,Double.parseDouble(weightsStr));
					item = new FrequentItem(Integer.parseInt(itemId), wordVal,
							Double.parseDouble(weightsStr), SingleBSFactory
									.getInstance());
				}
				int[] support = getSortedSupportArr(supportStrings);
				for (int trans : support) {
					item.addSupport(trans);
				}
				if (item.getSupportSize() < support.length) {
					System.out.println("support size should be "
							+ support.length + " but it is only "
							+ item.getSupportSize());
				}
				item.setIDFWeight();
				globalItemsMap.put(Integer.parseInt(itemId), item);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return globalItemsMap;
	}

	private static int[] getSortedSupportArr(String[] supportStrings) {
		int[] supports = new int[supportStrings.length];
		for (int i = 0; i < supportStrings.length; i++) {
			supports[i] = Integer.parseInt(supportStrings[i]);
		}
		Arrays.sort(supports);
		return supports;
	}

	private static int runAlg(String cmdLine, String outputFile) {
		File outFile = new File(outputFile);
		outFile.delete();
		int exitVal = -1;
		try {
			System.out.println("about to run " + cmdLine);
			Process p = Runtime.getRuntime().exec(cmdLine);

			InputStream stderr = p.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;

			boolean first = true;
			while ((line = br.readLine()) != null) {
				if (first) {
					first = false;
					System.out.println("<ERROR>");
				}
				System.out.println(line);
			}
			System.out.println("</ERROR>");

			exitVal = p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return exitVal;
	}
	
	public static String getUnixMFICmdLine() throws FileNotFoundException {
		String operatingSystem = getOSName();
		boolean isWindows = operatingSystem.toLowerCase().contains("windows");
		File file;
		if (isWindows) {
			file = new File("./fpgrowth.exe");
		} else {
			file = new File("./fpgrowth");
		}
		if (!file.exists()) {
			FileNotFoundException exception = new FileNotFoundException("fpgrowth not found at: " + file.getAbsolutePath());
			throw exception;
		}
		String path = file.getPath();
		path = path + " -tm -s-%d %s %s";
		return path;
	}

	private static String getOSName() {
		return System.getProperty("os.name");
	}

	public static File RunMFIAlg(int minSup, String recordsFile, File MFIDir){
		System.out.println("free mem before activating FPMax: "	+ Runtime.getRuntime().freeMemory());
		File file = null;
		if (!MFIDir.exists()) {
			if ( !MFIDir.mkdir() ) {
				System.err.println("Directory " + MFIDir.getAbsolutePath()
						+ " doesn't exist and failed to create it");
			}
		}
		try {
			file = File.createTempFile("MFIs", null, MFIDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		file.deleteOnExit();
		System.out.println("recordsFile= " + recordsFile);
		String cmd = null;
		try {
			cmd = String.format(getUnixMFICmdLine(), minSup, recordsFile, file.getAbsolutePath());
		} catch (FileNotFoundException e1) {
			System.err.println("Failed to execute UnixMFICmd");
			e1.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("About to execute: " + cmd);
		try {
			Socket client = new Socket("localhost", 7899);
			OutputStream os = client.getOutputStream();
			PrintWriter pw = new PrintWriter(os, true);
			pw.println(cmd);
			pw.println(file.getAbsolutePath());
			pw.flush();

			InputStream is = client.getInputStream();
			BufferedReader bd = new BufferedReader(new InputStreamReader(is));
			String read = bd.readLine();
			System.out.println("received " + read + " from the server");
			is.close();
			os.close();
			client.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	private final static String CFICmdLine = "D:\\Batya\\EntityResolution\\fimi06\\FP\\Debug\\FPClosed.exe %s %d %s";

	public static void RunCFIAlg(int minSup, String recordsFile, String CFIFile) {
		String cmd = String.format(CFICmdLine, recordsFile, minSup, CFIFile);
		runAlg(cmd, CFIFile);
	}

	public static Collection<Record> getClusterRecords(FrequentItemset fi,
			Map<Integer, Record> records) {
		Collection<Record> retVal = new ArrayList<Record>();
		BitSet support = fi.getSupport();
		for (int recordid = support.nextSetBit(0); recordid >= 0; recordid = support
				.nextSetBit(recordid + 1)) {
			retVal.add(records.get(recordid));
		}
		return retVal;
	}

	public static CandidatePairs readFIs(FrequentItemsetContext itemsetContext) {
		
		int minSup = itemsetContext.getMinimumSupport();
		double NG_PARAM = itemsetContext.getNeiborhoodGrowthLimit();
		String frequentItemsetFile = itemsetContext.getFrequentItemssetFilePath();
		int numOfLines = 0;
		BufferedReader FISReader = null;
		StringBuilder stringBuilder = new StringBuilder();
		double tooLarge = 0;
		double scorePruned = 0;
		
		System.out.println("reading FIs");

		Utilities.scoreThreshold = itemsetContext.getMinBlockingThreshold();
		// reset all parameters
		nonFIs.set(0);
		numOfFIs.set(0);
		timeSpentCalcScore.set(0);
		numOfBMs.set(0);
		numSet.set(0);
		time_in_supp_calc.set(0);
		resetAtomicIntegerArr(clusterScores);	
		ConcurrentHashMap<Integer, BitMatrix> coverageIndex = new ConcurrentHashMap<Integer, BitMatrix>(21);
		int maxSize = (int) Math.floor(minSup*NG_PARAM);
		CandidatePairs candidatePairs = new CandidatePairs(maxSize);
		StringSimTools.numOfMFIs.set(0);
		StringSimTools.numOfRoughMFIs.set(0);
		StringSimTools.timeInGetClose.set(0);
		StringSimTools.timeInRough.set(0);
		timeSpentUpdatingCoverage.set(0);
		BitMatrixPool.getInstance().restart();
		FIRunnablePool.getInstance().restart();

		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		int numOfProcessors = runtime.availableProcessors();		
		System.out.println("Running on a system with  " + numOfProcessors
				+ " processors");
		LimitedQueue<Runnable> LQ = new LimitedQueue<Runnable>(
				2 * numOfProcessors);
		ExecutorService executorService = new ThreadPoolExecutor((int) NR_PROCS_MULT
				* numOfProcessors, (int) NR_PROCS_MULT * numOfProcessors, 0L,
				TimeUnit.MILLISECONDS, LQ);
		System.out.println("used memory before processing MFIS for minsup " + minSup + " is "
				+ (double) ((double) GDS_NG.getMem().getActualUsed() / Math.pow(2, 30)) + " GB");

		long start = System.currentTimeMillis();
		try {
			FISReader = new BufferedReader(new FileReader(new File(frequentItemsetFile)));
			System.out.println("About to read MFIs from file "+ frequentItemsetFile);
			String currLine = "";
			stringBuilder.append("Size").append("\t").append("Score").append(Utilities.NEW_LINE);
			while (currLine != null) {
				try {
					currLine = FISReader.readLine();
					if (currLine == null)
						break;
					if (currLine.startsWith("(")) // the empty FI - ignore it
						continue;
					ParsedFrequentItemSet parsedFrequentItems = parseFILine(currLine);
					if (parsedFrequentItems.supportSize > minSup * NG_PARAM) {
						tooLarge++;
						continue;
					}
					List<Integer> currentItemSet = parsedFrequentItems.items;
					double maxClusterScore = StringSimTools.MaxScore(
							parsedFrequentItems.supportSize, currentItemSet, RecordSet.minRecordLength);
					if (maxClusterScore < 0.1 * Utilities.scoreThreshold) {
						scorePruned++;
						continue;
					}
					FIRunnable FIR = FIRunnablePool.getInstance().getRunnable(
							currentItemSet, minSup, NG_PARAM, coverageIndex, candidatePairs);
					executorService.execute(FIR);
					numOfLines++;
					if (numOfLines % 100000 == 0) {
						System.out.println("Read " + numOfLines + " FIs");
						System.out.println("queue size: " + LQ.size());
						System.out.println("GDS_NG.memAn.getFreePercent(): "
								+ GDS_NG.getMem().getFreePercent());
						System.out.println("GDS_NG.memAn.getFree(): "
								+ GDS_NG.getMem().getFree());
						System.out.println("GDS_NG.memAn.getActualFree(): "
								+ GDS_NG.getMem().getActualFree());
						System.out.println("GDS_NG.memAn.getTotal(): "
								+ GDS_NG.getMem().getTotal());

						System.out.println("memory statuses");
						System.out.println("DEBUG: size of coverageIndex "
								+ MemoryUtil.deepMemoryUsageOfAll(coverageIndex
										.values(), VisibilityFilter.ALL)
								/ Math.pow(2, 30) + " GB");
						System.out.println("DEBUG: size of BitMatrixPool "
								+ MemoryUtil.deepMemoryUsageOf(BitMatrixPool
										.getInstance(), VisibilityFilter.ALL)
								/ Math.pow(2, 30) + " GB");
						System.out.println("DEBUG: size of FIRunnablePool "
								+ MemoryUtil.deepMemoryUsageOf(FIRunnablePool
										.getInstance(), VisibilityFilter.ALL)
								/ Math.pow(2, 30) + " GB");
						System.gc();
					}
				} catch (Exception e) {
					System.out.println("Exception occured while parsing line "
							+ currLine);
					e.printStackTrace();
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				FISReader.close();
				executorService.shutdown();
				executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Total number of FIs read: " + numOfFIs.get()
				+ " out of " + numOfLines + " available");
		System.out.println("number of nonFIs: " + nonFIs.get());
		System.out.println("number of FIS with support larger than : " + minSup
				* NG_PARAM + " " + tooLarge);
		System.out.println("number of FIS with score smaller than : " + 0.1
				* Utilities.scoreThreshold + " " + scorePruned);
		System.out.println("Time spent calculating support "
				+ time_in_supp_calc.get() / 1000.0 + " seconds");
		System.out.println("Time spent calculating score "
				+ timeSpentCalcScore.get() / 1000.0 + " seconds");
		System.out.println("Time spent getting close items "
				+ StringSimTools.timeInGetClose.get() / 1000.0 + " seconds");
		System.out.println("Time spent calculating rough score "
				+ StringSimTools.timeInRough.get() / 1000.0 + " seconds");
		System.out.println("Time spent calculating coverage"
				+ Utilities.timeSpentUpdatingCoverage.get() / 1000.0
				+ " seconds");
		System.out.println("Total time to run: "
				+ (System.currentTimeMillis() - start) / 1000.0 + " seconds");
		System.out.println("number of rough MFIS "
				+ StringSimTools.numOfRoughMFIs.get() + " out of "
				+ StringSimTools.numOfMFIs.get());
		System.out.println("New threshold set is " + Utilities.scoreThreshold
				+ " correspondong to index "
				+ getIntForThresh(Utilities.scoreThreshold));
		System.out.println("size of  coverageIndex" + coverageIndex.size());

		System.out.println("cluster scores: " + Arrays.toString(clusterScores));
		if (DEBUG) {
			System.out.println(stringBuilder.toString());
		}
		executorService = null;
		System.gc();
		return candidatePairs;
	}

	private final static double NR_PROCS_MULT = 4;

	public static Map<Integer, GDS_NG> readFIsDB(String frequentItemsetFile,
			Map<Integer, FrequentItem> globalItemsMap, double scoreThreshold,
			Map<Integer, Record> records, int minSup, double NG_PARAM) {
		System.out.println("reading FIs");
		int numOfLines = 0;
		BufferedReader FISReader = null;
		StringBuilder sb = new StringBuilder();

		double tooLarge = 0;
		double scorePruned = 0;
		Utilities.scoreThreshold = scoreThreshold;
		// reset all parameters
		nonFIs.set(0);
		numOfFIs.set(0);
		timeSpentCalcScore.set(0);
		numOfBMs.set(0);
		numSet.set(0);
		time_in_supp_calc.set(0);
		resetAtomicIntegerArr(clusterScores);	
		ConcurrentHashMap<Integer, GDS_NG> coverageIndexDB = new ConcurrentHashMap<Integer, GDS_NG>(21);
		StringSimTools.numOfMFIs.set(0);
		StringSimTools.numOfRoughMFIs.set(0);
		StringSimTools.timeInGetClose.set(0);
		StringSimTools.timeInRough.set(0);
		timeSpentUpdatingCoverage.set(0);
		GDSPool.getInstance().restart();
		FIRunnableDBPool.getInstance().restart();
		System.gc();

		Runtime runtime = Runtime.getRuntime();
		int nrOfProcessors = runtime.availableProcessors();
		System.out.println("Entered readFIsDB Utilities.scoreThreshold="
				+ Utilities.scoreThreshold);
		System.out.println("Running on a system with  " + nrOfProcessors
				+ " processors");
		// ExecutorService exec =
		// Executors.newFixedThreadPool(nrOfProcessors+1);
		LimitedQueue<Runnable> LQ = new LimitedQueue<Runnable>(
				(int) NR_PROCS_MULT * nrOfProcessors);
		ExecutorService exec = new ThreadPoolExecutor((int) NR_PROCS_MULT
				* nrOfProcessors, (int) NR_PROCS_MULT * nrOfProcessors, 0L,
				TimeUnit.MILLISECONDS, LQ);
		System.out.println("used memory before processing MFIS for minsup "
				+ minSup
				+ " is "
				+ (double) ((double) GDS_NG.getMem().getActualUsed() / Math
						.pow(2, 30)) + " GB");

		long start = System.currentTimeMillis();
		try {
			FISReader = new BufferedReader(new FileReader(new File(
					frequentItemsetFile)));
			System.out.println("About to read MFIs from file "
					+ frequentItemsetFile);
			String currLine = "";
			sb.append("Size").append("\t").append("Score").append(
					Utilities.NEW_LINE);
			while (currLine != null) {
				try {
					currLine = FISReader.readLine();
					if (currLine == null)
						break;
					if (currLine.startsWith("(")) // the empty FI - ignore it
						continue;
					ParsedFrequentItemSet pfi = parseFILine(currLine);
					if (pfi.supportSize > minSup * NG_PARAM) {
						tooLarge++;
						continue;
					}
					List<Integer> currIS = pfi.items;
					double maxClusterScore = StringSimTools.MaxScore(
							pfi.supportSize, currIS, RecordSet.minRecordLength);
					if (maxClusterScore < 0.1 * Utilities.scoreThreshold) {
						scorePruned++;
						continue;
					}
					FIRunnableDB FIR = FIRunnableDBPool.getInstance()
							.getRunnable(currIS, minSup, records, NG_PARAM,
									pfi.supportSize,coverageIndexDB);
					exec.execute(FIR);
					numOfLines++;
					if (numOfLines % 100000 == 0) {
						System.out.println("Read " + numOfLines + " FIs");
						System.out.println("queue size: " + LQ.size());
						System.out.println("GDS_NG.memAn.getFreePercent(): "
								+ GDS_NG.getMem().getFreePercent());
						System.out.println("GDS_NG.memAn.getFree(): "
								+ GDS_NG.getMem().getFree());
						System.out.println("GDS_NG.memAn.getActualFree(): "
								+ GDS_NG.getMem().getActualFree());
						System.out.println("GDS_NG.memAn.getTotal(): "
								+ GDS_NG.getMem().getTotal());
						System.out.println("number of FIRunnableDBs created: "
								+ FIRunnableDBPool.getInstance()
										.getNumCreated());
						System.out.println("number of GDS created: "
								+ GDSPool.getInstance().getNumCreated());
						System.gc();
					}
				} catch (Exception e) {
					System.out.println("Exception occured while parsing line "
							+ currLine);
					e.printStackTrace();
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				FISReader.close();
				exec.shutdown();
				exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Total number of FIs read: " + numOfFIs.get()
				+ " out of " + numOfLines + " available");
		System.out.println("number of nonFIs: " + nonFIs.get());
		System.out.println("number of FIS with support larger than : " + minSup
				* NG_PARAM + " " + tooLarge);
		System.out.println("number of FIS with score smaller than : " + 0.1
				* Utilities.scoreThreshold + " " + scorePruned);
		System.out.println("Time spent calculating support "
				+ time_in_supp_calc.get() / 1000.0 + " seconds");
		System.out.println("Time spent calculating score "
				+ timeSpentCalcScore.get() / 1000.0 + " seconds");
		System.out.println("Time spent getting close items "
				+ StringSimTools.timeInGetClose.get() / 1000.0 + " seconds");
		System.out.println("Time spent calculating rough score "
				+ StringSimTools.timeInRough.get() / 1000.0 + " seconds");
		System.out.println("Time spent calculating coverage "
				+ Utilities.timeSpentUpdatingCoverage.get() / 1000.0
				+ " seconds");
		System.out.println("Time spent clearing DB "
				+ GDS_NG.timeSpentClearingDB.get() / 1000.0 + " seconds");
		System.out.println("Total time to run: "
				+ (System.currentTimeMillis() - start) / 1000.0 + " seconds");
		System.out.println("number of rough MFIS "
				+ StringSimTools.numOfRoughMFIs.get() + " out of "
				+ StringSimTools.numOfMFIs.get());
		System.out.println("New threshold set is " + Utilities.scoreThreshold
				+ " correspondong to index "
				+ getIntForThresh(Utilities.scoreThreshold));
		System.out
				.println("size of  coverageIndexDB " + coverageIndexDB.size());

		System.out.println("cluster scores: " + Arrays.toString(clusterScores));
		if (DEBUG) {
			System.out.println(sb.toString());
		}
		exec = null;
		System.gc();
		/*
		 * for(int i = 0 ; i <= coverageIndex.size() ; i++){ BitMatrix bm =
		 * coverageIndex.get(i); if(bm != null){
		 * System.out.println("coverageIndex.get(" + i + ").getNumBitsSet=" +
		 * bm.getSBS().getNumBitsSet()); System.out.println("coverageIndex.get("
		 * + i + ").getCoveredRows().cardinality()=" +
		 * bm.getCoveredRows().cardinality());
		 * System.out.println("coverageIndex.get(" + i + ").getMaxNG()=" +
		 * bm.getMaxNG()); } else{ System.out.println("coverageIndex.get(" + i +
		 * ")==null"); }
		 * 
		 * }
		 */
		return coverageIndexDB;
	}

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

	

	public static void main(String[] args) {
		BitMatrix test = new BitMatrix(100000);
		long mem0 = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
		double used = mem0 / Math.pow(2, 30);
		for (int i = 1; i < 100000; i++) {
			test.setPair(i, i + 10);
		}
		mem0 = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
		used = mem0 / Math.pow(2, 30);
	}

	public static int getIntForThresh(final double thresh) {
		int temp = ((int) (thresh * 100));
		int divisor = ((int) (BottomUp.THRESH_STEP * 100));
		return ((temp / divisor) + 1);

	}

	public static List<Record> getRecords(SparseBitSet support) {
		int size = new Long(support.getNumBitsSet()).intValue();
		List<Record> retVal = new ArrayList<Record>(size);
		Iterator It = support.getIterator();

		while (It.hasNext()) {
			int recordId = new Long(It.next()).intValue();
			retVal.add(RecordSet.values.get(recordId));
		}
		return retVal;

	}

	public static List<Record> getRecords(BitSet support) {
		List<Record> retVal = new ArrayList<Record>(support.cardinality());
		for (int i = support.nextSetBit(0); i >= 0; i = support
				.nextSetBit(i + 1)) {
			retVal.add(RecordSet.values.get(i));
		}

		return retVal;
	}

	public static List<IFRecord> getRecords(EWAHCompressedBitmap support) {
		List<IFRecord> retVal = new ArrayList<IFRecord>(support.cardinality());
		IntIterator iterator = support.intIterator();
		while (iterator.hasNext()) {
			int index = iterator.next();
			retVal.add(RecordSet.values.get(index));
		}
		if (support.cardinality() != retVal.size()) {
			System.out
					.println("getRecords: support.cardinality()="
							+ support.cardinality() + " retVal.size()="
							+ retVal.size());
		}
		return retVal;
	}

	public static List<IFRecord> getRecords(EWAHCompressedBitmap support,
			GraphDatabaseService recordsDB) {
		List<IFRecord> retVal = new ArrayList<IFRecord>(support.cardinality());
		IntIterator iterator = support.intIterator();
		while (iterator.hasNext()) {
			int index = iterator.next();
			retVal.add(getRecordFromDB(index, recordsDB));
		}
		if (support.cardinality() != retVal.size()) {
			System.out
					.println("getRecords: support.cardinality()="
							+ support.cardinality() + " retVal.size()="
							+ retVal.size());
		}
		return retVal;
	}

	private static DBRecord getRecordFromDB(int id,
			GraphDatabaseService recordsDB) {
		IndexManager IM = recordsDB.index();
		Index<Node> index = IM.forNodes(GDS_NG.IDS_INDEX_NAME);
		IndexHits<Node> hits = index.get(DBRecord.ID_PROP_NAME, id);
		Node retVal = hits.getSingle();
		if (retVal != null) {
			return new DBRecord(retVal);
		}
		return null;
	}

	private static AtomicLong time_in_supp_calc = new AtomicLong(0);


	private static BitSetFactory bsf = Java_BitSet_Factory.getInstance();
	private static LimitedPool limitedPool = LimitedPool.getInstance(bsf);
	/***
	 * Returns set of tuples as BitSetIF object (Jonathan Svirsky)
	 * @param items - q-grams ids returned by FP-growth (Jonathan Svirsky)
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
			time_in_supp_calc.addAndGet(System.currentTimeMillis() - start);
			return retVal;
		} catch (Exception e) {
			System.out.println("cought exception " + e);
			e.printStackTrace();
			return null;
		}

	}

	@SuppressWarnings("unused")
	private static List<FrequentItem> getItemsetFromLine(String line,
			Map<Integer, FrequentItem> globalItemsMap) {
		Pattern ISPatters = Pattern.compile(ItemsetExpression);
		Matcher fiMatcher = ISPatters.matcher(line);
		boolean matchFound = fiMatcher.find();
		if (!matchFound) {
			System.out.println("no match found in " + line);
		}
		String itemsAsString = fiMatcher.group(1);
		String[] items = itemsAsString.split(" ");
		List<FrequentItem> retVal = new ArrayList<FrequentItem>();
		for (String item : items) {
			retVal.add(globalItemsMap.get(Integer.parseInt(item)));
		}
		return retVal;

	}

	private static ParsedFrequentItemSet parseFILine(String line) {
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
		ParsedFrequentItemSet pFI = (new Utilities()).new ParsedFrequentItemSet(retVal, supportSize);
		return pFI;
	}

	private class ParsedFrequentItemSet {
		public ParsedFrequentItemSet(List<Integer> items, int supportSize) {
			this.items = items;
			this.supportSize = supportSize;
		}

		public List<Integer> items;
		public int supportSize;
	}

	public static Set<Integer> convertFromBitSet(BitSet bs) {
		Set<Integer> retVal = new HashSet<Integer>(bs.cardinality());
		for (Integer transaction = bs.nextSetBit(0); transaction >= 0; transaction = bs
				.nextSetBit(transaction + 1)) {
			retVal.add(transaction);
		}
		return retVal;
	}

	/**
	 * 
	 * @param bs1
	 * @param bs2
	 * @return true if bs1 contains all indexes in bs2
	 */
	public static boolean contains(BitSet bs1, BitSet bs2) {
		if (bs1.cardinality() < bs2.cardinality())
			return false;
		for (Integer index = bs2.nextSetBit(0); index >= 0; index = bs2
				.nextSetBit(index + 1)) {
			if (!bs1.get(index))
				return false;
		}
		return true;
	}

	/**
	 * This method will return the set of pairs which can be created in group,
	 * and whose scpre passes the defined threshold
	 * 
	 * @param group
	 *            - from which to generate pairs
	 * @param threshold
	 *            - below which the pairs are no considered
	 * @param records
	 *            - dataset records
	 * @param itemsMap
	 * @return
	 */
	public static Set<Pair> getPairs(Collection<Record> group,
			Map<Integer, Record> records) {
		Set<Pair> pairs = new HashSet<Pair>();
		List<Record> temp = new ArrayList<Record>(group.size());
		temp.addAll(group);
		for (int i = 0; i < temp.size(); i++) {
			for (int j = i + 1; j < temp.size(); j++) {
				Record S = temp.get(i);
				Record T = temp.get(j);
				Pair pair = new Pair(S.getId(), T.getId());
				pairs.add(pair);
			}
		}
		return pairs;
	}

	public static Collection<Integer> getIntSetFromBitSet(BitSet bs) {
		Collection<Integer> retVal = new ArrayList<Integer>(
				bs.cardinality() + 1);
		for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
			retVal.add(i);
		}
		return retVal;
	}

	private static String printRecordCluster(Pair pair,
			Map<Integer, Record> records) {
		double score = StringSimTools.softTFIDF(records.get(pair.r1), records
				.get(pair.r2));
		return pair.toString() + " score: " + score;
	}

	public static double[] evaluateResolution(Set<Pair> trueDupPairs,
			Map<Integer, Set<Pair>> actualDupPairs, String experimentTitle,
			Map<Integer, Cluster> clusters, Map<Integer, Record> records,
			Map<Integer, FrequentItem> itemsMap) {
		System.out.println(experimentTitle);
		float TP = 0, TN = 0, FN = 0, FP = 0;
		System.out.println("False Positives:");
		Set<Pair> actualPairs = new HashSet<Pair>(); // for later use
		double maxScoreForFP = 0;
		double minScoreForFN = 1;
		for (Map.Entry<Integer, Set<Pair>> currClusterEntry : actualDupPairs
				.entrySet()) {
			Set<Pair> clusterPairs = currClusterEntry.getValue();
			for (Pair clusterPair : clusterPairs) {
				if (trueDupPairs.contains(clusterPair)) {
					TP++;
				} else {
					FP++;
					if (WRITE_ALL_ERRORS) {
						System.out.println("Wrong pair: ");
						System.out.println(printRecordCluster(clusterPair,
								records));
						maxScoreForFP = Math.max(maxScoreForFP, StringSimTools
								.softTFIDF(RecordSet.values.get(clusterPair.r1),
										RecordSet.values.get(clusterPair.r2)));
					}
					/*
					 * System.out.println("inside FP cluster: " +
					 * clusters.get(currClusterEntry.getKey()).toString());
					 */
				}
			}
			actualPairs.addAll(clusterPairs);
		}
		System.out.println("Negatives:");
		for (Pair truePair : trueDupPairs) {
			if (!actualPairs.contains(truePair)) {
				FN++;
				if (WRITE_ALL_ERRORS) {
					System.out.println("missed cluster: ");
					if (RecordSet.values.get(truePair.r1) == null
							|| RecordSet.values.get(truePair.r2) == null) {
						System.out.println("Record " + truePair.r1 + " or "
								+ truePair.r2 + " doesn;t exist");
					}
					System.out.println(printRecordCluster(truePair, records));
				}
				minScoreForFN = Math.min(minScoreForFN, StringSimTools
						.softTFIDF(RecordSet.values.get(truePair.r1),
								RecordSet.values.get(truePair.r2)));
			} else {
				TN++;
			}
		}
		System.out.println("Number of true positives = " + TP);
		System.out.println("Number of true negatives = " + TN);
		System.out.println("Number of false negatives = " + FN);
		System.out.println("Number of false positives = " + FP);
		float p = (TP / (FP + TP));
		float r = (TP / (FN + TP));
		System.out.println("Precision is = " + p);
		System.out.println("Recall is = " + r);
		System.out.println("f-measure is = " + (2 * p * r / (p + r)));

		double[] retval = new double[3];
		retval[0] = p;
		retval[1] = r;
		retval[2] = (2 * p * r / (p + r));
		System.out.println("All false posotives scores are under "
				+ maxScoreForFP);
		System.out.println("All false negative scores are above "
				+ minScoreForFN);
		return retval;
	}

}
