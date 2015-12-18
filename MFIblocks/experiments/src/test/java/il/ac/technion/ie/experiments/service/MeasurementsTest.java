package il.ac.technion.ie.experiments.service;

import com.google.common.collect.*;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;
import il.ac.technion.ie.model.Record;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyList;
import static org.powermock.api.mockito.PowerMockito.*;

public class MeasurementsTest {

    @InjectMocks
    private Measurements classUnderTest;

    @Spy
    private iMeasurService measurService = new MeasurService();


    @Before
    public void setUp() throws Exception {
        classUnderTest = spy(new Measurements(0));
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCalculate_valueExist() {
        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.213);
        assertThat(classUnderTest.getRankedValueByThreshold(0.213), notNullValue());
        assertThat(classUnderTest.getMRRByThreshold(0.213), notNullValue());
    }

    @Test
    public void testCalculate_whenRankedValueThatIsCalculated() throws Exception {
        //mocking
        when(measurService.calcRankedValue(anyList())).thenReturn(0.3);
        when(measurService.calcMRR(anyList())).thenReturn(0.7);

        //execute
        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.213);
        assertThat(classUnderTest.getRankedValueByThreshold(0.213), closeTo(0.3, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.213), closeTo(0.7, 0.0001));
    }

    @Test
    public void testCalculate_calculateTwice() throws Exception {
        //mocking
        when(measurService.calcRankedValue(anyList())).thenReturn(0.3, 0.5);
        when(measurService.calcMRR(anyList())).thenReturn(0.7, 0.6);

        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.213);
        assertThat(classUnderTest.getRankedValueByThreshold(0.213), closeTo(0.3, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.213), closeTo(0.7, 0.0001));


        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.213);
        assertThat(classUnderTest.getRankedValueByThreshold(0.213), closeTo(0.5, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.213), closeTo(0.6, 0.0001));
    }

    @Test
    public void testGetRankedValueByThresholdTwice() throws Exception {
        //mocking
        when(measurService.calcRankedValue(anyList())).thenReturn(0.3, 0.5);
        when(measurService.calcMRR(anyList())).thenReturn(0.7, 0.6);

        //execution
        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.213);
        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.323);

        //assert
        assertThat(classUnderTest.getRankedValueByThreshold(0.323), closeTo(0.5, 0.0001));
        assertThat(classUnderTest.getRankedValueByThreshold(0.213), closeTo(0.3, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.213), closeTo(0.7, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.323), closeTo(0.6, 0.0001));
    }

    @Test
    public void testCalculate_ListIsNull() throws Exception {
        classUnderTest.calculate(null, 0.8);

        //assert
        assertThat(classUnderTest.getRankedValueByThreshold(0.8), closeTo(-1.0, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.8), closeTo(-1.0, 0.0001));
    }

    @Test
    public void testGetAllRankedValues() throws Exception {
        UniformRealDistribution realDistribution = new UniformRealDistribution(0.0, 1.0);
        UniformIntegerDistribution uniformIntegerDistribution = new UniformIntegerDistribution(4, 14);

        int sizeOfResults = uniformIntegerDistribution.sample();
        Map<Double, Double> map = new HashMap<>();

        for (int i = 0; i < sizeOfResults; i++) {
            map.put(realDistribution.sample(), realDistribution.sample());
        }

        //mocking
        final Iterator<Map.Entry<Double, Double>> iterator = map.entrySet().iterator();
        when(measurService.calcRankedValue(anyList())).thenAnswer(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock invocation) throws Throwable {
                return iterator.next().getValue();
            }
        });

        //execution
        for (Map.Entry<Double, Double> doubleDoubleEntry : map.entrySet()) {
            classUnderTest.calculate(new ArrayList<BlockWithData>(), doubleDoubleEntry.getKey());
        }

        //assert
        assertThat(classUnderTest.getRankedValuesSortedByThreshold(), hasSize(sizeOfResults));
        TreeMap<Double, Double> doubleDoubleTreeMap = new TreeMap<>(map);
        assertThat(classUnderTest.getRankedValuesSortedByThreshold(), contains(doubleDoubleTreeMap.values().toArray()));
    }

    @Test
    public void testGetAllMRRValues() throws Exception {
        //mocking
        Map<Double, Double> map = Maps.newHashMap(ImmutableMap.<Double, Double>builder().
                put(0.56, 0.17). //  <Threshold, value>
                put(0.14, 0.8).
                put(0.33, 0.15).
                build());

        final Iterator<Map.Entry<Double, Double>> iterator = map.entrySet().iterator();
        when(measurService.calcMRR(anyList())).thenAnswer(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock invocation) throws Throwable {
                return iterator.next().getValue();
            }
        });

        //execution
        for (Map.Entry<Double, Double> doubleDoubleEntry : map.entrySet()) {
            classUnderTest.calculate(new ArrayList<BlockWithData>(), doubleDoubleEntry.getKey());
        }

        //assert
        assertThat(classUnderTest.getMrrValuesSortedByThreshold(), hasSize(map.size()));
        TreeMap<Double, Double> doubleDoubleTreeMap = new TreeMap<>(map);
        assertThat(classUnderTest.getMrrValuesSortedByThreshold(), contains(doubleDoubleTreeMap.values().toArray()));
    }

    @Test
    public void testGetThresholdsSorted() throws Exception {
        //execute
        UniformRealDistribution realDistribution = new UniformRealDistribution(0.0, 1.0);
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            double threshold = realDistribution.sample();
            classUnderTest.calculate(new ArrayList<BlockWithData>(), threshold);
            list.add(threshold);
        }

        //assert
        Collections.sort(list);
        assertThat(classUnderTest.getThresholdSorted(), contains(list.toArray()));
    }

    @Test
    public void testGetAllNormalizedMRRValues() throws Exception {
        //mocking
        Map<Double, Double> map = Maps.newHashMap(ImmutableMap.<Double, Double>builder().
                put(0.56, 0.18). //  <Threshold, value>
                put(0.14, 0.8).
                put(0.33, 0.15).
                put(0.17, 0.4).
                build());

        final Iterator<Map.Entry<Double, Double>> iterator = map.entrySet().iterator();
        when(measurService.calcMRR(anyList())).thenAnswer(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock invocation) throws Throwable {
                return iterator.next().getValue();
            }
        });

        List blocksWithData = mock(List.class);
        when(blocksWithData.size()).thenReturn(map.size());
        when(blocksWithData.iterator()).thenReturn(mock(Iterator.class));

        //execution
        Whitebox.setInternalState(classUnderTest, "numberOfOriginalBlocks", 2);
        for (Map.Entry<Double, Double> doubleDoubleEntry : map.entrySet()) {
            classUnderTest.calculate(blocksWithData, doubleDoubleEntry.getKey());
        }

        //assert
        assertThat(classUnderTest.getNormalizedMRRValuesSortedByThreshold(), hasSize(map.size()));
        List<Double> expectedNormalizedMRRValues = Lists.newArrayList(0.4, 0.2, 0.075, 0.09);
        assertThat(classUnderTest.getNormalizedMRRValuesSortedByThreshold(), contains(expectedNormalizedMRRValues.toArray()));
    }

    @Test
    public void testGetAllNormalizedRankedValues() throws Exception {
        //mocking
        Map<Double, Double> map = Maps.newHashMap(ImmutableMap.<Double, Double>builder().
                put(0.56, 0.18). //  <Threshold, value>
                put(0.33, 0.15).
                put(0.0, 0.8).
                put(0.17, 0.4).
                build());

        final Iterator<Map.Entry<Double, Double>> iterator = map.entrySet().iterator();
        when(measurService.calcRankedValue(anyList())).thenAnswer(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock invocation) throws Throwable {
                return iterator.next().getValue();
            }
        });

        List blocksWithData = mock(List.class);
        when(blocksWithData.size()).thenReturn(map.size());
        when(blocksWithData.iterator()).thenReturn(mock(Iterator.class));

        //execution
        Whitebox.setInternalState(classUnderTest, "numberOfOriginalBlocks", 2);
        for (Map.Entry<Double, Double> doubleDoubleEntry : map.entrySet()) {
            classUnderTest.calculate(blocksWithData, doubleDoubleEntry.getKey());
        }

        //assert
        assertThat(classUnderTest.getNormalizedRankedValuesSortedByThreshold(), hasSize(map.size()));
        List<Double> expectedNormalizedMRRValues = Lists.newArrayList(0.4, 0.2, 0.075, 0.09);
        assertThat(classUnderTest.getNormalizedRankedValuesSortedByThreshold(), contains(expectedNormalizedMRRValues.toArray()));
    }

    @Test
    public void testGetAverageRankedValue_threeValues() throws Exception {
        //mocking
        doReturn(0.3).doReturn(0.6).doReturn(0.9).when(measurService).calcRankedValue(anyList());
        suppress(methods(classUnderTest.getClass(), "calcMRR"));

        double threshold = 0.4;
        classUnderTest.calculate(new ArrayList<BlockWithData>(), threshold);
        classUnderTest.calculate(new ArrayList<BlockWithData>(), threshold);
        classUnderTest.calculate(new ArrayList<BlockWithData>(), threshold);

        double averageRankedValue = Whitebox.invokeMethod(classUnderTest, "getAverageRankedValue", threshold);
        assertThat(averageRankedValue, closeTo(0.6, 0.00001));
    }

    @Test
    public void testGetAverageMRR_randomValues() throws Exception {
        //mocking
        doReturn(0.2).doReturn(0.7).doReturn(0.3).when(measurService).calcMRR(anyList());
        suppress(methods(classUnderTest.getClass(), "calcRankedValue"));

        double threshold = 0.2;
        classUnderTest.calculate(new ArrayList<BlockWithData>(), threshold);
        classUnderTest.calculate(new ArrayList<BlockWithData>(), threshold);
        classUnderTest.calculate(new ArrayList<BlockWithData>(), threshold);

        double averageMRR = Whitebox.invokeMethod(classUnderTest, "getAverageMRR", threshold);
        assertThat(averageMRR, closeTo(0.4, 0.00001));
    }

    @Test
    public void testRepresentativesDuplicateElimanation_dupRemoved() throws Exception {
        Record representative = mock(Record.class);
        Multimap duplicates = ArrayListMultimap.create();
        Multimap cleaned = ArrayListMultimap.create();

        duplicates.putAll(representative, Lists.newArrayList(
                mock(BlockWithData.class), mock(BlockWithData.class), mock(BlockWithData.class)));
        cleaned.putAll(representative, Lists.newArrayList(mock(BlockWithData.class), mock(BlockWithData.class)));

        //execute
        DuplicateReductionContext reductionContext = classUnderTest.representativesDuplicateElimanation(duplicates, cleaned, 1);

        //assert
        assertThat(reductionContext.getDuplicatesRemoved(), is(1));
        assertThat((double) reductionContext.getDupReductionPercentage(), closeTo(33.333333, 0.01));
        assertThat((double) reductionContext.getImprovementPercentage(), closeTo(100, 0.01));
    }

    @Test
    public void testRepresentativesDuplicateElimanation_noDupRemoved() throws Exception {
        Record representative = mock(Record.class);
        Multimap duplicates = ArrayListMultimap.create();
        Multimap cleaned = ArrayListMultimap.create();

        duplicates.putAll(representative, Lists.newArrayList(
                mock(BlockWithData.class), mock(BlockWithData.class)));
        cleaned.putAll(representative, Lists.newArrayList(mock(BlockWithData.class), mock(BlockWithData.class)));

        //execute
        DuplicateReductionContext reductionContext = classUnderTest.representativesDuplicateElimanation(duplicates, cleaned, 1);

        //assert
        assertThat(reductionContext.getDuplicatesRemoved(), is(0));
        assertThat((double) reductionContext.getDupReductionPercentage(), closeTo(0, 0.01));
        assertThat((double) reductionContext.getImprovementPercentage(), closeTo(0, 0.01));
    }
}