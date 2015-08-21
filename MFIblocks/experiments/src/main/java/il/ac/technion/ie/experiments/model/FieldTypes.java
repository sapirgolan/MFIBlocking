package il.ac.technion.ie.experiments.model;

/**
 * Created by I062070 on 21/08/2015.
 */
public enum FieldTypes {
    RECORD_ID("rec_id");

    private String name;

    FieldTypes(String fieldName) {
        this.name = fieldName;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
