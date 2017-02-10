package il.ac.technion.ie.experiments.service;

import com.google.common.collect.*;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.FebrlMeasuresContext;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;
import il.ac.technion.ie.model.Record;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by I062070 on 15/10/2015.
 */
public class Measurements implements IMeasurements {

    private static final Logger logger = Logger.getLogger(Measurements.class);

    public static final double VALUE_NOT_EXISTS = -1.0;
    private iMeasurService measurService;
    private ListMultimap<Double, Double> rankedValueMap;
    private ListMultimap<Double, Double> mrrValueMap;
    private ListMultimap<Double, Double> normalizedMRRValues;
    private int numberOfOriginalBlocks;
    private ListMultimap<Double, Double> normalizedRankedValues;


    public Measurements(int numOfOriginalBlocks) {
        measurService = new MeasurService();
        rankedValueMap = ArrayListMultimap.create();
        mrrValueMap = ArrayListMultimap.create();
        normalizedMRRValues = ArrayListMultimap.create();
        normalizedRankedValues = ArrayListMultimap.create();
        this.numberOfOriginalBlocks = numOfOriginalBlocks;
    }

    @Override
    public void calculate(List<BlockWithData> blocks, double threshold) {
        if (blocks != null) {
            logger.trace("calculating RankedValue and MRR for threshold " + threshold);
            calcRankedValue(blocks, threshold);

            calcMRR(blocks, threshold);
        }
    }

    private void calcMRR(List<BlockWithData> blocks, double threshold) {
        double mRRValue = measurService.calcMRR(blocks);
        logger.debug(String.format("MRR, %s", mRRValue));
        mrrValueMap.put(threshold, mRRValue);
        double normMRR = mRRValue / numberOfBlocks(blocks.size());
        normalizedMRRValues.put(threshold, normMRR);
        logger.debug(String.format("Norm MRR, %s", normMRR));
    }

    private void calcRankedValue(List<BlockWithData> blocks, double threshold) {
        double rankedValue = measurService.calcRankedValue(blocks);
        logger.debug(String.format("Ranked Value, %s", rankedValue));
        rankedValueMap.put(threshold, rankedValue);
        double normRV = rankedValue / numberOfBlocks(blocks.size());
        logger.debug(String.format("Norm RV, %s", normRV));
        normalizedRankedValues.put(threshold, normRV);
    }

    private int numberOfBlocks(int numberOfSpitedBlocks) {
        int delta = numberOfSpitedBlocks - numberOfOriginalBlocks;
        if (delta == 0) {
            delta = 1;
        }
        return delta;
    }

    @Override
    public double getRankedValueByThreshold(double threshold) {
        return getMeasurmentByThreshold(threshold, rankedValueMap);
    }

    @Override
    public double getMRRByThreshold(double threshold) {
        return getMeasurmentByThreshold(threshold, mrrValueMap);
    }

    private double getMeasurmentByThreshold(double threshold, ListMultimap<Double, Double> listMultimap) {
        List<Double> values = listMultimap.get(threshold);
        return values.isEmpty() ? VALUE_NOT_EXISTS : values.get(values.size() - 1);
    }

