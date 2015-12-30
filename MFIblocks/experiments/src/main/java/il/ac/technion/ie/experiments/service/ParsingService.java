package il.ac.technion.ie.experiments.service;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvWriter;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.builder.FebrlBlockBuilder;
import il.ac.technion.ie.experiments.builder.iBlockBuilder;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.DatasetStatistics;
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
    public static final String AVERAGE_RANKED_VALUE = "Average Ranked Value";
    public static final String AVERAGE_MRR = "Average MRR";
    public static final String FILE_NAME = "File Name";
    public static final String NUMBER_OF_BLOCKS = "# Blocks";
    public static final String AVG_BLOCK_SIZE = "Avg Block Size";
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

/*    public void writeExperimentsMeasurements(Map<Integer, FebrlMeasuresContext> measures, File tempFile) {
        CsvWriter csvWriter = dataParser.preparOutputFile(tempFile);
        csvWriter.writeHeaders(FEBERL_PARAMETER, AVERAGE_RANKED_VALUE, AVERAGE_MRR);
        for (Map.Entry<Integer, FebrlMeasuresContext> febrlMeasuresContextEntry : measures.entrySet()) {
            csvWriter.writeValue(FEBERL_PARAMETER, febrlMeasuresContextEntry.getKey());
            csvWriter.writeValue(AVERAGE_RANKED_VALUE, febrlMeasuresContextEntry.getValue().getAverageRankedValue());
            csvWriter.writeValue(AVERAGE_MRR, febrlMeasuresContextEntry.getValue().getAverageMRR());
            csvWriter.writeValuesToRow();
        }
        csvWriter.close();
    }*/

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

    public void writeStatistics(List<DatasetStatistics> datasetStatisticses, File outputFile) {
        CsvWriter csvWriter = dataParser.preparOutputFile(outputFile);
        csvWriter.writeHeaders(FILE_NAME, NUMBER_OF_BLOCKS, AVG_BLOCK_SIZE);
        for (DatasetStatistics statistics : datasetStatisticses) {
            csvWriter.writeValue(FILE_NAME, statistics.getFileName());
            csvWriter.writeValue(NUMBER_OF_BLOCKS, statistics.getNumberOfBlocks());
            csvWriter.writeValue(AVG_BLOCK_SIZE, statistics.getAvgBlockSize());
            csvWriter.writeValuesToRow();
        }
        csvWriter.close();
    }

    public void writeExperimentsMeasurements(DuplicateReductionContext duplicateReductionContext, File file) {
        CsvWriter csvWriter = dataParser.preparOutputFile(file);
        csvWriter.writeHeaders("diff of true representation vs found", "Power of real representatives in soft clusters", "wisdom of the crowd",
                "duplicatesRemoved", "improvementPercentage", "dupReductionPercentage");
        writeDuplicateReductionContext(duplicateReductionContext, csvWriter);
        csvWriter.close();
    }

    private void writeDuplicateReductionContext(DuplicateReductionContext duplicateReductionContext, CsvWriter csvWriter) {
        csvWriter.writeValue("diff of true representation vs found", duplicateReductionContext.getRepresentationDiff());
        csvWriter.writeValue("duplicatesRemoved", duplicateReductionContext.getDuplicatesRemoved());
        csvWriter.writeValue("improvementPercentage", duplicateReductionContext.getImprovementPercentage());
        csvWriter.writeValue("dupReductionPercentage", duplicateReductionContext.getDupReductionPercentage());
        csvWriter.writeValue("Power of real representatives in soft clusters", duplicateReductionContext.getRepresntativesPower());
        csvWriter.writeValue("wisdom of the crowd", duplicateReductionContext.getWisdomCrowds());
        csvWriter.writeValuesToRow();
    }

    public void writeExperimentsMeasurements(Map<Integer, DuplicateReductionContext> map, File expResults) {
        CsvWriter csvWriter = dataParser.preparOutputFile(expResults);
        csvWriter.writeHeaders(FEBERL_PARAMETER, "diff of true representation vs found", "Power of real representatives in soft clusters", "wisdom of the crowd",
                "duplicatesRemoved", "improvementPercentage", "dupReductionPercentage");
        for (Map.Entry<Integer, DuplicateReductionContext> entry : map.entrySet()) {
            csvWriter.writeValue(FEBERL_PARAMETER, entry.getKey());
            this.writeDuplicateReductionContext(entry.getValue(), csvWriter);
        }
        csvWriter.close();
    }
}
