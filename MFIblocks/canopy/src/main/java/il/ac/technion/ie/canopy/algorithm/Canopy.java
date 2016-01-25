package il.ac.technion.ie.canopy.algorithm;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.exception.InvalidSearchResultException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.canopy.search.CacheWrapper;
import il.ac.technion.ie.canopy.search.SearchCanopy;
import il.ac.technion.ie.canopy.utils.CanopyUtils;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.search.core.SearchEngine;
import il.ac.technion.ie.search.module.DocInteraction;
import il.ac.technion.ie.search.search.ISearch;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by I062070 on 21/11/2015.
 * Based on the article "Efficient Clustering of
 * High-Dimensional Data Sets
 * with Application to Reference Matching" at http://www.kamalnigam.com/papers/canopy-kdd00.pdf
 * <p/>
 * There are two parameters: T2, T1 and T2 > T1
 *
 * http://webdam.inria.fr/Jorge/html/wdmch18.html
 */
public class Canopy {

    private static final Logger logger = Logger.getLogger(Canopy.class);

    private final Map<Integer, Record> records;
    private final double T2;
    private final double T1;
    private final ISearch searcher;
    private final int cores;
    private SearchEngine searchEngine;
    private ListeningExecutorService listeningReadersExecutorService;
    private ListeningExecutorService listeningWritersExecutorService;

    public Canopy(List<Record> records, double t1, double t2) throws CanopyParametersException {
        CanopyUtils.assertT1andT2(t1, t2);
        this.records = new HashMap<>(records.size());
        for (Record record : records) {
            this.records.put(record.getRecordID(), record);
        }
        T2 = t2;
        T1 = t1;
        this.searcher = new SearchCanopy();
        cores = Runtime.getRuntime().availableProcessors();
        listeningReadersExecutorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(cores));
        listeningWritersExecutorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(cores));
        CacheWrapper.init(records.size());
    }

    public synchronized void initSearchEngine(DocInteraction canopyInteraction) {
        searchEngine = new SearchEngine(canopyInteraction);
        searchEngine.addRecords(records.values());
    }

    public List<CanopyCluster> createCanopies() throws InvalidSearchResultException {
        Set<Record> recordsPool = new HashSet<>(records.values());
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        Collection<ListenableFuture<CanopyCluster>> futureCanopies = new ArrayList<>();
        List<CanopyCluster> canopies = new ArrayList<>();
        long poolWaitTimeInMillis = (long)(Math.log10(recordsPool.size()) * 1000);

        try {

            while (!recordsPool.isEmpty()) {
                List<Reader> readers = new ArrayList<>();
                for (int i = 0; i < cores; i++) {
                    Reader reader = new Reader(recordsPool, searchEngine, searcher, readLock);
                    readers.add(reader);
                }

                final Object waitForReader = new Object();
                for (Reader reader : readers) {
                    logger.debug("Executing new 'reader' job ");
                    ListenableFuture<Reader.SearchResultContext> futureSearchResultContext = listeningReadersExecutorService.submit(reader);
                    futureSearchResultContext.addListener(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (waitForReader) {
                                waitForReader.notifyAll();
                            }
                        }
                    }, listeningReadersExecutorService);
                    ListenableFuture<CanopyCluster> futureCanopy = Futures.transform(
                            futureSearchResultContext, new Writer(records, recordsPool, T2, T1, lock), listeningWritersExecutorService);
                    futureCanopies.add(futureCanopy);
                }
                synchronized (waitForReader) {
                    waitForReader.wait();
                }

            }
            ListenableFuture<List<CanopyCluster>> successfulCanopyClusters = Futures.successfulAsList(futureCanopies);
            logger.debug("Start waiting for all threads to finish");
            long startTime = System.nanoTime();
            Thread.sleep(poolWaitTimeInMillis);
            int cancelledCanopies = 0,
                    completedCanopies = 0;
            for (ListenableFuture<CanopyCluster> futureCanopy : futureCanopies) {
                if (!futureCanopy.isDone()) {
                    futureCanopy.cancel(true);
                    cancelledCanopies++;
                } else {
                    completedCanopies++;
                }
            }

            canopies = successfulCanopyClusters.get();
            long endTime = System.nanoTime();
            logger.info("Out of " + futureCanopies.size() + " canopies, " + completedCanopies + " were created successfully");
            logger.debug("All threads finished after: " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " millis");
            logger.info("Created total of " + canopies.size() + " canopies");
            return canopies;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to calculate canopies", e);
        } finally {
            listeningReadersExecutorService.shutdown();
            listeningWritersExecutorService.shutdown();
        }
        return canopies;
    }

 /*   *//**
     * Retains only the elements in this list that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this list all of its elements that are not contained in the
     * specified collection.
     *  @param candidateRecordsForCanopy List with all elements
     * @param recordsPool               List containing elements to be retained in this list
     *//*
    private List<CanopyRecord> retainLegalCandidates(List<CanopyRecord> candidateRecordsForCanopy, List<Record> recordsPool) {
        List<CanopyRecord> recordsForCanopyCluster = new ArrayList<>(candidateRecordsForCanopy.size());
        for (CanopyRecord candidate : candidateRecordsForCanopy) {
            if (recordsPool.contains(candidate)) {
                recordsForCanopyCluster.add(candidate);
            }
        }
        return recordsForCanopyCluster;
    }

    private void removeRecords(Collection<Record> recordsPool, Record rootRecord, Collection<? extends Record> tightedRecords) {
        recordsPool.remove(rootRecord);
        recordsPool.removeAll(tightedRecords);
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

    private Record sampleRecordRandomly(List<Record> recordsPool) {
        Collections.shuffle(recordsPool);
        Random random = new Random();
        int randomIndex = random.nextInt(recordsPool.size());
        return recordsPool.get(randomIndex);
    }*/
}
