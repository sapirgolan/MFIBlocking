package il.ac.technion.ie.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: chbatey
 * Date: 31/05/2013
 * Time: 21:44
 * To change this template use File | Settings | File Templates.
 */
public class MockIterator {
    public static <T> void mockIterable(Iterable<T> iterable, T... values) {
        Iterator<T> mockIterator = mock(Iterator.class);
        when(iterable.iterator()).thenReturn(mockIterator);

        if (values.length == 0) {
            when(mockIterator.hasNext()).thenReturn(false);
            return;
        } else if (values.length == 1) {
            when(mockIterator.hasNext()).thenReturn(true, false);
            when(mockIterator.next()).thenReturn(values[0]);
        } else {
            // build boolean array for hasNext()
            Boolean[] hasNextResponses = new Boolean[values.length];
            for (int i = 0; i < hasNextResponses.length - 1; i++) {
                hasNextResponses[i] = true;
            }
            hasNextResponses[hasNextResponses.length - 1] = false;
            when(mockIterator.hasNext()).thenReturn(true, hasNextResponses);
            T[] valuesMinusTheFirst = Arrays.copyOfRange(values, 1, values.length);
            when(mockIterator.next()).thenReturn(values[0], valuesMinusTheFirst);
        }
    }

    public static <T> void mockIterable(Iterable<T> iterable, List<T> values) {

        T[] valuesArr = (T[]) new Object[values.size()];
        valuesArr = values.toArray(valuesArr);
        MockIterator.mockIterable(iterable, valuesArr);

        /*Iterator<T> mockIterator = mock(Iterator.class);
        when(iterable.iterator()).thenReturn(mockIterator);

        if (values.size() == 0) {
            when(mockIterator.hasNext()).thenReturn(false);
            return;
        } else if (values.size() == 1) {
            when(mockIterator.hasNext()).thenReturn(true, false);
            when(mockIterator.next()).thenReturn(values.get(0));
        } else {
            // build boolean array for hasNext()
            Boolean[] hasNextResponses = new Boolean[values.size()];
            for (int i = 0; i < hasNextResponses.length -1 ; i++) {
                hasNextResponses[i] = true;
            }
            hasNextResponses[hasNextResponses.length - 1] = false;
            when(mockIterator.hasNext()).thenReturn(true, hasNextResponses);
            List<T> valuesMinusTheFirst = values.subList(1, values.size());
            OngoingStubbing<T> tOngoingStubbing = when(mockIterator.next()).thenReturn(values.get(0));
            for (int i = 1; i < valuesMinusTheFirst.size(); i++) {
                tOngoingStubbing.thenReturn(valuesMinusTheFirst.get(i));
            }
        }*/
    }
}
