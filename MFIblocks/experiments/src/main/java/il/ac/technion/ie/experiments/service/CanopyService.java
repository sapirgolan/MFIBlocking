package il.ac.technion.ie.experiments.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by I062070 on 10/12/2015.
 */


public class CanopyService {

    static final Logger logger = Logger.getLogger(CanopyService.class);


    public Multimap<Record, CanopyCluster> fetchCanopiesOfSeeds(List<CanopyCluster> canopies, List<BlockWithData> cleanBlocks) {
        Multimap<Record, CanopyCluster> multimap = ArrayListMultimap.create();

        List<Record> trueRepresentatives = getAllTrueRepresentatives(cleanBlocks);
        for (Record trueRepresentative : trueRepresentatives) {
            for (CanopyCluster canopy : canopies) {
                if (canopy.contains(trueRepresentative)) {
                    multimap.put(trueRepresentative, canopy);
                }
            }
        }

        return multimap;
    }

    public List<Record> getAllTrueRepresentatives(List<BlockWithData> cleanBlocks) {
        List<Record> trueRepresentatives = new ArrayList<>(cleanBlocks.size());
        for (BlockWithData cleanBlock : cleanBlocks) {
            Record trueRepresentative = cleanBlock.getTrueRepresentative();
            logger.trace("Adding " + trueRepresentative + " to list of True Representatives");
            trueRepresentatives.add(trueRepresentative);
        }
        logger.trace("Total of " + trueRepresentatives.size() + " were found for " + cleanBlocks.size() + " blocks.");
        return trueRepresentatives;
    }
}
