package il.ac.technion.ie.search.search;

import il.ac.technion.ie.search.module.SearchResult;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;

import java.util.List;

/**
 * Created by I062070 on 20/11/2015.
 */
public interface ISearch {

    /**
     * Perform seach on the index according to the list of term that is passed as a parameter
     *
     * @param analyzer      Lucene Analyzer (Can be {@link org.apache.lucene.analysis.standard.StandardAnalyzer})
     * @param index         existing index of documents
     * @param hitsPerPage   Maximum number of hits to be returned.
     * @param terms         List of terms that form the query
     * @return List of recordIds that fits the query and <code>hitsPerPage</code> value
     */
    List<SearchResult> search(Analyzer analyzer, IndexReader index, Integer hitsPerPage, List<String> terms);
}
