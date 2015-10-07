package il.ac.technion.ie.experiments.service;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.UaiVariableContext;
import il.ac.technion.ie.experiments.parsers.UaiBuilder;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import il.ac.technion.ie.model.Record;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

public class UaiConsumerIntegrationTest {

    private UaiConsumer classUnderTest;
    private static UaiVariableContext variableContext;
    private static List<BlockWithData> fuzzyBlocks;

    @BeforeClass
    public static void initClass() throws URISyntaxException, SizeNotEqualException {
        fuzzyBlocks = ExperimentsUtils.createFuzzyBlocks();
        UaiBuilder uaiBuilder = new UaiBuilder(fuzzyBlocks);
        variableContext = uaiBuilder.createUaiFile();
    }

    @Before
    public void setUp() throws Exception {
        classUnderTest = spy(new UaiConsumer(UaiConsumerIntegrationTest.variableContext, ExperimentsUtils.getBinaryFile()));
    }

    @Test
    public void testConsumePotentials() throws Exception {
        //execute
        classUnderTest.consumePotentials();

        assertThat(classUnderTest.isPotentialConsumed(), is(true));
        ListMultimap<Integer, Double> variableIdToProbabilities = Whitebox.getInternalState(classUnderTest, "variableIdToProbabilities");
        ListMultimap<Integer, Double> blockIdToProbabilities = Whitebox.getInternalState(classUnderTest, "blockIdToProbabilities");

        //verify there are 8 variables
        assertThat(variableIdToProbabilities.keySet().size(), is(8));
        //verify there are 6 blocks
        assertThat(blockIdToProbabilities.keySet().size(), is(6));
        //verify there are 44 lines
        assertThat(variableIdToProbabilities.size(), is(44));

        Integer blockId;
        //assert on variable #0
        blockId = variableContext.getBlockIdByVariableId(0);
        assertThat(variableIdToProbabilities.get(0), contains(0.0, 0.0, 1.0));
        assertThat(blockIdToProbabilities.get(blockId), contains(0.0, 0.0, 1.0));

        //assert on variable #1
        blockId = variableContext.getBlockIdByVariableId(1);
        assertThat(variableIdToProbabilities.get(1), contains(1.0, 0.0));
        assertThat(blockIdToProbabilities.get(blockId), contains(1.0, 0.0));

        //assert on variable #2
        blockId = variableContext.getBlockIdByVariableId(2);
        assertThat(variableIdToProbabilities.get(2), contains(0.0, 0.0, 0.0, 1.0));
        assertThat(blockIdToProbabilities.get(blockId), contains(0.0, 0.0, 0.0, 1.0));

        //assert on variable #3
        blockId = variableContext.getBlockIdByVariableId(3);
        assertThat(variableIdToProbabilities.get(3), contains(0.0, 0.0, 0.0, 0.0, 1.0));
        assertThat(blockIdToProbabilities.get(blockId), contains(0.0, 0.0, 0.0, 0.0, 1.0));

        //assert on variable #4
        blockId = variableContext.getBlockIdByVariableId(4);
        assertThat(variableIdToProbabilities.get(4), contains(0.0, 0.0, 1.0, 0.0));
        assertThat(blockIdToProbabilities.get(blockId), contains(0.0, 0.0, 1.0, 0.0));

        //assert on variable #5
        blockId = variableContext.getBlockIdByVariableId(5);
        assertThat(variableIdToProbabilities.get(5), contains(0.0, 0.0, 0.0, 1.0));
        assertThat(blockIdToProbabilities.get(blockId), contains(0.0, 0.0, 0.0, 1.0));
    }

    @Test
    public void testApplyNewProbabilities() throws Exception {
        //consume binary File
        classUnderTest.consumePotentials();

        //execute
        classUnderTest.applyNewProbabilities(fuzzyBlocks);

        //assert
        for (BlockWithData fuzzyBlock : fuzzyBlocks) {
            double sum = 0.0;
            for (Record record : fuzzyBlock.getMembers()) {
                double probability = fuzzyBlock.getMemberProbability(record);
                assertThat(probability, allOf(greaterThanOrEqualTo(0.0), lessThanOrEqualTo(1.0)));
                sum += probability;
            }
            assertThat(sum, closeTo(1, 0.00001));
        }

        /*
        * With the given input only blocks #2 & #3 were not split,
        * therefore their TrueRepresentative MUST have a probability of 1.0
        */
        ArrayList<Integer> blocksThatWereNotSplit = Lists.newArrayList(2, 3);
        for (Integer blockPos : blocksThatWereNotSplit) {
            Record trueRepresentative = fuzzyBlocks.get(blockPos).getTrueRepresentative();
            double memberProbability = (double) fuzzyBlocks.get(blockPos).getMemberProbability(trueRepresentative);
            assertThat(memberProbability, closeTo(1.0, 0.0001));
        }

        //verify
        verifyPrivate(classUnderTest, times(1)).invoke("consumePotentials");

    }
}