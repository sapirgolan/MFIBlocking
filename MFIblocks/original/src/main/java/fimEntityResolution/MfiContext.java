package fimEntityResolution;

import java.util.Arrays;
import java.util.Map;

import fimEntityResolution.BottomUp.Alg;
import fimEntityResolution.BottomUp.Configuration;

public class MfiContext {

	private double[] minBlockingThresholds;
	private int[] minSup;
	private double[] neighborhoodGrowth;
	private Configuration configuration;
	private String matchFile;
	private Alg alg;
	private String lexiconFile;
	private String recordsFile;
	private String origRecordsFile;
	private Map<Integer, Record> records;
	private String srcFile;

	public void setConfig(Configuration config) {
		this.configuration = config;
	}

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

	public void setRecords(Map<Integer, Record> records) {
		this.records = records;
	}
	
	public void setSrcFile(String srcFile) {
		this.srcFile = srcFile;
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

	public String getSrcFile() {
		return srcFile;
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

	public int getRecordsSize() {
		return this.records.size();
	}

	public Configuration getConfig() {
		return this.configuration;
	}

}
