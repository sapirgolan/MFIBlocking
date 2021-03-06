package il.ac.technion.ie.experiments.service;

import com.google.common.collect.Multimap;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.builder.FebrlBlockBuilder;
import il.ac.technion.ie.experiments.builder.iBlockBuilder;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockResults;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.CompareAlgorithmResults;
import il.ac.technion.ie.experiments.parsers.DatasetParser;
import il.ac.technion.ie.model.Record;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 22/08/2015.
 */
public class ParsingService {

    public static final String RANKED_VALUE = "Ranked Value";
    public static final String MRR = "MRR";
    public static final String THRESHOLD = "Threshold";
    public static final String NORM_RANKED_VALUE = "Norm Ranked Value";
    public static final String NORM_MRR = "Norm MRR";
    private static final String MILLER_RANKED_VALUE = "Miller Ranked Value";
    private static final String MILLER_MRR_VALUE = "Miller MRR Value";
    public static final String FEBERL_PARAMETER = "Feberl parameter";
    private static final String DATASET_NAME = "Dataset Name";
    private static final String[] HEADERS_SINGLE_BLOCK_SET = {DATASET_NAME, "Missing Real Representatives",
            "Power of Real Reap - Recall", "Wisdom of the crowd - Precision - Precision",
            "duplicatesRemoved", "Duplicates Real Representatives",
            "Average block size", "Average number of blocks",
            "baseline Duration (mil)", "bcbp Duration (mil)"};
    private static final String[] HEADERS_BASELINE_AND_ALG = {DATASET_NAME,
            "True Reps % - Baseline",
            "True Reps % - BCBP",
            "Recall - Baseline",
            "Recall - BCBP",
            "Precision - Baseline",
            "Precision - BCBP",
            "Missing Real Representatives - Baseline",
            "Missing Real Representatives - BCBP",
            "Duplicates Real Representatives",
            "New added ground truth reps by BCBP",
            "removed ground truth reps by BCBP",
            "Duplicates Removed",
            "% pulled records, rep should remain",
            "% pulled records, rep should NOT remain",
            "Average block size", "Average number of blocks",
            "Baseline Duration (mil)", "BCBP Duration (mil)"};
    private DatasetParser dataParser;
    private iBlockBuilder blockBuilder;

    public ParsingService() {
        this.dataParser = new DatasetParser();
        blockBuilder = new FebrlBlockBuilder();
    }
    public List<BlockWithData> parseDataset(String pathToFile) {
        List<BlockWithData> blocksWithData = new ArrayList<>();
        CsvParser parser = dataParser.getParserForFile(pathToFile);
        String[] fields = parser.parseNext();

        if (fields != null) {
            List<String> fieldsNames = new ArrayList<>(Arrays.asList(fields));
            blocksWithData = blockBuilder.build(parser, fieldsNames);
        }
        return blocksWithData;
    }

    public void writeBlocks(List<BlockWithData> blocks, String pathToFile) {
        CsvWriter csvWriter = dataParser.preparOutputFile(pathToFile);
        if (csvWriter != null) {
            // Write the record headers of this file
            List<String> fieldsNames = getBlockFieldsNames(blocks);
            fieldsNames.add("Probability");
            csvWriter.writeHeaders(fieldsNames);

            // Let's write the rows one by one
            for (BlockWithData block : blocks) {
                for (Record record : block.getMembers()) {
                    for (String recordEntry : record.getEntries()) {
                        csvWriter.writeValue(recordEntry);
                    }
                    csvWriter.writeValues(block.getMemberProbability(record));
                    csvWriter.writeValuesToRow();
                }
            }
            // Here we just tell the writer to write everything and close the given output Writer instance.
            csvWriter.close();
        }
    }

    private List<String> getBlockFieldsNames(List<BlockWithData> blocks) {
        if (blocks != null && !blocks.isEmpty()) {
            final BlockWithData blockWithData = blocks.get(0);
            return blockWithData.getFieldNames();
        }
        return null;
    }

