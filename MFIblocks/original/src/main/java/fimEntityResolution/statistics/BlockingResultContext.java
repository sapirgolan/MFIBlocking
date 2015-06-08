package fimEntityResolution.statistics;

public class BlockingResultContext {

	private StatisticMeasuremnts statisticMeasuremnts;
	private double minBlockingThreshold;
	private double lastUsedBlockingThreshold;
	private double ngLimit;
	private double executionTime;
	private double timeOfERComparison;
    private NonBinaryResults nonBinaryResults;

    public BlockingResultContext(StatisticMeasuremnts results, NonBinaryResults nonBinaryResults,
                                 double minBlockingThreshold, double lastUsedBlockingThreshold,
                                 double ngLimit, double executionTime, double timeOfERComparison) {

        this.statisticMeasuremnts = results;
        this.nonBinaryResults = nonBinaryResults;
        this.minBlockingThreshold = minBlockingThreshold;
        this.lastUsedBlockingThreshold = lastUsedBlockingThreshold;
        this.ngLimit = ngLimit;
        this.executionTime = executionTime;
        this.timeOfERComparison = timeOfERComparison;
    }

    /**
	 * @return the statisticMeasuremnts
	 */
	public final StatisticMeasuremnts getStatisticMeasuremnts() {
		return statisticMeasuremnts;
	}

    public NonBinaryResults getNonBinaryResults() {
        return nonBinaryResults;
    }

	/**
	 * @return the minBlockingThreshold
	 */
	public final double getMinBlockingThreshold() {
		return minBlockingThreshold;
	}

	/**
	 * @return the lastUsedBlockingThreshold
	 */
	public final double getLastUsedBlockingThreshold() {
		return lastUsedBlockingThreshold;
	}

	/**
	 * @return the ngLimit
	 */
	public final double getNgLimit() {
		return ngLimit;
	}

	/**
	 * @return the executionTime
	 */
	public final double getExecutionTime() {
		return executionTime;
	}

	public double getTimeOfERComparison() {
		return timeOfERComparison;
	}

}
