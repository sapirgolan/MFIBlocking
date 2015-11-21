package il.ac.technion.ie.canopy.model;

import il.ac.technion.ie.search.module.DocInteraction;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.List;

/**
 * Created by I062070 on 20/11/2015.
 */
public class CanopyInteraction extends DocInteraction {

    public static final String CONTENT = "content";
    public static final String ID = "id";

    public CanopyInteraction() {
        super(null);
    }

    @Override
    public void addDoc(IndexWriter indexWriter, String recordId, String recordText) throws IOException {
        Document doc = new Document();
        doc.add(new StringField(ID, recordId, Field.Store.YES));
        doc.add(new TextField(CONTENT, recordText, Field.Store.YES));
        indexWriter.addDocument(doc);
    }

    @Override
    public void initFieldList(String scenario) {

    }

    @Override
    protected List<String> retrieveTextFieldsFromRecord(Document document) {
        String content = document.get(CONTENT);
        return separateContentBySpace(content);
    }
}
