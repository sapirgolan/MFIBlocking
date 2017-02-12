package il.ac.technion.ie.experiments.model;

import il.ac.technion.ie.experiments.util.ZipExtractor;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Created by I062070 on 11/02/2017.
 */
public class BlocksMapperTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private BlocksMapper classUnderTest;
    private File blocksRootFolder;
    private final String BASE_NAME = "40_75_5_5_16_uniform_all_0_parameter=40";
    private final String ZIP_FILE_NAME = BASE_NAME + ".zip";
    private final String DATASET_FILE_NAME = BASE_NAME + ".csv";
    private List<File> files;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new BlocksMapper();
        blocksRootFolder = temporaryFolder.newFolder("root_blocks");
        ZipExtractor.extractZipFromResources(blocksRootFolder, "/" + ZIP_FILE_NAME);

        files = new ArrayList<>(FileUtils.listFiles(blocksRootFolder, null, true));
    }

    @Test
    public void getBlockIndex() throws Exception {
        int index = Whitebox.invokeMethod(classUnderTest, "getBlockIndex", "baseline_2");
        assertThat(index, is(2));
    }

    @Test
    public void getNext() throws Exception {
        File baselineBlockFile_0 = files.get(0);
        File bcbpBlockFile_0 = files.get(5);
        classUnderTest.add(baselineBlockFile_0);
        classUnderTest.add(bcbpBlockFile_0);

        BlockPair next = classUnderTest.getNext(DATASET_FILE_NAME);
        assertThat(next.getBaseline(), is(baselineBlockFile_0));
        assertThat(next.getBcbp(), is(bcbpBlockFile_0));
    }

    @Test
    public void getNextTwice() throws Exception {
        for (int i = 0; i < 2; i++) {
            classUnderTest.add(files.get(i));
            classUnderTest.add(files.get(i + 5));
        }

        BlockPair next = classUnderTest.getNext(DATASET_FILE_NAME);
        assertThat(next.getBaseline(), is(files.get(0)));
        assertThat(next.getBcbp(), is(files.get(5)));

        next = classUnderTest.getNext(DATASET_FILE_NAME);
        assertThat(next.getBaseline(), is(files.get(1)));
        assertThat(next.getBcbp(), is(files.get(6)));
    }

    @Test
    public void getNextTwice_itemsWithIndex_0_and_2() throws Exception {
        for (int i = 0; i <= 2; i += 2) {
            classUnderTest.add(files.get(i));
            classUnderTest.add(files.get(i + 5));
        }

        BlockPair next = classUnderTest.getNext(DATASET_FILE_NAME);
        assertThat(next.getBaseline(), is(files.get(0)));
        assertThat(next.getBcbp(), is(files.get(5)));

        next = classUnderTest.getNext(DATASET_FILE_NAME);
        assertThat(next.getBaseline(), is(files.get(2)));
        assertThat(next.getBcbp(), is(files.get(7)));
    }

    @Test
    public void getNext_hasNoPairs() throws Exception {
        for (int i = 0; i < 5; i ++) {
            classUnderTest.add(files.get(i));
        }

        BlockPair next = classUnderTest.getNext(DATASET_FILE_NAME);
        assertThat(next, is(nullValue()));
    }
}

