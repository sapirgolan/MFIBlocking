package il.ac.technion.ie.experiments.experimentRunners;

import il.ac.technion.ie.experiments.Utils.ExpFileUtils;
import il.ac.technion.ie.experiments.model.BlockWithData;

import java.util.List;

/**
 * Created by I062070 on 30/12/2015.
 */
public class SimpleExperiment extends AbstractExperiment {

    @Override
    public void runExperiments(String pathToDatasetFile) {
        List<BlockWithData> blockWithDatas = parsingService.parseDataset(pathToDatasetFile);
        probabilityService.calcSimilaritiesAndProbabilitiesOfRecords(blockWithDatas);
        double rankedValue = measurService.calcRankedValue(blockWithDatas);
        double mrr = measurService.calcMRR(blockWithDatas);
        System.out.println("The RankedValue is: " + rankedValue);
        System.out.println("The MRR score is: " + mrr);
        String allBlocksFilePath = ExpFileUtils.getOutputFilePath("AllBlocks", ".csv");
        parsingService.writeBlocks(blockWithDatas, allBlocksFilePath);
        if (rankedValue > 0 || mrr < 1) {
            List<BlockWithData> filteredBlocks = exprimentsService.filterBlocksWhoseTrueRepIsNotFirst(blockWithDatas);
            String outputFilePath = ExpFileUtils.getOutputFilePath("BlocksWhereMillerWasWrong", ".csv");
            parsingService.writeBlocks(filteredBlocks, outputFilePath);
            System.out.print("Total of " + filteredBlocks.size() + " blocks representative is wrong. ");
            System.out.println("output file can be found at: " + outputFilePath);
        }
    }
}
