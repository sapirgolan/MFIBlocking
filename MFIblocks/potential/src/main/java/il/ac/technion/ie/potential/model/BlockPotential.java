package il.ac.technion.ie.potential.model;

import il.ac.technion.ie.exception.NotImplementedYetException;
import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.model.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public class BlockPotential {

    private Map<Integer, Double> potential;
    private int blockID;

    public BlockPotential(AbstractBlock abstractBlock) {
        this.potential = new HashMap<>();
        this.blockID = abstractBlock.getId();
        if (abstractBlock instanceof Block) {
            Block block = (Block) abstractBlock;
            for (Integer recordId : block.getMembers()) {
                double memberScore = (double) abstractBlock.getMemberProbability(recordId);
                potential.put(recordId, Math.log(memberScore));
            }
        } else {
            throw new NotImplementedYetException("The method doesn't support any type that is not 'Block'");
        }
    }

    public List<Double> getPotentialValues() {
        return new ArrayList<>(potential.values());
    }

    public int getBlockID() {
        return blockID;
    }
}
