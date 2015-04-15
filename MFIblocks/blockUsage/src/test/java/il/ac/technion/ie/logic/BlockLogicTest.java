package il.ac.technion.ie.logic;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.NeighborsVector;
import il.ac.technion.ie.model.RecordMatches;
import org.hamcrest.Matchers;
import org.hamcrest.core.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.util.List;
import java.util.Map;
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

    @Test
    public void testObtainTerms() throws Exception {
        List list = PowerMockito.mock(List.class);
        MfiContext context = PowerMockito.mock(MfiContext.class);
        PowerMockito.when(context.getOriginalRecordsPath()).thenReturn("NoSw.txt");
        Multimap<Integer, String> obtainTerms = Whitebox.invokeMethod(classUnderTest, "obtainTerms", list, context);
        assertThat(obtainTerms, IsNull.notNullValue());
    }

    @Test
    public void testRetriveRecords() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        String recordsFilePath = classLoader.getResource("NoSw.txt").getFile();
        Map<Integer, String> records = Whitebox.invokeMethod(classUnderTest, "retriveRecords", recordsFilePath);
        assertThat(records.size(), Is.is(26));
//        private AbstractMap<Integer, String> retriveRecords(String recordsPath) {
    }
}
