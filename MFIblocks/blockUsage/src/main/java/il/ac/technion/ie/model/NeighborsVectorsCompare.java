package il.ac.technion.ie.model;

import java.util.Comparator;
import java.util.List;

/**
 * Created by I062070 on 14/03/2015.
 */
public class NeighborsVectorsCompare implements Comparator<NeighborsVector> {

    @Override
    public int compare(NeighborsVector that, NeighborsVector other) {
        int vector1 = that.numberOfNeighbors();
        int vector2 = other.numberOfNeighbors();
        if (vector1 < vector2) {
            return -1;
        } else if (vector1 > vector2){
            return 1;
        }

        //both NeighborsVector have the same size, we will compare element by element
        List<Integer> o1Neighbors = that.getNeighbors();
        List<Integer> o2Neighbors = other.getNeighbors();

        for (int i = 0; i < o2Neighbors.size(); i++) {
            Integer o2Element = o2Neighbors.get(i);
            Integer o1Element = o1Neighbors.get(i);
            if (o1Element < o2Element) {
                return -1;
            } else if (o1Element > o2Element) {
                return 1;
            }
        }
        return 0;
    }
}
