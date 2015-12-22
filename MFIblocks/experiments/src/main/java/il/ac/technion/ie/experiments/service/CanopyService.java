package il.ac.technion.ie.experiments.service;

import com.google.common.collect.*;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.CanopyRecord;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by I062070 on 10/12/2015.
 */


public class CanopyService {

    static final Logger logger = Logger.getLogger(CanopyService.class);


    public Multimap<Record, CanopyCluster> fetchCanopiesOfSeeds(List<CanopyCluster> canopies, Collection<Record> trueRepresentatives) {
        Multimap<Record, CanopyCluster> multimap = ArrayListMultimap.create();

        for (Record trueRepresentative : trueRepresentatives) {
            for (CanopyCluster canopy : canopies) {
                if (canopy.contains(trueRepresentative)) {
                    multimap.put(trueRepresentative, canopy);
                }
            }
        }
        return multimap;
    }

    public BiMap<Record, BlockWithData> getAllTrueRepresentatives(List<BlockWithData> cleanBlocks) {
        HashBiMap<Record, BlockWithData> map = HashBiMap.create(cleanBlocks.size());
        for (BlockWithData cleanBlock : cleanBlocks) {
            Record trueRepresentative = cleanBlock.getTrueRepresentative();
            logger.trace("Adding " + trueRepresentative + " to list of True Representatives");
            map.put(trueRepresentative, cleanBlock);
        }
        logger.trace("Total of " + map.size() + " were found for " + cleanBlocks.size() + " blocks.");
        return map;
    }

    public BiMap<Record, CanopyCluster> selectCanopiesForRepresentatives(Multimap<Record, CanopyCluster> repToCanopyMap, Map<Record, BlockWithData> repToBlockMap) {
        HashBiMap<Record, CanopyCluster> result = HashBiMap.create(repToBlockMap.size());
        Set<Record> trueReps = repToCanopyMap.keySet();
        for (Record trueRep : trueReps) {
            Collection<CanopyCluster> canopyClusters = repToCanopyMap.get(trueRep);
            BlockWithData blockWithData = repToBlockMap.get(trueRep);

            TreeMap<Integer, CanopyCluster> treeMap = new TreeMap<>();
            for (CanopyCluster canopyCluster : canopyClusters) {
                TreeMap<Integer, CanopyCluster> entry = calcIntersection(blockWithData.getMembers(), canopyCluster);
                treeMap.putAll(entry);
            }
            CanopyCluster value = treeMap.lastEntry().getValue();
            logger.debug(String.format("Adding the pair <%s, %s> to mapping between True Rep records and their canopies",
                    trueRep, value));
            result.put(trueRep, value);

        }
        return result;
    }

    public BiMap<BlockWithData, CanopyCluster> mapCanopiesToBlocks(Map<Record, CanopyCluster> recordToCanopyMap, Map<Record, BlockWithData> repToBlockMap) {
        BiMap<BlockWithData, CanopyCluster> blockToCanopyMap = HashBiMap.create(recordToCanopyMap.size());
        for (Record record : recordToCanopyMap.keySet()) {
            BlockWithData blockWithData = repToBlockMap.get(record);
            CanopyCluster canopyCluster = recordToCanopyMap.get(record);
            logger.trace(String.format("%s is mapped to %s by using %s", blockWithData, canopyCluster, record));
            blockToCanopyMap.put(blockWithData, canopyCluster);
        }
        return blockToCanopyMap;
    }

    private TreeMap<Integer, CanopyCluster> calcIntersection(List<Record> blockMembers, CanopyCluster canopyCluster) {
        TreeMap<Integer, CanopyCluster> element = new TreeMap<>();
        Sets.SetView<Record> intersection = Sets.intersection(new HashSet<>(blockMembers), new HashSet<>(canopyCluster.getAllRecords()));
        logger.debug(String.format("The intersection between %s and %s is: %d",
                blockMembers, canopyCluster, intersection.size()));
        element.put(intersection.size(), canopyCluster);
        return element;
    }

    /**
     * The method created a BlockWithData out of a CanopyCluster.
     * It assigns similarities on block records according to the similarity score of each
     * {@link CanopyRecord} and calc the probabilities of the blocks records according to
     * that similarities.
     *
     * @param canopyCluster a cluster to be converted
     * @return BlockWithData
     */
    public BlockWithData convertCanopyToBlock(CanopyCluster canopyCluster) {
        ProbabilityService probabilityService = new ProbabilityService();
        List<CanopyRecord> allCanopyRecords = canopyCluster.getAllRecords();
        List<Record> allRecords = covertCanopyRecordsToRecords(allCanopyRecords);
        BlockWithData blockWithData = new BlockWithData(allRecords);
        for (Record blockRecord : allRecords) {
            if (allCanopyRecords.contains(blockRecord)) {
                int indexOf = allCanopyRecords.indexOf(blockRecord);
                blockWithData.setMemberSimScore(blockRecord, (float) allCanopyRecords.get(indexOf).getScore());
            } else {
                logger.warn("failed to set similarity score on " + blockRecord);
            }
        }
        probabilityService.calcProbabilitiesOfRecords(Lists.newArrayList(blockWithData));
        return blockWithData;
    }

    private List<Record> covertCanopyRecordsToRecords(List<CanopyRecord> allCanopyRecords) {
        List<Record> records = new ArrayList<>();
        if (allCanopyRecords != null) {
            for (CanopyRecord canopyRecord : allCanopyRecords) {
                Record record = new Record(canopyRecord);
                records.add(record);
            }
        }
        return records;
    }

    public List<BlockWithData> convertCanopiesToBlocks(List<CanopyCluster> canopies) {
        List<BlockWithData> list = new ArrayList<>();
        for (CanopyCluster canopy : canopies) {
            if (canopy.size() >= 2) {
                list.add(convertCanopyToBlock(canopy));
            }
        }
        return list;
    }
}
