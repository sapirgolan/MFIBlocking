package il.ac.technion.ie.service;


import il.ac.technion.ie.model.CandidatePairs;

import java.util.List;

/**
 * Created by I062070 on 13/03/2015.
 */
public interface iBlockService {
    public List<List<Integer>> getBlocks(CandidatePairs candidatePairs);

    public List<Integer> getBlocksOfRecord(CandidatePairs candidatePairs, int record);
}
