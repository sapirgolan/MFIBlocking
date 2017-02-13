package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.Multimap;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.ConvexBPService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

/**
 * Created by I062070 on 13/02/2017.
 */
public class ProcessBlocksTest extends AbstractProcessCanopiesTest{
    protected ProcessBlocks classUnderTest;

    @Before
    public void setUp_E2E() throws Exception {
        classUnderTest = PowerMockito.spy(new ProcessBlocks());
    }

    @After
    public void tearDown() throws Exception {
        verifyPrivate(classUnderTest).invoke("saveResultsToFS", Mockito.any(Multimap.class));
    }

    @Test
    public void runExperiments_E2E() throws Exception {
        classUnderTest.runExperiments(blocksRootFolder.getAbsolutePath(), datasetsRootFolder.getAbsolutePath());

        verifyPrivate(classUnderTest, Mockito.times(NUMBER_OF_CANOPIES_IN_TEST))
                .invoke("calculateMeasurements", Mockito.anyListOf(BlockWithData.class), Mockito.any(Multimap.class));
    }

}