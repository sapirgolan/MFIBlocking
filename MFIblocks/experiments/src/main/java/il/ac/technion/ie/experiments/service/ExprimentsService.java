package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by I062070 on 27/08/2015.
 */
public class ExprimentsService {

    /**
     * The method filters the blocks whose chosen representative is not the True Representative of the block.
     * It doesn't modify the input collection.
     *
     * @param blocks - A {@link java.util.Collection} of blocks
     * @return {@link java.util.Collection} of blocks. This collection shouldn't be modified
     */
    public final List<BlockWithData> filterBlocksWhoseTrueRepIsNotFirst(final Collection<BlockWithData> blocks) {
        List<BlockWithData> filteredBlocks = new ArrayList<>();
        for (BlockWithData block : blocks) {
            if (block.getTrueRepresentativePosition() != 1) {
                filteredBlocks.add(block);
            }
        }
        return filteredBlocks;
    }
}
