package il.ac.technion.ie.canopy.search;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.lucene.document.Document;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by I062070 on 16/01/2016.
 */
public class CacheWrapper {

    public static final int DEFAULT_SIZE = 10000;
    public static volatile CacheWrapper instance = null;

    private static final Object lock = new Object();
    private Cache<Integer, Document> cache;

    public static CacheWrapper getInstance() {
        CacheWrapper r = instance; //temp variable
        if (r == null) {
            synchronized (lock) {
                r = instance; //force method to recheck the value of 'instance'
                if (r == null) {
                    r = init(DEFAULT_SIZE);
                    instance = r;
                }
            }
        }
        return instance;
    }

    public static CacheWrapper init(int size) {
        return new CacheWrapper(size);
    }

    private CacheWrapper(long maxSize) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
    }

    public synchronized Document get(int docId, Callable<Document> callable) throws ExecutionException {
        return cache.get(docId, callable);
    }

    /**
     * Return all present items in the cache
     * @param docIDs
     * @return
     */
    public synchronized ImmutableMap<Integer, Document> getAll(Collection<Integer> docIDs) {
        return cache.getAllPresent(docIDs);
    }
}
