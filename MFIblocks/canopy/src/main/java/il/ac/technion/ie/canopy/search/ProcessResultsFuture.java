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

        Map<Integer, Float> docIdToScore = getAllDocsIDs();
        logger.debug("Fetching data in 'batch' from cache");
        ImmutableMap<Integer, Document> allPresentDocuments = cacheWrapper.getAll(docIdToScore.keySet());
        logger.debug("fetched " + allPresentDocuments.size() + " items from cache");
        logger.debug((docIdToScore.size() - allPresentDocuments.size()) + " items are not in cache.");

        for (Map.Entry<Integer, Document> entry : allPresentDocuments.entrySet()) {
            Integer docId = entry.getKey();
            Document document = entry.getValue();
            Float score = docIdToScore.get(docId);
            SearchResult searchResult = this.buildSearchResultFromDocument(document, score);
            results.add(searchResult);
        }

        docIdToScore.keySet().removeAll(allPresentDocuments.keySet());

        logger.debug("Fetching " + docIdToScore.size() + " Items from Lucene");
        for (Integer docId : docIdToScore.keySet()) {
            Document document = cacheWrapper.get(docId, new DocumentCallable(docId));
            Float score = docIdToScore.get(docId);
            SearchResult searchResult = buildSearchResultFromDocument(document, score);
            results.add(searchResult);
        }
        return results;
    }

    private SearchResult buildSearchResultFromDocument(Document document, Float score) {
        logger.trace(String.format("Received document with content '%s'", document.get(CanopyInteraction.CONTENT)));
        String recordID = document.get(CanopyInteraction.ID);
        return new SearchResult(recordID, score);

    }

    private Map<Integer, Float> getAllDocsIDs() {
        Map<Integer, Float> docIdToScore = new HashMap<>(scoreDocs.size());
        for (ScoreDoc scoreDoc : scoreDocs) {
            docIdToScore.put(scoreDoc.doc, scoreDoc.score);
        }
        return docIdToScore;
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
