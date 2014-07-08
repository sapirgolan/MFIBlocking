package fimEntityResolution;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import fimEntityResolution.BottomUp.Alg;
import fimEntityResolution.BottomUp.MFISetsCheckConfiguration;

public class MfiContext {
	
	public MfiContext() {
		this.firstDbSize = 0;
		this.configuration = MFISetsCheckConfiguration.DEFAULT;
	}

	private double[] minBlockingThresholds;
	private int[] minSup;
	private double[] neighborhoodGrowth;
	private MFISetsCheckConfiguration configuration;
	private String matchFile;
	private Alg alg;
	private String lexiconFile;
	private String recordsFile;
	private String origRecordsFile;
	//private Map<Integer, Record> records;
	private boolean inPerformanceMode;
	private int firstDbSize;

	public void setMatchFile(String matchFile) {
		this.matchFile = matchFile;
	}

	public void setMinSup(String minSup) {
		int[] minSupInt = getInts(minSup);
		Arrays.sort(minSupInt);
		this.minSup = minSupInt;
	}

	public void setMinBlockingThresholds(String minBlockingThresholds) {
		this.minBlockingThresholds = getThresholds(minBlockingThresholds);
		
	}

	public void setAlgorithm(Alg alg) {
		this.alg = alg;
	}

	public void setNGs(String neighborhoodGrowth) {
		this.neighborhoodGrowth = getDoubles(neighborhoodGrowth);
		
	}

	public void setLexiconFile(String lexiconFile) {
		this.lexiconFile = lexiconFile;
	}

	public void setRecordsFile(String recordsFile) {
		this.recordsFile = recordsFile;
	}

	public void setOrigRecordsFile(String origRecordsFile) {
		this.origRecordsFile = origRecordsFile;
		
	}

//	public void setRecords(Map<Integer, Record> records) {
//		this.records = records;
//	}
	
	public void setPerformanceFlag(String[] args) {
		this.inPerformanceMode = false;
		int lastArgument = args.length - 1;
		if ("perf".equalsIgnoreCase(args[lastArgument])) {
			this.inPerformanceMode = true;
		}
	}

	public String getLexiconFile() {
		return lexiconFile;
	}

	public String getRecordsFile() {
		return recordsFile;
	}
	
	public String getOriginalFile() {
		return origRecordsFile;
	}
	
	private double[] getThresholds(String strDoubles){
		String[] thresholds = strDoubles.split(",");
		double[] dThresholds = new double[thresholds.length];
		for(int i=0 ; i < thresholds.length ; i++ ){
			dThresholds[i] = Double.parseDouble(thresholds[i].trim());
		}
		return dThresholds;
	}
	
	private int[] getInts(String strInts){
		String[] intStrs = strInts.trim().split(",");
		int[] ints = new int[intStrs.length];
		for(int i=0 ; i < intStrs.length ; i++ ){
			ints[i] = Integer.parseInt(intStrs[i].trim());
		}
		return ints;
	}

	private  double[] getDoubles(String strDbs){
		String[] dbStrs = strDbs.trim().split(",");
		double[] dbs = new double[dbStrs.length];
		for(int i=0 ; i < dbStrs.length ; i++ ){
			dbs[i] = Double.parseDouble(dbStrs[i].trim());
		}
		return dbs;
	}

	public double[] getMinBlockingThresholds() {
		return this.minBlockingThresholds;
	}

	public String getAlgName() {
		return alg.toString();
	}

	public double[] getNeighborhoodGrowth() {
		return neighborhoodGrowth;
	}

	public int[] getMinSup() {
		return this.minSup;
	}

	public String getMatchFile() {
		return this.matchFile;
	}

//	public int getRecordsSize() {
//		return this.records.size();
//	}

	public MFISetsCheckConfiguration getConfig() {
		return this.configuration;
	}

//	public Map<Integer, Record> getRecords() {
//		return this.records;
//	}

	public boolean isInPerformanceMode() {
		return this.inPerformanceMode;
	}

	public void setFirstDbSize(String[] args) {
		if(args.length > 9 && args[9] != null){
			if (StringUtils.isNumeric(args[9]) ) {
				this.firstDbSize = Integer.parseInt(args[9]);
			}
		}
	}

	public int getFirstDbSize() {
		return firstDbSize;
	}

	public void setConfiguration(String configuration) {
		try {
			this.configuration = MFISetsCheckConfiguration.valueOf(configuration);
		} catch (Exception e) {
			System.err.println(String.format("Failed to read value of configuration, will use %s instead", MFISetsCheckConfiguration.DEFAULT.toString()));
		}
		
	}

}
