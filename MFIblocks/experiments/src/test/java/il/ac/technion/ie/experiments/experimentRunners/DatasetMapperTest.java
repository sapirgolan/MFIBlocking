package il.ac.technion.ie.experiments.experimentRunners;

import il.ac.technion.ie.experiments.util.ZipExtractor;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

/**
 * Created by I062070 on 06/01/2017.
 */
public class DatasetMapperTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void getDatasetFile() throws Exception {
        File root = temporaryFolder.newFolder();
        ZipExtractor.extractZipFromResources(root, "/01_NumberOfOriginalRecords_datasets.zip");
        Collection<File> allDatasetPermutations = FileUtils.listFiles(root, null , false);
        File datasetOfGivenPermutation = DatasetMapper.getDatasetFile("FebrlParam_25", allDatasetPermutations);

        assertThat(datasetOfGivenPermutation, notNullValue());
        assertThat(datasetOfGivenPermutation.getName(), is("25_75_5_5_16_uniform_all_0_parameter=25.csv"));
    }

}