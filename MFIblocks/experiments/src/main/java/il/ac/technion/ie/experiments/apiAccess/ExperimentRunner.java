package il.ac.technion.ie.experiments.apiAccess;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.ParsingService;
import il.ac.technion.ie.experiments.service.ProbabilityService;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;

import java.util.List;

/**
 * Created by I062070 on 27/08/2015.
 */
public class ExperimentRunner {

    private ParsingService parsingService;
    private ProbabilityService probabilityService;
    private iMeasurService measurService;

    public ExperimentRunner() {
        parsingService = new ParsingService();
        probabilityService = new ProbabilityService();
        measurService = new MeasurService();
    }

    public double runExp(String datasetPath) {
        List<BlockWithData> blockWithDatas = parsingService.parseDataset(datasetPath);
        probabilityService.calcProbabilitiesOfRecords(blockWithDatas);
        return measurService.calcRankedValue(blockWithDatas);
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.err.print("There are no file arguments");
        }
        String datasetFile = args[0];
        ExperimentRunner experimentRunner = new ExperimentRunner();
        double rankedValue = experimentRunner.runExp(datasetFile);
        System.out.print("The ranked value is: " + rankedValue);
    }
}
