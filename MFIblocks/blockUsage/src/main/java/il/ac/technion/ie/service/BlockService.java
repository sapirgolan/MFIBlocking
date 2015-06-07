package il.ac.technion.ie.service;

import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.logic.BlockLogic;
import il.ac.technion.ie.logic.iBlockLogic;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.CandidatePairs;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by I062070 on 13/03/2015.
 */
public class BlockService implements iBlockService {

    static final Logger logger = Logger.getLogger(BlockService.class);

    private iBlockLogic blockLogic;

    public BlockService() {
        this.blockLogic = new BlockLogic();
    }
    @Override
    public List<Block> getBlocks(CandidatePairs candidatePairs) {
        List<Block> result = blockLogic.findBlocks(candidatePairs);
        logger.debug("Finished finding blocks from input");
        return result;
    }

    @Override
    public void calcProbOnBlocks(List<Block> list, MfiContext context) {
        blockLogic.calcProbabilityOnRecords(list, context);
        logger.debug("Finished calculating probabilities on blocks");
    }

    @Override
    public List<Block> getBlocksOfRecord(List<Block> allBlocks, int record) {
        return blockLogic.findBlocksOfRecord(allBlocks, record);
    }
}
