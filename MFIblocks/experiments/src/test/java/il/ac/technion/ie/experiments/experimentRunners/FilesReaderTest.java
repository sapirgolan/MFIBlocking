package il.ac.technion.ie.experiments.experimentRunners;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

/**
 * Created by I062070 on 15/12/2016.
 */
public class FilesReaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File root;
    private FilesReader classUnderTest;

    @Before
    public void setUp() throws Exception {
        root = folder.newFolder("root");
        String path = root.getAbsolutePath();
        File permutationWithParam = new File(path + File.separator + "permutationOfFiledModifications" + File.separator
                + "permutationWithParam_7");
        assertTrue(permutationWithParam.mkdirs());
        for (int i = 0; i < 7; i++) {
            File file = new File(permutationWithParam, "canopy_" + i);
            assertTrue(file.createNewFile());
        }
        classUnderTest = new FilesReader(root.getPath());
    }

    @After
    public void cleanup() throws IOException {
        FileUtils.cleanDirectory(root);
    }

    @Test
    public void listAllCanopies_someRootPath() throws Exception {
        Collection<File> allFiles =  Whitebox.invokeMethod(classUnderTest, "listAllCanopies");
        assertThat(allFiles, hasSize(7));

    }
}