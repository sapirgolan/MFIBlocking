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
public class BlockInteraction extends DocInteraction {


    @Override
    public void addDoc(IndexWriter indexWriter, String recordId, String recordText) throws IOException {
        recordId = "#" + recordId;
        Document doc = new Document();
        doc.add(new TextField("id", recordId, Field.Store.YES));
        String[] strings = StringUtils.splitByWholeSeparatorPreserveAllTokens(recordText, ",");
        for (int i = 0; i < strings.length; i++) {
            String attribute = strings[i];
            doc.add(new StringField(fieldNames.get(i), attribute, Field.Store.YES));
        }
        indexWriter.addDocument(doc);
    }

    @Override
    public void initFieldList() {
        fieldNames = new ArrayList<>( Arrays.asList("author", "volume", "title", "venue", "year", "month") );
    }

    @Override
    protected List<String> retrieveTextFieldsFromRecord(Document document) {
        List<String> fields = new ArrayList<>();
        for (String fieldName : fieldNames) {
            fields.add( document.get(fieldName) );
        }
        return fields;
    }

}
