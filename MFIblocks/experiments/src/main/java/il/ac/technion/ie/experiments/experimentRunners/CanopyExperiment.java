package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.algorithm.Canopy;
import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.exception.InvalidSearchResultException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.Utils.ExpFileUtils;
import il.ac.technion.ie.experiments.exception.NoValueExistsException;
import il.ac.technion.ie.experiments.exception.OSNotSupportedException;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.CanopyService;
import il.ac.technion.ie.experiments.service.Measurements;
import il.ac.technion.ie.experiments.threads.CommandExacter;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by I062070 on 30/12/2015.
 */
public class CanopyExperiment extends AbstractExperiment {

    protected CanopyService canopyService;
    private static final Logger logger = Logger.getLogger(CanopyExperiment.class);


    public CanopyExperiment() {
        super();
        this.canopyService = new CanopyService();
    }

    @Override
    public void runExperiments(String pathToDatasetFile) {
        List<BlockWithData> cleanBlocks = parsingService.parseDataset(pathToDatasetFile);
        this.measurements = new Measurements(cleanBlocks.size());

        DuplicateReductionContext reductionContext = null;
        try {
            reductionContext = canopyCoreExperiment(cleanBlocks);
        } catch (InvalidSearchResultException | CanopyParametersException e) {
            logger.error("Failed to execute experiment", e);
        }
        if (reductionContext != null) {
            saveConvexBPResultsToCsv(reductionContext);
        }
    }

    protected DuplicateReductionContext canopyCoreExperiment(List<BlockWithData> cleanBlocks) throws CanopyParametersException, InvalidSearchResultException {
        List<Record> records = getRecordsFromBlcoks(cleanBlocks);
        Canopy canopy = new Canopy(records, 0.15, 0.05);
        canopy.initSearchEngine(new CanopyInteraction());
        List<CanopyCluster> canopies = canopy.createCanopies();
        List<BlockWithData> dirtyBlocks = canopyService.convertCanopiesToBlocks(canopies);
        logger.info("Converted " + dirtyBlocks.size() + " canopies to blocks. " + (canopies.size() - dirtyBlocks.size()) + " were of size 1 and therefore removed");
        super.calculateMillerResults(dirtyBlocks);
        Multimap<Record, BlockWithData> millerRepresentatives = exprimentsService.fetchRepresentatives(dirtyBlocks);
        DuplicateReductionContext reductionContext = null;
        try {
            boolean convexBP = super.runConvexBP(new CommandExacter(), 0.0, dirtyBlocks);
            if (!convexBP) {
                logger.error("Failed to run ConvexBP on canopy clusters");
                System.exit(1);
            }
            Multimap<Record, BlockWithData> convexBPRepresentatives = exprimentsService.fetchRepresentatives(dirtyBlocks);
            reductionContext = measurements.representativesDuplicateElimination(
                    millerRepresentatives, convexBPRepresentatives);
            BiMap<Record, BlockWithData> trueRepsMap = canopyService.getAllTrueRepresentatives(cleanBlocks);
            measurements.representationDiff(trueRepsMap.keySet(), convexBPRepresentatives.keySet(), reductionContext);
            measurements.calcPowerOfRep(trueRepsMap, convexBPRepresentatives, reductionContext);
            measurements.calcWisdomCrowds(trueRepsMap.values(), new HashSet<>(convexBPRepresentatives.values()), reductionContext);
            reductionContext.setNumberOfDirtyBlocks(dirtyBlocks.size());

        } catch (SizeNotEqualException e) {
            logger.error("Failed to create probabilities matrices for convexBP");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Cannot create context for ConvexBP algorithm", e);
        } catch (OSNotSupportedException e) {
            logger.error("Cannot run ConvexBP algorithm on current machine", e);
        } catch (InterruptedException e) {
            logger.error("Failed to wait till the execution of ConvexBP algorithm has finished", e);
        } catch (NoValueExistsException e) {
            logger.error("Failed to consume new probabilities", e);
        }
        return reductionContext;
    }

    private void saveConvexBPResultsToCsv(DuplicateReductionContext duplicateReductionContext) {
        File expResults = ExpFileUtils.createOutputFile("convexBPResults.csv");
        if (expResults != null) {
            logger.info("saving results of ExperimentsWithCanopy");
            parsingService.writeExperimentsMeasurements(duplicateReductionContext, expResults);
        } else {
            logger.warn("Failed to create file for measurements therefore no results are results will be given");
        }
        logger.info("Finished saving results of ExperimentsWithCanopy");
    }

    private List<Record> getRecordsFromBlcoks(List<BlockWithData> cleanBlocks) {
        Set<Record> set = new HashSet<>();
        for (BlockWithData cleanBlock : cleanBlocks) {
            set.addAll(cleanBlock.getMembers());
        }
        return new ArrayList<>(set);
    }
}
