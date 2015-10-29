package il.ac.technion.ie.experiments.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ExprimentsServiceTest {

    private ExprimentsService classUnderTest;

    @Before
    public void setUp() throws Exception {
        this.classUnderTest = new ExprimentsService();
        cleanOlfCsvFiles();
    }

    private void cleanOlfCsvFiles() throws IOException {
        Collection<File> csv = FileUtils.listFiles(FileUtils.getTempDirectory(), FileFilterUtils.suffixFileFilter("csv"), null);
        for (File file : csv) {
            FileUtils.forceDelete(file);
        }
    }

    @Test
    public void testFindDatasets() throws Exception {
        File tempDirectory = FileUtils.getTempDirectory();
        File file1 = File.createTempFile("file1", ".csv", tempDirectory);
        File file2 = File.createTempFile("file2", ".csv", tempDirectory);

        Collection<File> datasets = classUnderTest.findDatasets(tempDirectory.getAbsolutePath());
        assertThat(datasets, hasSize(2));
        assertThat(datasets, contains(file1, file2));
    }

    @Test
    public void testGetParameterValue() throws Exception {
        File file = new File(FileUtils.getTempDirectory(), "someDataset_parameter=9.csv");
        assertThat(classUnderTest.getParameterValue(file), is(9));
    }

    @Test
    public void testGetParameterValue_noParamExists() throws Exception {
        File file = new File(FileUtils.getTempDirectory(), "someDataset_parameter=VCS.csv");
        assertThat(classUnderTest.getParameterValue(file), nullValue());
    }

    @Test
    public void testGetParameterValue_fileNameNotInFormat() throws Exception {
        File file = new File(FileUtils.getTempDirectory(), "someDataset_paramer_8.csv");
        assertThat(classUnderTest.getParameterValue(file), nullValue());
    }
}