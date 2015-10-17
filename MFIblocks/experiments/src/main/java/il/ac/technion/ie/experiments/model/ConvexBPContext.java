package il.ac.technion.ie.experiments.model;

import java.io.File;

/**
 * Created by I062070 on 26/09/2015.
 */
public class ConvexBPContext {

    private String dir;
    private String uaiFileName;
    private String binaryOutputFile;
    private static final String COMMAND_TEMPLATE = "\"C:\\Program Files\\MPICH2\\bin\\mpiexec.exe\" -n 2 dcBP.exe -f %s -e 0.0 -c 10 -s 20 -o %s";
    private long waitInterval;
    private Double threshold;

    public ConvexBPContext(String dir, String uaiFileName, String binaryOutputFile, long waitIntervalInSeconds) {
        this.dir = dir;
        this.uaiFileName = uaiFileName;
        this.binaryOutputFile = binaryOutputFile;
        this.waitInterval = waitIntervalInSeconds;
    }

    public String getDir() {
        return this.dir;
    }

    public String getCommand() {
        return String.format(COMMAND_TEMPLATE, uaiFileName, binaryOutputFile);
    }

    public String getPathToOutputFile() {
        return dir + File.separator + binaryOutputFile;
    }

    public long getWaitInterval() {
        return waitInterval;
    }

    public String getUaiFileName() {
        return uaiFileName;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Double getThreshold() {
        return threshold;
    }
}
