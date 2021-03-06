package il.ac.technion.ie.experiments.service;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.experimentRunners.FilesReader;
import il.ac.technion.ie.experiments.experimentRunners.ProcessBlocks;
import il.ac.technion.ie.experiments.model.BlockPair;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.BlocksMapper;
import il.ac.technion.ie.experiments.parsers.SerializerUtil;
import il.ac.technion.ie.experiments.util.ZipExtractor;
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
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Measurements.class)
public class MeasurementsTest {

    private static final String DATASET_PERMUTATION_NAME = "25_75_5_5_16_uniform_all_0_parameter=25.csv";
    @Rule
    public Logging logging = new Logging();
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @InjectMocks
    private Measurements classUnderTest = new Measurements(0);

    @Spy
    private iMeasurService measurService = new MeasurService();
    private static List<Record> recordsFromCsv;

    @BeforeClass
    public static void prepareClass() throws Exception {
        recordsFromCsv = UtilitiesForBlocksAndRecords.getRecordsFromCsv();
    }

    @Before
    public void setUp_test() throws Exception {
//        recordsFromCsv = UtilitiesForBlocksAndRecords.getRecordsFromCsv();
        classUnderTest = spy(classUnderTest);
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
        when(measurService.calcRankedValue(anyList())).thenReturn(0.3, 0.6, 0.9);
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
        when(measurService.calcMRR(anyList())).thenReturn(0.2, 0.7, 0.3);
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
        /*
        * There are common 3 records.
        * 5 records from "source" are not present in "other"
        * */
        //
        Set<Record> source = new HashSet<>(recordsFromCsv.subList(0, 8));
        Set<Record> other = new HashSet<>(recordsFromCsv.subList(5, 8));

        //execution
        int missingRealRepresentatives = classUnderTest.missingRealRepresentatives(source, other);

        //assertion
        assertThat(missingRealRepresentatives, is(5));
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

        classUnderTest.calcPowerOfRep_Recall(trueRepsMap, convexBPRepresentatives, reductionContext);

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

        classUnderTest.calcPowerOfRep_Recall(trueRepsMap, convexBPRepresentatives, reductionContext);

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

        classUnderTest.calcPowerOfRep_Recall(trueRepsMap, convexBPRepresentatives, reductionContext);

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
        double wisdomCrowds = classUnderTest.calcWisdomCrowd_Precision(getCleanBlocks(), dirtyBlocks, new DuplicateReductionContext(0));

        //assertion
        assertThat(wisdomCrowds, closeTo(0.25, 0.0001));
    }

