package il.ac.technion.ie.experiments.model;

import com.google.common.collect.Multimap;
import il.ac.technion.ie.model.Record;

/**
 * Created by I062070 on 25/02/2017.
 */
public class BlocksPair {
    private final Multimap<Record, BlockWithData> blocksThatShouldRemain;
    private final Multimap<Record, BlockWithData> blocksThatShouldNotRemain;

    public BlocksPair(Multimap<Record, BlockWithData> blocksThatShouldRemain, Multimap<Record, BlockWithData> blocksThatShouldNotRemain) {
        this.blocksThatShouldRemain = blocksThatShouldRemain;
        this.blocksThatShouldNotRemain = blocksThatShouldNotRemain;
    }

    public Multimap<Record, BlockWithData> getBlocksThatShouldRemain() {
        return blocksThatShouldRemain;
    }

    public Multimap<Record, BlockWithData> getBlocksThatShouldNotRemain() {
        return blocksThatShouldNotRemain;
    }
}
