package il.ac.technion.ie.logic;


import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.CandidatePairs;

import java.util.List;

/**
 * Created by I062070 on 13/03/2015.
 */
public interface iBlockLogic {

    List<Block> findBlocks(CandidatePairs candidatePairs, int recordsSize);

    void calcProbabilityOnRecords(final List<Block> result, MfiContext context);

    List<Block> findBlocksOfRecord(List<Block> allBlocks, int recordId);
}
