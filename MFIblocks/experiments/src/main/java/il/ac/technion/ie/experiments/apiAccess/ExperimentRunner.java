package il.ac.technion.ie.experiments.apiAccess;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.exception.InvalidSearchResultException;
import il.ac.technion.ie.experiments.experimentRunners.AbstractExperiment;
import il.ac.technion.ie.experiments.experimentRunners.CreateCanopies;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by I062070 on 27/08/2015.
 */
public class ExperimentRunner {

    static final Logger logger = Logger.getLogger(ExperimentRunner.class);

    public static void main(String[] args) throws CanopyParametersException, InvalidSearchResultException {
        ArgumentsContext context = new ArgumentsContext(args).invoke();

        AbstractExperiment experiment = new CreateCanopies();
        logger.info("Starting an experiment");
        if (context.isProfilingMode()) {
            logger.info("Entering Profiling mode");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long startTime = System.nanoTime();
        try {
            experiment.runExperiments(context.getPathToDataset());
        } catch (Throwable e) {
            logger.error("There was an exception!", e);
        }
        TimeLogger.logDurationInSeconds(startTime, "Creating all canopies took");
        System.exit(0);
    }
}
