package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.*;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created by I062070 on 24/02/2017.
 */
public class AbstractProcessorTest {

    private AbstractProcessor classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new AbstractProcessor();
    }

    @Test
    public void repsByFrequencyThatAppearsIn() throws Exception {
        Record groundTruthRep_1 = mock(Record.class);
        Record groundTruthRep_2 = mock(Record.class);
        Record groundTruthRep_3 = mock(Record.class);
        Set<Record> groundTruthReps = Sets.newHashSet(groundTruthRep_1, groundTruthRep_2, groundTruthRep_3);

        Multimap<Record, BlockWithData> baselineBlocks = ArrayListMultimap.create();
        baselineBlocks.putAll(groundTruthRep_1, Lists.newArrayList(mock(BlockWithData.class), mock(BlockWithData.class)));
        baselineBlocks.put(groundTruthRep_2, mock(BlockWithData.class));
        baselineBlocks.putAll(groundTruthRep_3, Lists.newArrayList(mock(BlockWithData.class), mock(BlockWithData.class)));
        baselineBlocks.put(mock(Record.class), mock(BlockWithData.class));


        ImmutableListMultimap<Integer, Record> trueRepsByFrequency = classUnderTest.repsByFrequencyThatAppearsIn(baselineBlocks, groundTruthReps);
        assertThat(trueRepsByFrequency.size(), greaterThan(3));
        assertThat(trueRepsByFrequency.get(1), contains(groundTruthRep_2));
        assertThat(trueRepsByFrequency.get(2), containsInAnyOrder(groundTruthRep_1, groundTruthRep_3));
    }

    @Test
    public void getRecordsRepresentMoreThan() throws Exception {
        Multimap<Integer, Record> multimap = ArrayListMultimap.create();
        multimap.put(-1, mock(Record.class));
        multimap.putAll(1, Lists.newArrayList(mock(Record.class), mock(Record.class), mock(Record.class)));
        multimap.putAll(2, Lists.newArrayList(mock(Record.class)));
        multimap.putAll(3, Lists.newArrayList(mock(Record.class), mock(Record.class)));

        Set<Record> recordsRepresentMoreThan_1 = classUnderTest.getRecordsRepresentMoreThan(multimap, 1);
        assertThat(recordsRepresentMoreThan_1, hasSize(3));
    }

}