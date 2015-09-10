package il.ac.technion.ie.experiments.apiAccess;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.ExprimentsService;
import il.ac.technion.ie.experiments.service.ParsingService;
import il.ac.technion.ie.experiments.service.ProbabilityService;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;

import java.io.File;
import java.util.List;

/**
 * Created by I062070 on 27/08/2015.
 */
public class ExperimentRunner {

    private ParsingService parsingService;
    private ProbabilityService probabilityService;
    private iMeasurService measurService;
    private ExprimentsService exprimentsService;

    public ExperimentRunner() {
        parsingService = new ParsingService();
        probabilityService = new ProbabilityService();
        measurService = new MeasurService();
        exprimentsService = new ExprimentsService();
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.err.println("There are no file arguments!");
            System.exit(-1);
        }
        String datasetFile = args[0];
        ExperimentRunner experimentRunner = new ExperimentRunner();
        experimentRunner.runExp(datasetFile);
    }

    public void runExp(String datasetPath) {
        List<BlockWithData> blockWithDatas = parsingService.parseDataset(datasetPath);
        probabilityService.calcProbabilitiesOfRecords(blockWithDatas);
        double rankedValue = measurService.calcRankedValue(blockWithDatas);
        double mrr = measurService.calcMRR(blockWithDatas);
        System.out.println("The RankedValue is: " + rankedValue);
        System.out.println("The MRR score is: " + mrr);
        String allBlocksFilePath = getOutputFilePath("AllBlocks");
        parsingService.writeBlocks(blockWithDatas, allBlocksFilePath);
        if (rankedValue > 0 || mrr < 1) {
            List<BlockWithData> filteredBlocks = exprimentsService.filterBlocksWhoseTrueRepIsNotFirst(blockWithDatas);
            String outputFilePath = getOutputFilePath("BlocksWhereMillerWasWrong");
            parsingService.writeBlocks(filteredBlocks, outputFilePath);
            System.out.print("Total of " + filteredBlocks.size() + " blocks representative is wrong. ");
            System.out.println("output file can be found at: " + outputFilePath);
        }
    }

    private String getOutputFilePath(String fileName) {
        File runningDir = new File(System.getProperty("user.dir"));
        File parentFile = runningDir.getParentFile();
        return parentFile.getAbsolutePath() + File.separator + fileName + ".csv";
    }


}
