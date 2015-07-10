package il.ac.technion.ie.potential.logic;

import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPotential;

import java.util.List;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public interface iPotentialLogic {
    List<BlockPotential> getLocalPotential(List<Block> blocks);

    AdjustedMatrix calculateAdjustedMatrix(List<Block> blocks);
}
