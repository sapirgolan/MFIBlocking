/**
 * 
 */
package fimEntityResolution.statistics;

/**
 * @author XPS_Sapir
 *
 */
public class StatisticMeasurements {

    private double recall;
	private double precision;
	private double fMeasure;
	private double reductionRatio;
	private double duplicatesFound;
	private double totalDuplicates;
	private double comparisonsMade;

    public StatisticMeasurements() {
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

    public void setFMeasure(double fmeasure) {
        this.fMeasure = fmeasure;
    }

	public double getReductionRatio() {
		return reductionRatio;
	}

	public void setReductionRatio(double reductionRatio) {
		this.reductionRatio = reductionRatio;
	}

	public double getTotalDuplicates() {
		return totalDuplicates;
	}

    public void setTotalDuplicates(double totalDuplicates) {
        this.totalDuplicates = totalDuplicates;
    }

	public double getComparisonsMade() {
		return comparisonsMade;
    }

    public void setComparisonsMade(double comparisonsMade) {
        this.comparisonsMade = comparisonsMade;
    }

}
