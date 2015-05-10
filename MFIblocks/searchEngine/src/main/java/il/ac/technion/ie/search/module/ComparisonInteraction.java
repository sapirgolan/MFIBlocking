package il.ac.technion.ie.search.module;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by I062070 on 06/05/2015.
 */
public class ComparisonInteraction extends DocInteraction {

    @Override
    public void addDoc(IndexWriter indexWriter, String recordId, String recordText) throws IOException {
        recordId = "#" + recordId;
        Document doc = new Document();
        doc.add(new TextField("id", recordId, Field.Store.YES));
        doc.add(new StringField("qgrams", recordText, Field.Store.YES));
        indexWriter.addDocument(doc);
    }

    @Override
    public void initFieldList() {
    }

    @Override
    protected List<String> retrieveTextFieldsFromRecord(Document document) {
        String concatonatedQgrams = document.get("qgrams");
        return seperateQgrams(concatonatedQgrams);
    }

    //tested
    private List<String> seperateQgrams(String result) {
        List<String> qgrams = new ArrayList<>();
        if (result != null) {
            result = result.trim();
        }
        if (!StringUtils.isEmpty(result)) {
            String[] strings = StringUtils.splitByWholeSeparatorPreserveAllTokens(result, " ");
            qgrams = new ArrayList<>(Arrays.asList(strings));
        }
        return qgrams;
    }
}
