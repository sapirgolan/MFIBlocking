package il.ac.technion.ie.experiments.util;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParserSettings;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.parsers.DatasetParser;
import il.ac.technion.ie.experiments.service.FuzzyService;
import il.ac.technion.ie.experiments.service.ParsingService;
import il.ac.technion.ie.experiments.service.ProbabilityService;
import il.ac.technion.ie.model.Record;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 24/08/2015.
 */
public class ExperimentsUtils {
    public static List<String> hugeStringToList(String huge) {
        String[] strings = hugeStringToArray(huge);
        return new ArrayList<>( Arrays.asList(strings) );
    }

    public static String[] hugeStringToArray(String huge) {
        return huge.split(",");
    }

    public static String getPathToSmallRecordsFile() throws URISyntaxException {
        String pathToFile = "/20Records.csv";
        File file = getFileFromResourceDir(pathToFile);
        return file.getAbsolutePath();
    }

    public static File getUaiFile() throws URISyntaxException {
        String pathToFile = "/uaiFile.uai";
        return getFileFromResourceDir(pathToFile);
    }

    public static File getBinaryFile() throws URISyntaxException {
        String pathToFile = "/uaiBinaryFormat.txt";
        return getFileFromResourceDir(pathToFile);
    }

    private static File getFileFromResourceDir(String pathToFile) throws URISyntaxException {
        URL resourceUrl = ExperimentsUtils.class.getResource(pathToFile);
        return new File(resourceUrl.toURI());
    }

    public static String getPathToBigRecordsFile() throws URISyntaxException {
        String pathToFile = "/1kRecords.csv";
        File file = getFileFromResourceDir(pathToFile);
        return file.getAbsolutePath();
    }

    public static List<BlockWithData> createFuzzyBlocks() throws Exception {
        String recordsFile = ExperimentsUtils.getPathToSmallRecordsFile();

        ParsingService parsingService = new ParsingService();
        ProbabilityService probabilityService = new ProbabilityService();
        FuzzyService fuzzyService = initFuzzyService();

        List<BlockWithData> originalBlocks = parsingService.parseDataset(recordsFile);
        probabilityService.calcProbabilitiesOfRecords(originalBlocks);

        List<BlockWithData> copyOfOriginalBlocks = new ArrayList<>(originalBlocks);
        Map<Integer, Double> splitProbMap = PowerMockito.mock(Map.class);
        PowerMockito.when(splitProbMap.size()).thenReturn(originalBlocks.size());
        List<BlockWithData> fuzzyBlocks = fuzzyService.splitBlocks(copyOfOriginalBlocks, splitProbMap, 0.6);
        probabilityService.calcProbabilitiesOfRecords(fuzzyBlocks);

        return fuzzyBlocks;
    }

    private static FuzzyService initFuzzyService() throws Exception {
        FuzzyService fuzzyService = PowerMock.createPartialMock(FuzzyService.class, "getSplitProbability");
        PowerMock.expectPrivate(fuzzyService, "getSplitProbability", EasyMock.anyObject(Map.class), EasyMock.anyObject(BlockWithData.class))
                .andReturn(0.3).andReturn(0.7).andReturn(0.6).andReturn(0.4);

        PowerMock.replay(fuzzyService);

        return fuzzyService;
    }

    /**
     * Return a List<String[]> of the parsed file.
     * The first item in the list <code>list.get(0)</code> is a <code>String[]<code/> of headers. The rest
     * of the elements are rows in the file
     *
     * @param pathToFile Full path in File System to a CSV file that contains records
     * @return List<String[]> where the first record is the field names and the rest are the records
     */
    public static List<String[]> readRecordsFromTestFile(String pathToFile) {
        // The settings object provides many configuration options
        CsvParserSettings parserSettings = new CsvParserSettings();

        //You can configure the parser to automatically detect what line separator sequence is in the input
        parserSettings.setLineSeparatorDetectionEnabled(true);

        // A RowListProcessor stores each parsed row in a List.
        RowListProcessor rowProcessor = new RowListProcessor();

        // You can configure the parser to use a RowProcessor to process the values of each parsed row.
        // You will find more RowProcessors in the 'com.univocity.parsers.common.processor' package, but you can also create your own.
        parserSettings.setRowProcessor(rowProcessor);

        parserSettings.setEmptyValue(StringUtils.EMPTY);
        parserSettings.setNullValue(StringUtils.EMPTY);

        // Let's consider the first parsed row as the headers of each column in the file.
        parserSettings.setHeaderExtractionEnabled(true);

        DatasetParser datasetParser = new DatasetParser();
        datasetParser.getParserForFile(pathToFile, parserSettings);

        // get the parsed records from the RowListProcessor here.
        // Note that different implementations of RowProcessor will provide different sets of functionalities.
        String[] headers = rowProcessor.getHeaders();
        List<String[]> rows = rowProcessor.getRows();
        List<String[]> results = new ArrayList<>();
        results.add(headers);
        results.addAll(rows);
        return results;
    }

    public static List<Record> createRecordsFromTestFile(String pathToBigRecordsFile) {
        List<String[]> strings = ExperimentsUtils.readRecordsFromTestFile(pathToBigRecordsFile);
        List<Record> records = new ArrayList<>(strings.size());
        List<String> fieldNames = convertArrayToList(strings.get(0));
        for (int i = 1; i < strings.size(); i++) { //skipping first element since it is the field names
            List<String> values = convertArrayToList(strings.get(i));
            Record record = new Record(fieldNames, values, i);
            records.add(record);
        }
        return records;
    }

    private static List<String> convertArrayToList(String[] array) {
        List<String> list = new ArrayList<>(Arrays.asList(array));
        list.remove(0);
        return list;
    }
}
