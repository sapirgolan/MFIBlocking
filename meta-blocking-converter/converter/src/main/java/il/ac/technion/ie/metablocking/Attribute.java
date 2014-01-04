package il.ac.technion.ie.metablocking;

import java.io.Serializable;

/**
 *
 * @author gap2
 */

public class Attribute implements Serializable {

    private static final long serialVersionUID = 1245324342344634589L;

    private final String name;
    private final String value;

    public Attribute (String nm, String val) {
        name = nm;
        value = val;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Attribute other = (Attribute) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 59 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
}