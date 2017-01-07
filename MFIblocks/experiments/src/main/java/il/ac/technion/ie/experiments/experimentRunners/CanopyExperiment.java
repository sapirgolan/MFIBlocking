package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.algorithm.Canopy;
import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.exception.InvalidSearchResultException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.CanopyService;
import il.ac.technion.ie.experiments.service.Measurements;
import il.ac.technion.ie.experiments.threads.CommandExacter;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.util.*;

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
            PersistResult.saveConvexBPResultsToCsv(reductionContext);
        }
    }

    protected DuplicateReductionContext canopyCoreExperiment(List<BlockWithData> cleanBlocks) throws CanopyParametersException, InvalidSearchResultException {
        Collection<CanopyCluster> canopies = createCaniopies(cleanBlocks);
        List<BlockWithData> dirtyBlocks = canopyService.convertCanopiesToBlocks(canopies);
        logger.info("Converted " + dirtyBlocks.size() + " canopies to blocks. " + (canopies.size() - dirtyBlocks.size()) + " were of size 1 and therefore removed");
        super.calculateMillerResults(dirtyBlocks);
        Multimap<Record, BlockWithData> millerRepresentatives = exprimentsService.fetchRepresentatives(dirtyBlocks);
        DuplicateReductionContext reductionContext = null;
        boolean convexBP = convexBPService.runConvexBP(new CommandExacter(), 0.0, dirtyBlocks);
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
        measurements.calcAverageBlockSize(dirtyBlocks, reductionContext);
        reductionContext.setNumberOfDirtyBlocks(dirtyBlocks.size());
        double dupsRealRepresentatives = measurements.duplicatesRealRepresentatives(millerRepresentatives, convexBPRepresentatives, trueRepsMap);
        reductionContext.setDuplicatesRealRepresentatives(dupsRealRepresentatives);

        return reductionContext;
    }

    protected Collection<CanopyCluster> createCaniopies(List<BlockWithData> cleanBlocks) throws CanopyParametersException, InvalidSearchResultException {
        List<Record> records = getRecordsFromBlcoks(cleanBlocks);
        Canopy canopy = new Canopy(records, 0.15, 0.05);
        canopy.initSearchEngine(new CanopyInteraction());
        Collection<CanopyCluster> canopies = canopy.createCanopies();
        logger.info("There are " + canopies.size() + " canopies in dataset");
        return canopies;
    }

    private List<Record> getRecordsFromBlcoks(List<BlockWithData> cleanBlocks) {
        Set<Record> set = new HashSet<>();
        for (BlockWithData cleanBlock : cleanBlocks) {
            set.addAll(cleanBlock.getMembers());
        }
        return new ArrayList<>(set);
    }
}
