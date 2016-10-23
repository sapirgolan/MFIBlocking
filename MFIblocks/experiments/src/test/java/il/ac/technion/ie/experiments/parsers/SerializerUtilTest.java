package il.ac.technion.ie.experiments.parsers;

import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.model.CanopyRecord;
import il.ac.technion.ie.model.Record;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mock;

public class SerializerUtilTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testSerializeCanopies() throws Exception {
        //prepare
        List<CanopyCluster> canopies = generateCanopyClusters();
        File canopiesFile = testFolder.newFile();

        //execute
        boolean serializeCanopies = SerializerUtil.serializeCanopies(canopiesFile, canopies);

        //assert
        assertThat(serializeCanopies, is(true));
        assertThat(FileUtils.sizeOf(canopiesFile), greaterThan(0L));
    }

    @Test
    public void testDeSerializeCanopies() throws Exception {
        //prepare
        List<CanopyCluster> canopies = generateCanopyClusters();
        File canopiesFile = testFolder.newFile();
        assertThat(SerializerUtil.serializeCanopies(canopiesFile, canopies), is(true));

        //execute
        Collection<CanopyCluster> canopyClusters = SerializerUtil.deSerializeCanopies(canopiesFile);

        //assert
        assertThat(canopyClusters, hasSize(canopies.size()));
    }

    @Test
    public void endToEnd_Canopy() throws Exception {
        Collection<CanopyRecord> canopyRecords= new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Record record = new Record(Arrays.asList("First Name", "Last Name"), Arrays.asList("Jhon the " + i, "Alister"), i);
            CanopyRecord canopyRecord = new CanopyRecord(record, 1 - i / 1.0);
            canopyRecords.add(canopyRecord);
        }
        CanopyCluster canopyCluster = CanopyCluster.newCanopyCluster(canopyRecords, 0.1, 0.3);
        Collection<CanopyCluster> canopies = new ArrayList<>(Arrays.asList(canopyCluster));


        File canopiesFile = testFolder.newFile();
        
        //execute
        SerializerUtil.serializeCanopies(canopiesFile, canopies);
        Collection<CanopyCluster> canopyClusters = SerializerUtil.deSerializeCanopies(canopiesFile);
        ArrayList<CanopyCluster> clusters = new ArrayList<>(canopyClusters);
        

        //assert
        CanopyCluster canopyClusterDeserialize = clusters.get(0);
        List<CanopyRecord> allRecords = canopyClusterDeserialize.getAllRecords();
        for (CanopyRecord record : allRecords) {
            assertThat(record.getFieldNames(), containsInAnyOrder("First Name", "Last Name"));
            List<String> entries = record.getEntries();
            assertThat(entries, hasItem("Alister"));
            assertThat(record.getScore(), not(nullValue()));
        }

    }

    private List<CanopyCluster> generateCanopyClusters() {
        List<CanopyCluster> canopies = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            canopies.add(mock(CanopyCluster.class));
        }
        return canopies;
    }
}