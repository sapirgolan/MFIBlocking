package il.ac.technion.ie.experiments.service;

import com.google.common.collect.*;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.utils.Logging;
import il.ac.technion.ie.utils.UtilitiesForBlocksAndRecords;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
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

    @Rule
    public Logging logging = new Logging();

    @InjectMocks
    private Measurements classUnderTest;

    @Spy
    private iMeasurService measurService = new MeasurService();
    private static List<Record> recordsFromCsv;

    @BeforeClass
    public static void prepareClass() throws Exception {
        recordsFromCsv = UtilitiesForBlocksAndRecords.getRecordsFromCsv();
    }

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
        Multimap miller = ArrayListMultimap.create();
        Multimap convexBP = ArrayListMultimap.create();

        miller.putAll(representative, Lists.newArrayList(
                mock(BlockWithData.class), mock(BlockWithData.class), mock(BlockWithData.class)));
        convexBP.putAll(representative, Lists.newArrayList(mock(BlockWithData.class), mock(BlockWithData.class)));

        //execute
        DuplicateReductionContext reductionContext = classUnderTest.representativesDuplicateElimination(miller, convexBP);

        //assert
        assertThat((double) reductionContext.getDuplicatesRemoved(), closeTo(1.0, 0.001));
    }

    @Test
    public void testRepresentativesDuplicateElimanation_noDupRemoved() throws Exception {
        Record representative = mock(Record.class);
        Multimap miller = ArrayListMultimap.create();
        Multimap convexBP = ArrayListMultimap.create();

        miller.putAll(representative, Lists.newArrayList(
                mock(BlockWithData.class), mock(BlockWithData.class)));
        convexBP.putAll(representative, Lists.newArrayList(mock(BlockWithData.class), mock(BlockWithData.class)));

        //execute
        DuplicateReductionContext reductionContext = classUnderTest.representativesDuplicateElimination(miller, convexBP);

        //assert
        assertThat((double) reductionContext.getDuplicatesRemoved(), closeTo(0, 0.01));
    }

    @Test
    public void testRepresentationDiff() throws Exception {
        Set<Record> source = new HashSet<>();
        Set<Record> other = new HashSet<>();
        //create common records
        Set<Record> commonRecords = new HashSet<>();
        createMockRecords(commonRecords, 5);

        //create onlySourceRecords records
        createMockRecords(source, 3);
        source.addAll(commonRecords);

        //create onlyOtherRecords
        other.addAll(commonRecords);
        createMockRecords(other, 1);

        DuplicateReductionContext reductionContext = new DuplicateReductionContext(0);

        //execution
        classUnderTest.representationDiff(source, other, reductionContext);

        //assertion
        assertThat(reductionContext.getRepresentationDiff(), closeTo(3.0 / 8, 0.01));
    }

    private void createMockRecords(Set<Record> set, int numberOfRecords) {
        for (int i = 0; i < numberOfRecords; i++) {
            set.add(mock(Record.class));
        }
    }

    @Test
    public void testCalcPowerOfRep() throws Exception {
        Record trueRep = recordsFromCsv.get(3);
        Map<Record, BlockWithData> trueRepsMap = new HashMap<>();
        trueRepsMap.put(trueRep, new BlockWithData(recordsFromCsv.subList(0, 4)));

        Multimap<Record, BlockWithData> convexBPRepresentatives = ArrayListMultimap.create(2, 4);
        convexBPRepresentatives.put(trueRep, new BlockWithData(recordsFromCsv.subList(2, 7)));
        convexBPRepresentatives.put(trueRep, new BlockWithData(recordsFromCsv.subList(3, 6)));
        convexBPRepresentatives.put(recordsFromCsv.get(8), new BlockWithData(recordsFromCsv.subList(4, 9)));

        DuplicateReductionContext reductionContext = new DuplicateReductionContext(0);

        classUnderTest.calcPowerOfRep(trueRepsMap, convexBPRepresentatives, reductionContext);

        assertThat(logging.getAllLogsAbove(Level.WARN), empty());
        assertThat(reductionContext.getRepresentativesPower(), closeTo(0.366666667, 0.0001));
    }

    @Test
    public void testCalcPowerOfRep_oneTrueRepIsNotRep() throws Exception {
        Record trueRep = recordsFromCsv.get(3);
        Map<Record, BlockWithData> trueRepsMap = new HashMap<>();
        trueRepsMap.put(trueRep, new BlockWithData(recordsFromCsv.subList(0, 4)));

        Multimap<Record, BlockWithData> convexBPRepresentatives = ArrayListMultimap.create(2, 4);
        BlockWithData block = new BlockWithData(recordsFromCsv.subList(2, 7));
        Whitebox.setInternalState(block, "trueRepresentative", recordsFromCsv.get(6));
        convexBPRepresentatives.put(recordsFromCsv.get(6), block);
        convexBPRepresentatives.put(trueRep, new BlockWithData(recordsFromCsv.subList(3, 6)));
        convexBPRepresentatives.put(recordsFromCsv.get(8), new BlockWithData(recordsFromCsv.subList(4, 9)));

        DuplicateReductionContext reductionContext = new DuplicateReductionContext(0);

        classUnderTest.calcPowerOfRep(trueRepsMap, convexBPRepresentatives, reductionContext);

        assertThat(logging.getAllLogsAbove(Level.WARN), empty());
        assertThat(reductionContext.getRepresentativesPower(), closeTo(0.333333, 0.0001));
    }

    @Test
    public void testCalcPowerOfRep_TwoCleanBlocksOneTrueRepIsNotRep() throws Exception {
        Record BlockZeroTrueRep = recordsFromCsv.get(3);
        Record BlockOneTrueRep = recordsFromCsv.get(7);
        Map<Record, BlockWithData> trueRepsMap = new HashMap<>();
        trueRepsMap.put(BlockZeroTrueRep, new BlockWithData(recordsFromCsv.subList(0, 4)));
        trueRepsMap.put(BlockOneTrueRep, new BlockWithData(recordsFromCsv.subList(4, 8)));

        Multimap<Record, BlockWithData> convexBPRepresentatives = ArrayListMultimap.create(2, 4);
        convexBPRepresentatives.put(BlockZeroTrueRep, new BlockWithData(recordsFromCsv.subList(2, 7)));
        convexBPRepresentatives.put(BlockZeroTrueRep, new BlockWithData(recordsFromCsv.subList(3, 6)));
        convexBPRepresentatives.put(recordsFromCsv.get(5), new BlockWithData(recordsFromCsv.subList(3, 7)));
        convexBPRepresentatives.put(recordsFromCsv.get(7), new BlockWithData(recordsFromCsv.subList(4, 9)));

        DuplicateReductionContext reductionContext = new DuplicateReductionContext(0);

        classUnderTest.calcPowerOfRep(trueRepsMap, convexBPRepresentatives, reductionContext);

        assertThat(logging.getAllLogsAbove(Level.WARN), empty());
        assertThat(reductionContext.getRepresentativesPower(), closeTo(0.58333333, 0.0001));
    }

    @Test
    public void calcWisdomCrowdsSingleCleanBlock() throws Exception {
        //prepare
        BlockWithData dirtyBlockOne = new BlockWithData(recordsFromCsv.subList(2, 4));
        dirtyBlockOne.setMemberProbability(recordsFromCsv.get(2), (float) 0.6);
        dirtyBlockOne.setMemberProbability(recordsFromCsv.get(3), (float) 0.4);

        BlockWithData dirtyBlockTwo = new BlockWithData(recordsFromCsv.subList(1, 5));
        dirtyBlockTwo.setMemberProbability(recordsFromCsv.get(1), (float) 0.1);
        dirtyBlockTwo.setMemberProbability(recordsFromCsv.get(2), (float) 0.2);
        dirtyBlockTwo.setMemberProbability(recordsFromCsv.get(3), (float) 0.4);
        dirtyBlockTwo.setMemberProbability(recordsFromCsv.get(4), (float) 0.3);


        BlockWithData dirtyBlockThree = new BlockWithData(recordsFromCsv.subList(5, 7));
        dirtyBlockThree.setMemberProbability(recordsFromCsv.get(5), (float) 0.4);
        dirtyBlockThree.setMemberProbability(recordsFromCsv.get(6), (float) 0.4);

        Set<BlockWithData> dirtyBlocks = Sets.newHashSet(dirtyBlockThree, dirtyBlockOne, dirtyBlockTwo);
        //invoke findBlockRepresentatives() so that representatives will be known afterwards
        for (BlockWithData dirtyBlock : dirtyBlocks) {
            dirtyBlock.findBlockRepresentatives();
        }

        //execution
        double wisdomCrowds = classUnderTest.calcWisdomCrowds(getCleanBlocks(), dirtyBlocks, new DuplicateReductionContext(0));

        //assertion
        assertThat(wisdomCrowds, closeTo(0.25, 0.0001));
    }

    private final Set<BlockWithData> getCleanBlocks() {
        BlockWithData blockOne = new BlockWithData(recordsFromCsv.subList(0, 4));
        BlockWithData blockTwo = new BlockWithData(recordsFromCsv.subList(4, 8));
        BlockWithData blockThree = new BlockWithData(recordsFromCsv.subList(8, 13));
        BlockWithData blockFour = new BlockWithData(recordsFromCsv.subList(13, 20));
        return Collections.unmodifiableSet(Sets.newHashSet(blockOne, blockTwo, blockThree, blockFour));
    }

    @Test
    public void calcWisdomCrowdsSingleCleanBlockWithoutRealRepresentative() throws Exception {
        //prepare
        BlockWithData dirtyBlockOne = new BlockWithData(recordsFromCsv.subList(2, 4));
        dirtyBlockOne.setMemberProbability(recordsFromCsv.get(2), (float) 0.4);
        dirtyBlockOne.setMemberProbability(recordsFromCsv.get(3), (float) 0.6);

        BlockWithData dirtyBlockTwo = new BlockWithData(recordsFromCsv.subList(0, 3));
        dirtyBlockTwo.setMemberProbability(recordsFromCsv.get(0), (float) 0.2);
        dirtyBlockTwo.setMemberProbability(recordsFromCsv.get(1), (float) 0.3);
        dirtyBlockOne.setMemberProbability(recordsFromCsv.get(2), (float) 0.5);

        BlockWithData dirtyBlockThree = new BlockWithData(recordsFromCsv.subList(5, 7));
        dirtyBlockThree.setMemberProbability(recordsFromCsv.get(5), (float) 0.2);
        dirtyBlockThree.setMemberProbability(recordsFromCsv.get(6), (float) 0.8);

        HashSet<BlockWithData> dirtyBlocks = Sets.newHashSet(dirtyBlockThree, dirtyBlockOne, dirtyBlockTwo);
        //invoke findBlockRepresentatives() so that representatives will be known afterwards
        for (BlockWithData dirtyBlock : dirtyBlocks) {
            dirtyBlock.findBlockRepresentatives();
        }

        //execution
        double wisdomCrowds = classUnderTest.calcWisdomCrowds(getCleanBlocks(), dirtyBlocks, new DuplicateReductionContext(0));

        //assertion
        assertThat(wisdomCrowds, closeTo(0.0, 0.0001));
    }

    @Test
    public void initRecordToBlockMap_sameKeyExistsSeveralTimes() throws Exception {
        //prepare
        BlockWithData blockOne = new BlockWithData(recordsFromCsv.subList(0, 4));
        BlockWithData blockTwo = new BlockWithData(recordsFromCsv.subList(4, 8));
        BlockWithData blockThree = new BlockWithData(recordsFromCsv.subList(8, 12));
        BlockWithData blockFour = new BlockWithData(recordsFromCsv.subList(12, 20));
        BlockWithData blockFive = new BlockWithData(recordsFromCsv.subList(6, 10));

        //execute
        Map<Record, BlockWithData> recordToBlockMap = Whitebox.invokeMethod(classUnderTest, "initRecordToBlockMap",
                Sets.newHashSet(blockOne, blockTwo, blockThree, blockFour, blockFive));

        //assert
        assertThat(recordToBlockMap.keySet(), hasSize(20));
        assertThat(recordToBlockMap.keySet(), containsInAnyOrder(recordsFromCsv.toArray(new Record[20])));
        assertThat(recordToBlockMap.values(), hasSize(20));
    }

    @Test
    public void getBlockWithMostRecordsIn() throws Exception {
        //prepare
        BlockWithData dirtyBlock = new BlockWithData(recordsFromCsv.subList(2, 7));
        BlockWithData cleanBlockA = new BlockWithData(recordsFromCsv.subList(0, 4));
        BlockWithData cleanBlockB = new BlockWithData(recordsFromCsv.subList(4, 8));
        Map<Record, BlockWithData> recordToBlockMap = Whitebox.invokeMethod(classUnderTest, "initRecordToBlockMap", Sets.newHashSet(cleanBlockA, cleanBlockB));

        //execution
        Map<BlockWithData, Integer> numberOfRecordsInEachCleanBlock = Whitebox.invokeMethod(classUnderTest, "getNumberOfRecordsInEachCleanBlock", dirtyBlock, recordToBlockMap);

        //assertion
        assertThat(numberOfRecordsInEachCleanBlock.get(cleanBlockA), is(2));
        assertThat(numberOfRecordsInEachCleanBlock.get(cleanBlockB), is(3));
        assertThat(numberOfRecordsInEachCleanBlock.keySet(), containsInAnyOrder(cleanBlockA, cleanBlockB));
    }

    @Test
    public void getBlockWithMostRecordsIn_ThereAreTwoBlocks() throws Exception {
        //prepare
        BlockWithData dirtyBlock = new BlockWithData(recordsFromCsv.subList(2, 6));
        BlockWithData cleanBlockA = new BlockWithData(recordsFromCsv.subList(0, 4));
        BlockWithData cleanBlockB = new BlockWithData(recordsFromCsv.subList(4, 8));
        Map<Record, BlockWithData> recordToBlockMap = Whitebox.invokeMethod(classUnderTest, "initRecordToBlockMap", Sets.newHashSet(cleanBlockA, cleanBlockB));

        //execution
        Map<BlockWithData, Integer> numberOfRecordsInEachCleanBlock = Whitebox.invokeMethod(classUnderTest, "getNumberOfRecordsInEachCleanBlock", dirtyBlock, recordToBlockMap);

        //assertion
        //assertion
        assertThat(numberOfRecordsInEachCleanBlock.get(cleanBlockA), is(2));
        assertThat(numberOfRecordsInEachCleanBlock.get(cleanBlockB), is(2));
        assertThat(numberOfRecordsInEachCleanBlock.keySet(), containsInAnyOrder(cleanBlockA, cleanBlockB));
    }

    @Test
    public void testUpdateGlobalCountersUpdateAllCounters() throws Exception {
        //prepare
        BlockWithData cleanBlockA = new BlockWithData(recordsFromCsv.subList(0, 4));
        BlockWithData cleanBlockB = new BlockWithData(recordsFromCsv.subList(4, 8));
        BlockWithData dirtyBlock = new BlockWithData(recordsFromCsv.subList(2, 6));

        Multimap<BlockWithData, Measurements.BlockCounter> globalBlockCounters = ArrayListMultimap.create();
        globalBlockCounters.put(cleanBlockA, classUnderTest.new BlockCounter(mock(BlockWithData.class), 1));

        Map<BlockWithData, Integer> localBlockCounters = Maps.newHashMap(ImmutableMap.of(cleanBlockA, 2, cleanBlockB, 2));

        //execute
        Whitebox.invokeMethod(classUnderTest, "updateGlobalCounters", dirtyBlock, localBlockCounters, globalBlockCounters);

        //assertion
        assertThat(globalBlockCounters.keySet(), containsInAnyOrder(cleanBlockA, cleanBlockB));

        assertThat(globalBlockCounters.get(cleanBlockA), hasSize(1));
        Measurements.BlockCounter blockCounterA = globalBlockCounters.get(cleanBlockA).iterator().next();
        assertThat(blockCounterA.getCounter(), is(2));
        assertThat(blockCounterA.getBlock(), is(dirtyBlock));

        assertThat(globalBlockCounters.get(cleanBlockB), hasSize(1));
        Measurements.BlockCounter blockCounterB = globalBlockCounters.get(cleanBlockB).iterator().next();
        assertThat(blockCounterB.getCounter(), is(2));
        assertThat(blockCounterB.getBlock(), is(dirtyBlock));
    }

    @Test
    public void testUpdateGlobalCountersDontUpdateOneConter() throws Exception {
        //prepare
        BlockWithData cleanBlockA = new BlockWithData(recordsFromCsv.subList(0, 4));
        BlockWithData cleanBlockB = new BlockWithData(recordsFromCsv.subList(4, 8));
        BlockWithData dirtyBlock = new BlockWithData(recordsFromCsv.subList(2, 6));
        BlockWithData mock = mock(BlockWithData.class);

        Multimap<BlockWithData, Measurements.BlockCounter> globalBlockCounters = ArrayListMultimap.create();
        globalBlockCounters.put(cleanBlockA, classUnderTest.new BlockCounter(mock, 5));

        Map<BlockWithData, Integer> localBlockCounters = Maps.newHashMap(ImmutableMap.of(cleanBlockA, 2, cleanBlockB, 2));

        //execute
        Whitebox.invokeMethod(classUnderTest, "updateGlobalCounters", dirtyBlock, localBlockCounters, globalBlockCounters);

        //assertion
        assertThat(globalBlockCounters.keySet(), containsInAnyOrder(cleanBlockA, cleanBlockB));
        assertThat(globalBlockCounters.get(cleanBlockA), hasSize(1));
        Measurements.BlockCounter blockCounterA = globalBlockCounters.get(cleanBlockA).iterator().next();
        assertThat(blockCounterA.getCounter(), is(5));
        assertThat(blockCounterA.getBlock(), is(mock));

        assertThat(globalBlockCounters.get(cleanBlockB), hasSize(1));
        Measurements.BlockCounter blockCounterB = globalBlockCounters.get(cleanBlockB).iterator().next();
        assertThat(blockCounterB.getCounter(), is(2));
        assertThat(blockCounterB.getBlock(), is(dirtyBlock));
    }


    @Test
    public void testUpdateGlobalCountersHasTwoDirtyBlockSameCount() throws Exception {
        //prepare
        BlockWithData cleanBlockA = new BlockWithData(recordsFromCsv.subList(0, 4));
        BlockWithData cleanBlockB = new BlockWithData(recordsFromCsv.subList(4, 8));
        BlockWithData dirtyBlockA = new BlockWithData(recordsFromCsv.subList(2, 6));
        BlockWithData dirtyBlockB = mock(BlockWithData.class);

        Multimap<BlockWithData, Measurements.BlockCounter> globalBlockCounters = ArrayListMultimap.create();
        globalBlockCounters.put(cleanBlockA, classUnderTest.new BlockCounter(mock(BlockWithData.class), 1));

        Map<BlockWithData, Integer> localBlockCounters = Maps.newHashMap(ImmutableMap.of(cleanBlockA, 2, cleanBlockB, 2));

        //execute
        Whitebox.invokeMethod(classUnderTest, "updateGlobalCounters", dirtyBlockA, localBlockCounters, globalBlockCounters);
        localBlockCounters = Maps.newHashMap(ImmutableMap.of(cleanBlockA, 2, cleanBlockB, 1));
        Whitebox.invokeMethod(classUnderTest, "updateGlobalCounters", dirtyBlockB, localBlockCounters, globalBlockCounters);

        //assertion
        assertThat(globalBlockCounters.keySet(), containsInAnyOrder(cleanBlockA, cleanBlockB));

        //assert there are two BlockCounters associated with cleanBlockA
        assertThat(globalBlockCounters.get(cleanBlockA), hasSize(2));

        Iterator<Measurements.BlockCounter> blockCounterAIterator = globalBlockCounters.get(cleanBlockA).iterator();
        //assertion on the first blockCounter that should contain dirtyBlockA
        Measurements.BlockCounter blockCounterA = blockCounterAIterator.next();
        assertThat(blockCounterA.getBlock(), is(dirtyBlockA));
        assertThat(blockCounterA.getCounter(), is(2));
        //assertion on the first blockCounter that should contain dirtyBlockB
        blockCounterA = blockCounterAIterator.next();
        assertThat(blockCounterA.getBlock(), is(dirtyBlockB));
        assertThat(blockCounterA.getCounter(), is(2));

        assertThat(globalBlockCounters.get(cleanBlockB), hasSize(1));
        Measurements.BlockCounter blockCounterB = globalBlockCounters.get(cleanBlockB).iterator().next();
        assertThat(blockCounterB.getCounter(), is(2));
        assertThat(blockCounterB.getBlock(), is(dirtyBlockA));
    }

    @Test
    public void isTrueRepIdenticalToDirtyBlockRep_yes() throws Exception {
        //prepare
        Multimap<BlockWithData, Measurements.BlockCounter> globalBlockCounters = ArrayListMultimap.create();
        BlockWithData cleanBlock = mock(BlockWithData.class);
        Record realRep = mock(Record.class);
        when(cleanBlock.getTrueRepresentative()).thenReturn(realRep);

        BlockWithData dirtyBlock = mock(BlockWithData.class);
        when(dirtyBlock.findBlockRepresentatives()).thenReturn(Maps.newHashMap(ImmutableMap.of(realRep, (float) 0.2)));
        globalBlockCounters.put(cleanBlock, classUnderTest.new BlockCounter(dirtyBlock, 4));

        //execution
        boolean result = Whitebox.invokeMethod(classUnderTest, "isTrueRepIdenticalToDirtyBlockRep", globalBlockCounters, cleanBlock);

        //assertion
        assertThat(result, is(true));
    }

    @Test
    public void isTrueRepIdenticalToDirtyBlockRep_dirtyBlockHasSeveralReps() throws Exception {
        //prepare
        Multimap<BlockWithData, Measurements.BlockCounter> globalBlockCounters = ArrayListMultimap.create();
        BlockWithData cleanBlock = mock(BlockWithData.class);
        Record realRep = mock(Record.class);
        when(cleanBlock.getTrueRepresentative()).thenReturn(realRep);

        BlockWithData dirtyBlock = mock(BlockWithData.class);
        Map<Record, Float> dirtyRepresentatives = Maps.newHashMap(ImmutableMap.of(mock(Record.class), (float) 0.2, realRep, (float) 0.2));
        when(dirtyBlock.findBlockRepresentatives()).thenReturn(dirtyRepresentatives);
        globalBlockCounters.put(cleanBlock, classUnderTest.new BlockCounter(dirtyBlock, 4));

        //execution
        boolean result = Whitebox.invokeMethod(classUnderTest, "isTrueRepIdenticalToDirtyBlockRep", globalBlockCounters, cleanBlock);

        //assertion
        assertThat(result, is(true));
    }

    @Test
    public void isTrueRepIdenticalToDirtyBlockRep_dirtyBlockNotContaingTrueRep() throws Exception {
        //prepare
        Multimap<BlockWithData, Measurements.BlockCounter> globalBlockCounters = ArrayListMultimap.create();
        BlockWithData cleanBlock = mock(BlockWithData.class);
        Record realRep = mock(Record.class);
        when(cleanBlock.getTrueRepresentative()).thenReturn(realRep);

        BlockWithData dirtyBlock = mock(BlockWithData.class);
        globalBlockCounters.put(cleanBlock, classUnderTest.new BlockCounter(dirtyBlock, 4));

        //execution
        boolean result = Whitebox.invokeMethod(classUnderTest, "isTrueRepIdenticalToDirtyBlockRep", globalBlockCounters, cleanBlock);

        //assertion
        assertThat(result, is(false));
    }

    @Test
    public void isTrueRepIdenticalToDirtyBlockRep_globalBlockCountersContainsSeveralDirtyBlocks() throws Exception {
        //prepare
        Multimap<BlockWithData, Measurements.BlockCounter> globalBlockCounters = ArrayListMultimap.create();
        BlockWithData cleanBlock = mock(BlockWithData.class);
        Record realRep = mock(Record.class);
        when(cleanBlock.getTrueRepresentative()).thenReturn(realRep);

        BlockWithData dirtyBlock = mock(BlockWithData.class);
        BlockWithData dirtyBlockWithRep = mock(BlockWithData.class);
        Map<Record, Float> dirtyRepresentatives = Maps.newHashMap(ImmutableMap.of(mock(Record.class), (float) 0.2, realRep, (float) 0.2));
        when(dirtyBlockWithRep.findBlockRepresentatives()).thenReturn(dirtyRepresentatives);


        globalBlockCounters.put(cleanBlock, classUnderTest.new BlockCounter(dirtyBlock, 5));
        globalBlockCounters.put(cleanBlock, classUnderTest.new BlockCounter(dirtyBlockWithRep, 5));

        //execution
        boolean result = Whitebox.invokeMethod(classUnderTest, "isTrueRepIdenticalToDirtyBlockRep", globalBlockCounters, cleanBlock);

        //assertion
        assertThat(result, is(true));
    }

    @Test
    public void duplicatesRealRepresentatives() throws Exception {
        //prepare
        List<Record> records = generateRecords(10);
        List<BlockWithData> blocks = generateBlocks(10);
        Multimap<Record, BlockWithData> duplicates = ArrayListMultimap.create();
        Multimap<Record, BlockWithData> cleaned = ArrayListMultimap.create();
        BiMap<Record, BlockWithData> trueReps = HashBiMap.create();

        duplicates.putAll(records.get(0), Lists.newArrayList(blocks.get(0), blocks.get(1)));
        duplicates.putAll(records.get(1), Lists.newArrayList(blocks.get(1)));
        duplicates.putAll(records.get(2), Lists.newArrayList(blocks.get(1), blocks.get(2), blocks.get(0)));
        duplicates.putAll(records.get(3), Lists.newArrayList(blocks.get(3), blocks.get(4), blocks.get(5)));
        duplicates.putAll(records.get(9), Lists.newArrayList(blocks.get(6), blocks.get(7)));

        cleaned.putAll(records.get(0), Lists.newArrayList(blocks.get(0))); //fixed
        cleaned.putAll(records.get(1), Lists.newArrayList(blocks.get(1)));
        cleaned.putAll(records.get(2), Lists.newArrayList(blocks.get(1), blocks.get(2))); // didn't entirely fix
        cleaned.putAll(records.get(3), Lists.newArrayList(blocks.get(3), blocks.get(4), blocks.get(5))); // didn't fix
        cleaned.putAll(records.get(9), Lists.newArrayList(blocks.get(6))); // not real representative

        trueReps.put(records.get(0), blocks.get(0));
        trueReps.put(records.get(1), blocks.get(1));
        trueReps.put(records.get(2), blocks.get(2));
        trueReps.put(records.get(3), blocks.get(3));

        //execute
        double duplicatesRealRepresentatives = classUnderTest.duplicatesRealRepresentatives(duplicates, cleaned, trueReps);

        //assert
        assertThat(duplicatesRealRepresentatives, closeTo(0.5, 0.00001));
    }

    private List<BlockWithData> generateBlocks(int size) {
        List<BlockWithData> records = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            records.add(mock(BlockWithData.class));
        }
        return records;
    }

    private List<Record> generateRecords(int size) {
        List<Record> records = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            records.add(mock(Record.class));
        }
        return records;
    }
}