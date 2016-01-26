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

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
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
    private volatile Set<Record> recordsPool;

    public Canopy(List<Record> records, double t1, double t2) throws CanopyParametersException {
        CanopyUtils.assertT1andT2(t1, t2);
        this.records = new HashMap<>(records.size());
        for (Record record : records) {
            this.records.put(record.getRecordID(), record);
        }
        recordsPool = new HashSet<>(this.records.values());
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
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        List<CanopyCluster> canopies = new ArrayList<>();

        try {

            ArrayBlockingQueue<Reader.SearchResultContext> queue = new ArrayBlockingQueue<>(Integer.MAX_VALUE, true);
            Set<ListenableFuture<Collection<CanopyCluster>>> listenableFutures = new HashSet<>();

            for (int i = 0; i < cores; i++) {
                Reader reader = new Reader(recordsPool, searchEngine, searcher, readLock, queue);
                logger.debug("Executing new 'reader' job ");
                ListenableFuture<BigInteger> futureSearchResultContext = listeningReadersExecutorService.submit(reader);
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(10));
            for (int i = 0; i < cores; i++) {
                Writer writer = new Writer(records, recordsPool, T2, T1, lock, queue);
                logger.debug("Executing new 'writer' job ");
                ListenableFuture<Collection<CanopyCluster>> canopyClusterListenableFuture = listeningWritersExecutorService.submit(writer);
                listenableFutures.add(canopyClusterListenableFuture);
            }

            ListenableFuture<List<Collection<CanopyCluster>>> listListenableFuture = Futures.allAsList(listenableFutures);
            logger.info("Start waiting for all threads to finish");
            long startTime = System.nanoTime();
            List<Collection<CanopyCluster>> collectionsOfCanopies = listListenableFuture.get();
            for (Collection<CanopyCluster> collectionOfCanopy : collectionsOfCanopies) {
                if (collectionOfCanopy != null) {
                    canopies.addAll(collectionOfCanopy);
                }
            }
            long endTime = System.nanoTime();
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
