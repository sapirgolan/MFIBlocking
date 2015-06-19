package il.ac.technion.ie.measurements.roc;

import cern.colt.matrix.DoubleMatrix1D;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;
import il.ac.technion.ie.measurements.type.CellType;
import il.ac.technion.ie.model.Block;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by I062070 on 18/06/2015.
 */
public class RocCurve {
    private static final Logger logger = Logger.getLogger(RocCurve.class);
    private final List<Block> algorithmBlocks;
    private final List<Block> trueBlocks;
    private DoubleMatrix1D algSimilarityVector;
    private DoubleMatrix1D trueSimilarityVector;
    private iMeasurService measurService;
    private TreeMap<Double, Double> curveMap;


    public RocCurve(List<Block> algorithmBlocks, List<Block> trueBlocks) {
        this.algorithmBlocks = algorithmBlocks;
        this.trueBlocks = trueBlocks;
        curveMap = new TreeMap<>();

        this.invoke();
    }

    private void invoke() {
        measurService = new MeasurService();
        algSimilarityVector = measurService.buildSimilarityVector(algorithmBlocks, CellType.SIMILARITY);
        trueSimilarityVector = measurService.buildSimilarityVector(trueBlocks, CellType.SIMILARITY);

        this.calcRoc();
    }

    private void calcRoc() {
        RocMatcher rocMatcher = new RocMatcher();
        for (double threshold = 0; threshold < 1; threshold += 0.01) {
            rocMatcher.setThreshold(threshold);
            DoubleMatrix1D resultsCopy = applyThreshold(trueSimilarityVector, rocMatcher);
            updateRocCurve(resultsCopy);
        }
    }

    private void updateRocCurve(DoubleMatrix1D resultsCopy) {
        double positiveRate = measurService.calcTruePositiveRate(resultsCopy, trueSimilarityVector);
        double falsePositiveRate = measurService.calcFalsePositiveRate(resultsCopy, trueSimilarityVector);
        if (!curveMap.containsKey(falsePositiveRate)) {
            logger.debug(String.format("Updating Positive Rate%s at position False Positive Rate=%s",
                    positiveRate, falsePositiveRate));

            curveMap.put(falsePositiveRate, positiveRate);
        } else {
            logger.info(String.format("roc already has %s at False Positive Rate%sTherefore not updating the value to %s",
                    curveMap.get(falsePositiveRate), falsePositiveRate, positiveRate));
        }
    }

    private DoubleMatrix1D applyThreshold(DoubleMatrix1D trueSimilarityVector, RocMatcher rocMatcher) {
        DoubleMatrix1D resultsCopy = trueSimilarityVector.copy();
        resultsCopy.assign(rocMatcher);
        return resultsCopy;
    }

    public Map<Double, Double> getCordinatesForPlot() {
        return curveMap;
    }

  /*  public double getAreaUnderCurve() {

    }*/
}
