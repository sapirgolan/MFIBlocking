package il.ac.technion.ie.logic;

import candidateMatches.CandidatePairs;

import java.util.List;

/**
 * Created by I062070 on 13/03/2015.
 */
public interface iBlockLogic {

    List<List<Integer>> findBlocks(CandidatePairs candidatePairs);
}
