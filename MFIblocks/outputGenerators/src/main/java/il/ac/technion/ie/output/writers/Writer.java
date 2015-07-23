package il.ac.technion.ie.output.writers;

import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.BlockDescriptor;
import il.ac.technion.ie.model.CandidatePairs;
import il.ac.technion.ie.output.strategy.block.BlockCsvFormat;
import il.ac.technion.ie.output.strategy.block.MatlabFormat;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPotential;
import il.ac.technion.ie.potential.model.SharedMatrix;
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
        File matlabOutputFile = resultWriter.createMillerOutputFile(context.getDatasetName());

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
            resultWriter.writeBlocks(blocksOutputFile, blocks, new BlockCsvFormat());
            resultWriter.writeBlocks(matlabOutputFile, blocks, new MatlabFormat());
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
        AmbiguousRepresentativesWriter writer = new AmbiguousRepresentativesWriter();
        File outputFile = writer.generateFile(context.getDatasetName(), ".csv");
        try {
            writer.writeResults(outputFile, blocksAmbiguousRepresentatives);
        } catch (IOException e) {
            logger.error("Failed to write the AmbiguousRepresentatives", e);
        }
    }

    public static void printBlockPotential(List<BlockPotential> potentials, AdjustedMatrix adjustedMatrix,
                                           List<SharedMatrix> sharedMatrices, MfiContext context) {
        PotentialWriter potentialWriter = new PotentialWriter();
        File blockPotentialFile = potentialWriter.generateFile(context.getDatasetName() + "_BlockPotential", ".csv");
        File adjustedMatrixFile = potentialWriter.generateFile(context.getDatasetName() + "_AdjustedMatrix", ".csv");

        try {
            potentialWriter.writeResults(blockPotentialFile, potentials);
            potentialWriter.writeResults(adjustedMatrixFile, adjustedMatrix);
            for (SharedMatrix sharedMatrix : sharedMatrices) {
                File sharedRecordsFile = potentialWriter.generateFile(
                        String.format("%s_SharedRecords_%s_", context.getDatasetName(), sharedMatrix.getName()),
                        ".csv");
                potentialWriter.writeResults(sharedRecordsFile, sharedMatrix);
            }

        } catch (IOException e) {
            logger.error("Failed to write the Block Potential", e);
        }
    }
}
