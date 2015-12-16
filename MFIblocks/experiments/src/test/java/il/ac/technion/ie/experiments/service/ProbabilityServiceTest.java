package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.utils.UtilitiesForBlocksAndRecords;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
    public void testCalcSimilaritiesAndProbabilitiesOfRecords() throws Exception {
        List<BlockWithData> blocksWithData = getBlocks();
        classUnderTest.calcSimilaritiesAndProbabilitiesOfRecords(blocksWithData);
        for (BlockWithData blockWithData : blocksWithData) {
            double sumProbability = 0;
            for (Record record : blockWithData.getMembers()) {
                double memberProbability = blockWithData.getMemberProbability(record);
                sumProbability += memberProbability;
                assertThat(memberProbability, allOf(greaterThan(0.0), not(greaterThan(1.0))));
            }
            assertThat(sumProbability, is(closeTo(1.0, 0.000001)));
        }
    }

    private List<BlockWithData> getBlocks() throws URISyntaxException {
        ParsingService parsingService = new ParsingService();
        return parsingService.parseDataset(UtilitiesForBlocksAndRecords.getPathToBigRecordsFile());
    }
}