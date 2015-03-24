package il.ac.technion.ie.model;

import java.util.Comparator;

/**
 * Created by I062070 on 14/03/2015.
 */
public class NeighborsVectorsCompare implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        int vector1 = ((NeighborsVector) o1).numberOfNeighbors();
        int vector2 = ((NeighborsVector) o2).numberOfNeighbors();
        if (vector1 < vector2) {
            return -1;
        } else if (vector1 > vector2){
            return 1;
        } else {
            return 0;
        }
    }
}
