package il.ac.technion.ie.canopy.model;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by I062070 on 27/01/2016.
 */
public class RecordsPool<T> extends HashSet<T> {

    public RecordsPool(Collection<? extends T> c) {
        super(c);
    }

    /**
     * This method is thread safe <br>
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        synchronized (this) {
            return super.isEmpty();
        }
    }
}
