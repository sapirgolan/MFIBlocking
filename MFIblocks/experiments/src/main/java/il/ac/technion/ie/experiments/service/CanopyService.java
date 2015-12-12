package il.ac.technion.ie.experiments.service;

import com.google.common.collect.*;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.model.BlockWithData;
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
            blockToCanopyMap.put(repToBlockMap.get(record), recordToCanopyMap.get(record));
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
}
