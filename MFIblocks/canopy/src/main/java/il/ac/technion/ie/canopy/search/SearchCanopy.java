package il.ac.technion.ie.canopy.search;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.search.module.SearchResult;
import il.ac.technion.ie.search.search.ISearch;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by I062070 on 20/11/2015.
 */
public class SearchCanopy implements ISearch {

    public static final int DEFAULT_HITS_PER_PAGE = 50;
    /**
     * see  https://lucene.apache.org/core/2_9_4/queryparsersyntax.html
     */
    public static final String FUZZY_SYNTAX = "%s~0.7";
    private static final Logger logger = Logger.getLogger(SearchCanopy.class);
    private ListeningExecutorService listeningExecutorService;
    private int maxHits;

    @Override
    public List<SearchResult> search(Analyzer analyzer, IndexReader index, Integer hitsPerPage, List<String> terms) {

        List<SearchResult> recordsIDs = Collections.synchronizedList(new ArrayList<SearchResult>());
        listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
        List<ListenableFuture<List<SearchResult>>> futureRecordIDs = new ArrayList<>();

        try {
            // Instantiate a query parser
            QueryParser parser = new QueryParser(Version.LUCENE_48, CanopyInteraction.CONTENT, analyzer);
            maxHits = determineHitsPerPage(hitsPerPage);
            // Parse
            Query q = createFuzzyQuery(parser, terms);
            if (q != null) {
                // Instantiate a searcher
                IndexSearcher searcher = new IndexSearcher(index);
                performSearch(q, searcher, null);
                TopDocs topDocs = this.performSearch(q, searcher);

                int numberOfDocumentsInCorpus = topDocs.totalHits;
                int processedDocs = 0;

                while (numberOfDocumentsInCorpus > processedDocs) {
                    //get The results from TopScoreDocCollector
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    logger.debug("Retrieved total of " + scoreDocs.length + " docs");
                    ScoreDoc lastScoreDoc = scoreDocs[scoreDocs.length - 1];
                    processedDocs += scoreDocs.length;

                    // create a future job for processing the results of the query
                    createFutureForDocsProcessing(searcher, futureRecordIDs, scoreDocs);
                    //perform the actual search on documents
                    topDocs = performSearch(q, searcher, lastScoreDoc);
                }
                ListenableFuture<List<List<SearchResult>>> successfulRecordIDs = Futures.successfulAsList(futureRecordIDs);
                logger.debug("Start for all threads to finish");
                long startTime = System.nanoTime();
                List<List<SearchResult>> lists = successfulRecordIDs.get();
                long endTime = System.nanoTime();
                logger.debug("All threads finished after: " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " millis");
                for (List<SearchResult> list : lists) {
                    logger.debug("Adding '" + list.size() + "' docs to result from Search Engine");
                    recordsIDs.addAll(list);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to perform search", e);
        } catch (InterruptedException e) {
            logger.error("Failed to perform search", e);
        } catch (ExecutionException e) {
            logger.error("Failed to perform search", e);
        }
        return recordsIDs;
    }

    private TopDocs performSearch(Query q, IndexSearcher searcher) throws IOException {
        TopScoreDocCollector collector = TopScoreDocCollector.create(maxHits, true);
        return retriveDocs(q, searcher, collector);
    }

    private TopDocs performSearch(Query q, IndexSearcher searcher, ScoreDoc lastScoreDoc) throws IOException {
        TopScoreDocCollector collector = TopScoreDocCollector.create(maxHits, lastScoreDoc, true);
        return retriveDocs(q, searcher, collector);
    }

    private TopDocs retriveDocs(Query q, IndexSearcher searcher, TopScoreDocCollector collector) throws IOException {
        searcher.search(q, collector);
        return collector.topDocs();
    }

    private void createFutureForDocsProcessing(IndexSearcher searcher, List<ListenableFuture<List<SearchResult>>> futureRecordIDs, ScoreDoc[] scoreDocs) {
        ProcessResultsFuture future = new ProcessResultsFuture(scoreDocs, searcher);
        ListenableFuture<List<SearchResult>> submit = listeningExecutorService.submit(future);
        futureRecordIDs.add(submit);
    }

    private int determineHitsPerPage(Integer hitsPerPage) {
        int hits;
        if (hitsPerPage != null && hitsPerPage > 0) {
            hits = hitsPerPage;
        } else {
            hits = DEFAULT_HITS_PER_PAGE;
        }
        return hits;
    }

    private Query createFuzzyQuery(QueryParser parser, List<String> terms) {
        String concatenatedTerms = Joiner.on(" ").join(terms);
        try {
            String query = concatTermsToFuzzy(terms);
            logger.info(String.format("built following query '%s' out of '%s'", query, concatenatedTerms));
            return parser.parse(query);
        } catch (ParseException e) {
            logger.error(String.format("Failed to perform search on '%s'", concatenatedTerms));
        }
        return null;
    }

    private String concatTermsToFuzzy(List<String> terms) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < terms.size(); i++) {
            builder.append(String.format(FUZZY_SYNTAX, terms.get(i)));
            if (i + 1 != terms.size()) {
                builder.append(" OR ");
            }
        }
        return builder.toString();
    }
}
