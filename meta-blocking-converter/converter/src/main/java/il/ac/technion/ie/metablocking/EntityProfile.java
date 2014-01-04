package il.ac.technion.ie.metablocking;

import java.io.Serializable;
import java.util.HashSet;

/**
 *
 * @author gap2
 */

public class EntityProfile implements Serializable {

    private static final long serialVersionUID = 122354534453243447L;

    private HashSet<Attribute> attributes;
    private String entityUrl;

    public EntityProfile(String url) {
        entityUrl = url;
        attributes = new HashSet<Attribute>();
    }

    public void addAttribute(String propertyName, String propertyValue) {
        attributes.add(new Attribute(propertyName, propertyValue));
    }

    public String getEntityUrl() {
        return entityUrl;
    }

    public int getProfileSize() {
        return attributes.size();
    }
    
    public HashSet<Attribute> getAttributes() {
        return attributes;
    }
}