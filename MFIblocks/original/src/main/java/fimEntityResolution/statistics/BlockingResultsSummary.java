package fimEntityResolution.statistics;

import il.ac.technion.ie.utils.Utilities;

import java.util.Collection;

public class BlockingResultsSummary {
	double[] pairsCompletenessSummary; //0
	double[] reductionRatioSummary; //1
	double[] modifiedRRSummary;		//2
	double[] f_measureSummary; //3
	double[] modifiedFMeasureSummary; //4
	double blockingThresh; 
	long[] timeToRunInSecSummary; 
	double[] dedupPrecisionSummary; //5
	double[] dedupRecallSummary; //6
	double[] dedupFSummary; //7

	double[][] resultsSummary;
	double[] runTimeSummary;

	private double getAverage(double[] arr){
		double retval = 0;
		for (double d : arr) {
			retval += d;
		}
		return (retval/arr.length);	
	}
	private double getMin(double[] arr){
		double retval = Double.MAX_VALUE;
		for (double d : arr) {
			retval = Math.min(retval,d);
		}
		return retval;		
	}

	private double getMax(double[] arr){
		double retval = Double.MIN_VALUE;
		for (double d : arr) {
			retval = Math.max(retval,d);
		}
		return retval;			
	}

	private long getAverage(long[] arr){
		long retval = 0;
		for (long d : arr) {
			retval += d;
		}
		return (retval/arr.length);

	}
	private long getMin(long[] arr){
		long retval = Long.MAX_VALUE;
		for (long d : arr) {
			retval = Math.min(retval,d);
		}
		return retval;			
	}

	private long getMax(long[] arr){
		long retval = Long.MIN_VALUE;
		for (long d : arr) {
			retval = Math.max(retval,d);
		}
		return retval;			
	}

	public String getSummary(){
		StringBuilder sb = new StringBuilder();

		sb.append("Recall - PC summary:").append(Utilities.NEW_LINE);
		sb.append(getMin(resultsSummary[0])).append(" ").append(getAverage(resultsSummary[0])).append(" ")
		.append(getMax(resultsSummary[0])).append(Utilities.NEW_LINE);

		sb.append("Precision - PQ summary:").append(Utilities.NEW_LINE);
		sb.append(getMin(resultsSummary[1])).append(" ").append(getAverage(resultsSummary[1])).append(" ")
		.append(getMax(resultsSummary[1])).append(Utilities.NEW_LINE);

		sb.append("F-measure summary:").append(Utilities.NEW_LINE);
		sb.append(getMin(resultsSummary[2])).append(" ").append(getAverage(resultsSummary[2])).append(" ")
		.append(getMax(resultsSummary[2])).append(Utilities.NEW_LINE);


		sb.append("RR summary:").append(Utilities.NEW_LINE);
		sb.append(getMin(resultsSummary[3])).append(" ").append(getAverage(resultsSummary[3])).append(" ")
		.append(getMax(resultsSummary[3])).append(Utilities.NEW_LINE);			

		sb.append("Runtime/secs - summary:").append(Utilities.NEW_LINE);
		sb.append(getMin(runTimeSummary)).append(" ").append(getAverage(runTimeSummary)).append(" ")
		.append(getMax(runTimeSummary));


		return sb.toString();
	}
	
	public BlockingResultsSummary(Collection<BlockingRunResult> blockingRunResults){
		resultsSummary = new double[4][blockingRunResults.size()];
		runTimeSummary = new double[blockingRunResults.size()];
		int resIndex=0;
		for (BlockingRunResult result : blockingRunResults) {
			double[] asArr = result.asArray();
			for (int i = 3; i < 7; i++) {
				resultsSummary[i-3][resIndex] = asArr[i];
			}
			runTimeSummary[resIndex] = result.getTimeToRunInSec();
			resIndex++;
		}			
	}
}
