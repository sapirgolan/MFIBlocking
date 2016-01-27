package il.ac.technion.ie.canopy.search;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.search.core.LuceneUtils;
import il.ac.technion.ie.search.module.SearchResult;
import il.ac.technion.ie.search.search.ISearch;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by I062070 on 20/11/2015.
 */
public class SearchCanopy implements ISearch {

    public static final int DEFAULT_HITS_PER_PAGE = 50000;
    /**
     * see  https://lucene.apache.org/core/2_9_4/queryparsersyntax.html
     */
    public static final String FUZZY_SYNTAX = "%s~0.7";
    private static final Logger logger = Logger.getLogger(SearchCanopy.class);
    public static final String OR = " OR ";
    private ListeningExecutorService listeningExecutorService;
    private int maxHits;

    @Override
    public List<SearchResult> search(Analyzer analyzer, IndexReader index, Integer hitsPerPage, List<String> terms) {

        List<SearchResult> recordsIDs = Collections.synchronizedList(new ArrayList<SearchResult>());
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("searchLucene-%d")
                .setDaemon(true)
                .build();
        listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(6, threadFactory));

        try {
            // Instantiate a query parser
            QueryParser parser = new QueryParser(Version.LUCENE_48, CanopyInteraction.CONTENT, analyzer);
            maxHits = determineHitsPerPage(hitsPerPage);
            // Parse
            Query q = createFuzzyQuery(parser, terms);
            if (q != null) {
                // Instantiate a searcher
                IndexSearcher searcher = new IndexSearcher(index);
                TopDocs topDocs = this.performSearch(q, searcher);

                int numberOfDocumentsInCorpus = topDocs.totalHits;
                int processedDocs = 0;

                while (numberOfDocumentsInCorpus > processedDocs) {
                    if (topDocs == null) {
                        break;
                    }
                    //get The results from TopScoreDocCollector
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    logger.debug("Retrieved total of " + scoreDocs.length + " docs");
                    ScoreDoc lastScoreDoc = scoreDocs[scoreDocs.length - 1];
                    processedDocs += scoreDocs.length;

                    // create a future job for processing the results of the query
                    //this is where the "magic" happens. Here we pass the search results and build List<SearchResult>
                    List<SearchResult> docsInCanopy = RetriveDocsFromCanopy(searcher, scoreDocs);
                    recordsIDs.addAll(docsInCanopy);
                    
                    //perform the actual search on documents
                    topDocs = performSearch(q, searcher, lastScoreDoc);
                }
            } else {
                logger.warn("failed to create query from terms: " + Joiner.on(" ").join(terms));
            }
        } catch (IOException e) {
            logger.error("Failed to perform search", e);
        } finally {
            listeningExecutorService.shutdown();
        }
        return recordsIDs;
    }

    private List<SearchResult> RetriveDocsFromCanopy(IndexSearcher searcher, ScoreDoc[] scoreDocs) {
        CacheWrapper cacheWrapper = CacheWrapper.getInstance();
        List<SearchResult> results = new ArrayList<>(scoreDocs.length);
        //do processing on results
        logger.debug("Found " + scoreDocs.length + " hits. Now fetching those hits as records");

        Map<Integer, Float> docIdToScore = getAllDocsIDs(scoreDocs);
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
            try {
                Document document = cacheWrapper.get(docId, new DocumentCallable(docId, searcher));
                Float score = docIdToScore.get(docId);
                SearchResult searchResult = buildSearchResultFromDocument(document, score);
                results.add(searchResult);
            } catch (ExecutionException e) {
                logger.error("Failed to find a record in canopy", e);
            }
        }
        logger.debug("Finished searching for records in Lucene");
        return results;
    }

    private Map<Integer, Float> getAllDocsIDs(ScoreDoc[] scoreDocs) {
        Map<Integer, Float> docIdToScore = new HashMap<>(scoreDocs.length);
        for (ScoreDoc scoreDoc : scoreDocs) {
            docIdToScore.put(scoreDoc.doc, scoreDoc.score);
        }
        return docIdToScore;
    }

    private SearchResult buildSearchResultFromDocument(Document document, Float score) {
        logger.trace(String.format("Received document with content '%s'", document.get(CanopyInteraction.CONTENT)));
        String recordID = document.get(CanopyInteraction.ID);
        return new SearchResult(recordID, score);
    }

    private TopDocs performSearch(Query q, IndexSearcher searcher) throws IOException {
        TopScoreDocCollector collector = TopScoreDocCollector.create(maxHits, true);
        return retriveDocs(q, searcher, collector);
    }

    private TopDocs performSearch(Query q, IndexSearcher searcher, ScoreDoc lastScoreDoc) throws IOException {
        TopScoreDocCollector collector = TopScoreDocCollector.create(maxHits, lastScoreDoc, true);
        TopDocs topDocs;
        logger.debug("Trying to obtain sync on searchIndex");
        logger.debug("Submitting query to search engine");
        topDocs = retriveDocs(q, searcher, collector);
        return topDocs;
    }

    private TopDocs retriveDocs(Query q, IndexSearcher searcher, TopScoreDocCollector collector) throws IOException {
        try {
            searcher.search(q, collector);
            return collector.topDocs();
        } catch (BooleanQuery.TooManyClauses e) {
            logger.error("Failed to perform query. There were too many clauses", e);
        } catch (IOException e) {
            logger.error("Failed to perform query", e);
        }
        return null;
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
        String concatenatedTerms = Joiner.on(" ").join(terms),
                query = StringUtils.EMPTY;
        try {
            query = concatTermsToFuzzy(terms);
            logger.debug(String.format("built following query '%s' out of '%s'", query, concatenatedTerms));
            return parser.parse(query);
        } catch (ParseException e) {
            logger.error(String.format("Failed to perform search from: '%s'.\nThe output query was: '%s'", terms, query), e);
        }
        return null;
    }

    private String concatTermsToFuzzy(List<String> terms) {
        removeEmptyStrings(terms);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < terms.size(); i++) {
            String tokenTerm = LuceneUtils.tokenTerm(terms.get(i));
            if (StringUtils.isNotEmpty(tokenTerm)) {
                builder.append(String.format(FUZZY_SYNTAX, tokenTerm));
                if (i + 1 != terms.size()) {
                    builder.append(OR);
                }
            }
        }
        String query = builder.toString();
        return StringUtils.removeEnd(query, OR);
    }

    private void removeEmptyStrings(List<String> terms) {
        Iterator<String> iterator = terms.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (StringUtils.isEmpty(next)) {
                iterator.remove();
            }
        }
    }

    public class DocumentCallable implements Callable<Document> {
        private final int docId;
        private final IndexSearcher searcher;

        public DocumentCallable(int docId, IndexSearcher searcher) {
            this.docId = docId;
            this.searcher = searcher;
        }

        @Override
        public Document call() throws Exception {
            return searcher.doc(docId);
        }
    }
}
