package fimEntityResolution;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics implements Serializable {
	public static AtomicInteger nonFIs;
	public static AtomicInteger numOfFIs;
	public static AtomicLong timeSpentCalcScore;
	private static AtomicLong timeSpentUpdatingCoverage;
	private static AtomicInteger numOfBMs;
	public static AtomicInteger numOfGDs;
	private static AtomicInteger numSet;
	
	public Statistics(){
		nonFIs = new AtomicInteger(0);
		numOfFIs = new AtomicInteger(0);
		timeSpentCalcScore = new AtomicLong(0);
		timeSpentUpdatingCoverage = new AtomicLong(0);
		numOfBMs = new AtomicInteger(0);
		numOfGDs = new AtomicInteger(0);
		numSet = new AtomicInteger(0);
	}
	public void IncreaseNonFIs(){
		nonFIs.incrementAndGet();
	}
	public void IncreaseNumOfFIs(){
		numOfFIs.incrementAndGet();
	}
	public void IncreaseTimeSpentCalcScore(){
		timeSpentCalcScore.incrementAndGet();
	}
	public void IncreaseTimeSpentUpdatingCoverage(){
		timeSpentUpdatingCoverage.incrementAndGet();
	}
	public void IncreaseNumOfBMs(){
		numOfBMs.incrementAndGet();
	}
	public void IncreaseNumOfGDs(){
		numOfGDs.incrementAndGet();
	}
	public void IncreaseNumSet(){
		numSet.incrementAndGet();
	}
	
}