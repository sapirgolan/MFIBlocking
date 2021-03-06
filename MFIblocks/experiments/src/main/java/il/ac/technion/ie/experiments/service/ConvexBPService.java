package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.exception.NoValueExistsException;
import il.ac.technion.ie.experiments.exception.OSNotSupportedException;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.ConvexBPContext;
import il.ac.technion.ie.experiments.model.UaiVariableContext;
import il.ac.technion.ie.experiments.parsers.UaiBuilder;
import il.ac.technion.ie.experiments.threads.CommandExacter;
import il.ac.technion.ie.experiments.threads.IConvexBPExecutor;
import il.ac.technion.ie.experiments.utils.ExperimentUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by I062070 on 04/01/2017.
 */
public class ConvexBPService {

    private static final Logger logger = Logger.getLogger(ConvexBPService.class);
    protected final ExprimentsService exprimentsService;

    public ConvexBPService() {
        this.exprimentsService = new ExprimentsService();
    }

    private boolean runConvexBPInternally (IConvexBPExecutor commandExacter, Double threshold, List<BlockWithData> splitedBlocks) throws SizeNotEqualException, IOException, OSNotSupportedException, InterruptedException, NoValueExistsException, ExecutionException {
        UaiBuilder uaiBuilder = new UaiBuilder(splitedBlocks);
        logger.debug("creating UAI file");
        logger.info("Create UAI context. HeapSize = " + ExperimentUtils.humanReadableByteCount());
        UaiVariableContext uaiVariableContext = uaiBuilder.createUaiContext();
        logger.debug("UAI file was created at: " + uaiVariableContext.getUaiFile().getAbsoluteFile());
        logger.info("Create convexBP context. HeapSize = " + ExperimentUtils.humanReadableByteCount());
        ConvexBPContext convexBPContext = exprimentsService.createConvexBPContext(uaiVariableContext);
        convexBPContext.setThreshold(threshold);
        //critical section - cannot be multi-thread
        File outputFile = commandExacter.execute(convexBPContext);
        if (outputFile.exists()) {
            logger.debug("Binary output of convexBP was created on: " + outputFile.getAbsolutePath());
            UaiConsumer uaiConsumer = new UaiConsumer(uaiVariableContext, outputFile);
            uaiConsumer.consumePotentials();
            FileUtils.forceDeleteOnExit(outputFile);
            logger.debug("Applying new probabilities on blocks");
            uaiConsumer.applyNewProbabilities(splitedBlocks);
            ExperimentUtils.printBlocks(splitedBlocks, "Blocks and their Representatives according to ConvexBP");
            return true;
        }
        return false;
    }

    public boolean runConvexBP(IConvexBPExecutor commandExacter, Double threshold, List<BlockWithData> splitedBlocks) {
        boolean didConvexBPRan = false;
        try {
            didConvexBPRan = this.runConvexBPInternally(commandExacter, threshold, splitedBlocks);
        }
        catch (SizeNotEqualException e) {
            logger.error("Failed to create probabilities matrices for convexBP");
        } catch (IOException e) {
            logger.error("Cannot create context for ConvexBP algorithm", e);
        } catch (OSNotSupportedException e) {
            logger.error("Cannot run ConvexBP algorithm on current machine", e);
        } catch (InterruptedException e) {
            logger.error("Failed to wait till the execution of ConvexBP algorithm has finished", e);
        } catch (NoValueExistsException e) {
            logger.error("Failed to consume new probabilities", e);
        } catch (ExecutionException e) {
            logger.error("Failed to execute convexBP as a command line", e);
        }
        return didConvexBPRan;
    }
}
