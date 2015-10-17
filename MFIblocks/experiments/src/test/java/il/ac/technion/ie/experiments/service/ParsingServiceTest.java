package il.ac.technion.ie.experiments.service;

import com.google.common.collect.Lists;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class ParsingServiceTest {

    private ParsingService classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new ParsingService();
    }

    @Test
    public void testParseDataset() throws Exception {
        String recordsFile = ExperimentsUtils.getPathToSmallRecordsFile();
        List<BlockWithData> blockWithDatas = classUnderTest.parseDataset(recordsFile);
        assertThat(blockWithDatas, hasSize(4));
    }

    @Test
    public void testWriteExperimentsMeasurements_hasHeaders() throws Exception {
        IMeasurements measurements = PowerMockito.mock(IMeasurements.class);
        File tempFile = File.createTempFile("tempMesurmentFile", "csv");

        classUnderTest.writeExperimentsMeasurements(measurements, tempFile);
        List<String> lines = FileUtils.readLines(tempFile);
        assertThat(lines, hasSize(1));
        assertThat(lines.get(0), allOf(containsString("Ranked Value"), containsString("MRR"),
                containsString("Threshold"), containsString("Norm Ranked Value"), containsString("Norm MRR")));
    }

    @Test
    public void testWriteExperimentsMeasurements_hasLines() throws Exception {
        IMeasurements measurements = PowerMockito.mock(IMeasurements.class);
        when(measurements.getMrrValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.2, 0.3, 0.4));
        when(measurements.getThresholdSorted()).thenReturn(Lists.newArrayList(0.0, 0.0, 0.0));
        when(measurements.getRankedValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.0, 0.0, 0.0));
        when(measurements.getNormalizedMRRValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.0, 0.0, 0.0));
        when(measurements.getNormalizedRankedValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.0, 0.0, 0.0));

        File tempFile = File.createTempFile("tempMeasurementFile", ".csv");

        classUnderTest.writeExperimentsMeasurements(measurements, tempFile);
        List<String> lines = FileUtils.readLines(tempFile);
        assertThat(lines, hasSize(4));
        assertThat(lines.get(1), containsString("0.2"));
        assertThat(lines.get(2), containsString("0.3"));
        assertThat(lines.get(3), containsString("0.4"));
    }

    @Test
    public void testWriteExperimentsMeasurements_hasThresholdMRRandRVInRow() throws Exception {
        IMeasurements measurements = PowerMockito.mock(IMeasurements.class);
        when(measurements.getMrrValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.2, 0.4));
        when(measurements.getRankedValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.9, 0.88));
        when(measurements.getThresholdSorted()).thenReturn(Lists.newArrayList(0.1634, 0.354));
        when(measurements.getNormalizedMRRValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.0, 0.0));
        when(measurements.getNormalizedRankedValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.0, 0.0));

        File tempFile = File.createTempFile("tempMeasurementFile", ".csv");

        classUnderTest.writeExperimentsMeasurements(measurements, tempFile);
        List<String> lines = FileUtils.readLines(tempFile);
        assertThat(lines, hasSize(3));
        assertThat(lines.get(1), stringContainsInOrder(Lists.newArrayList("0.9", "0.2", "0.1634")));
        assertThat(lines.get(2), stringContainsInOrder(Lists.newArrayList("0.88", "0.4", "0.354")));
    }

    @Test(expected = SizeNotEqualException.class)
    public void testWriteExperimentsMeasurements_throwsExceptionIfSizeNotEqual() throws Exception {
        IMeasurements measurements = PowerMockito.mock(IMeasurements.class);
        when(measurements.getMrrValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.2, 0.3, 0.4));
        when(measurements.getThresholdSorted()).thenReturn(Lists.newArrayList(0.2, 0.3, 0.4));
        File tempFile = File.createTempFile("tempMeasurementFile", ".csv");

        classUnderTest.writeExperimentsMeasurements(measurements, tempFile);
    }

    @Test
    public void testWriteExperimentsMeasurements_hasValuesInRow() throws Exception {
        IMeasurements measurements = PowerMockito.mock(IMeasurements.class);
        when(measurements.getMrrValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.2, 0.4));
        when(measurements.getNormalizedMRRValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.1, 0.2));
        when(measurements.getRankedValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.9, 0.88));
        when(measurements.getNormalizedRankedValuesSortedByThreshold()).thenReturn(Lists.newArrayList(0.45, 0.44));
        when(measurements.getThresholdSorted()).thenReturn(Lists.newArrayList(0.1634, 0.354));

        File tempFile = File.createTempFile("tempMeasurementFile", ".csv");

        classUnderTest.writeExperimentsMeasurements(measurements, tempFile);
        List<String> lines = FileUtils.readLines(tempFile);
        assertThat(lines, hasSize(3));
        assertThat(lines.get(1), stringContainsInOrder(Lists.newArrayList("0.9", "0.2", "0.1634", "0.45", "0.1")));
        assertThat(lines.get(2), stringContainsInOrder(Lists.newArrayList("0.88", "0.4", "0.354", "0.44", "0.2")));
    }
}