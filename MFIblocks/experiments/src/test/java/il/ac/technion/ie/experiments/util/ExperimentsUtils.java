package il.ac.technion.ie.experiments.util;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.FuzzyService;
import il.ac.technion.ie.experiments.service.ParsingService;
import il.ac.technion.ie.experiments.service.ProbabilityService;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static List<BlockWithData> createFuzzyBlocks() throws URISyntaxException {
        String recordsFile = ExperimentsUtils.getPathToSmallRecordsFile();

        ParsingService parsingService = new ParsingService();
        ProbabilityService probabilityService = new ProbabilityService();
        FuzzyService fuzzyService = initFuzzyService();

        List<BlockWithData> originalBlocks = parsingService.parseDataset(recordsFile);
        probabilityService.calcProbabilitiesOfRecords(originalBlocks);

        List<BlockWithData> copyOfOriginalBlocks = new ArrayList<>(originalBlocks);
        List<BlockWithData> fuzzyBlocks = fuzzyService.splitBlocks(copyOfOriginalBlocks, 0.6);
        probabilityService.calcProbabilitiesOfRecords(fuzzyBlocks);

        return fuzzyBlocks;
    }

    private static FuzzyService initFuzzyService() {
        FuzzyService fuzzyService = new FuzzyService();
        UniformRealDistribution uniformRealDistribution = PowerMockito.mock(UniformRealDistribution.class);
        PowerMockito.when(uniformRealDistribution.sample()).thenReturn(0.3, 0.7, 0.6, 0.4);
        Whitebox.setInternalState(fuzzyService, "splitBlockProbThresh", uniformRealDistribution);
        return fuzzyService;
    }
}
