package il.ac.technion.ie.experiments.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FuzzyService.class)
public class FuzzyServiceTest {

    @InjectMocks
    private FuzzyService classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = PowerMockito.spy(new FuzzyService());
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSplitBlocks_split3Blocks() throws Exception {
        //mocking
        List<BlockWithData> originalBlocks = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            BlockWithData blockWithData = mock(BlockWithData.class);
            when(blockWithData.getId()).thenReturn(i);
            when(blockWithData.size()).thenReturn(3);
            originalBlocks.add(blockWithData);
        }

        Map<Integer, Double> mapSplitProb = Maps.newHashMap(new ImmutableMap.Builder<Integer, Double>()
                .put(0, 0.6).put(1, 0.2).put(2, 0.4).put(3, 0.3).put(4, 0.15).put(5, 0.9)
                .put(6, 0.9)
                .put(7, 0.9)
                .put(8, 0.9)
                .build());

        PowerMockito.doNothing().when(classUnderTest, Mockito.any(BlockWithData.class), Mockito.anyList(), Mockito.anyList());

        //execute
        List<BlockWithData> newBlocks = classUnderTest.splitBlocks(originalBlocks, mapSplitProb, 0.323);

        //assert
        assertThat(newBlocks, Matchers.hasSize(12));
    }

    @Test
    public void testSplitBlocks_sizeTwo() throws Exception {
        //mocking
        BlockWithData blockWithData = mock(BlockWithData.class);
        when(blockWithData.getId()).thenReturn(7);
        List<BlockWithData> blockWithDatas = Lists.newArrayList(blockWithData);

        Map<Integer, Double> splitProbs = Maps.newHashMap(ImmutableMap.of(7, 0.1));

        //execute
        List<BlockWithData> newBlocks = classUnderTest.splitBlocks(blockWithDatas, splitProbs, 0.323);

        //assert
        assertThat(newBlocks, Matchers.hasSize(1));

    }

    @Test
    public void testCollectRecordsForSplitedBlocks() throws Exception {
        final Record trueRepresentative = mock(Record.class);

        BlockWithData origBlock = mock(BlockWithData.class);
        when(origBlock.isRepresentative(Mockito.eq(trueRepresentative))).thenReturn(true);
        when(origBlock.getTrueRepresentative()).thenReturn(trueRepresentative);
        when(origBlock.size()).thenReturn(8);
        when(origBlock.getMembers()).thenAnswer(new Answer<List<Record>>() {
            @Override
            public List<Record> answer(InvocationOnMock invocation) throws Throwable {
                List<Record> records = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    records.add(mock(Record.class));
                }
                records.add(5, trueRepresentative);
                return records;
            }
        });

        List<Record> blockOneRecords = new ArrayList<>();
        List<Record> blockTwoRecords = new ArrayList<>();
        Whitebox.invokeMethod(classUnderTest, "collectRecordsForSplitedBlocks", origBlock, blockOneRecords, blockTwoRecords);
        assertThat(blockOneRecords, Matchers.hasItem(trueRepresentative));
        assertThat(blockTwoRecords, Matchers.hasItem(trueRepresentative));
        blockOneRecords.retainAll(blockTwoRecords);
        assertThat(blockOneRecords, Matchers.hasSize(1));
        assertThat(blockOneRecords, Matchers.contains(trueRepresentative));
    }

    @Test
    public void testGetSplitProbability_keyExists() throws Exception {
        //mock
        BlockWithData blockWithData = mock(BlockWithData.class);
        when(blockWithData.getId()).thenReturn(17);
        Map<Integer, Double> myMap = Maps.newHashMap(ImmutableMap.of(17, 0.76));

        //execute
        Double splitProb = Whitebox.invokeMethod(classUnderTest, "getSplitProbability", myMap, blockWithData);
        assertThat(splitProb, closeTo(0.76, 0.00001));

    }

    @Test
    public void testGetSplitProbability_keyNotExists() throws Exception {
        //mock
        BlockWithData blockWithData = mock(BlockWithData.class);
        when(blockWithData.getId()).thenReturn(17);
        Map<Integer, Double> myMap = Maps.newHashMap(ImmutableMap.of(15, 0.76));

        //execute
        Double splitProb = Whitebox.invokeMethod(classUnderTest, "getSplitProbability", myMap, blockWithData);
        assertThat(splitProb, closeTo(1.0, 0.00001));
    }
}