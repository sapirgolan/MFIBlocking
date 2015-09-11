package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest(FuzzyService.class)
public class FuzzyServiceTest {

    @InjectMocks
    private FuzzyService classUnderTest;

    @Mock
    private UniformRealDistribution splitBlockProbThresh;

    @Before
    public void setUp() throws Exception {
        classUnderTest = PowerMockito.spy(new FuzzyService());
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSplitBlocks_split3Blocks() throws Exception {

        List<BlockWithData> originalBlocks = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            originalBlocks.add(PowerMockito.mock(BlockWithData.class));
        }
        PowerMockito.when(splitBlockProbThresh.sample()).thenReturn(0.6, 0.2, 0.4, 0.3, 0.15, 0.9);
        PowerMockito.doNothing().when(classUnderTest, Mockito.any(BlockWithData.class), Mockito.anyList(), Mockito.anyList());
        List<BlockWithData> newBlocks = classUnderTest.splitBlocks(originalBlocks, 0.323);

        MatcherAssert.assertThat(newBlocks, Matchers.hasSize(12));
    }

    @Test
    public void testCollectRecordsForSplitedBlocks() throws Exception {
        final Record trueRepresentative = PowerMockito.mock(Record.class);

        BlockWithData origBlock = PowerMockito.mock(BlockWithData.class);
        PowerMockito.when(origBlock.isRepresentative(Mockito.eq(trueRepresentative))).thenReturn(true);
        PowerMockito.when(origBlock.getTrueRepresentative()).thenReturn(trueRepresentative);
        PowerMockito.when(origBlock.size()).thenReturn(8);
        PowerMockito.when(origBlock.getMembers()).thenAnswer(new Answer<List<Record>>() {
            @Override
            public List<Record> answer(InvocationOnMock invocation) throws Throwable {
                List<Record> records = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    records.add(PowerMockito.mock(Record.class));
                }
                records.add(5, trueRepresentative);
                return records;
            }
        });

        List<Record> blockOneRecords = new ArrayList<>();
        List<Record> blockTwoRecords = new ArrayList<>();
        Whitebox.invokeMethod(classUnderTest, "collectRecordsForSplitedBlocks", origBlock, blockOneRecords, blockTwoRecords);
        MatcherAssert.assertThat(blockOneRecords, Matchers.hasItem(trueRepresentative));
        MatcherAssert.assertThat(blockTwoRecords, Matchers.hasItem(trueRepresentative));
        blockOneRecords.retainAll(blockTwoRecords);
        MatcherAssert.assertThat(blockOneRecords, Matchers.hasSize(1));
        MatcherAssert.assertThat(blockOneRecords, Matchers.contains(trueRepresentative));
    }


}