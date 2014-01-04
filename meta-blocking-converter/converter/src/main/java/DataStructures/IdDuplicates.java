package DataStructures;

import java.io.Serializable;

/**
 *
 * @author gap2
 */

public class IdDuplicates implements Serializable {
 
    private static final long serialVersionUID = 7234234586147L;
    
    private final int entityId1;
    private final int entityId2;
    
    public IdDuplicates(int id1, int id2) {
        entityId1 = id1;
        entityId2 = id2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IdDuplicates other = (IdDuplicates) obj;
        if (this.entityId1 != other.entityId1) {
            return false;
        }
        if (this.entityId2 != other.entityId2) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.entityId1;
        hash = 83 * hash + this.entityId2;
        return hash;
    }
    
    public int getEntityId1() {
        return entityId1;
    }
    
    public int getEntityId2() {
        return entityId2;
    }
}