    @Override
    public List<Double> getRankedValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(rankedValueMap);
    }

    @Override
    public List<Double> getMrrValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(mrrValueMap);
    }

    private List<Double> getMeasureSortedByThreshold(ListMultimap<Double, Double> measureValue) {
        TreeSet<Double> sortedKeys = new TreeSet<>(measureValue.keySet());
        List<Double> rankedValuesSortedByThreshold = new ArrayList<>();
        for (Double key : sortedKeys) {
            rankedValuesSortedByThreshold.add(measureValue.get(key).get(0));
        }
        return rankedValuesSortedByThreshold;
    }

    @Override
    public List<Double> getThresholdSorted() {
        List<Double> list = new ArrayList<>(rankedValueMap.keySet());
        Collections.sort(list);
        return list;
    }

    @Override
    public List<Double> getNormalizedRankedValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(normalizedRankedValues);
    }

    @Override
    public FebrlMeasuresContext getFebrlMeasuresContext(Double threshold) {
        double averageRankedValue = this.getAverageRankedValue(threshold);
        double averageMRR = this.getAverageMRR(threshold);
        return new FebrlMeasuresContext(averageRankedValue, averageMRR);
    }

    @Override
    public DuplicateReductionContext representativesDuplicateElimination(
            Multimap<Record, BlockWithData> duplicates, Multimap<Record, BlockWithData> cleaned) {
        logger.info("In blocks that were created by Canopy and probs calculated by Miller, there are " + duplicates.keySet().size() + " unique representatives.");
        logger.info("In blocks that were created by Canopy and probs calculated by ConvexBP, there are " + cleaned.keySet().size() + " unique representatives.");
        if (logger.isTraceEnabled()) {
            writeToLogInfo(duplicates);
            writeToLogInfo(cleaned);
        }
        int millerSize = duplicates.keys().size() - duplicates.keySet().size();
        int convexSize = cleaned.keys().size() - cleaned.keySet().size();
        int duplicatesRemoved = millerSize - convexSize;
        logger.info("Total of " + duplicatesRemoved + " records represent less blocks than before.");

        return new DuplicateReductionContext(duplicatesRemoved);
    }

    @Override
    public double duplicatesRealRepresentatives(Multimap<Record, BlockWithData> duplicates, Multimap<Record, BlockWithData> cleaned, BiMap<Record, BlockWithData> trueRepsMap) {
        Set<Record> duplicateRecordsWhoRepresentMoreThanOneBlock = recordsWhoRepresentMoreThanOneBlock(duplicates);
        Set<Record> cleanRecordsWhoRepresentMoreThanOneBlock = recordsWhoRepresentMoreThanOneBlock(cleaned);
        duplicateRecordsWhoRepresentMoreThanOneBlock.removeAll(cleanRecordsWhoRepresentMoreThanOneBlock);

        Set<Record> trueRepresentatives = trueRepsMap.keySet();
        Sets.SetView<Record> intersection = Sets.intersection(trueRepresentatives, duplicateRecordsWhoRepresentMoreThanOneBlock);

        if (duplicateRecordsWhoRepresentMoreThanOneBlock.size() == 0) {
            return 0;
        }
        return intersection.size() / (double) duplicateRecordsWhoRepresentMoreThanOneBlock.size();
    }

    private Set<Record> recordsWhoRepresentMoreThanOneBlock(Multimap<Record, BlockWithData> duplicates) {
        Multiset<Record> millerKeyMultiset = HashMultiset.create(duplicates.keys());
        Set<Record> millerKeySet = new HashSet<>(duplicates.keySet());

        Multisets.removeOccurrences(millerKeyMultiset, millerKeySet);
        return millerKeyMultiset.elementSet();
    }

    @Override
    public void missingRealRepresentatives(final Set<Record> source, final Set<Record> other, DuplicateReductionContext reductionContext) {
        int missingRealRepresentatives = this.missingRealRepresentatives(source, other);
        reductionContext.setRepresentationDiff(missingRealRepresentatives);
    }

    @Override
    public int missingRealRepresentatives(Set<Record> source, Set<Record> other) {
        Set<Record> sourceCopy = new HashSet<>(source);
        Set<Record> otherCopy = new HashSet<>(other);
        sourceCopy.removeAll(otherCopy);
        return sourceCopy.size();
    }

    @Override
    public double calcPowerOfRep_Recall(Map<Record, BlockWithData> trueRepsMap, Multimap<Record, BlockWithData> convexBPRepresentatives, DuplicateReductionContext reductionContext) {
        double recall = this.calcPowerOfRep_Recall(trueRepsMap, convexBPRepresentatives);
        reductionContext.setRepresentativesPower(recall);

        return recall;
    }

    @Override
    public double calcPowerOfRep_Recall(Map<Record, BlockWithData> trueRepsMap, Multimap<Record, BlockWithData> convexBPRepresentatives) {
        int numberOfRecords = 0;
        double sumOfPowerOfRecord = 0;
        Set<Record> trueReps = trueRepsMap.keySet();
        logger.debug("Calculating the power measurement for " + trueReps.size() + " records");
        for (Record record : trueReps) {
            double powerOfRecord = 0;
            BlockWithData blockForRecord = trueRepsMap.get(record);
            verifyBlockNoEmpty(blockForRecord);
            if (verifyBlockNoEmpty(blockForRecord)) {
                logger.trace("The power measurement is calculated for '" + record + "'; representing of " + blockForRecord);
                numberOfRecords++;
                Collection<BlockWithData> blockWithDatas = convexBPRepresentatives.get(record);
                if (!blockWithDatas.isEmpty()) {
                    for (BlockWithData blockWithData : blockWithDatas) {
                        powerOfRecord += existingMembersDividedAllMembers(blockForRecord, blockWithData);
                    }
                    powerOfRecord = powerOfRecord / blockWithDatas.size();
                }
            }
            sumOfPowerOfRecord += powerOfRecord;
            logger.info("The power of '" + record + "' as representative is: " + powerOfRecord);
        }
        double power = sumOfPowerOfRecord / numberOfRecords;
        logger.info("The total average power of all representatives is: " + power);
        return power;
    }

    @Override
    public double calcWisdomCrowd_Precision(Set<BlockWithData> cleanBlocks, Set<BlockWithData> dirtyBlocks) {
        //todo: for performance, we can change BlockWithData to its hashCode
        final Map<Record, BlockWithData> recordToBlockMap = initRecordToBlockMap(cleanBlocks);
        Multimap<BlockWithData, BlockCounter> globalBlockCounters = ArrayListMultimap.create();
        for (BlockWithData dirtyBlock : dirtyBlocks) {
            Map<BlockWithData, Integer> localBlockCounters = getNumberOfRecordsInEachCleanBlock(dirtyBlock, recordToBlockMap);
            updateGlobalCounters(dirtyBlock, localBlockCounters, globalBlockCounters);
        }

        int representativesIdentical = 0;
        for (BlockWithData cleanBlock : globalBlockCounters.keySet()) {
            boolean trueRepIdenticalToDirtyBlockRep = isTrueRepIdenticalToDirtyBlockRep(globalBlockCounters, cleanBlock);
            if (trueRepIdenticalToDirtyBlockRep) {
                representativesIdentical++;
            }
        }

        return representativesIdentical / (double) cleanBlocks.size();
    }

    @Override
    public double calcWisdomCrowd_Precision(Set<BlockWithData> cleanBlocks, Set<BlockWithData> dirtyBlocks, DuplicateReductionContext reductionContext) {
        double precision = this.calcWisdomCrowd_Precision(cleanBlocks, dirtyBlocks);
        reductionContext.setWisdomCrowds(precision);
        return precision;
    }

    @Override
    public void calcAverageBlockSize(List<BlockWithData> dirtyBlocks, DuplicateReductionContext reductionContext) {
        double size = 0;
        for (BlockWithData dirtyBlock : dirtyBlocks) {
            size += dirtyBlock.size();
        }
        size = (size / dirtyBlocks.size());
        reductionContext.setAverageBlockSize(size);
    }

    @Override
    public float trueRepsPercentage(Set<Record> groundTruthReps, Set<Record> algReps) {
        return (float) Sets.intersection(groundTruthReps, algReps).size() / groundTruthReps.size();
    }

    @Override
    public int removedGroundTruthReps(Set<Record> baselineRepresentatives, Set<Record> bcbpRepresentatives, Set<Record> groundTruthReps) {
        Sets.SetView<Record> baselineTrueReps = Sets.intersection(groundTruthReps, baselineRepresentatives);
        Sets.SetView<Record> bcbpTrueReps = Sets.intersection(groundTruthReps, bcbpRepresentatives);
        return Math.max(0, Sets.difference(baselineTrueReps, bcbpTrueReps).size() );
    }

    @Override
    public int newAddedReps(Set<Record> baselineRepresentatives, Set<Record> bcbpRepresentatives, Set<Record> groundTruthReps) {
        Sets.SetView<Record> addedByBcbp = Sets.difference(bcbpRepresentatives, baselineRepresentatives);
        return Sets.intersection(addedByBcbp, groundTruthReps).size();
    }

    private boolean isTrueRepIdenticalToDirtyBlockRep(Multimap<BlockWithData, BlockCounter> globalBlockCounters, BlockWithData cleanBlock) {
        Record trueRepresentative = cleanBlock.getTrueRepresentative();
        for (BlockCounter blockCounter : globalBlockCounters.get(cleanBlock)) {
            Set<Record> dirtyBlockRepresentatives = blockCounter.getBlock().findBlockRepresentatives().keySet();
            if (dirtyBlockRepresentatives.contains(trueRepresentative)) {
                return true;
            }
        }
        return false;
    }

    private void updateGlobalCounters(BlockWithData dirtyBlock, Map<BlockWithData, Integer> localBlockCounters, Multimap<BlockWithData, BlockCounter> globalBlockCounters) {
        for (Map.Entry<BlockWithData, Integer> entry : localBlockCounters.entrySet()) {
            BlockWithData cleanBlock = entry.getKey();
            Integer localCounter = entry.getValue();
            Collection<BlockCounter> blockCounterOfCleanBlock = globalBlockCounters.get(cleanBlock);
            if (blockCounterOfCleanBlock.isEmpty() || blockCounterOfCleanBlock.iterator().next().getCounter() == localCounter) {
                globalBlockCounters.put(cleanBlock, new BlockCounter(dirtyBlock, localCounter));
            } else if (blockCounterOfCleanBlock.iterator().next().getCounter() < localCounter) {
                globalBlockCounters.removeAll(cleanBlock);
                globalBlockCounters.put(cleanBlock, new BlockCounter(dirtyBlock, localCounter));
            }
        }
    }

    private Map<BlockWithData, Integer> getNumberOfRecordsInEachCleanBlock(BlockWithData dirtyBlock, Map<Record, BlockWithData> recordToBlockMap) {
        Map<BlockWithData, Integer> map = new HashMap<>();

        for (Record record : dirtyBlock.getMembers()) {
            BlockWithData blockWithData = recordToBlockMap.get(record);
            if (map.containsKey(blockWithData)) {
                int counter = map.get(blockWithData);
                map.put(blockWithData, counter + 1);
            } else {
                map.put(blockWithData, 1);
            }
        }
        return map;
    }

    private Map<Record, BlockWithData> initRecordToBlockMap(Set<BlockWithData> cleanBlocks) {
        Map<Record, BlockWithData> recordToBlockMap = new HashMap<>(cleanBlocks.size());
        for (BlockWithData cleanBlock : cleanBlocks) {
            for (Record record : cleanBlock.getMembers()) {
                recordToBlockMap.put(record, cleanBlock);
            }
        }
        return Collections.unmodifiableMap(recordToBlockMap);
    }

    private double existingMembersDividedAllMembers(BlockWithData cleanBlock, BlockWithData dirtyBlock) {
        List<Record> membersDirtyBlock = new ArrayList<>(dirtyBlock.getMembers());
        int membersDirtyBlockSize = membersDirtyBlock.size();
        membersDirtyBlock.retainAll(cleanBlock.getMembers());
        int membersContainedInCleanBlockSize = membersDirtyBlock.size();
        return (double) membersContainedInCleanBlockSize / membersDirtyBlockSize;
    }

    private boolean verifyBlockNoEmpty(BlockWithData blockForRecord) {
        boolean returnStatment = blockForRecord.size() > 0;
        if (returnStatment == false) {
            logger.warn(blockForRecord + " doesn't contain any records");
        }
        return returnStatment;
    }

    private BlockWithData getBlockForRecord(Record record, Collection<BlockWithData> realBlocks) {
        BlockWithData blockWithData = null;
        Iterator<BlockWithData> iterator = realBlocks.iterator();
        if (!iterator.hasNext()) {
            logger.warn("Record '" + record + "' is not assigned to any block." +
                    "Therefore we will not calculate its power as representative");
        } else {
            blockWithData = iterator.next();
        }
        return blockWithData;
    }

    public void writeToLogInfo(Multimap<Record, BlockWithData> duplicates) {
        for (Map.Entry<Record, Collection<BlockWithData>> entry : duplicates.asMap().entrySet()) {
            if (entry.getValue().size() >= 2) {
                StringBuilder message = new StringBuilder();
                message.append(entry.getKey()).append(" represents more than one block: ");
                for (BlockWithData blockWithData : entry.getValue()) {
                    message.append(blockWithData);
                }
                logger.trace(message.toString());
            }
        }
    }

    private double getAverageRankedValue(double threshold) {
        return getAverageMeasurement(threshold, rankedValueMap);
    }

    private double getAverageMRR(double threshold) {
        return getAverageMeasurement(threshold, mrrValueMap);
    }

    private double getAverageMeasurement(double threshold, ListMultimap<Double, Double> listMultimap) {
        final List<Double> values = listMultimap.get(threshold);
        double[] valuesToPrimitive = ArrayUtils.toPrimitive(values.toArray(new Double[values.size()]));
        return StatUtils.sum(valuesToPrimitive) / values.size();
    }

    @Override
    public List<Double> getNormalizedMRRValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(normalizedMRRValues);
    }

    protected class BlockCounter {

        private final BlockWithData block;
        private int counter;

        public BlockCounter(BlockWithData block, int counter) {
            this.block = block;
            this.counter = counter;
        }

        public int getCounter() {
            return counter;
        }

        public BlockWithData getBlock() {
            return block;
        }
    }
}
