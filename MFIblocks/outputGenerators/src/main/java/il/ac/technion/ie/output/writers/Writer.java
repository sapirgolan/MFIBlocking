package il.ac.technion.ie.output.writers;

import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.BlockDescriptor;
import il.ac.technion.ie.model.CandidatePairs;
import il.ac.technion.ie.potential.model.BlockPotential;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by XPS_Sapir on 11/07/2015.
 */
public class Writer {

    static final Logger logger = Logger.getLogger(Writer.class);

    /**
     * the method write the blocking output to a file for later usage
     * @param cps
     */
    public static void printNeighborsAndBlocks(CandidatePairs cps, MfiContext context, List<Block> blocks) {
        ResultWriter resultWriter = new ResultWriter();
        File neighborsOutputFile = resultWriter.createNeighborsOutputFile();
        File blocksOutputFile = resultWriter.createBlocksOutputFile(context.getDatasetName());

        try {
            switch (context.getPrntFormat().toLowerCase()) {
                case "s" :
                    resultWriter.writeBlocksStatistics(blocksOutputFile, cps, context);
                    break;
                case "b":
                    resultWriter.writeEachRecordNeighbors(blocksOutputFile, cps);
                    break;
                default:
                    logger.debug("No blocks were printed");
            }
            resultWriter.writeBlocks(blocksOutputFile, blocks);
            logger.info("Outfile was written to: " + neighborsOutputFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to write blocks", e);
        }
    }

    public static void printRocCurveDots(Map<Double, Double> cordinatesForPlot) {
        ResultWriter resultWriter = new ResultWriter();
        File rocOutputFile = resultWriter.createRocOutputFile();
        try {
            resultWriter.writeRocDots(rocOutputFile, cordinatesForPlot);
            logger.info("Finished writing ROC dots");
        } catch (IOException e) {
            logger.error("Failed to write ROC dots", e);
        }
    }

    public static void printAmbiguousRepresentatives(Map<Integer, List<BlockDescriptor>> blocksAmbiguousRepresentatives, MfiContext context) {
        ResultWriter resultWriter = new ResultWriter();
        File outputFile = resultWriter.createAmbiguousRepresentativesOutputFile(context.getDatasetName());
        try {
            resultWriter.writeAmbiguousRepresentatives(outputFile, blocksAmbiguousRepresentatives);
        } catch (IOException e) {
            logger.error("Failed to write the AmbiguousRepresentatives", e);
        }
    }

    public static void printBlockPotential(List<BlockPotential> potentials, MfiContext context) {
        PotentialWriter potentialWriter = new PotentialWriter();
        File file = potentialWriter.generateFile(context.getDatasetName(), ".csv");
        try {
            potentialWriter.writeResults(file, potentials);
        } catch (IOException e) {
            logger.error("Failed to write the Block Potential", e);
        }
    }
}
