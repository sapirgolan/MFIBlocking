package il.ac.technion.ie.potential.model;

/**
 * Created by I062070 on 12/09/2015.
 */
public class MatrixContext<M extends AbstractPotentialMatrix> {
    private final BlockPair pair;
    private final M matrix;

    public MatrixContext(BlockPair pair, M matrix) {
        this.pair = pair;
        this.matrix = matrix;
    }

    public BlockPair getPair() {
        return pair;
    }

    public M getMatrix() {
        return matrix;
    }
}