    public void writeExperimentsMeasurements(IMeasurements measurements, File tempFile) throws SizeNotEqualException {
        CsvWriter csvWriter = dataParser.preparOutputFile(tempFile);
        csvWriter.writeHeaders(THRESHOLD, RANKED_VALUE, MRR, NORM_RANKED_VALUE, NORM_MRR, MILLER_RANKED_VALUE, MILLER_MRR_VALUE);

        List<Double> mrrValues = measurements.getMrrValuesSortedByThreshold();
        List<Double> rankedValues = measurements.getRankedValuesSortedByThreshold();
        List<Double> thresholds = measurements.getThresholdSorted();
        List<Double> normalizedRankedValues = measurements.getNormalizedRankedValuesSortedByThreshold();
        List<Double> normalizedMRRValues = measurements.getNormalizedMRRValuesSortedByThreshold();
        assertSize(measurements);

        Double millerRankedValue = getMillerRankedValue(rankedValues);
        Double millerMRRValue = getMillerMRRValue(mrrValues);
        for (int i = 1; i < thresholds.size(); i++) {
            csvWriter.writeValue(MRR, mrrValues.get(i));
            csvWriter.writeValue(THRESHOLD, thresholds.get(i));
            csvWriter.writeValue(RANKED_VALUE, rankedValues.get(i));
            csvWriter.writeValue(NORM_RANKED_VALUE, normalizedRankedValues.get(i));
            csvWriter.writeValue(NORM_MRR, normalizedMRRValues.get(i));
            csvWriter.writeValue(MILLER_RANKED_VALUE, millerRankedValue);
            csvWriter.writeValue(MILLER_MRR_VALUE, millerMRRValue);

            csvWriter.writeValuesToRow();
        }
        csvWriter.close();
    }

    private Double getMillerMRRValue(List<Double> mrrValues) {
        if (!mrrValues.isEmpty()) {
            return mrrValues.get(0);
        } else {
            return null;
        }
    }

    private Double getMillerRankedValue(List<Double> rankedValues) {
        if (!rankedValues.isEmpty()) {
            return rankedValues.get(0);
        } else {
            return null;
        }
    }

    private void assertSize(IMeasurements measurements) throws SizeNotEqualException {
        List<Double> mrrValues = measurements.getMrrValuesSortedByThreshold();
        List<Double> rankedValues = measurements.getRankedValuesSortedByThreshold();
        List<Double> thresholds = measurements.getThresholdSorted();
        List<Double> normalizedRankedValues = measurements.getNormalizedRankedValuesSortedByThreshold();
        List<Double> normalizedMRRValues = measurements.getNormalizedMRRValuesSortedByThreshold();

        if ((thresholds.size() != rankedValues.size()) ||
                (thresholds.size() != mrrValues.size()) ||
                (thresholds.size() != normalizedRankedValues.size()) ||
                (thresholds.size() != normalizedMRRValues.size())) {
            throw new SizeNotEqualException(String.format("The size of %s, %s and %s is not equal", RANKED_VALUE, MRR, THRESHOLD));
        }
    }

    public void writeExperimentsMeasurements(DuplicateReductionContext duplicateReductionContext, File file) {
        CsvWriter csvWriter = dataParser.preparOutputFile(file);
        csvWriter.writeHeaders("Missing Real Representatives", "Power of Real Reap - Recall", "Wisdom of the crowd - Precision",
                "duplicatesRemoved");
        writeDuplicateReductionContext(duplicateReductionContext, csvWriter);
        csvWriter.close();
    }

    private void writeDuplicateReductionContext(DuplicateReductionContext duplicateReductionContext, CsvWriter csvWriter) {
        csvWriter.writeValue("Missing Real Representatives", duplicateReductionContext.getRepresentationDiff());
        csvWriter.writeValue("Duplicates Real Representatives", duplicateReductionContext.getDuplicatesRealRepresentatives());
        csvWriter.writeValue("duplicatesRemoved", duplicateReductionContext.getDuplicatesRemoved());
        csvWriter.writeValue("Power of Real Reap - Recall", duplicateReductionContext.getRepresentativesPower());
        csvWriter.writeValue("Wisdom of the crowd - Precision", duplicateReductionContext.getWisdomCrowds());
        csvWriter.writeValue("Average number of blocks", duplicateReductionContext.getNumberOfDirtyBlocks());
        csvWriter.writeValue("Average block size", duplicateReductionContext.getAverageBlockSize());

        csvWriter.writeValuesToRow();
    }

