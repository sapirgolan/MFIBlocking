package il.ac.technion.ie.potential.model;

/**
 * Created by I062070 on 12/09/2015.
 */
public class MatrixContext<M extends AbstractPotentialMatrix> {
    private final BlockPair<Integer, Integer> pair;
    private final M matrix;

    public MatrixContext(BlockPair<Integer, Integer> pair, M matrix) {
        this.pair = pair;
        this.matrix = matrix;
    }

    public BlockPair<Integer, Integer> getPair() {
        return pair;
    }

    public M getMatrix() {
        return matrix;
    }
}
