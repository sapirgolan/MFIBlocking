package fimEntityResolution.statistics;

public class BlockingResultContext {

	private StatisticMeasuremnts statisticMeasuremnts;
	private double minBlockingThreshold;
	private double lastUsedBlockingThreshold;
	private double ngLimit;
	private double executionTime;

	public BlockingResultContext(StatisticMeasuremnts results,
			double minBlockingThreshold, double lastUsedBlockingThreshold,
			double nG_LIMIT, double executionTime) {
		
		this.statisticMeasuremnts = results;
		this.minBlockingThreshold = minBlockingThreshold;
		this.lastUsedBlockingThreshold = lastUsedBlockingThreshold;
		this.ngLimit = nG_LIMIT;
		this.executionTime = executionTime;
	}

	/**
	 * @return the statisticMeasuremnts
	 */
	public final StatisticMeasuremnts getStatisticMeasuremnts() {
		return statisticMeasuremnts;
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

}