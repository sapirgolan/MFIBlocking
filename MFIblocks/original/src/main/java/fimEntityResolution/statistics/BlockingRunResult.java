package fimEntityResolution.statistics;

import java.text.DecimalFormat;


public class BlockingRunResult {

    private final NonBinaryResults nonBRs;
    double ngLimit;
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
	private double timeOfERComparison;

	public double getTimeToRunInSec() {
		return timeToRunInSec;
	}
	
	private double format(double number){
		if (Double.isNaN(number)) {
			return 0;
		} else {
			return Double.valueOf(decimalFormat.format(number));
		}
	}
	
	public static String[] getColumnsNames() {
		return new String[] {"MaxNG", "minBlockingThresh", "usedThresh", 
			    "Recall (PC)", "Precision (PQ)", "F-measure", "RR",
                "NBRecall Blocks", "NBPrecision Blocks",
                "BRecall Blocks", "BPrecision Blocks",
			    "Duplicates found", "#Duplicates in dataset", "Comparisons made",
			    "time to run", "ER calcilate Time"};
	}
	
	public Object[] getValues() {
		return new Object[] {ngLimit, minBlockingThreshold, format(actualUsedThreshold),
				format(recall), format(precision), format(f_measure), format(reductionRatio),
                format(nonBRs.getNonBinaryRecall()),format(nonBRs.getNonBinaryPrecision()),
                format(nonBRs.getBinaryRecall()),format(nonBRs.getBinaryPrecision()),
				duplicatesFound, totalDuplicates, comparisonsMade, timeToRunInSec, timeOfERComparison};
	}	

    @Deprecated
	public double[] asArray(){
		double[] retVal = new double[8];
		retVal[0] = ngLimit;
		retVal[1] = minBlockingThreshold;
		retVal[2] = actualUsedThreshold;			
		retVal[3] = recall;
		retVal[4] = precision;
		retVal[5] = f_measure;
		retVal[6] = reductionRatio;			
		retVal[7] = timeToRunInSec;
		return retVal;
	}

	public BlockingRunResult(BlockingResultContext resultContext) {
		this.ngLimit = resultContext.getNgLimit();
		this.minBlockingThreshold = resultContext.getMinBlockingThreshold();
		this.actualUsedThreshold = resultContext.getLastUsedBlockingThreshold();
		this.timeToRunInSec = resultContext.getExecutionTime();
		this.timeOfERComparison = resultContext.getTimeOfERComparison();
		this.recall = resultContext.getStatisticMeasuremnts().getRecall();
		this.precision = resultContext.getStatisticMeasuremnts().getPrecision();
		this.f_measure = resultContext.getStatisticMeasuremnts().getFMeasure();
		this.duplicatesFound = resultContext.getStatisticMeasuremnts().getDuplicatesFound();
		this.reductionRatio = resultContext.getStatisticMeasuremnts().getReductionRatio();
		this.totalDuplicates = resultContext.getStatisticMeasuremnts().getTotalDuplicates();
		this.comparisonsMade = resultContext.getStatisticMeasuremnts().getComparisonsMade();
        this.nonBRs = resultContext.getNonBinaryResults();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(ngLimit).append("\t")
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

}
