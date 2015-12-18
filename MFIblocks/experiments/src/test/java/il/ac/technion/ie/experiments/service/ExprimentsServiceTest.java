package il.ac.technion.ie.experiments.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.util.CanopyUtils;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.utils.UtilitiesForBlocksAndRecords;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class ExprimentsServiceTest {

    private ExprimentsService classUnderTest;

    @Before
    public void setUp() throws Exception {
        this.classUnderTest = new ExprimentsService();
        cleanOldCsvFiles();
    }

    private void cleanOldCsvFiles() throws IOException {
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

        Collection<File> datasets = classUnderTest.findDatasets(tempDirectory.getAbsolutePath(), false);
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

    @Test
    public void testCalcAvgBlockSize() throws Exception {
        List<BlockWithData> blocks = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            BlockWithData mock = PowerMockito.mock(BlockWithData.class);
            when(mock.size()).thenReturn(i);
            blocks.add(mock);
        }
        assertThat(classUnderTest.calcAvgBlockSize(blocks), closeTo(7.0, 0.00001));
    }

    @Test
    public void testCalcAvgBlockSize_resultIsNotInteger() throws Exception {
        List<BlockWithData> blocks = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            BlockWithData mock = PowerMockito.mock(BlockWithData.class);
            when(mock.size()).thenReturn(i);
            blocks.add(mock);
        }
        assertThat(classUnderTest.calcAvgBlockSize(blocks), closeTo(6.5, 0.00001));
    }

    @Test
    public void testFetchRepresentativesEmptyList() throws Exception {
        //execute
        Multimap<Record, BlockWithData> representatives = classUnderTest.fetchRepresentatives(new ArrayList<BlockWithData>());

        //assert
        assertThat(representatives.size(), is(0));
    }

    @Test
    public void testFetchRepresentatives_singleRep() throws Exception {
        //prepare
        CanopyService canopyService = new CanopyService();
        List<Record> recordsFromCsv = UtilitiesForBlocksAndRecords.getRecordsFromCsv();
        List<Record> records = recordsFromCsv.subList(1, 6);
        List<Double> similarities = Lists.newArrayList(0.8, 2.88, 2.2, 1.7, 0.4);

        CanopyCluster canopyCluster = CanopyUtils.createCanopySingleList(records, similarities);
        BlockWithData blockWithData = canopyService.convertCanopyToBlock(canopyCluster);

        //execute
        Multimap<Record, BlockWithData> representatives = classUnderTest.fetchRepresentatives(Lists.newArrayList(blockWithData));

        //assertion
        assertThat(representatives.asMap(), hasKey(records.get(1)));
        assertThat(representatives.values(), contains(blockWithData));
        assertThat(representatives.size(), is(1));
    }

    @Test
    public void testFetchRepresentatives_sameRepInSeveralBlocks() throws Exception {
        //prepare
        CanopyService canopyService = new CanopyService();
        List<Record> recordsFromCsv = UtilitiesForBlocksAndRecords.getRecordsFromCsv();
        List<Record> records = recordsFromCsv.subList(1, 6);
        List<Double> similarities = Lists.newArrayList(0.8, 2.88, 2.2, 1.7, 0.4);

        CanopyCluster canopyClusterOne = CanopyUtils.createCanopySingleList(records, similarities);
        BlockWithData blockWithDataOne = canopyService.convertCanopyToBlock(canopyClusterOne);
        CanopyCluster canopyClusterTwo = CanopyUtils.createCanopySingleList(records.subList(0, 3), similarities.subList(0, 3));
        BlockWithData blockWithDataTwo = canopyService.convertCanopyToBlock(canopyClusterTwo);

        //execute
        Multimap<Record, BlockWithData> representatives = classUnderTest.fetchRepresentatives(
                Lists.newArrayList(blockWithDataOne, blockWithDataTwo));

        //assertion
        assertThat(representatives.asMap(), hasKey(records.get(1)));
        assertThat(representatives.values(), contains(blockWithDataOne, blockWithDataTwo));
        assertThat(representatives.get(records.get(1)), contains(blockWithDataOne, blockWithDataTwo));
        assertThat(representatives.size(), is(2));
    }
}