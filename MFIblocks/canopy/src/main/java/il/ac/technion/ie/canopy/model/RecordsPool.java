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
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * the specified initial capacity and default load factor (0.75).
     *
     * @param initialCapacity the initial capacity of the hash table
     * @throws IllegalArgumentException if the initial capacity is less
     *                                            than zero
     */
    public RecordsPool(int initialCapacity) {
        super(initialCapacity);
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
