package il.ac.technion.ie.utils;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class MockIteratorTest {

    @Test
    public void testManyItemsInIterator_oneByOne() {
        //given
        Set<String> mockIterable = mock(Set.class);
        List<String> expectedResultsFromIterator = Arrays.asList("one", "two", "three");
        //when
        MockIterator.mockIterable(mockIterable, "one", "two", "three");
        //then
        List<String> results = new ArrayList<>();
        for (String s : mockIterable) {
            results.add(s);
        }
        assertEquals(expectedResultsFromIterator, results);
    }

    @Test
    public void testManyItemsInIterator_list() {
        //given
        Set<String> mockIterable = mock(Set.class);
        List<String> expectedResultsFromIterator = Arrays.asList("one", "two", "three");
        //when
        MockIterator.mockIterable(mockIterable, expectedResultsFromIterator);
        //then
        List<String> results = new ArrayList<>();
        for (String s : mockIterable) {
            results.add(s);
        }
        assertEquals(expectedResultsFromIterator, results);
    }

    @Test
    public void testSingleItemInIterator_oneByOne() {
        //given
        Set<String> mockIterable = mock(Set.class);
        List<String> expectedResultsFromIterator = Arrays.asList("one");
        //when
        MockIterator.mockIterable(mockIterable, "one");
        //then
        List<String> results = new ArrayList<>();
        for (String s : mockIterable) {
            results.add(s);
        }
        assertEquals(expectedResultsFromIterator, results);
    }

    @Test
    public void testSingleItemInIterator_list() {
        //given
        Set<String> mockIterable = mock(Set.class);
        List<String> expectedResultsFromIterator = Arrays.asList("one");
        //when
        MockIterator.mockIterable(mockIterable, expectedResultsFromIterator);
        //then
        List<String> results = new ArrayList<>();
        for (String s : mockIterable) {
            results.add(s);
        }
        assertEquals(expectedResultsFromIterator, results);
    }

    @Test
    public void testEmtpyIteratorResults() {
        //given
        Set<String> mockIterable = mock(Set.class);
        //when
        MockIterator.mockIterable(mockIterable);
        //then
        List<String> results = new ArrayList<>();
        for (String s : mockIterable) {
            results.add(s);
        }
        assertEquals(Collections.emptyList(), results);
    }

}
