package il.ac.technion.ie.potential.service;

import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPotential;
import il.ac.technion.ie.potential.model.SharedMatrix;

import java.util.List;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public interface iPotentialService {
    AdjustedMatrix getAdjustedMatrix(List<? extends AbstractBlock> blocks);

    List<BlockPotential> getLocalPotential(List<? extends AbstractBlock> blocks);

    List<SharedMatrix> getSharedMatrices(List<? extends AbstractBlock> blocks);
}
