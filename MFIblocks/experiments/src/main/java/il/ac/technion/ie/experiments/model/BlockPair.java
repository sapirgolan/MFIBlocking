package il.ac.technion.ie.experiments.model;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;

/**
 * Created by I062070 on 11/02/2017.
 */
public class BlockPair {
    private File baseline;
    private File bcbp;

    public BlockPair(File baseline, File bcbp) {
        this.baseline = baseline;
        this.bcbp = bcbp;
    }

    public File getBaseline() {
        return baseline;
    }


    public File getBcbp() {
        return bcbp;
    }
}
