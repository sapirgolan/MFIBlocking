package il.ac.technion.ie.potential.service;

import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.potential.logic.PotentialLogic;
import il.ac.technion.ie.potential.logic.iPotentialLogic;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPotential;
import il.ac.technion.ie.potential.model.MatrixContext;
import il.ac.technion.ie.potential.model.SharedMatrix;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public class PotentialService implements iPotentialService {

    private iPotentialLogic potentialLogic;
    static final Logger logger = Logger.getLogger(PotentialService.class);

    public PotentialService() {
        potentialLogic = new PotentialLogic();
    }

    @Override
    public AdjustedMatrix getAdjustedMatrix(List<? extends AbstractBlock> blocks) {
        logger.info("calculating an Adjusted Matrix");
        return potentialLogic.calculateAdjustedMatrix(blocks);
    }

    @Override
    public List<BlockPotential> getLocalPotential(List<? extends AbstractBlock> blocks) {
        logger.info("getting local potential of blocks");
        return potentialLogic.getLocalPotential(blocks);
    }

    @Override
    public List<SharedMatrix> getSharedMatrices(List<? extends AbstractBlock> blocks) {
        return potentialLogic.getSharedMatrices(blocks);
    }

    @Override
    public List<MatrixContext<SharedMatrix>> getSharedMatricesWithContext(List<? extends AbstractBlock> blocks) {
        return potentialLogic.getSharedMatricesWithContext(blocks);
    }
}
