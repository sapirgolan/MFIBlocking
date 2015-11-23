package il.ac.technion.ie.canopy.model;

import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import il.ac.technion.ie.model.Record;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CanopyClusterTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testRemoveRecordsBelowT2() throws Exception {
        //read records from CSV file
        String pathToSmallRecordsFile = ExperimentsUtils.getPathToSmallRecordsFile();
        List<Record> records = ExperimentsUtils.createRecordsFromTestFile(pathToSmallRecordsFile);
        assertThat(records, hasSize(20));

        //fetch subset of records
        List<Record> clusterOfRecordZero = records.subList(0, 5);
        assertThat(clusterOfRecordZero, hasSize(5));

        //execute
        CanopyCluster canopyCluster = new CanopyCluster(clusterOfRecordZero, 0.3, 0.6);
        canopyCluster.removeRecordsBelowT2();
        assertThat(canopyCluster.getAllRecords(), hasSize(4));
    }
}