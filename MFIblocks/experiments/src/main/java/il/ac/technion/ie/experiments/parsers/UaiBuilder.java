package il.ac.technion.ie.experiments.parsers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.experiments.Utils.ExpFileUtils;
import il.ac.technion.ie.experiments.exception.KeyNotExistException;
import il.ac.technion.ie.experiments.exception.NoValueExistsException;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.UaiVariableContext;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.MatrixCell;
import il.ac.technion.ie.potential.model.MatrixContext;
import il.ac.technion.ie.potential.model.SharedMatrix;
import il.ac.technion.ie.potential.service.PotentialService;
import il.ac.technion.ie.potential.service.iPotentialService;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by I062070 on 10/09/2015.
 */
public class UaiBuilder {

    public static final String UAI = ".uai";

    private static final Logger logger = Logger.getLogger(UaiBuilder.class);
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final char SPACE = ' ';

    private final iPotentialService potentialService;
    private File file;
    private List<BlockWithData> blocks;
    private List<MatrixContext<SharedMatrix>> matricesWithContext;

    public UaiBuilder(List<BlockWithData> blocks) {
        potentialService = new PotentialService();
        this.blocks = blocks;
    }

    public UaiVariableContext createUaiFile() throws SizeNotEqualException {
        file = createOutputFile();
        matricesWithContext = createSharedMatrices();

        int numberOfVariables = countNumberOfVariables();
        logger.info("Number Of Variables: " + numberOfVariables);
        logger.info("Number of Blocks:" + blocks.size());
        logger.info("Number Of Shared Matrices: " + matricesWithContext.size());
        if (numberOfVariables != (blocks.size() + matricesWithContext.size())) {
            throw new SizeNotEqualException("#Variables is not equal to the sum of (#Blocks + #SharedMatrices)");
        }

        UaiVariableContext variableContext = UaiVariableContext.createUaiVariableContext(blocks, matricesWithContext, file);

        try {
            writeMarkov();
            writeNumberOfVariables(numberOfVariables);
            writeSizeOfEachVariable(variableContext.getSizeOfVariables());
            writeNumberOfVariables(numberOfVariables);
            writeVariableSizeAndIndecies(variableContext.getSizeAndIndexOfVariables());
            writeBlocksProbabilities(variableContext);
            writeCliquesSharedMatrix(variableContext);

        } catch (IOException | KeyNotExistException | SizeNotEqualException | NoValueExistsException e) {
            logger.error("Failed to create UAI File", e);
        }
        return variableContext;
    }

    private void writeCliquesSharedMatrix(UaiVariableContext variableContext) throws IOException, SizeNotEqualException, NoValueExistsException {
        String cliquesSharedMatrix = buildCliquesAndSharedMatrix(variableContext);
        this.appendStringToFile(cliquesSharedMatrix);
    }

    private String buildCliquesAndSharedMatrix(UaiVariableContext variableContext) throws SizeNotEqualException, NoValueExistsException {
        StringBuilder builder = new StringBuilder();
        List<Integer> variablesIdsSorted = variableContext.getVariablesIdsWithSharedMatricesSorted();
        for (Integer variableId : variablesIdsSorted) {
            int variableSize = variableContext.getSharedMatrixSizeByVariableId(variableId);
            builder.append(NEW_LINE);
            builder.append(variableSize);
            builder.append(NEW_LINE);
            SharedMatrix sharedMatrix = variableContext.getSharedMatrixByVariableId(variableId);
            if (sharedMatrix == null) {
                throw new NoValueExistsException(String.format("Variable with ID: '%d'doesn't have a variableId", variableId));
            }
            addProbsOfMatrix(builder, sharedMatrix);
        }
        return builder.toString();
    }

    private void addProbsOfMatrix(StringBuilder builder, SharedMatrix sharedMatrix) {
        int numberOfRows = sharedMatrix.numberOfRows();
        for (int rowIndex = 0; rowIndex < numberOfRows; rowIndex++) {
            List<Integer> rowValues = sharedMatrix.viewRow(rowIndex);
            this.addProbsOfBlock(builder, rowValues);
        }
    }

    private void writeBlocksProbabilities(UaiVariableContext variableContext) throws KeyNotExistException, IOException, SizeNotEqualException {
        String stringOfBlocksAndProbabilities = buildStringOfBlocksAndProbabilities(variableContext);
        this.appendStringToFile(stringOfBlocksAndProbabilities.toString());
    }

    private String buildStringOfBlocksAndProbabilities(UaiVariableContext variableContext) throws KeyNotExistException, SizeNotEqualException {
        StringBuilder builder = new StringBuilder();
        BiMap<Integer, Integer> variableIdToBlockId = variableContext.getVariableIdToBlockId();
        List<Integer> variablesIds = variableContext.getVariablesIdsSorted();

        for (Integer variablesId : variablesIds) {
            Integer cliqueID = variableIdToBlockId.get(variablesId);
            if (isCliqueABlock(variableIdToBlockId, cliqueID)) {
                List<Double> probsOfBlockByID = variableContext.getProbsOfBlockByID(cliqueID);
                if (probsOfBlockByID.size() != variableContext.getSizeOfBlockById(cliqueID)) {
                    throw new SizeNotEqualException(String.format("size of variableID '%d' is: %d and not :%d",
                            cliqueID, probsOfBlockByID.size(), variableContext.getSizeOfBlockById(cliqueID)));
                }
                builder.append(probsOfBlockByID.size());
                builder.append(NEW_LINE);
                addProbsOfBlock(builder, probsOfBlockByID);
            }
        }
        return builder.toString();
    }

