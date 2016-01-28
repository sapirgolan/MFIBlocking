package il.ac.technion.ie.canopy.algorithm;

import com.google.common.collect.Sets;
import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.canopy.search.SearchCanopy;
import il.ac.technion.ie.model.CanopyRecord;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.search.core.SearchEngine;
import il.ac.technion.ie.search.module.SearchResult;
import il.ac.technion.ie.search.search.ISearch;
import net.jcip.annotations.GuardedBy;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by I062070 on 27/01/2016.
 */
public class CanopyWorker implements Callable<Collection<CanopyCluster>>{
    private static final Logger logger = Logger.getLogger(CanopyWorker.class);

    private final ReentrantReadWriteLock recordsPoolLock;
    private final ArrayBlockingQueue<Record> queue;
    private final SearchEngine searchEngine;
    private final ISearch searcher;
    private final Map<Integer, Record> records;
    @GuardedBy("recordsPoolLock") private final Set<Record> recordsPool;
    private final double T1;
    private final double T2;
    @GuardedBy("executedRecordsPoolLock") private final Set<Record> executedRecordsPool;
    private final ReentrantReadWriteLock executedRecordsPoolLock;
    private final int numberOfRecords;

    public CanopyWorker(ArrayBlockingQueue<Record> queue, Canopy.SearchContext searchContext) {
        this.queue = queue;
        searchEngine = searchContext.getSearchEngine();
        searcher = searchContext.getSearcher();
        records = searchContext.getRecords();
        recordsPool = searchContext.getRecordsPool();
        recordsPoolLock = searchContext.getRecordsPoolLock();
        executedRecordsPool = searchContext.getExecutedRecordsPool();
        executedRecordsPoolLock = searchContext.getExecutedRecordsPoolLock();
        numberOfRecords = searchContext.getNumberOfRecords();

        T1 = searchContext.getT1();
        T2 = searchContext.getT2();
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Collection<CanopyCluster> call() throws Exception {
        Collection<CanopyCluster> canopies = new ArrayList<>();

        Record rootRecord;
        logger.trace("Taking a record from Pool");
        while ((rootRecord = queue.poll(10, TimeUnit.SECONDS)) != null && numberOfRecords > executedRecordsPool.size()) {
            logger.debug("Sampled records is: " + rootRecord);
            logger.debug("number of records in the system: " + numberOfRecords);
            logger.debug("number of records executed : " + executedRecordsPool.size());
            logger.debug("Size of queue is:" + queue.size());
            executedRecordsPoolLock.readLock().lock();
            boolean isRecordInTightRecordsOfAnotherCluster = executedRecordsPool.contains(rootRecord);
            executedRecordsPoolLock.readLock().unlock();

            if (isRecordInTightRecordsOfAnotherCluster) {
                logger.debug(rootRecord + " is in TightRecordsOfAnotherCluster, skipping this iteration");
                continue;
            }

            List<SearchResult> searchResults = searchEngine.searchInIndex(searcher, SearchCanopy.DEFAULT_HITS_PER_PAGE, rootRecord.getEntries());
            if (searchResults.isEmpty()) {
                logger.error("The search engine has failed to find any records, even the one that was submitted to search");
            }

            List<CanopyRecord> candidateRecordsForCanopy = findCandidateRecordsForCanopy(searchResults);
            logger.debug("There are " + candidateRecordsForCanopy.size() + " candidate for the canopy");

            long startWait = System.nanoTime();

            logWaitTimeForLock(startWait);
            try {
                logger.debug("Creating a Canopy with input records of size " + candidateRecordsForCanopy.size());
                if (doesAllCandidatesRecordsStillExists(candidateRecordsForCanopy)) {
                    CanopyCluster canopyCluster = CanopyCluster.newCanopyCluster(candidateRecordsForCanopy, T2, T1);
                    logger.debug(String.format("Created Canopy cluster with %d records and seed of %d records",
                            canopyCluster.getAllRecords().size(), canopyCluster.getTightRecords().size()));

                    boolean isCanopyValid = assertAndRemoveCanopyTightRecordsFromPool(rootRecord, canopyCluster);

                    if (isCanopyValid) {
                        canopies.add(canopyCluster);
                        logger.trace("Added canopy");
                    }
                } else {
                    logger.debug("One of the records contained in the candidate for the Canopy no longer exists. " +
                            "This might happen if the candidates were created before one of them was removed by another thread");
                }
            } catch (CanopyParametersException e) {
                logger.error("Failed to create Canopy", e);
            }
            logger.trace("Taking a record from Pool");
        }
        logger.info("there are no more records in the pool. The queue size however is: " + queue.size());
        return canopies;
    }

    private boolean assertAndRemoveCanopyTightRecordsFromPool(Record rootRecord, CanopyCluster canopyCluster) {
        boolean isCanopyValid = false;
        executedRecordsPoolLock.writeLock().lock();
        logger.trace("Obtained write executedRecordsPoolLock on executedRecordsPool");
        boolean stillExists = doesAllCandidatesRecordsStillExists(canopyCluster.getAllRecords());
        if (stillExists) {
            updateRecordsPool(rootRecord, canopyCluster.getTightRecords());
            logger.info("After removing records, there are more " + (numberOfRecords - executedRecordsPool.size()) + " records available for future processing");
            isCanopyValid = true;
        } else {
            logger.debug("One of the records contained in the candidates for the Canopy no longer exists. " +
                    "This might happen if the candidates were created before one of them was removed by another thread");
        }
        executedRecordsPoolLock.writeLock().unlock();
        return isCanopyValid;
    }

    private List<CanopyRecord> findCandidateRecordsForCanopy(List<SearchResult> searchResults) {
        List<CanopyRecord> canopyRecords = fetchRecordsBasedOnIDs(searchResults);
        logger.debug("Transformed " + canopyRecords.size() + " SearchResult records into canopyRecords");
        canopyRecords = retainLegalCandidates(canopyRecords);
        logger.debug("There are " + canopyRecords.size() + " candidates for a canopy");
        return canopyRecords;
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
        executedRecordsPoolLock.readLock().lock();
        int initialSize = candidateRecordsForCanopy.size();
        candidateRecordsForCanopy.removeAll(executedRecordsPool);
        executedRecordsPoolLock.readLock().unlock();
        List<CanopyRecord> recordsForCanopyCluster = new ArrayList<>(candidateRecordsForCanopy);
        logger.debug("Removed " + (initialSize - candidateRecordsForCanopy.size()) + " records since they're already assigned to canopies");
        return recordsForCanopyCluster;
    }

    private void logWaitTimeForLock(long startWait) {
        long endWait = System.nanoTime();
        long waitInMils = TimeUnit.NANOSECONDS.toMillis(endWait - startWait);
        logger.debug(String.format("Waiting time till obtained recordsPoolLock and removed from the pool records " +
                "in T1 of Canopy is: %d Millis", waitInMils));
    }

    /**
     * If any of the candidateRecordsForCanopy exists in executedRecordsPool then the canopy is not valid.
     *
     * @param candidateRecordsForCanopy
     * @return
     */
    private boolean doesAllCandidatesRecordsStillExists(List<CanopyRecord> candidateRecordsForCanopy) {
        executedRecordsPoolLock.readLock().lock();
        Sets.SetView<Record> intersection = Sets.intersection(executedRecordsPool, new HashSet<>(candidateRecordsForCanopy));
        boolean areAllCandidatesStillValid = intersection.isEmpty();
        executedRecordsPoolLock.readLock().unlock();
        return areAllCandidatesStillValid;
    }

    private void updateRecordsPool(Record rootRecord, Collection<? extends Record> tightRecords) {
        recordsPoolLock.writeLock().lock();
        synchronized (recordsPool) {
            recordsPool.remove(rootRecord);
            logger.trace("Removed root records from recordsPool");
            recordsPool.removeAll(tightRecords);
            logger.trace("Removed tightRecords from recordsPool");
        }
        recordsPoolLock.writeLock().unlock();
        synchronized (executedRecordsPool){
            int beforeSize = executedRecordsPool.size();
            executedRecordsPool.add(rootRecord);
            logger.trace("Added root record to executedRecordsPool");
            executedRecordsPool.addAll(tightRecords);
            logger.trace("Added tightRecords to executedRecordsPool");
            logger.debug("added total of " + (executedRecordsPool.size() + 1 - beforeSize) + " records to executedRecordsPool");
        }
    }

}
