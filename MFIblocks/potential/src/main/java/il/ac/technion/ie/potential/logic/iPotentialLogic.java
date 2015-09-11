package il.ac.technion.ie.potential.logic;

import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPotential;
import il.ac.technion.ie.potential.model.SharedMatrix;

import java.util.List;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public interface iPotentialLogic {
    List<BlockPotential> getLocalPotential(List<? extends AbstractBlock> blocks);

    AdjustedMatrix calculateAdjustedMatrix(List<? extends AbstractBlock> blocks);

    List<SharedMatrix> getSharedMatrices(List<? extends AbstractBlock> blocks);
}