    private <T> void addProbsOfBlock(StringBuilder builder, List<T> list) {
        for (T probability : list) {
            builder.append(SPACE);
            builder.append(probability);
        }
        builder.append(NEW_LINE);
    }

    private boolean isCliqueABlock(BiMap<Integer, Integer> variableIdToBlockId, Integer cliqueID) {
        return variableIdToBlockId.containsValue(cliqueID);
    }

    private File createOutputFile() {
        String filePath = ExpFileUtils.getOutputFilePath("Blocks", UAI);
        return ExpFileUtils.createFile(filePath);
    }

    /**
     * The method creates the sharedMatrices with context based on the Blocks.
     */
    private List<MatrixContext<SharedMatrix>> createSharedMatrices() {
        return potentialService.getSharedMatricesWithContext(blocks);
    }


    private int countNumberOfVariables() {
        AdjustedMatrix adjustedMatrix = potentialService.getAdjustedMatrix(blocks);
        List<MatrixCell<Double>> cellsCongaingNonZeroValue = adjustedMatrix.getCellsCongaingNonZeroValue();

        int numberOfVariables = adjustedMatrix.size();
        numberOfVariables += countCliques(cellsCongaingNonZeroValue);
        return numberOfVariables;
    }

    /**
     * The method count the number of cliques.
     * <p>
     * <code>Clique</code> is defined as a block who at least one of his records appears also in another block.
     * If Block_1 has records that appear also in Block_M and block_N the number of cliques will be 2.
     * </p>
     *
     * @param cellsCongaingNonZeroValue a List of all {@link il.ac.technion.ie.potential.model.MatrixCell} who don't contain
     *                                  0, and therefore represents a record that appears in two blocks.
     * @return
     */
    private int countCliques(List<MatrixCell<Double>> cellsCongaingNonZeroValue) {
        HashMultimap<Integer, Integer> multimap = HashMultimap.create(cellsCongaingNonZeroValue.size(), 2);
        for (MatrixCell<Double> cell : cellsCongaingNonZeroValue) {
            multimap.put(cell.getRowPos(), cell.getColPos());
        }
        /*
            Since the matrix who generated the list is symmetric, every two cells represents the same clique.
            The same clique who is represented by cell<i,j> is also represented by cell<j,i>
         */
        return multimap.values().size() / 2;
    }

    private void writeMarkov() throws IOException {
        FileUtils.writeStringToFile(file, "MARKOV" + "\n", Charset.defaultCharset());
    }

    private void writeNumberOfVariables(int numberOfVariables) throws IOException {
        appendStringToFile(String.valueOf(numberOfVariables));
    }

    private void writeSizeOfEachVariable(List<Integer> sizeOfVariables) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (Integer sizeOfVariable : sizeOfVariables) {
            builder.append(sizeOfVariable);
            builder.append(" ");
        }
        builder.deleteCharAt(builder.length() - 1);
        appendStringToFile(builder.toString());
    }

    private void writeVariableSizeAndIndecies(Multimap<Integer, Integer> sizeAndIndexOfVariables) throws IOException, NoValueExistsException {
        String sizeAndIndecies = buildStringOfVariableSizeAndIndecies(sizeAndIndexOfVariables);
        appendStringToFile(sizeAndIndecies);
    }

    private String buildStringOfVariableSizeAndIndecies(Multimap<Integer, Integer> sizeAndIndexOfVariables) throws NoValueExistsException {
        Map<Integer, Collection<Integer>> map = sizeAndIndexOfVariables.asMap();
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, Collection<Integer>> entry : map.entrySet()) {
            Integer variableSize = entry.getKey();
            Iterator<Integer> variableIDsIterator = entry.getValue().iterator();

            addVariableSizeEntries(builder, variableSize, variableIDsIterator);
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(NEW_LINE);
        return builder.toString();
    }

    /**
     * The method adds entries, line by line of block\clique size and the variableID
     * in the following format: {@code <SIZE> <variableID>,...,<variableID>}
     *
     * @param builder
     * @param variableSize
     * @param variableIDsIterator
     */
    private void addVariableSizeEntries(StringBuilder builder, Integer variableSize, Iterator<Integer> variableIDsIterator) throws NoValueExistsException {
        while (variableIDsIterator.hasNext()) {
            builder.append(variableSize);
            builder.append(SPACE);
            for (int i = 0; i < variableSize; i++) {
                if (!variableIDsIterator.hasNext()) {
                    throw new NoValueExistsException("A variable doesn't have any blocks\\cliques");
                }
                Integer variableID = variableIDsIterator.next();
                builder.append(variableID);
                builder.append(SPACE);
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(NEW_LINE);
        }
    }

    private void appendStringToFile(String str) throws IOException {
        FileUtils.writeStringToFile(file, str + "\n", Charset.defaultCharset(), true);
    }
}
