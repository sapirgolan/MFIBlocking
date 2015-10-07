package il.ac.technion.ie.experiments.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import il.ac.technion.ie.experiments.exception.NoValueExistsException;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.UaiVariableContext;
import il.ac.technion.ie.experiments.parsers.ReadBinaryFile;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by I062070 on 04/10/2015.
 */
public class UaiConsumer {

    static final Logger logger = Logger.getLogger(UaiConsumer.class);


    private final File binaryFile;
    private UaiVariableContext variableContext;
    private ReadBinaryFile readBinaryFile;
    private ListMultimap<Integer, Double> variableIdToProbabilities;
    private ListMultimap<Integer, Double> blockIdToProbabilities;
    private boolean potentialConsumed;


    public UaiConsumer(UaiVariableContext variableContext, File binaryFile) {
        this.variableContext = variableContext;
        this.binaryFile = binaryFile;
        this.readBinaryFile = new ReadBinaryFile();
        this.potentialConsumed = false;
    }

    public boolean isPotentialConsumed() {
        return potentialConsumed;
    }

    public void consumePotentials() throws NoValueExistsException, FileNotFoundException {
        if (binaryFile == null) {
            throw new FileNotFoundException("binary fies doesn't exists");
        }
        try {
            Map<Integer, Double> lineToProbabilityMap = readBinaryFile.readFile(binaryFile);
            variableIdToProbabilities = ArrayListMultimap.create();
            List<Integer> sizeOfVariables = variableContext.getSizeOfVariables();

            logger.debug("Mapping variables to probabilities");
            Iterator<Map.Entry<Integer, Double>> iterator = lineToProbabilityMap.entrySet().iterator();
            for (int variableId = 0; variableId < sizeOfVariables.size(); variableId++) {
                Integer size = sizeOfVariables.get(variableId);
                logger.debug(String.format("Adding %d probabilities to variableId #%d", size, variableId));
                List<Double> probs = fillProbsForVariable(iterator, size);
                logger.debug(String.format("Added total of %d probabilities to variableId #%d", probs.size(), variableId));
                variableIdToProbabilities.putAll(variableId, probs);
            }

            logger.debug("Mapping blocksIds to probabilities");
            mapBlocksToProbabilities();
            potentialConsumed = true;
        } catch (FileNotFoundException e) {
            logger.error("Binary files doesn't exists at: " + binaryFile.getAbsolutePath(), e);
            throw e;
        }
        logger.info("Successfully finished to read probabilities from Binary file and map them to variables & blocks");
    }

    public void applyNewProbabilities(List<BlockWithData> blocks) throws FileNotFoundException, NoValueExistsException, SizeNotEqualException {
        assertPotentialsExist();
        long startTime = System.nanoTime();
        for (BlockWithData block : blocks) {
            logger.debug("Changing probabilities of block " + block.getId());
            int blockId = block.getId();
            List<Double> newProbabilities = blockIdToProbabilities.get(blockId);
            List<Record> sortedMembers = block.getSortedMembers();
            assertBlockSize(newProbabilities, sortedMembers);
            for (int i = 0; i < sortedMembers.size(); i++) {
                Record record = sortedMembers.get(i);
                float newProbability = newProbabilities.get(i).floatValue();
                logger.trace(String.format("Changing probability of '%s' from %s to %s",
                        record.getRecordName(), block.getMemberProbability(record), newProbability));
                block.setMemberProbability(record, newProbability);
            }
        }
        long endTime = System.nanoTime() - startTime;
        logger.info(String.format("Successfully finished to apply new probabilities on %d blocks in %d Millis",
                blocks.size(), TimeUnit.NANOSECONDS.toMillis(endTime)));
    }

    private void assertBlockSize(List<Double> newProbabilities, List<Record> sortedMembers) throws SizeNotEqualException {
        if (sortedMembers.size() != newProbabilities.size()) {
            throw new SizeNotEqualException(String.format("The number of blocks members (%d)is not equal to number of probabilities (%d)",
                    sortedMembers.size(), newProbabilities.size()));
        }
    }

    private void assertPotentialsExist() throws NoValueExistsException, FileNotFoundException {
        if (!potentialConsumed) {
            logger.info("Cannot apply new probabilities before Potential are consumed, consuming them now");
            this.consumePotentials();
        }
    }

    private void mapBlocksToProbabilities() {
        blockIdToProbabilities = ArrayListMultimap.create();
        for (Integer variableId : variableIdToProbabilities.keySet()) {
            Integer blockId = variableContext.getBlockIdByVariableId(variableId);
            if (blockId != null) {
                blockIdToProbabilities.putAll(blockId, variableIdToProbabilities.get(variableId));
            }
        }
    }

    private List<Double> fillProbsForVariable(Iterator<Map.Entry<Integer, Double>> iterator, Integer size) throws NoValueExistsException {
        List<Double> probs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            assertIterator(iterator);
            Double prob = iterator.next().getValue();
            logger.trace(String.format("Adding probability %s", prob));
            probs.add(prob);
        }
        return probs;
    }

    private void assertIterator(Iterator<Map.Entry<Integer, Double>> iterator) throws NoValueExistsException {
        if (!iterator.hasNext()) {
            throw new NoValueExistsException("Tried to fetch too many value from binary file");
        }
    }
}
