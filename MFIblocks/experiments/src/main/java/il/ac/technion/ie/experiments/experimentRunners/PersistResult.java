package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.Utils.ExpFileUtils;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.parsers.SerializerUtil;
import il.ac.technion.ie.experiments.service.ParsingService;
import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PersistResult {

    private static final Logger logger = Logger.getLogger(PersistResult.class);

    public static void saveConvexBPResultsToCsv(DuplicateReductionContext duplicateReductionContext) {
        File expResults = ExpFileUtils.createOutputFile(generateFileName());

        if (expResults != null) {
            logger.info("saving results of ExperimentsWithCanopy");
            new ParsingService().writeExperimentsMeasurements(duplicateReductionContext, expResults);
        } else {
            logger.warn("Failed to create file for measurements therefore no results are results will be given");
        }
        logger.info("Finished saving results of ExperimentsWithCanopy");
    }

    private static String generateFileName() {
        LocalDateTime time = new LocalDateTime();
        return String.format("convexBPResults_%d_%d_%d_%d-%d-%d_%d.csv", time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
                time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute(),
                time.getMillisOfSecond());
    }

    public static void saveConvexBPResultsToCsv(Multimap<String, DuplicateReductionContext> results, boolean printSingleBlocsType) {
        File expResults = ExpFileUtils.createOutputFile(generateFileName());

        if (expResults != null) {
            logger.info("saving results of ExperimentsWithCanopy");
            ParsingService parsingService = new ParsingService();
            if (printSingleBlocsType) {
                parsingService.writeExperimentsMeasurements(results, expResults);
            } else {
                parsingService.writeComparisonExperimentsMeasurements(results, expResults);
            }
        } else {
            logger.warn("Failed to create file for measurements therefore no results are results will be given");
        }
        logger.info("Finished saving results of ExperimentsWithCanopy");

    }

    public static void saveBlocksToDisk(List<BlockWithData> blocks, String datasetName, String blockNamePrefix) {
        try {
            File blocksFolder = ExpFileUtils.createOrGetFolder(datasetName);
            File blocksSerializeFile = ExpFileUtils.createBlocksFile(blocksFolder, blockNamePrefix);
            SerializerUtil.serialize(blocksSerializeFile, blocks);
        } catch (IOException e) {
            logger.error("Failed to serialize blocks", e);
        }
    }
}