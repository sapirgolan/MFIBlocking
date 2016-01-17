package il.ac.technion.ie.canopy.search;

import com.google.common.collect.ImmutableMap;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.search.module.SearchResult;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by I062070 on 25/11/2015.
 */
public class ProcessResultsFuture implements Callable<List<SearchResult>> {

    private static final Logger logger = Logger.getLogger(ProcessResultsFuture.class);

    List<ScoreDoc> scoreDocs;
    IndexSearcher searcher;

    public ProcessResultsFuture(ScoreDoc[] scoreDocs, IndexSearcher searcher) {
        if (scoreDocs != null) {
            this.scoreDocs = new ArrayList<>(Arrays.asList(scoreDocs));
        }
        this.searcher = searcher;
    }

    @Override
    public List<SearchResult> call() throws Exception {
        List<SearchResult> results = new ArrayList<>(scoreDocs.size());
        CacheWrapper cacheWrapper = CacheWrapper.getInstance();
        //do processing on results
        logger.debug("Found " + scoreDocs.size() + " hits.");

        Set<Integer> scoreDocsIDs = getAllDocsIDs();
        ImmutableMap<Integer, Document> allPresentDocuments = cacheWrapper.getAll(scoreDocsIDs);
        for (Map.Entry<Integer, Document> entry : allPresentDocuments.entrySet()) {
            SearchResult searchResult = this.buildSearchResultFromDocument(entry.getKey(), entry.getValue());
            results.add(searchResult);
        }

        scoreDocsIDs.removeAll(allPresentDocuments.keySet());

        for (Integer docId : scoreDocsIDs) {
            Document document = cacheWrapper.get(docId, new DocumentCallable(docId));
            SearchResult searchResult = buildSearchResultFromDocument(docId, document);
            results.add(searchResult);
        }
        return results;
    }

    private SearchResult buildSearchResultFromDocument(Integer docId, Document document) {
        logger.trace(String.format("Received document with content '%s'", document.get(CanopyInteraction.CONTENT)));
        String recordID = document.get(CanopyInteraction.ID);
        return new SearchResult(recordID, docId);

    }

    private Set<Integer> getAllDocsIDs() {
        Set<Integer> set = new HashSet<>(scoreDocs.size());
        for (ScoreDoc scoreDoc : scoreDocs) {
            set.add(scoreDoc.doc);
        }
        return set;
    }

    public class DocumentCallable implements Callable<Document> {
        private final int docId;

        public DocumentCallable(int docId) {
            this.docId = docId;
        }

        @Override
        public Document call() throws Exception {
            return searcher.doc(docId);
        }
    }
}
