package il.ac.technion.ie.logic;

import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.NeighborsVector;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by I062070 on 13/03/2015.
 */
public class FindBlockAlgorithm implements iFindBlockAlgorithm{

    private static final int TWO = 2;
    static final Logger logger = Logger.getLogger(FindBlockAlgorithm.class);


    @Override
    public <T> void sort(List<T> matches, Comparator comparator) {
        Collections.sort(matches, comparator);
        logger.debug("Finished sorting input of algorithm");
    }

    @Override
    public <E extends NeighborsVector> List<Block> findBlocks(List<E> matches) {
        int largestBlockCreated = 0;
        ArrayList<Integer> itemsSeen = new ArrayList<>();
        List<Block> result = new ArrayList<>();
        for (E match : matches) {
            if (isSingleOrDoubleBlock(match)) {
                logger.trace("input block is of size <=2, " + match.toString());
                largestBlockCreated = updateBlocks(itemsSeen, result, match, match.getNeighbors());
            } else {
                logger.debug("input block is of size  " + match.numberOfNeighbors() + match.toString());
                List<Integer> neighbors = new ArrayList<>(match.getNeighbors());
                logger.debug("Removing following items from Block since their origin block " +
                        "was already processed: " + itemsSeen.toString());
                neighbors.removeAll(itemsSeen);
                logger.debug("After removal block's size is " + neighbors.size());
                if (neighbors.size() >= largestBlockCreated) {
                    logger.debug("After removal block size >= 'last added block' (" + largestBlockCreated + ")");
                    largestBlockCreated = updateBlocks(itemsSeen, result, match, neighbors);
                }
            }
        }
        return result;
    }

    /**
     * update the blocks
     *
     * @param itemsSeen - List of items that their row vectors has already processed
     * @param result - list of block to be returned by algorithm
     * @param match - current {@link il.ac.technion.ie.model.NeighborsVector}
     * @param neighbors - List of all records that share some block with current {@link il.ac.technion.ie.model.NeighborsVector}
     * @return
     */
    private <E extends NeighborsVector> int updateBlocks(List<Integer> itemsSeen, List<Block> result, E match, List<Integer> neighbors) {
        itemsSeen.add(match.getReresentativeId());
        Block block = new Block(neighbors);
        result.add(block);
        logger.debug("Added to result block: " + block.toString());
        return neighbors.size();
    }

    private <E extends NeighborsVector> boolean isSingleOrDoubleBlock(E match) {
        return match.numberOfNeighbors() <= TWO;
    }
}
