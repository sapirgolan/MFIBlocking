package il.ac.technion.ie.model;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by I062070 on 27/06/2015.
 */
public class BlockDescriptor {
    static final Logger logger = Logger.getLogger(BlockDescriptor.class);
    private Block block;
    private List<Integer> blockRepresentatives;
    private double representativeScore;
    private Map<Integer, List<String>> blockRecords;


    public BlockDescriptor(Block block) {
        this.block = block;
        this.blockRepresentatives = new ArrayList<>();
        this.blockRecords = new TreeMap<>();
        Map<Integer, Float> representatives = block.findBlockRepresentatives();
        initRepresentatives(representatives);
    }

    private void initRepresentatives(Map<Integer, Float> representatives) {
        for (Map.Entry<Integer, Float> representative : representatives.entrySet()) {
            representativeScore = representative.getValue();
            blockRepresentatives.add(representative.getKey());
        }
    }

    public List<Integer> getMembers() {
        return block.getMembers();
    }

    public void addTextRecord(Integer recordId, List<String> recordAttributes) {
        blockRecords.put(recordId, recordAttributes);
    }

    public List<Integer> getBlockRepresentatives() {
        return blockRepresentatives;
    }

    public Map<Integer, List<String>> getBlockRecords() {
        return blockRecords;
    }

    public List<String> getContentOfRecord(Integer recordId) {
        if (!blockRecords.containsKey(recordId)) {
            return new ArrayList<>();
        }
        return blockRecords.get(recordId);
    }
}
