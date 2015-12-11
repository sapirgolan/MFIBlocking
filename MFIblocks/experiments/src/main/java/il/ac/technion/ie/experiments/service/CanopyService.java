package il.ac.technion.ie.experiments.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;

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
}
