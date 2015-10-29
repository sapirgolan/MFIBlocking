package il.ac.technion.ie.experiments.apiAccess;

import com.google.common.base.Splitter;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by I062070 on 29/10/2015.
 */
class ArgumentsContext {
    private String[] args;
    private String pathToDataset;
    private List<Double> thresholds;

    static final Logger logger = Logger.getLogger(ArgumentsContext.class);


    public ArgumentsContext(String... args) {
        this.args = args;
        thresholds = new ArrayList<>();
    }

    public ArgumentsContext invoke() {
        synchronized (this) {
            if (args == null || args.length == 0) {
                System.err.println("There are no file arguments!");
                System.exit(-1);
            }
            pathToDataset = args[0];
            if (args.length > 1) {
                parseThresholds();
            }
        }
        return this;
    }

    private void parseThresholds() {
        List<String> thresholdsString = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(args[1]);
        for (String thresholdStr : thresholdsString) {
            try {
                thresholds.add(Double.valueOf(thresholdStr));
            } catch (NumberFormatException e) {
                logger.error(String.format("Following threshold '%s' is not a number:", thresholdStr));
            }
        }
    }

    public String getPathToDataset() {
        return pathToDataset;
    }

    public double getThreshold() {
        if (thresholds.isEmpty()) {
            return 0;
        }
        return thresholds.get(0);
    }

    public List<Double> getThresholds() {
        return thresholds;
    }
}
