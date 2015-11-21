package il.ac.technion.ie.canopy.search;

import com.google.common.base.Joiner;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.search.search.ISearch;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by I062070 on 20/11/2015.
 */
public class SearchCanopy implements ISearch {

    static final Logger logger = Logger.getLogger(SearchCanopy.class);
    /**
     * see  https://lucene.apache.org/core/2_9_4/queryparsersyntax.html
     */
    public static final String FUZZY_SYNTAX = "%s~0.7";

    @Override
    public List<String> search(Analyzer analyzer, IndexReader index, int hitsPerPage, List<String> terms) {

        List<String> recordsIDs = new ArrayList<>();
        try {
            // Instantiate a query parser
            QueryParser parser = new QueryParser(Version.LUCENE_48, CanopyInteraction.CONTENT, analyzer);
            // Parse
            Query q = createFuzzyQuery(parser, terms);
            if (q != null) {
                // Instantiate a searcher
                IndexSearcher searcher = new IndexSearcher(index);
                // Ranker
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
                // Search!
                searcher.search(q, collector);
                // Retrieve the top-10 documents
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                logger.debug("Retrieved total of " + hits.length + " docs");

                // Display results
                logger.info("Found " + hits.length + " hits.");
                for (ScoreDoc hit : hits) {
                    int docId = hit.doc;
                    Document document = searcher.doc(docId);
                    logger.debug(String.format("Received document with content '%s'", document.get(CanopyInteraction.CONTENT)));
                    String recordID = document.get(CanopyInteraction.ID);
                    recordsIDs.add(recordID);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to perform search", e);
        }
        return recordsIDs;
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
