package fimEntityResolution.statistics;

import fimEntityResolution.TrueClusters;
import il.ac.technion.ie.model.CandidatePairs;

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

    public StatisticMeasurements calculate() {
        long start = System.currentTimeMillis();
		
		this.calcTPFT();
		
		int totalDuplicates = groundTruth.getCardinality();
		long comparisonsMade = truePositive + falsePositive;
		long duplicatesFound = truePositive;
		double precision = (double)truePositive/(truePositive+falsePositive);
		double recall = (double)truePositive/(truePositive+falseNegative);
		double fMeasure; 
		if (precision!=0 && recall!=0) 
			fMeasure=(double)(2*precision*recall)/(precision+recall);
		else fMeasure=0.0;
		double totalComparisonsAvailable = ((numOfRecords * (numOfRecords - 1))*0.5);	
		double reductionRatio = Math.max(0.0, (1.0-((comparisonsMade)/totalComparisonsAvailable)));		
		
		System.out.println("TP = " + truePositive +", FP= " + falsePositive + ", FN="+ falseNegative  + " totalComparisons= " + totalComparisonsAvailable);
		System.out.println("recall = " + recall +", precision= " + precision + ", f-measure="+ fMeasure + " RR= " + reductionRatio);

        StatisticMeasurements statisticMeasurements = new StatisticMeasurements();
        statisticMeasurements.setRecall(recall);
        statisticMeasurements.setPrecision(precision);
        statisticMeasurements.setFMeasure(fMeasure);
        statisticMeasurements.setRR(reductionRatio);

        statisticMeasurements.setDuplicatesFound(duplicatesFound);
        statisticMeasurements.setTotalDuplicates(totalDuplicates);
        statisticMeasurements.setComparisonsMade(comparisonsMade);

        System.out.println("time to calculateFinalResults: " + Double.toString((double)(System.currentTimeMillis()-start)/1000.0));

        return statisticMeasurements;
    }
	
	private void calcTPFT () {
		long[] TPFP = groundTruth.getGroundTruthCandidatePairs().calcTrueAndFalsePositives( algorithmAssignment );
		truePositive = TPFP[0];		
		falsePositive = TPFP[1];
		falseNegative = TPFP[2];
	}
	
}
