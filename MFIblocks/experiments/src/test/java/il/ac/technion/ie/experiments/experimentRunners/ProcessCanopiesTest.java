package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.ConvexBPService;
import il.ac.technion.ie.experiments.threads.CommandExacter;
import il.ac.technion.ie.model.Record;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created by I062070 on 02/01/2017.
 */
public class ProcessCanopiesTest extends AbstractProcessCanopiesTest {

    protected ProcessCanopies classUnderTest;
    protected ConvexBPService convexBPService = PowerMockito.spy(new ConvexBPService());

    @Before
    public void setUp_unitTest() throws Exception {
        classUnderTest = PowerMockito.spy(new ProcessCanopies());
        Whitebox.setInternalState(classUnderTest, "convexBPService", convexBPService);
    }

    @Test
    public void readAndParseCanopiesFromDir_hasAllCanopyFiles() throws Exception {
        Whitebox.invokeMethod(classUnderTest, "readAndInitCanopiesFromDir", canopiesRootFolder.getAbsolutePath());
        BiMap<File, Collection<CanopyCluster>> fileToCanopies = Whitebox.getInternalState(classUnderTest, "fileToCanopies");

        assertThat(fileToCanopies.entrySet(), hasSize(NUMBER_OF_CANOPIES_IN_TEST));
        assertThat(fileToCanopies.inverse().entrySet(), hasSize(NUMBER_OF_CANOPIES_IN_TEST));
    }

    @Test
    public void runExperiments_verifyExperimentIsExecutedForEachCanopy() throws Exception {
        PowerMockito.doReturn(null).when(classUnderTest, "performExperimentComparison", Mockito.any(File.class));
        doNothing().when(classUnderTest, "saveResultsToFS", Mockito.any(Multimap.class));

        classUnderTest.runExperiments(canopiesRootFolder.getAbsolutePath(), datasetsRootFolder.getAbsolutePath());
        verifyPrivate(classUnderTest, Mockito.times(NUMBER_OF_CANOPIES_IN_TEST)).invoke("performExperimentComparison", Mockito.any(File.class));
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
        String permutation = "FebrlParam_40";
        File datasetFile = DatasetMapper.getDatasetFile(permutation, allDatasetPermutations);
        Whitebox.invokeMethod(classUnderTest, "initMembersThatDependsOnOriginalDataset", datasetFile, permutation);

        BiMap<Record, BlockWithData> trueRepsMap = Whitebox.getInternalState(classUnderTest, "trueRepsMap");
        assertThat(trueRepsMap, notNullValue());
        assertThat(trueRepsMap.size(), is(24));
        assertThat(Whitebox.getInternalState(classUnderTest, "measurements"), notNullValue());
    }

    @Test
    public void runExperiments_measurmentsNotCalculatedIfConvexBpFails() throws Exception {
        doReturn(false).when(classUnderTest, "executeConvexBP", Mockito.anyListOf(BlockWithData.class));
        doNothing().when(classUnderTest, "saveResultsToFS", Mockito.any(Multimap.class));

        classUnderTest.runExperiments(canopiesRootFolder.getAbsolutePath(), datasetsRootFolder.getAbsolutePath());

        verifyPrivate(classUnderTest, Mockito.never())
                .invoke("calculateMeasurements", Mockito.anyListOf(BlockWithData.class), Mockito.any(Multimap.class));
    }
}