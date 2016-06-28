package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.exception.InvalidSearchResultException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.apiAccess.TimeLogger;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.parsers.SerializerUtil;
import il.ac.technion.ie.experiments.service.Measurements;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 22/01/2016.
 */
public class CreateCanopies extends CanopyExperiment {
    private static final Logger logger = Logger.getLogger(CreateCanopies.class);
    private static final int NUMBER_OF_EXPERIMENTS = 5;
    public static final String FEBRL_PARAM = "FebrlParam_";

    @Override
    public void runExperiments(String pathToDatasetFile) {
        logger.info("Starting Febrl Experiment");
        Collection<File> datasets = exprimentsService.findDatasets(pathToDatasetFile, true);
        logger.info(String.format("There're %d under experiment", datasets.size()));
        Table<String, List<BlockWithData>, Integer> dirToDatasetToFebrlParamTable = parseDatasetsToListsOfBlocks(datasets);

        //for each dataset, the experiment NUMBER_OF_EXPERIMENTS
        Map<String, Map<List<BlockWithData>, Integer>> map = dirToDatasetToFebrlParamTable.rowMap();
        for (Map.Entry<String, Map<List<BlockWithData>, Integer>> entry : map.entrySet()) { //each Febrl parameter
            Map<List<BlockWithData>, Integer> datasetToFebrlParamMap = entry.getValue();
            for (List<BlockWithData> cleanBlocks : datasetToFebrlParamMap.keySet()) { //for each dataset
                String dirName = buildDirPath(entry.getKey(), datasetToFebrlParamMap.get(cleanBlocks));
                File directory;
                try {
                    directory = createDirectory(dirName);
                    for (int i = 0; i < NUMBER_OF_EXPERIMENTS; i++) { //repeat experiment several times
                        measurements = new Measurements(cleanBlocks.size());
                        logger.debug(String.format("Executing #%d out of %d experiments", i, NUMBER_OF_EXPERIMENTS));
                        try {
                            String FileName = "Canopy_" + i;
                            File canopiesFile = new File(directory, FileName);
                            long startTime = System.nanoTime();
                            Collection<CanopyCluster> canopies = super.createCaniopies(cleanBlocks);
                            TimeLogger.logDurationInSeconds(startTime, "Creation of canopies with some configuration took");
                            logger.debug(canopies.size() + " were created");
                            SerializerUtil.serializeCanopies(canopiesFile, canopies);
                            logger.debug("Finished serializing Canopies");
                        } catch (InvalidSearchResultException | CanopyParametersException e) {
                            logger.error("Failed to run single canopy experiment", e);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Failed to create Dir to write canopies in", e);
                }

            }
        }
    }

    private String buildDirPath(String datasetName, int febrlNumericParam) {
        String rootDirectoryPath = FileUtils.getUserDirectoryPath();
        if (!StringUtils.endsWith(rootDirectoryPath, File.separator)) {
            rootDirectoryPath = rootDirectoryPath + File.separator;
        }
        String febrlParam = FEBRL_PARAM + febrlNumericParam;

        return rootDirectoryPath + datasetName + File.separator + febrlParam;
    }

    private File createDirectory(String dirPath) throws IOException {
        File dir = new File(dirPath);
        if (dir.exists() ) {
            if (dir.isFile()) {
                FileUtils.forceDelete(dir);
            } else {
                FileUtils.cleanDirectory(dir);
            }
        }
        FileUtils.forceMkdir(dir);
        return dir;
    }

    private Table<String, List<BlockWithData>, Integer> parseDatasetsToListsOfBlocks(Collection<File> datasets) {
        Table<String, List<BlockWithData>, Integer> filesTable = HashBasedTable.create();
        for (File dataset : datasets) {
            Integer febrlParamValue = exprimentsService.getParameterValue(dataset);
            if (febrlParamValue != null) {
                logger.debug("Parsing dataset - '" + dataset.getAbsolutePath() + "'");
                List<BlockWithData> blocks = parsingService.parseDataset(dataset.getAbsolutePath());
                filesTable.put(dataset.getParentFile().getName(), blocks, febrlParamValue);
            } else {
                logger.error("Failed to determine Febrl ParamValue, therefore will not process file named " + dataset.getAbsolutePath());
            }
        }
        return filesTable;
    }


}
