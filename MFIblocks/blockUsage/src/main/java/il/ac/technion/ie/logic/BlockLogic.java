package il.ac.technion.ie.logic;

import il.ac.technion.ie.model.CandidatePairs;
import il.ac.technion.ie.model.RecordMatches;
import il.ac.technion.ie.model.NeighborsVector;
import il.ac.technion.ie.model.NeighborsVectorsCompare;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by I062070 on 13/03/2015.
 */
public class BlockLogic implements iBlockLogic{

    private iFindBlockAlgorithm algorithm;
    static final Logger logger = Logger.getLogger(BlockLogic.class);


    public BlockLogic() {
        this.algorithm = new FindBlockAlgorithm();
    }

    @Override
    public List<List<Integer>> findBlocks(CandidatePairs candidatePairs) {
        ConcurrentHashMap<Integer, RecordMatches> matches = candidatePairs.getAllMatches();
        Set<Integer> recordsIds = matches.keySet();

        List<NeighborsVector> neighborsVectors = buildNeighborVectors(matches, recordsIds);

        algorithm.sort(neighborsVectors, new NeighborsVectorsCompare());
        return algorithm.findBlocks(neighborsVectors);
    }

    private List<NeighborsVector> buildNeighborVectors(ConcurrentHashMap<Integer, RecordMatches> matches, Set<Integer> recordsIds) {
        int numberOfRecords = recordsIds.size();
        List<NeighborsVector> neighborsVectors = new ArrayList<>(numberOfRecords);
        for (Integer recordsId : recordsIds) {
            NeighborsVector vector = new NeighborsVector(recordsId, numberOfRecords);
            Set<Integer> matchedIds = matches.get(recordsId).getMatchedIds();
            for (Integer matchedId : matchedIds) {
                vector.exitsNeighbor(matchedId);
            }
            neighborsVectors.add(vector);
        }
        logger.debug("Finished building 'NeighborVectors' for matched input");
        return neighborsVectors;
    }

}
