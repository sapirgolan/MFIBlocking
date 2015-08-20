package il.ac.technion.ie.search.module;

import il.ac.technion.ie.search.exception.TooManySearchResults;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.List;

/**
 * Created by I062070 on 06/05/2015.
 */
public abstract class DocInteraction {

    protected List<String> fieldNames;

    public DocInteraction(String scenario) {
        this.initFieldList(scenario);
    }
    public abstract void addDoc(IndexWriter indexWriter, String recordId, String recordText) throws IOException;

    public abstract void initFieldList(String scenario);
    public final List<String> obtainTopResult(IndexSearcher searcher, ScoreDoc[] hits) throws TooManySearchResults, IOException {
        if (hits.length != 1) {
            throw new TooManySearchResults(String.format("Search has obtained %d results", hits.length));
        }
        int docId = hits[0].doc;
        Document document = searcher.doc(docId);
        return retrieveTextFieldsFromRecord(document);
    }

    protected abstract List<String> retrieveTextFieldsFromRecord(Document document);
}
