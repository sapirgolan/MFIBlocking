package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.BiMap;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import net.lingala.zip4j.exception.ZipException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import net.lingala.zip4j.core.ZipFile;
import org.powermock.reflect.Whitebox;


import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

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
        extractZipFromResources();
    }

    private void extractZipFromResources() throws IOException, URISyntaxException, ZipException {
        rootFolder = temporaryFolder.newFolder("root");
        File tesZip = new File( this.getClass().getResource("/01_NumberOfOriginalRecords.zip").toURI() );
        ZipFile zipFile = new ZipFile(tesZip);
        zipFile.extractAll(rootFolder.getAbsolutePath());
    }

    @Test
    public void readAndParseCanopiesFromDir_hasAllCanopyFiles() throws Exception {
        Whitebox.invokeMethod(classUnderTest, "readAndParseCanopiesFromDir", rootFolder.getAbsolutePath());
        BiMap<File, Collection<CanopyCluster>> fileToCanopies = Whitebox.getInternalState(classUnderTest, BiMap.class);

        assertThat(fileToCanopies.entrySet(), hasSize(35));
        assertThat(fileToCanopies.inverse().entrySet(), hasSize(35));
    }
}