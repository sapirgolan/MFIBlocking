package il.ac.technion.ie.canopy.algorithm;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.model.CanopyRecord;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.search.module.SearchResult;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by I062070 on 20/01/2016.
 */
public class Writer implements Callable<Collection<CanopyCluster>> {

    private static final Logger logger = Logger.getLogger(Writer.class);

    private final Map<Integer, Record> records;
    private final ReentrantReadWriteLock.WriteLock writeLock;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ArrayBlockingQueue<Reader.SearchResultContext> queue;
    private Set<Record> recordsPool;
    private double T2;
    private double T1;

    public Writer(Map<Integer, Record> records, Set<Record> recordsPool, double t2, double t1, ReentrantReadWriteLock lock, ArrayBlockingQueue<Reader.SearchResultContext> queue) {
        this.records = records;
        this.recordsPool = recordsPool;
        T2 = t2;
        T1 = t1;
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
        this.queue = queue;
    }

    @Override
    public Collection<CanopyCluster> call() throws Exception {
        ArrayList<CanopyCluster> canopies = new ArrayList<>();
        while (!recordsPool.isEmpty()) {
            Reader.SearchResultContext searchResultContext = queue.poll(1, TimeUnit.SECONDS);
            queue.notifyAll();
            if (searchResultContext != null) {
                Record rootRecord = searchResultContext.getRootRecord();
                List<SearchResult> searchResults = searchResultContext.getSearchResults();
                if (searchResults.isEmpty()) {
                    logger.error("The search engine has failed to find any records, even the one that was submitted to search");
                }
                List<CanopyRecord> candidateRecordsForCanopy = fetchRecordsBasedOnIDs(searchResults);
                candidateRecordsForCanopy = retainLegalCandidates(candidateRecordsForCanopy);
                //can extract this to another future
                long startWait = System.nanoTime();
                writeLock.lock();
                logWaitTimeForLock(startWait);
                try {
                    logger.debug("Creating a Canopy with input records of size " + candidateRecordsForCanopy.size());
                    if (doesAllCandidatesRecordsStillEists(candidateRecordsForCanopy)) {
                        CanopyCluster canopyCluster = new CanopyCluster(candidateRecordsForCanopy, T2, T1);
                        canopyCluster.removeRecordsBelowT2();
                        canopyCluster.removeRecordsBelowT1();
                        List<CanopyRecord> tightRecords = canopyCluster.getTightRecords();
                        logger.debug(String.format("Created Canopy cluster with %d records and seed of %d records",
                                canopyCluster.getAllRecords().size(), canopyCluster.getTightRecords().size()));
                        removeRecords(rootRecord, tightRecords);
                        canopies.add(canopyCluster);
                    } else {
                        logger.debug("One of the records contained in the candidate for the Canopy no longer exists. " +
                                "This might happen if the candidates were created before one of them was removed by another thread");
                    }
                } catch (CanopyParametersException e) {
                    logger.error("Failed to create Canopy", e);
                } finally {
                    writeLock.unlock();
                }
            }
        }

        return canopies;
    }

    private void logWaitTimeForLock(long startWait) {
        long endWait = System.nanoTime();
        long waitInMils = TimeUnit.NANOSECONDS.toMillis(endWait - startWait);
        logger.debug(String.format("Waiting time till obtained lock and removed from the pool records " +
                "in T1 of Canopy is: %d Millis", waitInMils));
    }

    private boolean doesAllCandidatesRecordsStillEists(List<CanopyRecord> candidateRecordsForCanopy) {
        return recordsPool.containsAll(candidateRecordsForCanopy);
    }

    private List<CanopyRecord> fetchRecordsBasedOnIDs(List<SearchResult> searchResults) {
        ArrayList<CanopyRecord> list = new ArrayList<>();
        for (SearchResult searchResult : searchResults) {
            try {
                int intID = Integer.parseInt(searchResult.getID());
                Record record = records.get(intID);
                if (record != null) {
                    list.add(new CanopyRecord(record, searchResult.getScore()));
                } else {
                    logger.warn(String.format("Didn't find a records that corresponds to ID: '%d'", intID));
                }
            } catch (NumberFormatException e) {
                logger.error(String.format("Failed to retrieve record corresponds to ID '%s'", searchResult), e);
            }
        }
        return list;
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this list all of its elements that are not contained in the
     * specified collection.
     * @param candidateRecordsForCanopy List with all elements
     */
    private List<CanopyRecord> retainLegalCandidates(List<CanopyRecord> candidateRecordsForCanopy) {
        List<CanopyRecord> recordsForCanopyCluster = new ArrayList<>(candidateRecordsForCanopy.size());
        readLock.lock();
        for (CanopyRecord candidate : candidateRecordsForCanopy) {
            if (recordsPool.contains(candidate)) {
                recordsForCanopyCluster.add(candidate);
            }
        }
        readLock.unlock();
        return recordsForCanopyCluster;
    }

    private void removeRecords(Record rootRecord, Collection<? extends Record> tightRecords) {
        recordsPool.remove(rootRecord);
        recordsPool.removeAll(tightRecords);
        logger.info("There are " + recordsPool.size() + " available for processing");
    }
}
