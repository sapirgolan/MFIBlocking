package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import il.ac.technion.ie.canopy.algorithm.Canopy;
import il.ac.technion.ie.experiments.util.ZipExtractor;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Created by I062070 on 15/12/2016.
 */
public class FilesReaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File root;
    private FilesReader classUnderTest;
    private List<File> filesCreated = new ArrayList<>();
    private Set<String> expectedFileNames = Sets.newHashSet("25_75_5_5_16_uniform_all_0_parameter=25.csv",
            "45_75_5_5_16_uniform_all_0_parameter=45.csv",
            "30_75_5_5_16_uniform_all_0_parameter=30.csv",
            "50_75_5_5_16_uniform_all_0_parameter=50.csv",
            "35_75_5_5_16_uniform_all_0_parameter=35.csv",
            "55_75_5_5_16_uniform_all_0_parameter=55.csv",
            "40_75_5_5_16_uniform_all_0_parameter=40.csv");

    @Before
    public void setUp() throws Exception {
        root = folder.newFolder("root");
        classUnderTest = new FilesReader(root.getPath());
    }

    //root
    //--01_NumberOfOriginalRecords
    //----permutationWith_parameter=25
    //------canopy_0
    //------canopy_1
    //...
    //------canopy_6
    private void createCanopiesFiles() throws IOException {
        String path = root.getAbsolutePath();
        File permutationWithParam = new File(path + File.separator + "01_NumberOfOriginalRecords" + File.separator
                + "permutationWith_parameter=25");
        assertTrue(permutationWithParam.mkdirs());
        for (int i = 0; i < 7; i++) {
            File file = new File(permutationWithParam, "canopy_" + i);
            assertTrue(file.createNewFile());
            filesCreated.add(file);
        }
    }

    @After
    public void cleanup() throws IOException {
        filesCreated.clear();
        FileUtils.cleanDirectory(root);
    }

    @Test
    public void listAllCanopies_someRootPath() throws Exception {
        createCanopiesFiles();
        Collection<File> allFiles =  Whitebox.invokeMethod(classUnderTest, "listAllCanopies");
        assertThat(allFiles, hasSize(7));
    }

    @Test
    public void getAllCanopies() throws IOException {
        createCanopiesFiles();
        Table<String, String, Set<File>> canopiesTable = classUnderTest.getAllCanopies();
        assertThat(canopiesTable.rowKeySet(), hasItem("01_NumberOfOriginalRecords"));
        assertThat(canopiesTable.columnKeySet(), hasItem("permutationWith_parameter=25"));
        assertThat(canopiesTable.get("01_NumberOfOriginalRecords", "permutationWith_parameter=25"), hasSize(filesCreated.size()));
        assertThat(canopiesTable.get("01_NumberOfOriginalRecords", "permutationWith_parameter=25"),
                containsInAnyOrder(filesCreated.toArray(new File[filesCreated.size()])));
    }


    @Test
    public void getAllDataSets() throws ZipException, IOException, URISyntaxException {
        ZipExtractor.extractZipFromResources(root, "/01_NumberOfOriginalRecords_datasets.zip");
        Collection<File> datasets = classUnderTest.getAllDatasets();

        assertThat(datasets, hasSize(7));
        for (File dataset : datasets) {
            assertThat(expectedFileNames, hasItem(dataset.getName()));
        }
    }

}