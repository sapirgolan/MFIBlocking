package il.ac.technion.ie.service;


import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.BlockDescriptor;
import il.ac.technion.ie.model.CandidatePairs;

import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 13/03/2015.
 */
public interface iBlockService {
    public List<Block> getBlocks(CandidatePairs candidatePairs, int recordsSize);

    public void calcProbOnBlocks(List<Block> list, MfiContext context);

    List<Block> getBlocksOfRecord(List<Block> allBlocks, int record);

    void setTrueMatch(List<Block> blocks);

    Map<Integer, List<BlockDescriptor>> findAmbiguousRepresentatives(List<Block> algorithmBlocks, MfiContext context);
}
