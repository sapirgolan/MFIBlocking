package il.ac.technion.ie.experiments.experimentRunners;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CreateCanopiesTest {

    private CreateCanopies classUnderTest;
    private File dir;

    @Before
    public void setUp() throws Exception {
        this.classUnderTest = new CreateCanopies();
    }

    @After
    public void tearDown() throws Exception {
        if (dir != null && dir.exists()) {
            FileUtils.forceDelete(dir);
        }
    }

    @Test
    public void buildDirPath() throws Exception {
        String dirPath = Whitebox.invokeMethod(classUnderTest, "buildDirPath", "MockDataSet", 7);

        String expected = FileUtils.getUserDirectoryPath() + File.separator + "MockDataSet" + File.separator + CreateCanopies.FEBRL_PARAM + 7;
        assertThat(dirPath, is(expected));
    }

    @Test
    public void createDirectory_notExists() throws Exception {
        //prepare
        String dirPath = Whitebox.invokeMethod(classUnderTest, "buildDirPath", "MockDataSet", 9);

        //execute
        dir = Whitebox.invokeMethod(classUnderTest, "createDirectory", dirPath);

        Assert.assertTrue("Failed to create dir", dir.exists());
        Assert.assertTrue("Failed to create dir", dir.isDirectory());

    }

    @Test
    public void createDirectory_fileHasSameName() throws Exception {
        //prepare
        String dirPath = Whitebox.invokeMethod(classUnderTest, "buildDirPath", "MockDataSet", 9);
        boolean newFileCreated = new File(dirPath).createNewFile();
        assertThat(newFileCreated, is(true));

        //execute
        dir = Whitebox.invokeMethod(classUnderTest, "createDirectory", dirPath);

        Assert.assertTrue("Failed to create dir", dir.exists());
        Assert.assertTrue("Failed to create dir", dir.isDirectory());
    }

    @Test
    public void createDirectory_dirExistsWithContent() throws Exception {
        //prepare
        String dirPath = Whitebox.invokeMethod(classUnderTest, "buildDirPath", "MockDataSet", 9);
        dir = Whitebox.invokeMethod(classUnderTest, "createDirectory", dirPath);

        boolean newFileCreated =  new File(dirPath, "fileToRemoved.tmp").createNewFile();
        assertThat(newFileCreated, is(true));

        //execute
        dir = Whitebox.invokeMethod(classUnderTest, "createDirectory", dirPath);

        //assert
        Assert.assertTrue("Failed to create dir", dir.exists());
        Assert.assertTrue("Failed to create dir", dir.isDirectory());
        assertThat(FileUtils.listFiles(dir, null, true), hasSize(0));
    }
}