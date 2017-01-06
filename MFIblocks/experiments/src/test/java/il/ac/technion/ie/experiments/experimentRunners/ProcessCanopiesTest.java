package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.BiMap;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.util.ZipExtractor;
import org.apache.commons.math3.analysis.function.Pow;
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
import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Created by I062070 on 02/01/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ProcessCanopies.class)
public class ProcessCanopiesTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    ProcessCanopies classUnderTest;
    private File rootFolder;
    private final int numberOfCanopiesInTest = 35;

    @Before
    public void setUp() throws Exception {
        classUnderTest = PowerMockito.spy(new ProcessCanopies());
        rootFolder = temporaryFolder.newFolder("root");
        ZipExtractor.extractZipFromResources(rootFolder, "/01_NumberOfOriginalRecords_canopies.zip");
    }

    @Test
    public void readAndParseCanopiesFromDir_hasAllCanopyFiles() throws Exception {
        Whitebox.invokeMethod(classUnderTest, "readAndInitCanopiesFromDir", rootFolder.getAbsolutePath());
        BiMap<File, Collection<CanopyCluster>> fileToCanopies = Whitebox.getInternalState(classUnderTest, "fileToCanopies");

        assertThat(fileToCanopies.entrySet(), hasSize(numberOfCanopiesInTest));
        assertThat(fileToCanopies.inverse().entrySet(), hasSize(numberOfCanopiesInTest));
    }

    @Test
    public void runExperiments_verifyExperimentIsExecutedForEachCanopy() throws Exception {
        classUnderTest.runExperiments(rootFolder.getAbsolutePath(), "");
        PowerMockito.verifyPrivate(classUnderTest, Mockito.times(numberOfCanopiesInTest)).invoke("performExperimentComparison");
    }
}