package il.ac.technion.ie.experiments.apiAccess;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.exception.InvalidSearchResultException;
import il.ac.technion.ie.experiments.experimentRunners.AbstractExperiment;
import il.ac.technion.ie.experiments.experimentRunners.FebrlExperiment;
import org.apache.log4j.Logger;

/**
 * Created by I062070 on 27/08/2015.
 */
public class ExperimentRunner {

    static final Logger logger = Logger.getLogger(ExperimentRunner.class);

    public static void main(String[] args) throws CanopyParametersException, InvalidSearchResultException {
        ArgumentsContext context = new ArgumentsContext(args).invoke();

        AbstractExperiment experiment = new FebrlExperiment();
        experiment.runExperiments(context.getPathToDataset());
        System.exit(0);
    }
}
