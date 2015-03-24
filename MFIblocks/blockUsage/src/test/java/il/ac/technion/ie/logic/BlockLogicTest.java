package il.ac.technion.ie.logic;

import com.google.common.collect.Sets;
import il.ac.technion.ie.model.RecordMatches;
import il.ac.technion.ie.model.NeighborsVector;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertThat;

public class BlockLogicTest {

    private BlockLogic classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new BlockLogic();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testBuildNeighborVectors() throws Exception {
        Set<Integer> recordsIds = Sets.newHashSet(1, 2, 3, 4, 5);
        final RecordMatches recordMatches = PowerMockito.mock(RecordMatches.class);

        ConcurrentHashMap matches = PowerMockito.mock(ConcurrentHashMap.class);
        PowerMockito.when(matches.get(Mockito.anyObject())).then(new Answer<RecordMatches>() {
            @Override
            public RecordMatches answer(InvocationOnMock invocation) throws Throwable {
                int id = (int) invocation.getArguments()[0];
                switch (id) {
                    case 1:
                        PowerMockito.when(recordMatches.getMatchedIds()).thenReturn(Sets.newHashSet(2,3,5));
                        break;
                    case 2:
                        PowerMockito.when(recordMatches.getMatchedIds()).thenReturn(Sets.newHashSet(1,3,4));
                        break;
                    case 3:
                        PowerMockito.when(recordMatches.getMatchedIds()).thenReturn(Sets.newHashSet(1,2,4));
                        break;
                    case 4:
                        PowerMockito.when(recordMatches.getMatchedIds()).thenReturn(Sets.newHashSet(2,3));
                        break;
                    case 5:
                        PowerMockito.when(recordMatches.getMatchedIds()).thenReturn(Sets.newHashSet(1));
                        break;
                }
                return recordMatches;
            }
        });
        List<NeighborsVector> list = Whitebox.invokeMethod(classUnderTest, matches, recordsIds);

        assertThat(list.get(0).getNeighbors(), Matchers.containsInAnyOrder(1,2,3,5));
        assertThat(list.get(1).getNeighbors(), Matchers.containsInAnyOrder(1,2,3,4));
        assertThat(list.get(2).getNeighbors(), Matchers.containsInAnyOrder(1,2,3,4));
        assertThat(list.get(3).getNeighbors(), Matchers.containsInAnyOrder(2,3,4));
        assertThat(list.get(4).getNeighbors(), Matchers.containsInAnyOrder(1,5));
    }
}