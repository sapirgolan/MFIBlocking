package il.ac.technion.ie.experiments.service;

import com.google.common.collect.ListMultimap;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.UaiVariableContext;
import il.ac.technion.ie.experiments.parsers.UaiBuilder;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class UaiConsumerIntegrationTest {

    private UaiConsumer classUnderTest;
    private static UaiVariableContext variableContext;

    @BeforeClass
    public static void initClass() throws URISyntaxException, SizeNotEqualException {
        List<BlockWithData> fuzzyBlocks = ExperimentsUtils.createFuzzyBlocks();
        UaiBuilder uaiBuilder = new UaiBuilder(fuzzyBlocks);
        variableContext = uaiBuilder.createUaiFile();
    }

    @Before
    public void setUp() throws Exception {
        classUnderTest = new UaiConsumer(UaiConsumerIntegrationTest.variableContext, ExperimentsUtils.getBinaryFile());
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
}