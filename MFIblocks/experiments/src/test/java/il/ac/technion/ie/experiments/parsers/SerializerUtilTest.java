package il.ac.technion.ie.experiments.parsers;

import il.ac.technion.ie.canopy.model.CanopyCluster;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
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
        List<CanopyCluster> canopyClusters = SerializerUtil.deSerializeCanopies(canopiesFile);

        //assert
        assertThat(canopyClusters, hasSize(canopies.size()));
    }


    private List<CanopyCluster> generateCanopyClusters() {
        List<CanopyCluster> canopies = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            canopies.add(mock(CanopyCluster.class));
        }
        return canopies;
    }
}