package il.ac.technion.ie.potential.model;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by XPS_Sapir on 14/07/2015.
 */
public class BlockPair<L,R> extends Pair<L, R> {
    private final L left;
    private final R right;

    public BlockPair(L left, R right) {
        Pair<L, R> of = Pair.of(left, right);
        this.left = left;
        this.right = right;
    }

    @Override
    public L getLeft() {
        return this.left;
    }

    @Override
    public R getRight() {
        return this.right;
    }

    @Override
    public R setValue(R value) {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof BlockPair)) {
            return false;
        }
        Pair<L, R> other = (Pair<L, R>) obj;
        if (right.equals(other.getRight()) && left.equals(other.getLeft()) ) {
            return true;
        } else if (right.equals(other.getLeft()) && left.equals(other.getRight()) ) {
            return true;
        }
        return false;
    }
}
