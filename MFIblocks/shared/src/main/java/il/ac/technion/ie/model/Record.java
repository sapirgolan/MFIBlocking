package il.ac.technion.ie.model;

import il.ac.technion.ie.utils.MathUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by I062070 on 21/08/2015.
 */
public class Record {
    protected Map<String, Field> fields;
    protected Integer recordID;
    protected String recordName;

    static final Logger logger = Logger.getLogger(Record.class);

    public Record(List<String> fieldsName, List<String> values, int id) {
        fields = new TreeMap<>();
        recordID = id;

        if (fieldsName.size() != values.size()) {
            String message = String.format("Number of fields and values doesn't match! There are %d and %d values",
                    fieldsName.size(), values.size());
            logger.error(message);
            return;
        }

        for (int i = 0; i < fieldsName.size(); i++) {
            String fieldName =  fieldsName.get(i);
            if (isFieldRecordID(fieldName)) {
                this.recordName = values.get(i);
            }
            fields.put(fieldName, new Field(fieldName, values.get(i)));
        }
    }

    /**
     * DO NOT USE THIS CONSTRUCTOR
     */
    protected Record() {

    }

    protected boolean isFieldRecordID(String fieldName) {
        return FieldTypes.RECORD_ID.toString().equalsIgnoreCase(fieldName);
    }

    public Integer getRecordID() {
        return recordID;
    }

    public String getRecordName() {
        return recordName;
    }

    /**
     * @return {@link java.util.List List} a List with the values of all the fields
     */
    public List<String> getEntries() {
        List<String> entries = new ArrayList<>();
        for (Field field : fields.values()) {
            entries.add(field.getValue());
        }
        return entries;
    }

    /**
     * @return {@link java.util.List List} a List with the names of all the fields
     */
    public List<String> getFieldNames() {
        List<String> fieldNames = new ArrayList<>();
        for (Field field : fields.values()) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public static Logger getLogger() {
        return logger;
    }

    @Override
    public String toString() {
        if (recordName != null) {
            return recordName;
        }
//        return Joiner.on(" ").join(fields.values());
        return String.valueOf(recordID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Record)) {
            return false;
        }

        Record record = (Record) o;

        if (!recordID.equals(record.recordID)) {
            return false;
        }
        List<String> copyOfValues = new ArrayList<>(this.getEntries());

        if (!copyOfValues.containsAll(record.getEntries())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        List<String> entries = this.getEntries();
        List<Integer> primeNumbers = MathUtils.getPrimeNumbers(entries.size() + 1);
        if (primeNumbers != null) {
            for (int i = 0; i < entries.size(); i++) {
                hashCode += entries.get(i).hashCode() * primeNumbers.get(i);
            }
            hashCode += recordID.hashCode() * primeNumbers.get(primeNumbers.size() - 1);

        } else {
            hashCode = fields.hashCode();
            hashCode = 31 * hashCode + recordID.hashCode();
        }
        return hashCode;
    }


}
