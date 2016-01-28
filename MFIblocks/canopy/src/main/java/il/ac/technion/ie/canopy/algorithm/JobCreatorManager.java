package il.ac.technion.ie.canopy.algorithm;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import il.ac.technion.ie.model.Record;
import net.jcip.annotations.GuardedBy;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by I062070 on 27/01/2016.
 */
public class JobCreatorManager {
    private static final Logger logger = Logger.getLogger(JobCreatorManager.class);

    @GuardedBy("readLock")
    private final Set<Record> recordsPool;
    private final ArrayBlockingQueue<Record> queue;
    private final Lock readLock;
    private ExecutorService executorService;

    public JobCreatorManager(ReentrantReadWriteLock lock, Set<Record> recordsPool, ArrayBlockingQueue<Record> queue) {
        this.readLock = lock.readLock();
        this.recordsPool = recordsPool;
        this.queue = queue;
    }

    public Future<Boolean> start() {
        executorService = Executors.newSingleThreadExecutor(getThreadFactory());
        JobCreatorCallable creatorCallable = new JobCreatorCallable(readLock, recordsPool, queue);
        logger.info("Submitting JobCreatorCallable");
        return executorService.submit(creatorCallable);
//            Boolean wereAllJobsCreated = booleanFuture.get();
//            logger.info("Were all jobs created? - " + wereAllJobsCreated);
//        } catch (InterruptedException | ExecutionException e) {
//            logger.error("Failed to determine if all jobs were created. Got an exception in Future.get()", e);
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            boolean wasJobsExecutorServiceClosed = executorService.awaitTermination(1, TimeUnit.MINUTES);
            logger.debug("was Jobs Executor Service Closed ? " + wasJobsExecutorServiceClosed);
        } catch (InterruptedException e) {
            logger.error("Failed to wait till reviling if JobsExecutorServiceClosed was closed", e);
        }

    }

    private ThreadFactory getThreadFactory() {
        return new ThreadFactoryBuilder()
                .setNameFormat("JobCreator-%d")
                .setDaemon(true)
                .build();
    }
}
