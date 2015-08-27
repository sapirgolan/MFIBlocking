package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.List;

public class ProbabilityServiceTest {

    private ProbabilityService classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new ProbabilityService();
    }

    /**
     * This is a sanity test to verify the all flow is working
     *
     * @throws Exception
     */
    @Test
    public void testCalcProbabilitiesOfRecords() throws Exception {
        List<BlockWithData> blocksWithData = getBlocks();
        classUnderTest.calcProbabilitiesOfRecords(blocksWithData);
    }

    private List<BlockWithData> getBlocks() throws URISyntaxException {
        ParsingService parsingService = new ParsingService();
        return parsingService.parseDataset(ExperimentsUtils.getPathToRecordsFile());
    }
}