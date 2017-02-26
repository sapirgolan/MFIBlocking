package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.Multimap;
import il.ac.technion.ie.experiments.model.BlockPair;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.BlocksMapper;
import il.ac.technion.ie.experiments.parsers.SerializerUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

/**
 * Created by I062070 on 13/02/2017.
 */
public class ProcessBlocksTest extends AbstractProcessCanopiesTest {
    protected ProcessBlocks classUnderTest;
    private static final String DATASET_PERMUTATION_NAME = "25_75_5_5_16_uniform_all_0_parameter=25.csv";

    @Before
    public void setUp_E2E() throws Exception {
        classUnderTest = PowerMockito.spy(new ProcessBlocks());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void runExperiments_E2E() throws Exception {
        classUnderTest.runExperiments(blocksRootFolder.getAbsolutePath(), datasetsRootFolder.getAbsolutePath());

        verifyPrivate(classUnderTest, Mockito.times(NUMBER_OF_CANOPIES_IN_TEST))
                .invoke("calculateMeasurements", Mockito.anyListOf(BlockWithData.class), Mockito.any(Multimap.class));

        verifyPrivate(classUnderTest).invoke("saveResultsToFS", Mockito.any(Multimap.class));
    }

    @Test
    public void verifyBaselineAndBcbpBlockAreTheSameObjects_byID() throws Exception {
        BlocksMapper blocksMapper = new FilesReader(blocksRootFolder.getAbsolutePath()).getAllBlocks();
        BlockPair blockPair = blocksMapper.getNext(DATASET_PERMUTATION_NAME);
        List<BlockWithData> baselineBlocks = new ArrayList<>(SerializerUtil.<BlockWithData>deSerialize(blockPair.getBaseline()));
        List<BlockWithData> bcbpBlocks = new ArrayList<>(SerializerUtil.<BlockWithData>deSerialize(blockPair.getBcbp()));

        Set<Integer> ids = new HashSet<>();
        for (BlockWithData baselineBlock : baselineBlocks) {
            ids.add(baselineBlock.getId());
        }
        for (BlockWithData bcbpBlock : bcbpBlocks) {
            assertThat(ids, hasItem(bcbpBlock.getId()));
        }
    }

    @Test
    public void verifyBaselineAndBcbpBlockAreTheSameObjects_byContains() throws Exception {
        BlocksMapper blocksMapper = new FilesReader(blocksRootFolder.getAbsolutePath()).getAllBlocks();
        BlockPair blockPair = blocksMapper.getNext(DATASET_PERMUTATION_NAME);
        List<BlockWithData> baselineBlocks = new ArrayList<>(SerializerUtil.<BlockWithData>deSerialize(blockPair.getBaseline()));
        List<BlockWithData> bcbpBlocks = new ArrayList<>(SerializerUtil.<BlockWithData>deSerialize(blockPair.getBcbp()));

        for (BlockWithData baselineBlock : baselineBlocks) {
            assertThat(bcbpBlocks, hasItem(baselineBlock));
            assertTrue(bcbpBlocks.contains(baselineBlock));
        }
        assertTrue(bcbpBlocks.containsAll(baselineBlocks));
    }
}