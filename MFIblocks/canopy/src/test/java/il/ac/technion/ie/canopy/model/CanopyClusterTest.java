package il.ac.technion.ie.canopy.model;

import com.google.common.collect.Lists;
import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.model.CanopyRecord;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.utils.UtilitiesForBlocksAndRecords;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CanopyClusterTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testRemoveRecordsBelowT2() throws Exception {
        //fetch subset of records
        List<Record> records = UtilitiesForBlocksAndRecords.getRecordsFromCsv();
        List<Record> subList = records.subList(0, 5);
        assertThat(subList, hasSize(5));
        List<CanopyRecord> canopyRecords = generateSimScoresOnRecords(subList, Lists.newArrayList(0.77, 0.88, 0.65, 0.7, 0.2));

        //execute
        CanopyCluster canopyCluster = CanopyCluster.newCanopyCluster(canopyRecords, 0.3, 0.6);
        assertThat(canopyCluster.getAllRecords(), hasSize(4));
    }

    /**
     * This test use thresholds that is not in the range of 0.0 - 1.0 to test the convert range method
     * that exists in CanopyCluster.
     * <p/>
     * If it fails, it means that the way values are converted was messed up.
     *
     * @throws Exception
     */
    @Test
    public void testGetTightRecords() throws Exception {
        //fetch subset of records
        List<Record> recordsFromCsv = UtilitiesForBlocksAndRecords.getRecordsFromCsv();
        List<Record> subList = recordsFromCsv.subList(11, 20);
        assertThat(subList, hasSize(9));

        double lower = 0.1,
                upper = 0.8,
                range = upper - lower;

        double t2 = 0.3,
                t1 = 0.6;

        //adding sim score for first two records
        UniformRealDistribution nonRelevantRecordsDist = new UniformRealDistribution(lower, range * t2 + lower);
        List<Double> simScores = Lists.newArrayList(nonRelevantRecordsDist.sample(), nonRelevantRecordsDist.sample());
        //adding sim score for one records that will be in the converted range of 0.3 - 0.6
        simScores.add(range * t2 + lower + 0.1);

        //adding the rest...
        UniformRealDistribution relevantRecordsDist = new UniformRealDistribution(t1 * range + lower, upper);
        int numberRelevantRecords = subList.size() - simScores.size();
        for (int i = 0; i < numberRelevantRecords; i++) {
            simScores.add(relevantRecordsDist.sample());
        }

        List<CanopyRecord> canopyRecords = generateSimScoresOnRecords(subList, simScores);

        //execute
        CanopyCluster canopyCluster = CanopyCluster.newCanopyCluster(canopyRecords, t2, t1);
        assertThat(canopyCluster.getTightRecords(), hasSize(numberRelevantRecords));
    }

    @Test
    public void testCanopyContainItsOwnRecords() throws Exception {
        CanopyCluster cluster = createCluster();
        cluster.removeRecordsBelowT2();
        List<CanopyRecord> allRecords = cluster.getAllRecords();
        for (CanopyRecord record : allRecords) {
            assertThat("record with ID '" + record.toString() + "' should have been in cluster but it is not", cluster.contains(record));
        }
    }

    @Test
    public void testCanopyNotContainAllCandidateRecords() throws Exception {
        CanopyCluster cluster = createCluster();

        List<CanopyRecord> allRecords = cluster.getAllRecords();
        assertThat(allRecords, hasSize(4));
    }

    @Test
    public void testCanopyContainsRecordNotReferenced() throws Exception {
        CanopyCluster cluster = createCluster();
        cluster.removeRecordsBelowT2();

        List<Record> records = UtilitiesForBlocksAndRecords.getRecordsFromCsv();

        for (Record record : records.subList(0, 4)) {
            assertThat("record with ID '" + record.toString() + "' should have been in cluster but it is not", cluster.contains(record));
        }
    }

    @Test
    public void testCanopyNotContainsRecordNotReferenced() throws Exception {
        CanopyCluster cluster = createCluster();
        cluster.removeRecordsBelowT2();

        List<Record> records = UtilitiesForBlocksAndRecords.getRecordsFromCsv();

        for (Record record : records.subList(4, 20)) {
            assertThat("record with ID '" + record.toString() + "' should NOT have been in cluster but it is not", !cluster.contains(record));
        }
    }

    private List<CanopyRecord> generateSimScoresOnRecords(List<Record> records) {
        UniformRealDistribution realDistribution = new UniformRealDistribution();
        List<Double> sampledScores = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            sampledScores.add(realDistribution.sample());
        }
        return generateSimScoresOnRecords(records, sampledScores);

    }

    private List<CanopyRecord> generateSimScoresOnRecords(List<Record> records, List<Double> sampledScores) {
        assertThat(records.size(), is(sampledScores.size()));
        List<CanopyRecord> canopyRecords = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            canopyRecords.add(new CanopyRecord(records.get(i), sampledScores.get(i)));
        }
        return canopyRecords;
    }

    private CanopyCluster createCluster() throws URISyntaxException, CanopyParametersException {
        //fetch subset of records
        List<Record> records = UtilitiesForBlocksAndRecords.getRecordsFromCsv();
        List<Record> subList = records.subList(0, 5);
        assertThat(subList, hasSize(5));
        List<CanopyRecord> canopyRecords = generateSimScoresOnRecords(subList, Lists.newArrayList(0.77, 0.88, 0.65, 0.7, 0.2));

        //execute
        CanopyCluster canopyCluster = CanopyCluster.newCanopyCluster(canopyRecords, 0.3, 0.6);
        return canopyCluster;
    }

}