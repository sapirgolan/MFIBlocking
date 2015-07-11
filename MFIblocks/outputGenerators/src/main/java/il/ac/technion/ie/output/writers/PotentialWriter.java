package il.ac.technion.ie.output.writers;

import il.ac.technion.ie.potential.model.BlockPotential;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        if (other[0] instanceof List<?>) {
            List<?> list = (List<?>) other[0];
            if (list.get(0) instanceof BlockPotential) {
                List<BlockPotential> blockPotentials = (List<BlockPotential>) list;
                for (BlockPotential blockPotential : blockPotentials) {
                    String csvEntry = generateCsvRow(blockPotential);
                    bufferedWriter.write(csvEntry);
                    bufferedWriter.newLine();
                }
            }
        }
        bufferedWriter.close();
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
}
