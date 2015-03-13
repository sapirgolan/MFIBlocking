package il.ac.technion.ie.logic;

import il.ac.technion.ie.model.NeighborsVector;

import java.util.Comparator;
import java.util.List;

/**
 * Created by I062070 on 13/03/2015.
 */
public interface iFindBlockAlgorithm {
    public <T> void sort(List<T> matches, Comparator comparator);

    /**
        This method returns a List of Blocks. The Blocks' content can be of any type.<br>
        It assumes that the List is sorted by cardinality
     */
    public <E extends NeighborsVector> List<List<Integer>> findBlocks(List<E> matches);
}
