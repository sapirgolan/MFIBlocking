package il.ac.technion.ie.experiments.model;

/**
 * Created by I062070 on 31/10/2015.
 */
public class DatasetStatistics {
    private final String dataSetName;
    private int numberOfBlocks;
    private double avgBlockSize;

    public DatasetStatistics(String name) {
        this.dataSetName = name;
    }

    public void setNumberOfBlocks(int numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
    }

    public int getNumberOfBlocks() {
        return numberOfBlocks;
    }

    public void setAvgBlockSize(double avgBlockSize) {
        this.avgBlockSize = avgBlockSize;
    }

    public double getAvgBlockSize() {
        return avgBlockSize;
    }

    public String getFileName() {
        return dataSetName;
    }
}
