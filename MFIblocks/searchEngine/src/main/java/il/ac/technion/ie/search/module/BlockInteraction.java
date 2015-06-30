package il.ac.technion.ie.search.module;

import il.ac.technion.ie.utils.PropertiesReader;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by I062070 on 06/05/2015.
 */
public class BlockInteraction extends DocInteraction {

    static final Logger logger = Logger.getLogger(BlockInteraction.class);

    public BlockInteraction(String scenario) {
        super(scenario);
    }

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

    /**
     * For any dataset (scenario), make sure it has a key,value pair in fieldsName.properties
     *
     * @param scenario
     */
    @Override
    public void initFieldList(String scenario) {
        try {
            fieldNames = PropertiesReader.getFields(scenario);
        } catch (ConfigurationException e) {
            logger.error("Didn't find any attributes for scenario name'" + scenario + "'", e);
        }
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
