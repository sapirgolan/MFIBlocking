package il.ac.technion.ie.canopy.search;

import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.search.module.SearchResult;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
//        List<String> recordsIDs = new ArrayList<>(scoreDocs.size());
        List<SearchResult> results = new ArrayList<>(scoreDocs.size());
        //do processing on results
        logger.debug("Found " + scoreDocs.size() + " hits.");
        for (ScoreDoc hit : scoreDocs) {
            int docId = hit.doc;
            Document document = searcher.doc(docId);
            logger.trace(String.format("Received document with content '%s'", document.get(CanopyInteraction.CONTENT)));
            String recordID = document.get(CanopyInteraction.ID);
            results.add(new SearchResult(recordID, hit.score));
//            recordsIDs.add(recordID);
        }
        return results;
    }
}
