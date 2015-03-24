package il.ac.technion.ie.utils;

import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.FrequentItem;

import java.util.Map;

public class FrequentItemsetContext {

	private String absolutePath;
	private double minBlockingThreshold;
	private int minimumSupport;
	private MfiContext mfiContext;
	private Map<Integer, FrequentItem> golbalItemsMap;
	private double NeighborhoodGrowthLimit;
	
	public String getFrequentItemssetFilePath() {
		return absolutePath;
	}
	
	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
	public double getMinBlockingThreshold() {
		return minBlockingThreshold;
	}
	
	public void setMinBlockingThreshold(double minBlockingThreshold) {
		this.minBlockingThreshold = minBlockingThreshold;
	}
	
	public int getMinimumSupport() {
		return minimumSupport;
	}
	
	public void setMinimumSupport(int minimumSupport) {
		this.minimumSupport = minimumSupport;
	}
	
	public MfiContext getMfiContext() {
		return mfiContext;
	}
	
	public void setMfiContext(MfiContext mfiContext) {
		this.mfiContext = mfiContext;
	}

    public void setGolbalItemsMap(Map<Integer, FrequentItem> golbalItemsMap) {
		this.golbalItemsMap = golbalItemsMap;
	}
	
	public double getNeighborhoodGrowthLimit() {
		return NeighborhoodGrowthLimit;
	}
	
	public void setNeighborhoodGrowthLimit(double neighborhoodGrowthLimit) {
		NeighborhoodGrowthLimit = neighborhoodGrowthLimit;
	}
	
	

}
