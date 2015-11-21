package il.ac.technion.ie.search.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;

import java.util.List;

/**
 * Created by I062070 on 20/11/2015.
 */
public interface ISearch {
    List<String> search(Analyzer analyzer, IndexReader index, int hitsPerPage, List<String> terms);
}