    private Set<BlockWithData> getCleanBlocks() {
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
        double wisdomCrowds = classUnderTest.calcWisdomCrowd_Precision(getCleanBlocks(), dirtyBlocks, new DuplicateReductionContext(0));

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

    @Test
    public void duplicatesRealRepresentatives_duplicateRecordsWhoRepresentMoreThanOneBlockIsZero() throws Exception {
        List<Record> records = generateRecords(10);
        List<BlockWithData> blocks = generateBlocks(10);
        Multimap<Record, BlockWithData> duplicates = ArrayListMultimap.create();
        Multimap<Record, BlockWithData> cleaned = ArrayListMultimap.create();
        BiMap<Record, BlockWithData> trueReps = HashBiMap.create();

        duplicates.putAll(records.get(1), Lists.newArrayList(blocks.get(1)));
        cleaned.putAll(records.get(1), Lists.newArrayList(blocks.get(1)));
        trueReps.put(records.get(1), blocks.get(1));

        // execute your test
        double duplicatesRealRepresentatives = classUnderTest.duplicatesRealRepresentatives(duplicates, cleaned, trueReps);
        assertThat(duplicatesRealRepresentatives, closeTo(0.0, 0.00001));
    }

    @Test
    public void trueRepsPercentage() throws Exception {
        Set<Record> groundTruthReps = new HashSet<>(recordsFromCsv.subList(0, 10));
        Set<Record> algReps = new HashSet<>(recordsFromCsv.subList(6, 14));

        float trueRepsPercentage = classUnderTest.trueRepsPercentage(groundTruthReps, algReps);
        assertThat(trueRepsPercentage, is(0.4F));
    }

    @Test
    public void removedGroundTruthReps_repsWereRemoved() throws Exception {
        /*
        * There are 4 ground truth reps,
        * baseline contains 3 ground truth reps
        * bcbp contains 2 ground truth reps
        */
        List<Record> groundTruth = recordsFromCsv.subList(0, 4);
        Set<Record> baseline = new HashSet<>(recordsFromCsv.subList(4, 10));
        assertTrue(baseline.addAll(groundTruth.subList(0, 3)));
        Set<Record> bcbp = new HashSet<>(recordsFromCsv.subList(10, 12));
        assertTrue(bcbp.addAll(groundTruth.subList(0, 2)));

        int removedGroundTruthReps = classUnderTest.removedGroundTruthReps(baseline, bcbp, new HashSet<>(groundTruth));
        assertThat(removedGroundTruthReps, is(1));
    }

    @Test
    public void removedGroundTruthReps_repsWereAddedAndRemoved() throws Exception {
        /*
        * There are 4 ground truth reps,
        * baseline contains 2 ground truth reps
        * bcbp contains 3 ground truth reps, only one from baseline
        */
        List<Record> groundTruth = recordsFromCsv.subList(0, 4);
        Set<Record> baseline = new HashSet<>(recordsFromCsv.subList(4, 10));
        assertTrue(baseline.addAll(groundTruth.subList(0, 2)));
        Set<Record> bcbp = new HashSet<>(recordsFromCsv.subList(10, 12));
        assertTrue(bcbp.addAll(groundTruth.subList(1, 4)));

        int removedGroundTruthReps = classUnderTest.removedGroundTruthReps(baseline, bcbp, new HashSet<>(groundTruth));
        assertThat(removedGroundTruthReps, is(1));
    }

    @Test
    public void removedGroundTruthReps_repsWereAdded() throws Exception {
        /*
        * There are 4 ground truth reps,
        * baseline contains 2 ground truth reps
        * bcbp contains 3 ground truth reps, all exist in baseline
        */
        List<Record> groundTruth = recordsFromCsv.subList(0, 4);
        Set<Record> baseline = new HashSet<>(recordsFromCsv.subList(4, 10));
        assertTrue(baseline.addAll(groundTruth.subList(0, 2)));
        Set<Record> bcbp = new HashSet<>(recordsFromCsv.subList(10, 12));
        assertTrue(bcbp.addAll(groundTruth.subList(0, 3)));

        int removedGroundTruthReps = classUnderTest.removedGroundTruthReps(baseline, bcbp, new HashSet<>(groundTruth));
        assertThat(removedGroundTruthReps, is(0));
    }

    @Test
    public void newAddedReps_notAddedNewReps() throws Exception {
        /*
        * There are 4 ground truth reps,
        * baseline contains 2 ground truth reps
        * bcbp contains 2 ground truth reps
        */
        List<Record> groundTruth = recordsFromCsv.subList(0, 4);
        Set<Record> baseline = new HashSet<>(recordsFromCsv.subList(4, 10));
        assertTrue(baseline.addAll(groundTruth.subList(0, 2)));
        Set<Record> bcbp = new HashSet<>(recordsFromCsv.subList(10, 12));
        assertTrue(bcbp.addAll(groundTruth.subList(0, 1)));

        int newAddedReps = classUnderTest.newAddedReps(baseline, bcbp, new HashSet<>(groundTruth));
        assertThat(newAddedReps, is(0));
    }

    @Test
    public void newAddedReps_addedNewRepNotContainedInBaseline() throws Exception {
        /*
        * There are 4 ground truth reps,
        * baseline contains 2 ground truth reps
        * bcbp contains 3 ground truth reps
        */
        List<Record> groundTruth = recordsFromCsv.subList(0, 4);
        Set<Record> baseline = new HashSet<>(recordsFromCsv.subList(4, 10));
        assertTrue(baseline.addAll(groundTruth.subList(0, 2)));
        Set<Record> bcbp = new HashSet<>(recordsFromCsv.subList(10, 12));
        assertTrue(bcbp.addAll(groundTruth.subList(0, 3)));

        int newAddedReps = classUnderTest.newAddedReps(baseline, bcbp, new HashSet<>(groundTruth));
        assertThat(newAddedReps, is(1));
    }

    @Test
    public void newAddedReps_addedNewRepNotContainedInBaseline_andRemovedSomeThereWereContained() throws Exception {
        /*
        * There are 4 ground truth reps,
        * baseline contains 2 ground truth reps
        * bcbp contains 3 ground truth reps
        */
        List<Record> groundTruth = recordsFromCsv.subList(0, 4);
        Set<Record> baseline = new HashSet<>(recordsFromCsv.subList(4, 10));
        assertTrue(baseline.addAll(groundTruth.subList(0, 2)));
        Set<Record> bcbp = new HashSet<>(recordsFromCsv.subList(10, 12));
        assertTrue(bcbp.addAll(groundTruth.subList(1, 4)));

        int newAddedReps = classUnderTest.newAddedReps(baseline, bcbp, new HashSet<>(groundTruth));
        assertThat(newAddedReps, is(2));
    }

    @Test
    public void findBlocksThatShouldRemain() throws Exception {
        Multimap<Record, BlockWithData> baselineBlocks = getBaselineBlocks();
        List<BlockWithData> blocks = new ArrayList<>(baselineBlocks.values());

        final Record record_20_org = blocks.get(0).getTrueRepresentative();
        float record_20_org_probability = blocks.get(0).getMemberProbability(record_20_org);
        blocks.get(12).setMemberProbability(record_20_org, record_20_org_probability - 0.001F);
        baselineBlocks.putAll(record_20_org, Lists.newArrayList(blocks.get(0), blocks.get(12)));

        final Record record_15_org = blocks.get(7).getTrueRepresentative();
        float record_15_org_probability = blocks.get(7).getMemberProbability(record_15_org);
        blocks.get(4).setMemberProbability(record_15_org, record_15_org_probability - 0.001F);
        baselineBlocks.put(record_15_org, blocks.get(4));

        Multimap<Record, BlockWithData> filteredBaselineBlocks = Multimaps.filterKeys(baselineBlocks, new Predicate<Record>() {
            @Override
            public boolean apply(Record input) {
                return input.equals(record_15_org) || input.equals(record_20_org);
            }
        });
        Multimap<Record, BlockWithData> blocksThatShouldRemain = Whitebox.invokeMethod(classUnderTest, "findBlocksThatShouldRemain", filteredBaselineBlocks);
        assertThat(blocksThatShouldRemain.values(), hasSize(2));
        assertThat(blocksThatShouldRemain.get(record_15_org), contains(blocks.get(7)));
        assertThat(blocksThatShouldRemain.get(record_20_org), contains(blocks.get(0)));
    }

    @Test
    public void percentageOfRecordsPulledFromGroundTruth_noMatchingRecords() throws Exception {
        Multimap<Record, BlockWithData> bcbpReps = ArrayListMultimap.create();
        Multimap<Record, BlockWithData> blocksShouldRemain = ArrayListMultimap.create();
        bcbpReps.put(mock(Record.class), mock(BlockWithData.class));
        blocksShouldRemain.put(mock(Record.class), mock(BlockWithData.class));
        double percentageOfPulled = classUnderTest.percentageOfRecordsPulledFromGroundTruth(bcbpReps, blocksShouldRemain, null);
        assertThat(percentageOfPulled, is(0.0));
    }

    @Test
    public void percentageOfRecordsPulledFromGroundTruth_groundTruthRepWasAssignedToWrongBlock() throws Exception {
        Multimap<Record, BlockWithData> bcbpReps = ArrayListMultimap.create();
        Multimap<Record, BlockWithData> blocksShouldRemain = ArrayListMultimap.create();
        BiMap<Record, BlockWithData> biMap = HashBiMap.create();

        Record rep = mock(Record.class);
        bcbpReps.put(rep, mock(BlockWithData.class));
        biMap.put(rep, mock(BlockWithData.class));
        blocksShouldRemain.put(rep, biMap.get(rep));

        double percentageOfPulled = classUnderTest.percentageOfRecordsPulledFromGroundTruth(bcbpReps, blocksShouldRemain, biMap);
        assertThat(percentageOfPulled, is(0.0));
    }

    @Test
    public void percentageOfRecordsPulledFromGroundTruth_groundTruthRepWasAssignedToBcbpBlock() throws Exception {
        final ImmutableList<Record> commonRecords = ImmutableList.copyOf(recordsFromCsv.subList(0, 4));
        Record rep = mock(Record.class);

        List<Record> groundTruthRecords = buildBlockRecords(commonRecords, recordsFromCsv.subList(4, 5), rep);
        List<Record> bcbpRecords = buildBlockRecords(commonRecords, recordsFromCsv.subList(5, 7), rep);

        BlockWithData algBlock = mock(BlockWithData.class);
        when(algBlock.getMembers()).thenReturn(bcbpRecords);

        BlockWithData gTBlock = mock(BlockWithData.class);
        when(gTBlock.getMembers()).thenReturn(groundTruthRecords);


        Multimap<Record, BlockWithData> bcbpReps = ArrayListMultimap.create();
        Multimap<Record, BlockWithData> blocksShouldRemain = ArrayListMultimap.create();
        bcbpReps.put(rep, algBlock);
        blocksShouldRemain.put(rep, algBlock);

        BiMap<Record, BlockWithData> biMap = HashBiMap.create();
        biMap.put(rep, gTBlock);

        double percentageOfPulled = classUnderTest.percentageOfRecordsPulledFromGroundTruth(bcbpReps, blocksShouldRemain, biMap);
        assertThat(percentageOfPulled, is(commonRecords.size() / (groundTruthRecords.size() - 1.0)));
    }

    @Test
    public void percentageOfRecordsPulledFromGroundTruth_GTBlockIsSingleton() throws Exception {
        final ImmutableList<Record> commonRecords = ImmutableList.copyOf(recordsFromCsv.subList(0, 1));
        Record rep = mock(Record.class);

        List<Record> groundTruthRecords = Lists.newArrayList(rep);
        List<Record> bcbpRecords = buildBlockRecords(commonRecords, rep);

        BlockWithData algBlock = mock(BlockWithData.class);
        when(algBlock.getMembers()).thenReturn(bcbpRecords);

        BlockWithData gTBlock = mock(BlockWithData.class);
        when(gTBlock.getMembers()).thenReturn(groundTruthRecords);

        Multimap<Record, BlockWithData> bcbpReps = ArrayListMultimap.create();
        Multimap<Record, BlockWithData> blocksShouldRemain = ArrayListMultimap.create();
        bcbpReps.put(rep, algBlock);
        blocksShouldRemain.put(rep, algBlock);

        BiMap<Record, BlockWithData> biMap = HashBiMap.create();
        biMap.put(rep, gTBlock);

        double percentageOfPulled = classUnderTest.percentageOfRecordsPulledFromGroundTruth(bcbpReps, blocksShouldRemain, biMap);
        assertThat(percentageOfPulled, is(0.0));
        PowerMockito.verifyPrivate(classUnderTest, Mockito.never())
                .invoke("getNumberOfPulledRecordsFromGroundTruthPercentage", Mockito.anyListOf(Record.class), Mockito.anyListOf(Record.class));
    }

    private List<Record> buildBlockRecords(ImmutableList<Record> commonRecords, Record rep) {
        return this.buildBlockRecords(commonRecords, new ArrayList<Record>(), rep);
    }

    private List<Record> buildBlockRecords(ImmutableList<Record> commonRecords, List<Record> records, Record trueRep) {
        List<Record> result = new ArrayList<>(records);
        result.addAll(commonRecords);
        result.add(trueRep);
        return result;
    }

    @Test
    public void percentageOfRecordsPulledFromGroundTruth_withSeveralReps() throws Exception {
        BiMap<Record, BlockWithData> trueReps = getTrueReps();
        Multimap<Record, BlockWithData> baselineBlocks = getBaselineBlocks();

        List<BlockWithData> algBlocks = new ArrayList<>(baselineBlocks.values());
        List<BlockWithData> gtBlocks = new ArrayList<>(trueReps.values());

        Multimap<Record, BlockWithData> bcbpReps = ArrayListMultimap.create();
        Multimap<Record, BlockWithData> blocksShouldRemain = ArrayListMultimap.create();
        BiMap<Record, BlockWithData> biMap = HashBiMap.create();

        // record-15-org is set as representative of 2 blocks
        bcbpReps.put(algBlocks.get(7).getTrueRepresentative(), algBlocks.get(7));
        bcbpReps.put(algBlocks.get(4).getTrueRepresentative(), algBlocks.get(4));

        // records: record-15-org & record-10-org and blocks they should represent
        blocksShouldRemain.put(algBlocks.get(7).getTrueRepresentative(), algBlocks.get(7));
        blocksShouldRemain.put(algBlocks.get(13).getTrueRepresentative(), algBlocks.get(13));
        blocksShouldRemain.put(algBlocks.get(4).getTrueRepresentative(), algBlocks.get(4));

        //record record-15-org ground truth block
        biMap.put(gtBlocks.get(20).getTrueRepresentative(), gtBlocks.get(20));
        //record record-22-org ground truth block
        biMap.put(gtBlocks.get(4).getTrueRepresentative(), gtBlocks.get(4));

        double percentageOfPulled = classUnderTest.percentageOfRecordsPulledFromGroundTruth(bcbpReps, blocksShouldRemain, biMap);
        assertThat(percentageOfPulled, is(1.0 / 2));
    }

    @Test
    public void findBlocksThatShouldNotRemain() throws Exception {
        List<BlockWithData> blocks = generateBlocks(20);
        List<Record> records = generateRecords(20);
        Multimap<Record, BlockWithData> baselineFiltered = ArrayListMultimap.create();
        //record_0 --> block_0 & block_10
        baselineFiltered.putAll(records.get(0), Lists.newArrayList(blocks.get(0), blocks.get(10)));
        //record_1 --> block_1 & block_11
        baselineFiltered.putAll(records.get(1), Lists.newArrayList(blocks.get(1), blocks.get(11)));
        //record_2 --> block_2 & block_12 && block_13
        baselineFiltered.putAll(records.get(2), Lists.newArrayList(blocks.get(2), blocks.get(12), blocks.get(13)));


        Multimap<Record, BlockWithData> blocksThatShouldRemain = ArrayListMultimap.create();
        //record_0 --> block_0
        blocksThatShouldRemain.put(records.get(0), blocks.get(0));
        //record_1 --> block_1 & block_11
        blocksThatShouldRemain.putAll(records.get(1), Lists.newArrayList(blocks.get(1), blocks.get(11)));
        //record_2 --> block_2 & block_12
        blocksThatShouldRemain.putAll(records.get(2), Lists.newArrayList(blocks.get(2), blocks.get(12)));

        //execute
        Multimap<Record, BlockWithData> blocksThatShouldNotRemain = Whitebox.invokeMethod(classUnderTest, "findBlocksThatShouldNotRemain", baselineFiltered, blocksThatShouldRemain);

        //assert
        assertThat(blocksThatShouldNotRemain.get(records.get(0)), contains(blocks.get(10)));
        assertThat(blocksThatShouldNotRemain.asMap(), not(hasKey(records.get(1))));
        assertThat(blocksThatShouldNotRemain.get(records.get(2)), contains(blocks.get(13)));
    }

    @Test
    public void percentageOfRecordsPulledFromGroundTruth_forWrongAssignmentOfRecordToRep() throws Exception {
        BiMap<Record, BlockWithData> trueReps = getTrueReps();
        Multimap<Record, BlockWithData> baselineBlocks = getBaselineBlocks();

        List<BlockWithData> algBlocks = new ArrayList<>(baselineBlocks.values());
        List<Record> records = new ArrayList<>(trueReps.keySet());
        List<BlockWithData> trueBlocks = new ArrayList<>(trueReps.values());

        //modify memners
        algBlocks.get(13).getMembers().remove(1);
        algBlocks.get(13).getMembers().remove(3);
        algBlocks.get(12).getMembers().remove(6);

        Multimap<Record, BlockWithData> bcbpReps = ArrayListMultimap.create();
        //record_0 --> block_0 (wrong)
        bcbpReps.put(records.get(0), algBlocks.get(0));
        //record_1 --> block_1 & block_11 (correct partially)
        bcbpReps.putAll(records.get(1), Lists.newArrayList(algBlocks.get(1), algBlocks.get(11)));
        //record_2 --> block_2 (wrong)
        bcbpReps.put(records.get(2), algBlocks.get(12));
        //record_3 --> block_3 (wrong)
        bcbpReps.put(records.get(3), algBlocks.get(13));
        //record_3 --> block_3 (wrong)
        bcbpReps.put(records.get(4), algBlocks.get(4));

        BiMap<Record, BlockWithData> biMap = HashBiMap.create();
        biMap.put(records.get(1), algBlocks.get(1));
        biMap.put(records.get(2), trueBlocks.get(2));
        biMap.put(records.get(3), trueBlocks.get(3));
        biMap.put(records.get(4), algBlocks.get(4));

        Multimap<Record, BlockWithData> blocksunderMeasure = ArrayListMultimap.create();
        //record_0 is not GT rep
        blocksunderMeasure.put(records.get(2), algBlocks.get(12));
        blocksunderMeasure.put(records.get(3), algBlocks.get(13));

        //execute
        double percentageOfPulled = classUnderTest.percentageOfRecordsPulledFromGroundTruth(bcbpReps, blocksunderMeasure, biMap);

        //assert
        assertThat(percentageOfPulled, closeTo(0.55, 0.0001));
    }

    private BiMap<Record, BlockWithData> getTrueReps() throws Exception {
        File datasetsRootFolder = temporaryFolder.newFolder("root_datasetsPermutation");
        ;
        ZipExtractor.extractZipFromResources(datasetsRootFolder, "/01_NumberOfOriginalRecords_datasets.zip");

        Collection<File> allDatasetPermutations = new FilesReader(datasetsRootFolder.getAbsolutePath()).getAllDatasets();
        for (File datasetFile : allDatasetPermutations) {
            if (DATASET_PERMUTATION_NAME.equals(datasetFile.getName())) {
                List<BlockWithData> cleanBlocks = new ParsingService().parseDataset(datasetFile.getAbsolutePath());
                return new CanopyService().getAllTrueRepresentatives(cleanBlocks);
            }
        }

        return null;
    }

    private Multimap<Record, BlockWithData> getBaselineBlocks() throws Exception {
        File blocksRootFolder = temporaryFolder.newFolder("root_blocks");
        ZipExtractor.extractZipFromResources(blocksRootFolder, "/01_NumberOfOriginalRecords_blocks.zip");

        BlocksMapper blocksMapper = new FilesReader(blocksRootFolder.getAbsolutePath()).getAllBlocks();
        BlockPair blockPair = blocksMapper.getNext(DATASET_PERMUTATION_NAME);
        List<BlockWithData> baselineBlocks = new ArrayList<>(SerializerUtil.<BlockWithData>deSerialize(blockPair.getBaseline()));
        ProcessBlocks processBlocks = new ProcessBlocks();
        return Whitebox.invokeMethod(processBlocks, "getRepresentatives", baselineBlocks);
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