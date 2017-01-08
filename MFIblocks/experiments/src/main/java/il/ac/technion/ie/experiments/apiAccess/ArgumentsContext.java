package il.ac.technion.ie.experiments.apiAccess;

import org.apache.log4j.Logger;

/**
 * Created by I062070 on 29/10/2015.
 */
class ArgumentsContext {
    static final Logger logger = Logger.getLogger(ArgumentsContext.class);

    private String[] args;
    private String pathToDataset;
    private boolean profilingMode;
    private String pathToCanapies;

    public ArgumentsContext(String... args) {
        this.args = args;
    }

    public ArgumentsContext invoke() {
        synchronized (this) {
            if (args == null || args.length == 0) {
                System.err.println("There are no file arguments!");
                System.exit(-1);
            }
            pathToDataset = args[0];
            if (args.length > 1) {
                if (!isEqualToPerf(args, 1)) {
                    pathToCanapies = args[1];
                }
                if (isEqualToPerf(args, args.length-1)) {
                    this.profilingMode = true;
                }
            }
        }
        return this;
    }

    private boolean isEqualToPerf(String[] argsArray, int index) {
        return argsArray[index].equalsIgnoreCase("perf");
    }

    public boolean isProfilingMode() {
        return profilingMode;
    }

    public String getPathToDataset() {
        return pathToDataset;
    }

    public String getPathToCanapies() {
        return pathToCanapies;
    }
}
