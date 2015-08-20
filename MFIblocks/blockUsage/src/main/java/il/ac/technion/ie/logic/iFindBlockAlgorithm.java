package il.ac.technion.ie.logic;

import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.NeighborsVector;

import java.util.List;

/**
 * Created by I062070 on 13/03/2015.
 */
public interface iFindBlockAlgorithm {
    /**
        This method returns a List of Blocks. The Blocks' content can be of any type.<br>
        It assumes that the List is sorted by cardinality
     */
    public <E extends NeighborsVector> List<Block> findBlocks(List<E> matches);
}
