package il.ac.technion.ie.output.writers;

import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPotential;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by XPS_Sapir on 11/07/2015.
 */
public class PotentialWriter extends AbstractWriter {
    @Override
    public File generateFile(String fileName, String fileExtention) {
        File file = this.createUniqueOutputFile("/BlockPotential_" + fileName, fileExtention);
        return file;
    }

    @Override
    public void writeResults(File file, Object... other) throws IOException {

        for (Object o : other) {
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            List<BlockPotential> blockPotentials = isBlockPotential(o);
            AdjustedMatrix adjustedMatrix = isType(o, AdjustedMatrix.class);
            if (blockPotentials != null) {
                for (BlockPotential blockPotential : blockPotentials) {
                    String csvEntry = generateCsvRow(blockPotential);
                    bufferedWriter.write(csvEntry);
                    bufferedWriter.newLine();
                }
            }
            if (adjustedMatrix != null) {
                for (int i = 0; i < adjustedMatrix.size(); i++) {
                    List<Integer> rowValues = adjustedMatrix.viewRow(i);
                    String csvEntry = generateCsvRow(rowValues);
                    bufferedWriter.write(csvEntry);
                    bufferedWriter.newLine();
                }
            }
            bufferedWriter.close();
        }
    }

    private List<BlockPotential> isBlockPotential(Object parameter) {
        List list = isType(parameter, ArrayList.class);
        if (list != null && !list.isEmpty()) {
            BlockPotential blockPotential = isType(list.get(0), BlockPotential.class);
            if (blockPotential != null) {
                return (List<BlockPotential>) list;
            }
        }
        return null;
    }

    private <T> T isType(Object parameter, Class<T> tClass){
        if (parameter.getClass().isAssignableFrom(tClass)) {
            return (T) parameter;
        }
        return null;
    }

    private String generateCsvRow(BlockPotential blockPotential) {
        StringBuilder builder = new StringBuilder();

        builder.append(blockPotential.getBlockID());
        this.addCsvSeperator(builder);

        List<Double> potentialValues = blockPotential.getPotentialValues();
        for (Double potentialValue : potentialValues) {
            builder.append(potentialValue);
            builder.append(" ");
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    private String generateCsvRow(List<Integer> rowValues) {
        StringBuilder builder = new StringBuilder();
        for (Integer cellValue : rowValues) {
            builder.append(cellValue);
            this.addCsvSeperator(builder);
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }
}
