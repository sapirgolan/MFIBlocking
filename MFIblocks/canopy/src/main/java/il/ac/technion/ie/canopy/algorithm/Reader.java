package il.ac.technion.ie.canopy.algorithm;

import il.ac.technion.ie.canopy.search.SearchCanopy;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.search.core.SearchEngine;
import il.ac.technion.ie.search.module.SearchResult;
import il.ac.technion.ie.search.search.ISearch;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Created by I062070 on 20/01/2016.
 */
public class Reader implements Callable<Reader.SearchResultContext> {

    private static final Logger logger = Logger.getLogger(Reader.class);

    private final Lock lock;
    private Set<Record> recordsPool;
    private SearchEngine searchEngine;
    private ISearch searcher;

    public Reader(Set<Record> recordsPool, SearchEngine searchEngine, ISearch searcher, Lock readLock) {
        this.recordsPool = recordsPool;
        this.searchEngine = searchEngine;
        this.searcher = searcher;
        this.lock = readLock;
    }

    @Override
    public SearchResultContext call() throws Exception {
        Record rootRecord = sampleRecordRandomly();
        List<SearchResult> searchResults = searchEngine.searchInIndex(searcher, SearchCanopy.DEFAULT_HITS_PER_PAGE, rootRecord.getEntries());
        SearchResultContext context = new SearchResultContext(searchResults, rootRecord);
        return context;
    }

    private Record sampleRecordRandomly() {
        long start = System.nanoTime();
        lock.lock();
        long endTime = System.nanoTime();
        logger.debug("Waited " + TimeUnit.NANOSECONDS.toMillis(endTime - start) + " millis to gain read lock");

        int randomIndex = new Random().nextInt(recordsPool.size());
        Record randomedRecord = null;
        int i = 0;
        for (Record record : recordsPool) {
            if (i == randomIndex) {
                randomedRecord = record;
                break;
            }
            i++;
        }
        lock.unlock();
        return randomedRecord;
    }

    public class SearchResultContext {
        public List<SearchResult> getSearchResults() {
            return searchResults;
        }

        public Record getRootRecord() {
            return rootRecord;
        }

        private final List<SearchResult> searchResults;
        private final Record rootRecord;

        public SearchResultContext(List<SearchResult> searchResults, Record rootRecord) {
            this.searchResults = searchResults;
            this.rootRecord = rootRecord;
        }
    }
}
