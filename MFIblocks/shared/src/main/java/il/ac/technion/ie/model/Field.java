package il.ac.technion.ie.model;

import java.io.Serializable;

/**
 * Created by I062070 on 21/08/2015.
 */
public class Field implements Serializable{
    private static final long serialVersionUID = 5415930039414906542L;
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

    @Override
    public String toString() {
        return "{" +
                name + "='" + value + '\'' +
                '}';
    }
}
