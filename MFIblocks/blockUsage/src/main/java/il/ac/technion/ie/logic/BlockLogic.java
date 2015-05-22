package il.ac.technion.ie.logic;

import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.*;
import il.ac.technion.ie.search.core.SearchEngine;
import il.ac.technion.ie.search.module.BlockInteraction;
import org.apache.log4j.Logger;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by I062070 on 13/03/2015.
 */
public class BlockLogic implements iBlockLogic {

    static final Logger logger = Logger.getLogger(BlockLogic.class);
    private iFindBlockAlgorithm algorithm;


    public BlockLogic() {
        this.algorithm = new FindBlockAlgorithm();
    }

    @Override
    public List<Block> findBlocks(CandidatePairs candidatePairs) {
        ConcurrentHashMap<Integer, RecordMatches> matches = candidatePairs.getAllMatches();
        Set<Integer> recordsIds = matches.keySet();

        List<NeighborsVector> neighborsVectors = buildNeighborVectors(matches, recordsIds);

        algorithm.sort(neighborsVectors, new NeighborsVectorsCompare());
        return algorithm.findBlocks(neighborsVectors);
    }

    @Override
    public void calcProbabilityOnRecords(final List<Block> blocks, MfiContext context) {
        SearchEngine searchEngine = buildSearchEngineForRecords(context);
        calcProbability(blocks, searchEngine);
    }

    private void calcProbability(List<Block> blocks, SearchEngine searchEngine) {
        SimilarityCalculator similarityCalculator = new SimilarityCalculator(new JaroWinkler());
        //iterate on each block
        for (Block block : blocks) {
            List<Integer> blockMembers = block.getMembers();
            //retrieve block Text attributes
            Map<Integer, List<String>> blockAtributess = getMembersAtributes(blockMembers, searchEngine);
            for (Integer currentRecordId : blockAtributess.keySet()) {
                float currentRecordProb = calcRecordSimilarityInBlock(currentRecordId, blockAtributess, similarityCalculator);
                block.setMemberSimScore(currentRecordId, currentRecordProb);
            }
            calcRecordsProbabilityInBlock(block);
        }
    }

    private void calcRecordsProbabilityInBlock(Block block) {
        float allScores = 0;
        for (Integer member : block.getMembers()) {
            allScores += block.getMemberScore(member);
        }

        for (Integer member : block.getMembers()) {
            block.setMemberProbability(member, block.getMemberScore(member) / allScores);
        }

    }

    private float calcRecordSimilarityInBlock(Integer currentRecordId, final Map<Integer, List<String>> blockAtributess,
                                              SimilarityCalculator similarityCalculator) {
        float recordsSim = 0;
        //case block contains a single record
        if (blockAtributess.size() == 1) {
            return 1;
        }
        for (Integer next : blockAtributess.keySet()) {
            if (next != currentRecordId) {
                recordsSim += similarityCalculator.calcRecordsSim(blockAtributess.get(next), blockAtributess.get(currentRecordId));
            }
        }
        return recordsSim;
    }

    /**
     * The method retrieves list if IDs and an instance of @SearchEngine and returns for each ID a list
     * with fields that record has.
     *
     * @param recordID
     * @param searchEngine
     * @return <Integer, List<String>>
     */
    private Map<Integer, List<String>> getMembersAtributes(List<Integer> recordID, SearchEngine searchEngine) {
        Map<Integer, List<String>> blockFields = new HashMap<>();
        for (Integer blockMemberID : recordID) {
            List<String> recordAttributes = searchEngine.getRecordAttributes(String.valueOf(blockMemberID));
            blockFields.put(blockMemberID, recordAttributes);
        }
        return blockFields;
    }

    /**
     *
     * @param context
     * @return
     */

    private SearchEngine buildSearchEngineForRecords(MfiContext context) {
        String recordsPath = context.getOriginalRecordsPath();
        logger.debug("about to index: " + recordsPath);
        SearchEngine searchEngine = new SearchEngine(new BlockInteraction());
        searchEngine.addRecords(recordsPath);
        return searchEngine;
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
