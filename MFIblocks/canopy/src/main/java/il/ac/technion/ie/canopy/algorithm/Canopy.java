package il.ac.technion.ie.canopy.algorithm;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.exception.InvalidSearchResultException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.canopy.model.RecordsPool;
import il.ac.technion.ie.canopy.search.CacheWrapper;
import il.ac.technion.ie.canopy.search.SearchCanopy;
import il.ac.technion.ie.canopy.utils.CanopyUtils;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.search.core.SearchEngine;
import il.ac.technion.ie.search.module.DocInteraction;
import il.ac.technion.ie.search.search.ISearch;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
    private SearchEngine searchEngine;
    private volatile RecordsPool<Record> recordsPool;
    private volatile RecordsPool<Record> executedRecordsPool;

    public Canopy(List<Record> records, double t1, double t2) throws CanopyParametersException {
        CanopyUtils.assertT1andT2(t1, t2);
        this.records = new HashMap<>(records.size());
        for (Record record : records) {
            this.records.put(record.getRecordID(), record);
        }
        recordsPool = new RecordsPool<>(this.records.values());
        executedRecordsPool = new RecordsPool<>(this.records.size());
        T2 = t2;
        T1 = t1;
        this.searcher = new SearchCanopy();
        CacheWrapper.init(records.size());
    }

    public synchronized void initSearchEngine(DocInteraction canopyInteraction) {
        searchEngine = new SearchEngine(canopyInteraction);
        searchEngine.addRecords(records.values());
    }

    public Collection<CanopyCluster> createCanopies() throws InvalidSearchResultException {
        ReentrantReadWriteLock recordsPoolLock = new ReentrantReadWriteLock(true);
        ReentrantReadWriteLock executedRecordsPoolLock = new ReentrantReadWriteLock(true);
        ArrayBlockingQueue<Record> queue = new ArrayBlockingQueue<>(100, false);

        JobCreatorManager jobCreatorManager = new JobCreatorManager(recordsPoolLock, recordsPool, queue);
        SearchContext searchContext = new SearchContext(recordsPool, recordsPoolLock, executedRecordsPool, executedRecordsPoolLock, searchEngine, searcher, records, T2, T1);
        WorkersManager workersManager = new WorkersManager(queue, searchContext);

        Future<Boolean> finishedStatus = jobCreatorManager.start();
        Collection<CanopyCluster> canopyClusters = workersManager.start();

        try {
            boolean wereAllJobsCreated = finishedStatus.get();
            logger.info("Were all jobs created? - " + wereAllJobsCreated);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to determine if all jobs were created. Got an exception in Future.get()", e);
        }
        jobCreatorManager.shutdown();

        return canopyClusters;
    }

    public class SearchContext {
        private final Set<Record> recordsPool;
        private final ReentrantReadWriteLock recordsPoolLock;
        private final ReentrantReadWriteLock executedRecordsPoolLock;
        private final SearchEngine searchEngine;
        private final ISearch searcher;
        private final Map<Integer, Record> records;
        private final double t2;
        private final double t1;
        private final Set<Record> executedRecordsPool;
        public int numberOfRecords;

        public ReentrantReadWriteLock getRecordsPoolLock() {
            return recordsPoolLock;
        }

        public ReentrantReadWriteLock getExecutedRecordsPoolLock() {
            return executedRecordsPoolLock;
        }

        public Set<Record> getExecutedRecordsPool() {
            return executedRecordsPool;
        }

        public Set<Record> getRecordsPool() {
            return recordsPool;
        }

        public SearchEngine getSearchEngine() {
            return searchEngine;
        }

        public ISearch getSearcher() {
            return searcher;
        }

        public Map<Integer, Record> getRecords() {
            return records;
        }

        public double getT2() {
            return t2;
        }

        public double getT1() {
            return t1;
        }

        public int getNumberOfRecords() {
            return numberOfRecords;
        }

        public SearchContext(Set<Record> recordsPool, ReentrantReadWriteLock recordsPoolLock, Set<Record> executedRecordsPool, ReentrantReadWriteLock executedRecordsPoolLock, SearchEngine searchEngine, ISearch searcher, Map<Integer, Record> records, double t2, double t1) {
            this.recordsPool = recordsPool;
            this.recordsPoolLock = recordsPoolLock;
            this.executedRecordsPoolLock = executedRecordsPoolLock;
            this.executedRecordsPool = executedRecordsPool;
            this.searchEngine = searchEngine;
            this.searcher = searcher;
            this.records = records;
            this.t2 = t2;
            this.t1 = t1;
        }

    }
}
