package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.threads.CalcProbabilityAction;
import il.ac.technion.ie.probability.SimilarityCalculator;
import org.apache.log4j.Logger;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * Created by I062070 on 26/08/2015.
 */
public class ProbabilityService {

    static final Logger logger = Logger.getLogger(ProbabilityService.class);
    private ForkJoinPool pool;

    public ProbabilityService() {
        initPool();
    }

    private void initPool() {
        if (pool != null) {
            if (pool.isShutdown()) {
                pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            }
        } else {
            pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        }
    }

    public void calcProbabilitiesOfRecords(List<BlockWithData> blocks) {
        this.initPool();
        //iterate on each block
        long startTime = System.nanoTime();
        SimilarityCalculator calculator = new SimilarityCalculator(new JaroWinkler());
        for (BlockWithData block : blocks) {
            logger.trace("adding a new job to pool");
            pool.execute(new CalcProbabilityAction(block, calculator));
        }
        try {
            logger.debug("Executing shutdown command on pool");
            long waitForJobsToFinish = System.nanoTime();
            pool.shutdown();
            boolean termination = pool.awaitTermination(10, TimeUnit.SECONDS);
            long endTime = System.nanoTime();
            logger.debug(String.format("Total executing time is: %d Millis", TimeUnit.NANOSECONDS.toMillis(endTime - startTime)));
            logger.debug(String.format("Waited %d Millis till all jobs finished", TimeUnit.NANOSECONDS.toMillis(endTime - waitForJobsToFinish)));
            String result = termination ? "successful" : "unsuccessful";
            logger.info("Calculating probabilities on blocks was " + result);

        } catch (InterruptedException e) {
            logger.error("Failed to wait till termination of jobs pool", e);
        }
    }
}