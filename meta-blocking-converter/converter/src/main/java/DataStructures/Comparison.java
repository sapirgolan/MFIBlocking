package DataStructures;

import java.io.Serializable;

/**
 * created on 11.02.2010
 * by gap2
 */

public class Comparison implements Serializable {

    private static final long serialVersionUID = 723425435776147L;

    private boolean cleanCleanER;
    private final int entityId1;
    private final int entityId2;
    private double utilityMeasure;

    public Comparison (boolean ccER, int id1, int id2) {
        cleanCleanER = ccER;
        entityId1 = id1;
        entityId2 = id2;
        utilityMeasure = -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Comparison other = (Comparison) obj;
        if (this.entityId1 != other.entityId1) {
            return false;
        }
        if (this.entityId2 != other.entityId2) {
            return false;
        }
        return true;
    }
    
    public int getEntityId1() {
        return entityId1;
    }

    public int getEntityId2() {
        return entityId2;
    }
    
    public double getUtilityMeasure() {
        return utilityMeasure;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + this.entityId1;
        hash = 61 * hash + this.entityId2;
        return hash;
    }
    
    public boolean isCleanCleanER() {
        return cleanCleanER;
    }
    
    public void setUtilityMeasure(double utilityMeasure) {
        this.utilityMeasure = utilityMeasure;
    }
}