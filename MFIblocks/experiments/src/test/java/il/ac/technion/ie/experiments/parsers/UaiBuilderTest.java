package il.ac.technion.ie.experiments.parsers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.potential.model.MatrixCell;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UaiBuilder.class})
public class UaiBuilderTest {

    private UaiBuilder classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new UaiBuilder(PowerMockito.mock(List.class));

    }

    @Test
    public void testCountCliques() throws Exception {

        List<MatrixCell<Double>> cells = Lists.newArrayList(new MatrixCell<>(1, 2, 0.2), new MatrixCell<>(1, 3, 0.2), new MatrixCell<>(1, 5, 0.2),
                new MatrixCell<>(2, 1, 0.2), new MatrixCell<>(2, 3, 0.2),
                new MatrixCell<>(3, 1, 0.2), new MatrixCell<>(3, 2, 0.2), new MatrixCell<>(3, 5, 0.2),
                new MatrixCell<>(5, 1, 0.2), new MatrixCell<>(5, 3, 0.2));
        int numberOfCliques = Whitebox.invokeMethod(classUnderTest, "countCliques", cells);
        MatcherAssert.assertThat(numberOfCliques, Matchers.is(cells.size() / 2));
    }

    /*@Test
    public void testCountNumberOfVariables() throws Exception {
        //prepare data for test
        Record record_1 = PowerMockito.mock(Record.class);
        Record record_2 = PowerMockito.mock(Record.class);
        Record record_3 = PowerMockito.mock(Record.class);
        Record record_4 = PowerMockito.mock(Record.class);
        Record record_5 = PowerMockito.mock(Record.class);

        BlockWithData block_1 = new BlockWithData(Lists.newArrayList(record_1, record_5));
        BlockWithData block_2 = new BlockWithData(Lists.newArrayList(record_1, record_2));
        BlockWithData block_3 = new BlockWithData(Lists.newArrayList(record_1, record_2, record_3));
        BlockWithData block_4 = new BlockWithData(Lists.newArrayList(record_4));
        BlockWithData block_5 = new BlockWithData(Lists.newArrayList(record_1, record_3));

        List<BlockWithData> blocks = Lists.newArrayList(block_1, block_2, block_3, block_4, block_5);

        //execute tested method
        int numberOfVariables = Whitebox.invokeMethod(classUnderTest, "countNumberOfVariables", blocks);

        //there are 5 blocks and 5 cliques
        MatcherAssert.assertThat(numberOfVariables, Matchers.is(10));

    }*/

    @Test
    public void testCountNumberOfVariables() throws Exception {
        //prepare data for test
        Record record_1 = PowerMockito.mock(Record.class);
        PowerMockito.when(record_1.getRecordID()).thenReturn(1);
        Record record_2 = PowerMockito.mock(Record.class);
        PowerMockito.when(record_2.getRecordID()).thenReturn(2);
        Record record_3 = PowerMockito.mock(Record.class);
        PowerMockito.when(record_3.getRecordID()).thenReturn(3);
        Record record_4 = PowerMockito.mock(Record.class);
        PowerMockito.when(record_4.getRecordID()).thenReturn(4);
        Record record_5 = PowerMockito.mock(Record.class);
        PowerMockito.when(record_5.getRecordID()).thenReturn(5);
        Record record_6 = PowerMockito.mock(Record.class);
        PowerMockito.when(record_6.getRecordID()).thenReturn(6);
        Record record_7 = PowerMockito.mock(Record.class);
        PowerMockito.when(record_7.getRecordID()).thenReturn(7);
        Record record_8 = PowerMockito.mock(Record.class);
        PowerMockito.when(record_8.getRecordID()).thenReturn(8);
        Record record_9 = PowerMockito.mock(Record.class);
        PowerMockito.when(record_9.getRecordID()).thenReturn(9);

        BlockWithData block_1 = new BlockWithData(Lists.newArrayList(record_1, record_2, record_4));
        BlockWithData block_2 = new BlockWithData(Lists.newArrayList(record_1, record_3, record_5));
        BlockWithData block_3 = new BlockWithData(Lists.newArrayList(record_7, record_8));
        BlockWithData block_4 = new BlockWithData(Lists.newArrayList(record_7, record_9));

        List<BlockWithData> blocks = Lists.newArrayList(block_1, block_2, block_3, block_4);
        Whitebox.setInternalState(classUnderTest, "blocks", blocks);

        //execute tested method
        int numberOfVariables = Whitebox.invokeMethod(classUnderTest, "countNumberOfVariables");

        //there are 4 blocks and 2 cliques
        MatcherAssert.assertThat(numberOfVariables, Matchers.is(6));

    }

    @Test
    public void testWriteVariableSizeAndIndecies() throws Exception {
        Multimap<Integer, Integer> multimap = ArrayListMultimap.create();
        multimap.put(1, 0);
        multimap.put(1, 1);
        multimap.putAll(1, Lists.newArrayList(2, 3, 4, 5));
        multimap.put(1, 6);
        multimap.put(2, 0);
        multimap.put(2, 1);
        multimap.put(2, 0);
        multimap.put(2, 2);
        multimap.put(2, 2);
        multimap.put(2, 3);

        String expected = "1 0\n" +
                "1 1\n" +
                "1 2\n" +
                "1 3\n" +
                "1 4\n" +
                "1 5\n" +
                "1 6\n" +
                "2 0 1\n" +
                "2 0 2\n" +
                "2 2 3";
        String sizeAndIndecies = Whitebox.invokeMethod(classUnderTest, "buildStringOfVariableSizeAndIndecies", multimap);
        MatcherAssert.assertThat(sizeAndIndecies, Matchers.equalToIgnoringWhiteSpace(expected));
    }
}