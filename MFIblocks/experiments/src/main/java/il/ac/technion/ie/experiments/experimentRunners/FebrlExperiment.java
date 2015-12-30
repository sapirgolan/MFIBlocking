package il.ac.technion.ie.experiments.experimentRunners;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.exception.InvalidSearchResultException;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.Utils.ExpFileUtils;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.Measurements;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * Created by I062070 on 30/12/2015.
 */
public class FebrlExperiment extends CanopyExperiment {

    private static final Logger logger = Logger.getLogger(FebrlExperiment.class);
    private static final int NUMBER_OF_EXPERIMENTS = 3;

    @Override
    public void runExperiments(String pathToDatasetFile) {
        Collection<File> datasets = exprimentsService.findDatasets(pathToDatasetFile, false);
        Map<List<BlockWithData>, Integer> datasetToFebrlParamMap = parseDatasetsToListsOfBlocks(datasets);
        Map<Integer, DuplicateReductionContext> experimentsResults = new HashMap<>();

        //for each dataset, the experiment NUMBER_OF_EXPERIMENTS
        for (List<BlockWithData> cleanBlocks : datasetToFebrlParamMap.keySet()) {
            List<DuplicateReductionContext> reductionContexts = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_EXPERIMENTS; i++) {
                measurements = new Measurements(cleanBlocks.size());
                logger.debug(String.format("Executing #%d out of %d experiments", i, NUMBER_OF_EXPERIMENTS));
                try {
                    DuplicateReductionContext reductionContext = super.canopyCoreExperiment(cleanBlocks);
                    reductionContexts.add(reductionContext);
                } catch (InvalidSearchResultException | CanopyParametersException e) {
                    logger.error("Failed to run single canopy experiment", e);
                }
            }
            DuplicateReductionContext avgReductionContext = avgAllReductionContext(reductionContexts);
            experimentsResults.put(datasetToFebrlParamMap.get(cleanBlocks), avgReductionContext);
        }
        saveFebrlResultsToCsv(experimentsResults);
    }

    private Map<List<BlockWithData>, Integer> parseDatasetsToListsOfBlocks(Collection<File> datasets) {
        Map<List<BlockWithData>, Integer> listIntegerHashMap = new HashMap<>();
        for (File dataset : datasets) {
            Integer febrlParamValue = exprimentsService.getParameterValue(dataset);
            if (febrlParamValue != null) {
                List<BlockWithData> blocks = parsingService.parseDataset(dataset.getAbsolutePath());
                listIntegerHashMap.put(blocks, febrlParamValue);
            } else {
                logger.error("Failed to determine Febrl ParamValue, therefore will not process file named " + dataset.getAbsolutePath());
            }
        }
        return listIntegerHashMap;
    }

    private void saveFebrlResultsToCsv(Map<Integer, DuplicateReductionContext> map) {
        File expResults = ExpFileUtils.createOutputFile("CanopyExpResults.csv");
        if (expResults != null) {
            logger.info("saving results of ExperimentsWithCanopy");
            parsingService.writeExperimentsMeasurements(map, expResults);
        } else {
            logger.warn("Failed to create file for measurements therefore no results are results will be given");
        }
    }

    private DuplicateReductionContext avgAllReductionContext(List<DuplicateReductionContext> reductionContexts) {
        float size = reductionContexts.size();
        int duplicatesRemoved = 0,
                representationDiff = 0;
        double representativesPower = 0,
                wisdomCrowds = 0;

        for (DuplicateReductionContext reductionContext : reductionContexts) {
            duplicatesRemoved += reductionContext.getDuplicatesRemoved();
            representationDiff += reductionContext.getRepresentationDiff();
            representativesPower += reductionContext.getRepresentativesPower();
            wisdomCrowds += reductionContext.getWisdomCrowds();
        }

        return new DuplicateReductionContext(duplicatesRemoved / size, representationDiff / size,
                representativesPower / size, wisdomCrowds / size);
    }
}
