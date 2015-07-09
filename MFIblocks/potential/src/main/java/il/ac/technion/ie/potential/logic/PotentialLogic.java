package il.ac.technion.ie.potential.logic;

import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.potential.model.BlockPotential;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public class PotentialLogic implements iPotentialLogic {
    static final Logger logger = Logger.getLogger(PotentialLogic.class);

    @Override
    public List<BlockPotential> getLocalPotential(List<Block> blocks) {
        List<BlockPotential> result = new ArrayList<>(blocks.size());
        for (Block block : blocks) {
            if (block.size() > 1) {
                logger.debug("calculating local potential of Block: " + block.toString());
                result.add( new BlockPotential(block));
            }
        }
        return result;
    }
}
