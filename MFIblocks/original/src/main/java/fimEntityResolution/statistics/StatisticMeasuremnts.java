/**
 * 
 */
package fimEntityResolution.statistics;

/**
 * @author XPS_Sapir
 *
 */
public class StatisticMeasuremnts {
	
	private double recall;
	private double precision;
	private double fMeasure;
	private double reductionRatio;
	private double duplicatesFound;
	private double totalDuplicates;
	private double comparisonsMade;

	public StatisticMeasuremnts() {
		recall = 0;
		precision = 0;
		fMeasure = 0;
		reductionRatio = 0;
		duplicatesFound = 0;
		totalDuplicates = 0;
	}

	public double getRecall() {
		return recall;
	}
	
	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getPrecision() {
		return precision;
	}
	
	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public void setFMeasure(double pr_f_measure) {
		this.fMeasure = pr_f_measure;
	}

	public void setRR(double reductionRatio) {
		this.reductionRatio = reductionRatio;
	}

	public double getDuplicatesFound() {
		return duplicatesFound;
	}

	public void setDuplicatesFound(double truePositive) {
		this.duplicatesFound = truePositive;
	}

	public double getFMeasure() {
		return fMeasure;
	}

	public void setfMeasure(double fMeasure) {
		this.fMeasure = fMeasure;
	}

	public double getReductionRatio() {
		return reductionRatio;
	}

	public void setReductionRatio(double reductionRatio) {
		this.reductionRatio = reductionRatio;
	}

	public void setTotalDuplicates(double totalDuplicates) {
		this.totalDuplicates = totalDuplicates;
	}

	public double getTotalDuplicates() {
		return totalDuplicates;
	}

	public void setComparisonsMade(double comparisonsMade) {
		this.comparisonsMade = comparisonsMade;
	}

	public double getComparisonsMade() {
		return comparisonsMade;
	}

}
