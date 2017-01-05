package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.BiMap;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.util.ZipExtractor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Created by I062070 on 02/01/2017.
 */
public class ProcessCanopiesTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    ProcessCanopies classUnderTest;
    private File rootFolder;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new ProcessCanopies();
        rootFolder = temporaryFolder.newFolder("root");
        ZipExtractor.extractZipFromResources(rootFolder, "/01_NumberOfOriginalRecords_canopies.zip");
    }

    @Test
    public void readAndParseCanopiesFromDir_hasAllCanopyFiles() throws Exception {
        Whitebox.invokeMethod(classUnderTest, "readAndParseCanopiesFromDir", rootFolder.getAbsolutePath());
        BiMap<File, Collection<CanopyCluster>> fileToCanopies = Whitebox.getInternalState(classUnderTest, "fileToCanopies");

        assertThat(fileToCanopies.entrySet(), hasSize(35));
        assertThat(fileToCanopies.inverse().entrySet(), hasSize(35));
    }
}