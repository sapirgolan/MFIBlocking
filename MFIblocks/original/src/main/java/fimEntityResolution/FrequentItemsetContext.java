package fimEntityResolution;

import java.util.Map;

public class FrequentItemsetContext {

	private String absolutePath;
	private double minBlockingThreshold;
	private int minimumSupport;
	private MfiContext mfiContext;
	private Map<Integer, FrequentItem> golbalItemsMap;
	private double NeiborhoodGrowthLimit;
	
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
	
	public Map<Integer, FrequentItem> getGolbalItemsMap() {
		return golbalItemsMap;
	}
	
	public void setGolbalItemsMap(Map<Integer, FrequentItem> golbalItemsMap) {
		this.golbalItemsMap = golbalItemsMap;
	}
	
	public double getNeiborhoodGrowthLimit() {
		return NeiborhoodGrowthLimit;
	}
	
	public void setNeiborhoodGrowthLimit(double neiborhoodGrowthLimit) {
		NeiborhoodGrowthLimit = neiborhoodGrowthLimit;
	}
	
	

}
