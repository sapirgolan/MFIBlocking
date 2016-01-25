package il.ac.technion.ie.experiments.apiAccess;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.exception.InvalidSearchResultException;
import il.ac.technion.ie.experiments.experimentRunners.AbstractExperiment;
import il.ac.technion.ie.experiments.experimentRunners.CreateCanopies;
import org.apache.log4j.Logger;

/**
 * Created by I062070 on 27/08/2015.
 */
public class ExperimentRunner {

    static final Logger logger = Logger.getLogger(ExperimentRunner.class);

    public static void main(String[] args) throws CanopyParametersException, InvalidSearchResultException {
        ArgumentsContext context = new ArgumentsContext(args).invoke();

        AbstractExperiment experiment = new CreateCanopies();
        logger.info("Starting an experiment");

        try {
            experiment.runExperiments(context.getPathToDataset());
        } catch (Throwable e) {
            logger.error("There was an exception!", e);
        }
        System.exit(0);
    }
}
