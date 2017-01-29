package il.ac.technion.ie.experiments.utils;

import com.google.common.collect.Lists;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by I062070 on 08/01/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ExperimentUtils.class, BlockWithData.class})
public class ExperimentUtilsTest {
    public static final int INDEX_OF_BLOCK_WITH_DUPLICATE_AS_REPRESENTATIVE = 1;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        PowerMockito.spy(ExperimentUtils.class);
    }

    @Test
    public void printBlocks() throws Exception {
        Logger logger = Logger.getLogger(ExperimentUtils.class);
        logger.setLevel(Level.ERROR);

        //execute
        ExperimentUtils.printBlocks(new ArrayList<BlockWithData>(), "fakeTitle");

        //verify
        PowerMockito.verifyStatic(Mockito.never());
        ExperimentUtils.buildTitle(Mockito.anyString());
    }

    @Test
    public void getRepresentativesSorted() throws Exception {
        Record record_1 = mock(Record.class);
        Record record_2 = mock(Record.class);
        Record record_3 = mock(Record.class);
        when(record_1.getRecordID()).thenReturn(101);
        when(record_2.getRecordID()).thenReturn(22);
        when(record_3.getRecordID()).thenReturn(13);

        Whitebox.setInternalState(ExperimentUtils.class, "representatives", Lists.newArrayList(record_1, record_2, record_3));
        List<Record> representatives = ExperimentUtils.getRepresentativesSorted();

        assertThat(representatives, hasSize(3));
        assertThat(representatives, contains(record_3, record_2, record_1));
    }

    //    @Test
    public void getRepresentativesSorted_beforeListWasInitilized() throws Exception {

        List<Record> representatives = ExperimentUtils.getRepresentativesSorted();

        assertThat(representatives, hasSize(0));
    }

    @Test
    public void sortBlocks() throws Exception {
        BlockWithData block_1 = createBlockAndSetRepresentativeID(88);
        BlockWithData block_2 = createBlockAndSetRepresentativeID(220);
        BlockWithData block_3 = createBlockAndSetRepresentativeID(17);

        List<BlockWithData> blocks = Lists.newArrayList(block_1, block_2, block_3);
        ExperimentUtils.sortBlocksByTrueRep(blocks);

        assertThat(blocks, contains(block_3, block_1, block_2));
    }

    @Test
    public void sortBlocks_oneOfBlocksDontHaveTrueRepresentative() throws Exception {
        BlockWithData block_1 = createBlockAndSetRepresentativeID(88);
        BlockWithData block_2 = mock(BlockWithData.class);;
        BlockWithData block_3 = createBlockAndSetRepresentativeID(17);

        List<BlockWithData> blocks = Lists.newArrayList(block_1, block_2, block_3);
        ExperimentUtils.sortBlocksByTrueRep(blocks);

        assertThat(blocks, contains(block_3, block_1, block_2));
    }

    @Test
    public void getBlockTextRepresentation() throws Exception {
        String expectedText = "0.17070405 rec-10-dup-0" + System.lineSeparator() +
                "0.1700955 rec-10-dup-4" + System.lineSeparator() +
                "0.1674011 rec-10-org" + System.lineSeparator() +
                "0.16457209 rec-10-dup-2" + System.lineSeparator() +
                "0.16453668 rec-10-dup-3" + System.lineSeparator() +
                "0.1626906 rec-10-dup-1" + System.lineSeparator();

        List<BlockWithData> realBlocks = ExperimentsUtils.getRealBlocks(temporaryFolder);
        BlockWithData block = realBlocks.get(INDEX_OF_BLOCK_WITH_DUPLICATE_AS_REPRESENTATIVE);

        String blockTextRepresentation = ExperimentUtils.getBlockTextRepresentation(block);

        assertThat(blockTextRepresentation, equalTo(expectedText));
    }

    @Test
    public void printBlocks_E2E() throws Exception {
        Logger logger = Logger.getLogger(ExperimentUtils.class);
        logger.setLevel(Level.ALL);
        String expectedPrint = "Blocks of experiment JUnit" + System.lineSeparator() +
                "block rec-10-org, #12345" + System.lineSeparator() +
                "====================================================" + System.lineSeparator() +
                "0.17070405 rec-10-dup-0" + System.lineSeparator() +
                "0.1700955 rec-10-dup-4" + System.lineSeparator() +
                "0.1674011 rec-10-org" + System.lineSeparator() +
                "0.16457209 rec-10-dup-2" + System.lineSeparator() +
                "0.16453668 rec-10-dup-3" + System.lineSeparator() +
                "0.1626906 rec-10-dup-1" + System.lineSeparator() +
                "block rec-15-org, #12345" + System.lineSeparator() +
                "====================================================" + System.lineSeparator() +
                "0.34593725 rec-15-org" + System.lineSeparator() +
                "0.32729614 rec-15-dup-1" + System.lineSeparator() +
                "0.32676664 rec-15-dup-0" + System.lineSeparator();

        List<BlockWithData> blocks = ExperimentsUtils.getRealBlocks(temporaryFolder).subList(0, 2);
        for (int i = 0; i < blocks.size(); i++) {
            BlockWithData spy = PowerMockito.spy(blocks.get(i));
            doReturn(12345).when(spy).getId();
            blocks.set(i, spy);
        }

        String printedBlocks = ExperimentUtils.printBlocks(blocks, "Blocks of experiment JUnit");

        assertThat(printedBlocks, equalToIgnoringWhiteSpace(expectedPrint));
    }

    @Test
    public void printAllBlocks_ForDebuggingOnly() throws Exception {
        Logger logger = Logger.getLogger(ExperimentUtils.class);
        logger.setLevel(Level.ALL);
        String expectedPrint = "Blocks of experiment JUnit" + System.lineSeparator() +
                "block rec-10-org, #12345" + System.lineSeparator() +
                "====================================================" + System.lineSeparator() +
                "0.17070405 rec-10-dup-0" + System.lineSeparator() +
                "0.1700955 rec-10-dup-4" + System.lineSeparator() +
                "0.1674011 rec-10-org" + System.lineSeparator() +
                "0.16457209 rec-10-dup-2" + System.lineSeparator() +
                "0.16453668 rec-10-dup-3" + System.lineSeparator() +
                "0.1626906 rec-10-dup-1" + System.lineSeparator() +
                "block rec-15-org, #12345" + System.lineSeparator() +
                "====================================================" + System.lineSeparator() +
                "0.34593725 rec-15-org" + System.lineSeparator() +
                "0.32729614 rec-15-dup-1" + System.lineSeparator() +
                "0.32676664 rec-15-dup-0" + System.lineSeparator();

        List<BlockWithData> blocks = ExperimentsUtils.getRealBlocks(temporaryFolder);
        for (int i = 0; i < blocks.size(); i++) {
            BlockWithData spy = PowerMockito.spy(blocks.get(i));
            doReturn(12345).when(spy).getId();
            blocks.set(i, spy);
        }

        ExperimentUtils.printBlocks(blocks, "Blocks of experiment JUnit");
    }

    private BlockWithData createBlockAndSetRepresentativeID(Integer blockNumder) {
        BlockWithData block = mock(BlockWithData.class);
        Record block1TrueRep = mock(Record.class);
        when(block1TrueRep.getRecordName()).thenReturn("rec-" + blockNumder + "-org");
        when(block.getTrueRepresentative()).thenReturn(block1TrueRep);
        return block;
    }

}