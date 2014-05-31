package fimEntityResolution.statistics;

import candidateMatches.CandidatePairs;
import fimEntityResolution.TrueClusters;

/**
 * Calculate output measurements: F-measure, Precision (PQ), Recall (PC) and others... 
 * 
 * @author Sapir
 *
 */
public class ExperimentResult {

	private TrueClusters groundTruth;
	private CandidatePairs algorithmAssignment;
	private int numOfRecords;
	private long truePositive;
	private long falsePositive;
	private long falseNegative;
	

	public ExperimentResult (TrueClusters groundTruth,CandidatePairs resultMatrix, int numOfRecords) {
		this.groundTruth = groundTruth;
		this.algorithmAssignment = resultMatrix;
		this.numOfRecords = numOfRecords;
	}
	
	public StatisticMeasuremnts calculate() {
		long start = System.currentTimeMillis();
		
		this.calcTPFT();
		
		int totalDuplicates = groundTruth.getCardinality();
		long comparisonsMade = truePositive + falsePositive;
		long duplicatesFound = truePositive;
		double precision = (double)truePositive/(truePositive+falsePositive);
		double recall = (double)truePositive/(truePositive+falseNegative);
		double fMeasure = (double)(2*precision*recall)/(precision+recall);
		double totalComparisonsAvailable = ((numOfRecords * (numOfRecords - 1))*0.5);	
		double reductionRatio = Math.max(0.0, (1.0-((comparisonsMade)/totalComparisonsAvailable)));		
		
		System.out.println("TP = " + truePositive +", FP= " + falsePositive + ", FN="+ falseNegative  + " totalComparisons= " + totalComparisonsAvailable);
		System.out.println("recall = " + recall +", precision= " + precision + ", f-measure="+ fMeasure + " RR= " + reductionRatio);
		
		StatisticMeasuremnts statisticMeasuremnts = new StatisticMeasuremnts();
		statisticMeasuremnts.setRecall(recall);
		statisticMeasuremnts.setPrecision(precision);
		statisticMeasuremnts.setFMeasure(fMeasure);
		statisticMeasuremnts.setRR(reductionRatio);
		
		statisticMeasuremnts.setDuplicatesFound(duplicatesFound);
		statisticMeasuremnts.setTotalDuplicates(totalDuplicates);
		statisticMeasuremnts.setComparisonsMade(comparisonsMade);
		
		System.out.println("time to calculateFinalResults: " + Double.toString((double)(System.currentTimeMillis()-start)/1000.0));
		
		return statisticMeasuremnts;
	}
	
	private void calcTPFT () {
		long[] TPFP = groundTruth.getGroundTruthCandidatePairs().calcTrueAndFalsePositives( algorithmAssignment );
		truePositive = TPFP[0];		
		falsePositive = TPFP[1];
		falseNegative = TPFP[2];
	}
	
}
