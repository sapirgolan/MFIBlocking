package il.ac.technion.ie.potential.service;

import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.potential.logic.iPotentialLogic;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPotential;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public class PotentialService implements iPotentialService {

    private iPotentialLogic potentialLogic;
    static final Logger logger = Logger.getLogger(PotentialService.class);


    @Override
    public AdjustedMatrix getAdjustedMatrix(List<Block> blocks) {
        logger.info("calculating an Adjusted Matrix");
        return potentialLogic.calculateAdjustedMatrix(blocks);
    }

    @Override
    public List<BlockPotential> getLocalPotential(List<Block> blocks) {
        logger.info("getting local potential of blocks");
        return potentialLogic.getLocalPotential(blocks);
    }
}
