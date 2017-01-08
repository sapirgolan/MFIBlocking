package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.Multimap;
import il.ac.technion.ie.experiments.model.BlockWithData;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

/**
 * Created by I062070 on 07/01/2017.
 */
public class ProcessCanopiesE2ETest extends AbstractProcessCanopiesTest{

    @After
    public void tearDown() throws Exception {
        verifyPrivate(classUnderTest).invoke("saveResultsToFS", Mockito.any(Multimap.class));
    }

    @Test
    public void runExperiments_onTwoDatasets() throws Exception {
        reduceDatasetSizeTo(datasetsRootFolder, 2);
        reduceDatasetSizeTo(canopiesRootFolder, 2);

        classUnderTest.runExperiments(canopiesRootFolder.getAbsolutePath(), datasetsRootFolder.getAbsolutePath());

        verifyPrivate(classUnderTest, Mockito.times(10))
                .invoke("calculateMeasurements", Mockito.anyListOf(BlockWithData.class), Mockito.any(Multimap.class));
    }

    @Test
    public void runExperiments_onSevenDatasets() throws Exception {
        classUnderTest.runExperiments(canopiesRootFolder.getAbsolutePath(), datasetsRootFolder.getAbsolutePath());

        verifyPrivate(classUnderTest, Mockito.times(35))
                .invoke("calculateMeasurements", Mockito.anyListOf(BlockWithData.class), Mockito.any(Multimap.class));
    }

    private void reduceDatasetSizeTo(File datasetRootFolder, int reduceToSize) throws IOException {
        File[] files = datasetRootFolder.listFiles();
        for (int i = files.length -1; i > reduceToSize - 1; i--) {
            File deteleTarget = files[i];
            if (deteleTarget.isDirectory()) {
                FileUtils.deleteDirectory(deteleTarget);
            } else {
                deteleTarget.delete();
            }
        }
    }
}
