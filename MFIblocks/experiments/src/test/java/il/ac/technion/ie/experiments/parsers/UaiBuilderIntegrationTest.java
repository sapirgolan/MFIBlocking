package il.ac.technion.ie.experiments.parsers;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.FuzzyService;
import il.ac.technion.ie.experiments.service.ParsingService;
import il.ac.technion.ie.experiments.service.ProbabilityService;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UaiBuilder.class})
public class UaiBuilderIntegrationTest {

    private ParsingService parsingService;
    private ProbabilityService probabilityService;
    private FuzzyService fuzzyService;

    private UaiBuilder classUnderTest;
    private List<BlockWithData> originalBlocks;
    private File outputFile;
    private List<BlockWithData> fuzzyBlocks;

    @Before
    public void setUp() throws Exception {
        createBlocks();
        classUnderTest = PowerMockito.spy(new UaiBuilder(fuzzyBlocks));

        outputFile = Whitebox.invokeMethod(classUnderTest, "createOutputFile");
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.forceDelete(outputFile);
    }

    private void createBlocks() throws URISyntaxException {
        String recordsFile = ExperimentsUtils.getPathToRecordsFile();

        parsingService = new ParsingService();
        probabilityService = new ProbabilityService();
        initFuzzyService();

        originalBlocks = parsingService.parseDataset(recordsFile);
        probabilityService.calcProbabilitiesOfRecords(originalBlocks);

        List<BlockWithData> copyOfOriginalBlocks = new ArrayList<>(originalBlocks);
        fuzzyBlocks = fuzzyService.splitBlocks(copyOfOriginalBlocks, 0.6);
        probabilityService.calcProbabilitiesOfRecords(fuzzyBlocks);
    }

    private void initFuzzyService() {
        fuzzyService = new FuzzyService();
        UniformRealDistribution uniformRealDistribution = PowerMockito.mock(UniformRealDistribution.class);
        PowerMockito.when(uniformRealDistribution.sample()).thenReturn(0.3, 0.7, 0.6, 0.4);
        Whitebox.setInternalState(fuzzyService, "splitBlockProbThresh", uniformRealDistribution);
    }

    @Test
    public void testCreateUaiFile_cleanBlocks() throws Exception {
        //mocking
        PowerMockito.doReturn(outputFile).when(classUnderTest, "createOutputFile");

        //execution
        classUnderTest.createUaiFile();

        //assertion
        Assert.assertThat(FileUtils.sizeOf(outputFile), Matchers.greaterThan(100L));
    }
}