    public void writeExperimentsMeasurements(Map<Integer, DuplicateReductionContext> map, File expResults) {
        CsvWriter csvWriter = dataParser.preparOutputFile(expResults);
        csvWriter.writeHeaders(FEBERL_PARAMETER, "Missing Real Representatives",
                "Power of Real Reap - Recall", "Wisdom of the crowd - Precision",
                "duplicatesRemoved", "Duplicates Real Representatives",
                "Average block size", "Average number of blocks");
        for (Map.Entry<Integer, DuplicateReductionContext> entry : map.entrySet()) {
            csvWriter.writeValue(FEBERL_PARAMETER, entry.getKey());
            this.writeDuplicateReductionContext(entry.getValue(), csvWriter);
        }
        csvWriter.close();
    }

    public void writeExperimentsMeasurements(Multimap<String, DuplicateReductionContext> results, File expResults) {
        CsvWriter csvWriter = dataParser.preparOutputFile(expResults);
        csvWriter.writeHeaders(HEADERS_SINGLE_BLOCK_SET);
        for (Map.Entry<String, DuplicateReductionContext> entry : results.entries()) {
            csvWriter.writeValue(DATASET_NAME, entry.getKey());
            DuplicateReductionContext reductionContext = entry.getValue();
            this.writeDuplicateReductionContext(reductionContext, csvWriter);
            csvWriter.writeValue("baseline Duration (mil)", reductionContext.getBaselineDuration());
            csvWriter.writeValue("bcbp Duration (mil)", reductionContext.getBcbpDuration());
        }
        csvWriter.close();

    }

    public void writeComparisonExperimentsMeasurements(Multimap<String, DuplicateReductionContext> results, File expResults) {
        CsvWriterSettings settings = new CsvWriterSettings();
        settings.setHeaders(HEADERS_BASELINE_AND_ALG);
        settings.selectFields(HEADERS_BASELINE_AND_ALG);

        CsvWriter csvWriter = dataParser.preparOutputFile(expResults, settings);
        csvWriter.writeHeaders();

        for (Map.Entry<String, DuplicateReductionContext> entry : results.entries()) {
            csvWriter.writeRow(buildComparisonRow(entry));
        }
        csvWriter.close();
    }

    private List<Object> buildComparisonRow(Map.Entry<String, DuplicateReductionContext> entry) {
        DuplicateReductionContext reductionContext = entry.getValue();
        BlockResults baselineResults = reductionContext.getBaselineResults();
        BlockResults bcbpResults = reductionContext.getBcbpResults();
        CompareAlgorithmResults compareAlgsResults = reductionContext.getCompareAlgsResults();

        List<Object> rowContent = new ArrayList<>();
        rowContent.add(entry.getKey());
        //TrueRepsPercentage
        rowContent.add(baselineResults.getTrueRepsPercentage());
        rowContent.add(bcbpResults.getTrueRepsPercentage());
        //Recall
        rowContent.add(baselineResults.getRecall());
        rowContent.add(bcbpResults.getRecall());
        //Precision
        rowContent.add(baselineResults.getPrecision());
        rowContent.add(bcbpResults.getPrecision());
        //MRR
        rowContent.add(baselineResults.getMrr());
        rowContent.add(bcbpResults.getMrr());

        //DRR
        rowContent.add(compareAlgsResults.getDrr());

        rowContent.add(compareAlgsResults.getNewAddedReps());
        rowContent.add(compareAlgsResults.getRemovedGroundTruthReps());

        rowContent.add(reductionContext.getDuplicatesRemoved());

        //% pulled records by representatives that should and should not remain
        rowContent.add(reductionContext.getBlockShouldRemainPulled());
        rowContent.add(reductionContext.getBlockShouldNotRemainPulled());

        rowContent.add(reductionContext.getNumberOfDirtyBlocks());
        rowContent.add(reductionContext.getAverageBlockSize());

        rowContent.add(reductionContext.getBaselineDuration());
        rowContent.add(reductionContext.getBcbpDuration());

        return rowContent;
    }
}
