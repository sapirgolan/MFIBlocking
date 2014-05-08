package fimEntityResolution.statistics;

import java.text.DecimalFormat;


public class BlockingRunResult {

	double maxNG;
	double minBlockingThreshold;
	double actualUsedThreshold;		
	double recall; //0
	double precision; //1
	double f_measure; //2
	double reductionRatio; //3	
	double timeToRunInSec; //4	
	private double duplicatesFound;
	private double totalDuplicates;
	private double comparisonsMade;
	private final DecimalFormat decimalFormat = new DecimalFormat("#.####");
	private StatisticMeasuremnts measuremnts;

	private double format(double number){
		return Double.valueOf(decimalFormat.format(number));
	}
	public String[] getCoulmnsName() {
		return new String[] {"MaxNG", "minBlockingThresh", "usedThresh", 
			"Recall (PC)", "Precision (PQ)", "F-measure", "RR", 
			"Duplicates found", "#Duplicates in dataset", "Comparisons made", "Comparisons could have made",
			"time to run"};
	}
	
	public Object[] getValues() {
		return new Object[] {maxNG, minBlockingThreshold, format(actualUsedThreshold),
				format(recall), format(precision), format(f_measure), format(reductionRatio),
				duplicatesFound, totalDuplicates, comparisonsMade, measuremnts.getComparisonsCouldHaveMake(),
				timeToRunInSec};
	}	
	
	public double[] asArray(){
		double[] retVal = new double[8];
		retVal[0] = maxNG;
		retVal[1] = minBlockingThreshold;
		retVal[2] = actualUsedThreshold;			
		retVal[3] = recall;
		retVal[4] = precision;
		retVal[5] = f_measure;
		retVal[6] = reductionRatio;			
		retVal[7] = timeToRunInSec;
		return retVal;
	}

	public BlockingRunResult(StatisticMeasuremnts statisticMeasuremnts, double minBlockingThresh, 
			double actualUsedThreshold, double maxNG, double timeToRunInSec){
		this.maxNG = maxNG;
		this.minBlockingThreshold = minBlockingThresh;
		this.actualUsedThreshold = actualUsedThreshold;
		this.recall = statisticMeasuremnts.getRecall();
		this.precision = statisticMeasuremnts.getPrecision();
		this.f_measure = statisticMeasuremnts.getFMeasure();
		this.duplicatesFound = statisticMeasuremnts.getDuplicatesFound();
		this.reductionRatio = statisticMeasuremnts.getReductionRatio();
		this.totalDuplicates = statisticMeasuremnts.getTotalDuplicates();
		this.comparisonsMade = statisticMeasuremnts.getComparisonsMade();
		this.timeToRunInSec = timeToRunInSec;	
		this.measuremnts = statisticMeasuremnts;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();			
		sb.append(maxNG).append("\t")
		.append(String.format("%.3f", minBlockingThreshold)).append("\t")
		.append(String.format("%.3f",actualUsedThreshold)).append("\t")
		.append(String.format("%.3f",recall)).append("\t")
		.append(String.format("%.3f",precision)).append("\t")
		.append(String.format("%.3f",f_measure)).append("\t")
		.append(String.format("%.3f",reductionRatio)).append("\t") 
		.append(String.format("%.3f",duplicatesFound)).append("\t")
		.append(String.format("%.3f",totalDuplicates)).append("\t")
		.append(String.format("%.3f",comparisonsMade)).append("\t")
		.append(timeToRunInSec);

		return sb.toString();
	}

	public double getTimeToRunInSec() {
		return timeToRunInSec;
	}
}
