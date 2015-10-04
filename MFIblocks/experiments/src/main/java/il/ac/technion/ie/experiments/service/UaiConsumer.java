package il.ac.technion.ie.experiments.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import il.ac.technion.ie.experiments.exception.NoValueExistsException;
import il.ac.technion.ie.experiments.model.UaiVariableContext;
import il.ac.technion.ie.experiments.parsers.ReadBinaryFile;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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


    public UaiConsumer(UaiVariableContext variableContext, File binaryFile) {
        this.variableContext = variableContext;
        this.binaryFile = binaryFile;
        this.readBinaryFile = new ReadBinaryFile();
    }

    public void consumePotentials() throws NoValueExistsException, FileNotFoundException {
        if (binaryFile == null) {
            throw new FileNotFoundException("binary fies doesn't exists");
            //throw exception
        }
        try {
            Map<Integer, Double> lineToProbabilityMap = readBinaryFile.readFile(binaryFile);
            variableIdToProbabilities = ArrayListMultimap.create();
            List<Integer> sizeOfVariables = variableContext.getSizeOfVariables();

            Iterator<Map.Entry<Integer, Double>> iterator = lineToProbabilityMap.entrySet().iterator();
            for (int variableId = 0; variableId < sizeOfVariables.size(); variableId++) {
                Integer size = sizeOfVariables.get(variableId);
                logger.debug(String.format("Adding %d probabilities to variableId #%d", size, variableId));
                List<Double> probs = fillProbsForVariable(iterator, size);
                logger.debug(String.format("Added total of %d probabilities to variableId #%d", probs.size(), variableId));
                variableIdToProbabilities.putAll(variableId, probs);
            }

            mapBlocksToProbabilities();
        } catch (FileNotFoundException e) {
            logger.error("Binary files doesn't exists " + binaryFile.getAbsolutePath(), e);
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
