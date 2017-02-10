package il.ac.technion.ie.experiments.Utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by I062070 on 03/02/2017.
 */
public class ExpFileUtilsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void createBlocksFile_someFilesExist() throws Exception {
        File folder = temporaryFolder.newFolder("dataset__X");
        File baseline_0 = new File(folder, "baseline_0");
        baseline_0.createNewFile();
        File baseline_1 = new File(folder, "baseline_1");
        baseline_1.createNewFile();
        File bcbp_0 = new File(folder, "bcbp_0");
        bcbp_0.createNewFile();

        File newFile = ExpFileUtils.createBlocksFile(folder, "baseline");
        assertThat(newFile.getName(), is("baseline_2"));
    }

    @Test
    public void createBlocksFile_noFilesExist() throws Exception {
        File folder = temporaryFolder.newFolder("dataset__X");
        File bcbp_0 = new File(folder, "bcbp_0");
        bcbp_0.createNewFile();

        File newFile = ExpFileUtils.createBlocksFile(folder, "baseline");
        assertThat(newFile.getName(), is("baseline_0"));
    }

}