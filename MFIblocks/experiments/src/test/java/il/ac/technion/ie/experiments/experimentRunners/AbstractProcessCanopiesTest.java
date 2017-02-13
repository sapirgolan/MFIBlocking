package il.ac.technion.ie.experiments.experimentRunners;

import il.ac.technion.ie.experiments.service.ConvexBPService;
import il.ac.technion.ie.experiments.util.ZipExtractor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;

/**
 * Created by I062070 on 07/01/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ProcessCanopies.class)
public class AbstractProcessCanopiesTest {
    protected final int NUMBER_OF_CANOPIES_IN_TEST = 35;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected File canopiesRootFolder;
    protected File datasetsRootFolder;
    protected File blocksRootFolder;

    @Before
    public void setUp() throws Exception {
        canopiesRootFolder = temporaryFolder.newFolder("root_canopies");
        datasetsRootFolder = temporaryFolder.newFolder("root_datasetsPermutation");
        blocksRootFolder = temporaryFolder.newFolder("root_blocks");
        ZipExtractor.extractZipFromResources(canopiesRootFolder, "/01_NumberOfOriginalRecords_canopies.zip");
        ZipExtractor.extractZipFromResources(datasetsRootFolder, "/01_NumberOfOriginalRecords_datasets.zip");
        ZipExtractor.extractZipFromResources(blocksRootFolder, "/01_NumberOfOriginalRecords_blocks.zip");
    }
}
