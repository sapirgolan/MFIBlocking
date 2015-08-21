package il.ac.technion.ie.experiments.model;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 21/08/2015.
 */
public class Record {
    private Map<String, Field> fields;
    private String recordID;

    static final Logger logger = Logger.getLogger(Record.class);

    public Record(List<String> fieldsName, List<String> values) {
        fields = new HashMap<>();

        if (fieldsName.size() != values.size()) {
            String message = String.format("Number of fields and values doesn't match! There are %d and %d values",
                    fieldsName.size(), values.size());
            logger.error(message);
            return;
        }

        for (int i = 0; i < fieldsName.size(); i++) {
            String fieldName =  fieldsName.get(i);
            if (isFieldRecordID(fieldName)) {
                this.recordID = values.get(i);
            }
            fields.put(fieldName, new Field(fieldName, values.get(i)));
        }
    }

    private boolean isFieldRecordID(String fieldName) {
        return FieldTypes.RECORD_ID.toString().equalsIgnoreCase(fieldName);
    }

    public String getRecordID() {
        return recordID;
    }
}
