package il.ac.technion.ie.potential.model;

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

    public BlockPotential(Block block) {
        this.potential = new HashMap<>();
        this.blockID = block.getId();
        for (Integer recordId : block.getMembers()) {
            double memberScore = (double)block.getMemberProbability(recordId);
            potential.put(recordId, Math.log(memberScore));
        }
    }

    public List<Double> getPotentialValues() {
        return new ArrayList<>(potential.values());
    }

    public int getBlockID() {
        return blockID;
    }
}
