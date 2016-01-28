package il.ac.technion.ie.canopy.algorithm;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by I062070 on 27/01/2016.
 */
public class WorkersManager {
    private static final Logger logger = Logger.getLogger(WorkersManager.class);

    private final ArrayBlockingQueue<Record> queue;
    private final Canopy.SearchContext searchContext;
    private final int numberOfCores;

    public WorkersManager(ArrayBlockingQueue<Record> queue, Canopy.SearchContext searchContext) {
        numberOfCores = Runtime.getRuntime().availableProcessors();

        this.queue = queue;
        this.searchContext = searchContext;
        this.searchContext.numberOfRecords = searchContext.getRecordsPool().size();
    }

    public Collection<CanopyCluster> start() {
        Collection<CanopyCluster> canopies = new ArrayList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfCores, getThreadFactory());
        ExecutorCompletionService<Collection<CanopyCluster>> completionService = new ExecutorCompletionService<>(executorService);

        for (int i = 0; i < numberOfCores; i++) {
            CanopyWorker worker = new CanopyWorker(queue, searchContext);
            logger.info("Submitting Canopy Worker " + i + " out of " + (numberOfCores - 1));
            completionService.submit(worker);
        }

        try {
            for (int i = 0; i < numberOfCores; i++) {
                try {
                    logger.info("Trying to obtain canopies from Canopy Worker " + i + " out of " + (numberOfCores - 1));
                    Collection<CanopyCluster> someCanopies = completionService.take().get();
                    logger.debug("Finished waiting for canopies from worker");
                    logger.info("Obtained " + someCanopies.size() + " canopies");
                    canopies.addAll(someCanopies);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Failed to obtain canopies from worker. Got an exception in Future.get()", e);
                }
            }
        } finally {
            executorService.shutdown();
            try {
                boolean wereAllCanopiesWorkersClosed = executorService.awaitTermination(1, TimeUnit.MINUTES);
                logger.debug("were All Canopies Workers Closed ? " + wereAllCanopiesWorkersClosed);
            } catch (InterruptedException e) {
                logger.error("Failed to wait till reviling if wereAllCanopiesWorkersClosed was closed", e);
            }
        }
        return canopies;
    }

    private ThreadFactory getThreadFactory() {
        return new ThreadFactoryBuilder()
                .setNameFormat("canopyWorker-%d")
                .setDaemon(true)
                .build();
    }

}
