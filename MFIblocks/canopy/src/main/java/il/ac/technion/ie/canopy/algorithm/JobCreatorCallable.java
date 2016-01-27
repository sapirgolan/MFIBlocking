package il.ac.technion.ie.canopy.algorithm;

import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Created by I062070 on 27/01/2016.
 */
public class JobCreatorCallable implements Callable<Boolean> {
    private static final Logger logger = Logger.getLogger(JobCreatorCallable.class);


    private final Lock readLock;
    private final Set<Record> recordsPool;
    private final ArrayBlockingQueue<Record> queue;

    public JobCreatorCallable(Lock readLock, Set<Record> recordsPool, ArrayBlockingQueue<Record> queue) {
        this.readLock = readLock;
        this.recordsPool = recordsPool;
        this.queue = queue;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Boolean call() throws Exception {
        boolean response = false;
        try {
            do {
                readLock.unlock();
                logger.trace("Released reading lock from 'recordsPool'");
                Record record = this.sampleRecordRandomly();
                if (record != null) {
                    logger.debug("Creating a new job for record: " + record);
                    boolean wasInserted = false;
                    while (!wasInserted) {
                        logger.trace("Trying to add job into jobs pool");
                        wasInserted = queue.offer(record);
                        if (!wasInserted) {
                            logger.trace("pool is full. Skipping current thread using 'yield()'");
                            Thread.yield();
                        } else {
                            logger.trace("new job was added into the pool");
                        }
                    }
                    logger.trace("locking for reading 'recordsPool'");
                    readLock.lock();
                }
            } while (!recordsPool.isEmpty());
            response = true;
        } catch (Exception e) {
            logger.error("Failed to create all jobs", e);
        }
        return response;
    }

    private Record sampleRecordRandomly() {
        long start = System.nanoTime();
        readLock.lock();
        long endTime = System.nanoTime();
        logger.debug("Waited " + TimeUnit.NANOSECONDS.toMillis(endTime - start) + " millis to gain read lock");

        Record randomRecord;
        synchronized (recordsPool) {
            randomRecord = recordsPool.iterator().hasNext() ? recordsPool.iterator().next() : null;
            if (randomRecord != null) {
                logger.trace("Removing " + randomRecord + " from records pool");
                recordsPool.remove(randomRecord);
            }
        }
        readLock.unlock();
        return randomRecord;
    }
}
