package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.ConvexBPService;
import il.ac.technion.ie.experiments.threads.CommandExacter;
import il.ac.technion.ie.experiments.util.ZipExtractor;
import il.ac.technion.ie.model.Record;
import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

/**
 * Created by I062070 on 02/01/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ProcessCanopies.class})
public class ProcessCanopiesTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ProcessCanopies classUnderTest;

    private ConvexBPService convexBPService = PowerMockito.spy(new ConvexBPService());

    private File canopiesRootFolder;
    private File datasetsRootFolder;
    private final int numberOfCanopiesInTest = 35;

    @Before
    public void setUp() throws Exception {
        classUnderTest = PowerMockito.spy(new ProcessCanopies());
        Whitebox.setInternalState(classUnderTest, "convexBPService", convexBPService);

        canopiesRootFolder = temporaryFolder.newFolder("root_canopies");
        datasetsRootFolder = temporaryFolder.newFolder("root_datasetsPermutation");
        ZipExtractor.extractZipFromResources(canopiesRootFolder, "/01_NumberOfOriginalRecords_canopies.zip");
        ZipExtractor.extractZipFromResources(datasetsRootFolder, "/01_NumberOfOriginalRecords_datasets.zip");
    }

    @Test
    public void readAndParseCanopiesFromDir_hasAllCanopyFiles() throws Exception {
        Whitebox.invokeMethod(classUnderTest, "readAndInitCanopiesFromDir", canopiesRootFolder.getAbsolutePath());
        BiMap<File, Collection<CanopyCluster>> fileToCanopies = Whitebox.getInternalState(classUnderTest, "fileToCanopies");

        assertThat(fileToCanopies.entrySet(), hasSize(numberOfCanopiesInTest));
        assertThat(fileToCanopies.inverse().entrySet(), hasSize(numberOfCanopiesInTest));
    }

    @Test
    public void runExperiments_verifyExperimentIsExecutedForEachCanopy() throws Exception {
        PowerMockito.doNothing().when(classUnderTest, "performExperimentComparison", Mockito.any(File.class));
        classUnderTest.runExperiments(canopiesRootFolder.getAbsolutePath(), datasetsRootFolder.getAbsolutePath());
        verifyPrivate(classUnderTest, Mockito.times(numberOfCanopiesInTest)).invoke("performExperimentComparison", Mockito.any(File.class));
    }

    @Test
    public void executeConvexBP_convexBpRunFail() throws Exception {
        doReturn(false)
                .when(convexBPService)
                .runConvexBP(Mockito.any(CommandExacter.class), Mockito.anyDouble(), Mockito.anyListOf(BlockWithData.class));

        boolean convexBPExecuted = Whitebox.invokeMethod(classUnderTest, "executeConvexBP", new ArrayList<BlockWithData>());
        assertThat(convexBPExecuted, is(false));
    }

    @Test
    public void executeConvexBP_convexBpRunSucced() throws Exception {
        doReturn(true)
                .when(convexBPService)
                .runConvexBP(Mockito.any(CommandExacter.class), Mockito.anyDouble(), Mockito.anyListOf(BlockWithData.class));

        boolean convexBPExecuted = Whitebox.invokeMethod(classUnderTest, "executeConvexBP", new ArrayList<BlockWithData>());
        assertThat(convexBPExecuted, is(true));
    }

    @Test
    public void initMembersThatDependsOnOriginalDataset() throws Exception {
        Collection<File> allDatasetPermutations = FileUtils.listFiles(datasetsRootFolder, null, false);

        Whitebox.invokeMethod(classUnderTest, "initMembersThatDependsOnOriginalDataset", "FebrlParam_40", allDatasetPermutations);

        BiMap<Record, BlockWithData> trueRepsMap = Whitebox.getInternalState(classUnderTest, "trueRepsMap");
        assertThat(trueRepsMap, notNullValue());
        assertThat(trueRepsMap.size(), is(24));
        assertThat(Whitebox.getInternalState(classUnderTest, "measurements"), notNullValue());
    }

    @Test
    public void runExperiments_measurmentsNotCalculatedIfConvexBpFails() throws Exception {
        doReturn(false).when(classUnderTest, "executeConvexBP", Mockito.anyListOf(BlockWithData.class));

        classUnderTest.runExperiments(canopiesRootFolder.getAbsolutePath(), datasetsRootFolder.getAbsolutePath());

        verifyPrivate(classUnderTest, Mockito.never())
                .invoke("calculateMeasurements", Mockito.anyListOf(BlockWithData.class), Mockito.any(Multimap.class));
    }

    @Test
    public void runExperiments_convexBpRuns() throws Exception {
        reduceDatasetSizeTo(datasetsRootFolder, 2);
        reduceDatasetSizeTo(canopiesRootFolder, 2);

        classUnderTest.runExperiments(canopiesRootFolder.getAbsolutePath(), datasetsRootFolder.getAbsolutePath());

        verifyPrivate(classUnderTest, Mockito.times(10))
                .invoke("calculateMeasurements", Mockito.anyListOf(BlockWithData.class), Mockito.any(Multimap.class));
    }

    private void reduceDatasetSizeTo(File datasetRootFolder, int reduceToSize) throws IOException {
        File[] files = datasetRootFolder.listFiles();
        for (int i = files.length -1; i > reduceToSize - 1; i--) {
            File deteleTarget = files[i];
            if (deteleTarget.isDirectory()) {
                FileUtils.deleteDirectory(deteleTarget);
            } else {
                deteleTarget.delete();
            }
        }
    }
}