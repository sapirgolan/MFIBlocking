package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
        MatcherAssert.assertThat(blockWithDatas, Matchers.hasSize(4));
    }
}