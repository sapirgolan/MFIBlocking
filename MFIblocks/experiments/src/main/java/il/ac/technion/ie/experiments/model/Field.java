package il.ac.technion.ie.experiments.model;

/**
 * Created by I062070 on 21/08/2015.
 */
public class Field {
    private String name;
    private String value;

    public Field(String fieldName, String value) {
        this.name = fieldName;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
