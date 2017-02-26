package il.ac.technion.ie.experiments.apiAccess;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.exception.InvalidSearchResultException;
import il.ac.technion.ie.experiments.experimentRunners.AbstractExperiment;
import il.ac.technion.ie.experiments.experimentRunners.CreateCanopies;
import il.ac.technion.ie.experiments.experimentRunners.ProcessBlocks;
import il.ac.technion.ie.experiments.experimentRunners.ProcessCanopies;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by I062070 on 27/08/2015.
 */
public class ExperimentRunner {

    static final Logger logger = Logger.getLogger(ExperimentRunner.class);

    public static void main(String[] args) throws CanopyParametersException, InvalidSearchResultException {
        ArgumentsContext context = new ArgumentsContext(args).invoke();

        logger.info("Starting an experiment");
        enterDebugModeIfNeeded(context);
        long startTime = System.nanoTime();
        runExperiments(context);
        TimeLogger.logDurationInSeconds(startTime, "Creating all canopies took");
        System.exit(0);
    }

    private static void runExperiments(ArgumentsContext context) {
        try {
            if (isProcessBlocksExperiment(context)) {
                ProcessBlocks processBlocks = new ProcessBlocks();
                processBlocks.runExperiments(context.getPathToSerializedFiles(), context.getPathToDataset());
            } else if (isProcessCanopiesExperiment(context)) {
                ProcessCanopies processCanopies = new ProcessCanopies();
                processCanopies.runExperiments(context.getPathToSerializedFiles(), context.getPathToDataset());
            } else {
                AbstractExperiment experiment = new CreateCanopies();
                experiment.runExperiments(context.getPathToDataset());
            }
        } catch (Throwable e) {
            logger.error("There was an exception!", e);
        }
    }

    private static void enterDebugModeIfNeeded(ArgumentsContext context) {
        if (context.isProfilingMode()) {
            logger.info("Entering Profiling mode");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isProcessCanopiesExperiment(ArgumentsContext context) {
        if (context.getPathToDataset() != null && context.getPathToSerializedFiles() != null) {
            return true;
        }
        return false;
    }

    private static boolean isProcessBlocksExperiment(ArgumentsContext context) {
        return isProcessCanopiesExperiment(context) && context.shouldProcessBlocks();
    }
}
