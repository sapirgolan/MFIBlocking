package DataStructures;

import java.io.Serializable;

/**
 *
 * @author G.A.P. II
 */

public class AttributesPair implements Serializable {

    private static final long serialVersionUID = 4385960492L;
    
    private final double similarity;
    private final String attribute1;
    private final String attribute2;

    public AttributesPair(double sm, String a1, String a2) {
        similarity = sm;
        attribute1 = a1;
        attribute2 = a2;
    }

    public String getAttribute1() {
        return attribute1;
    }

    public String getAttribute2() {
        return attribute2;
    }

    public double getSimilarity() {
        return similarity;
    }

    @Override
    public String toString() {
        return "at1\t:\t" + attribute1 + "\tat2\t:\t" + attribute2 + "\tsim\t:\t" + similarity;
    }
}