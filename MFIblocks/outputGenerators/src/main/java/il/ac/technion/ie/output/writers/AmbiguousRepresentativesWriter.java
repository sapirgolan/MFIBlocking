package il.ac.technion.ie.output.writers;

import il.ac.technion.ie.model.BlockDescriptor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by XPS_Sapir on 11/07/2015.
 */
public class AmbiguousRepresentativesWriter extends AbstractWriter{
    @Override
    public File generateFile(String fileName, String fileExtention) {
        return this.createUniqueOutputFile("/AmbiguousRepresentatives_" + fileName, fileExtention);
    }

    @Override
    public void writeResults(File file, Object... other) throws IOException {
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        Map<Integer, List< BlockDescriptor >> blocksAmbiguousRepresentatives = (Map<Integer, List< BlockDescriptor >>) other[0];
        for (Map.Entry<Integer, List<BlockDescriptor>> entry : blocksAmbiguousRepresentatives.entrySet()) {
            String csvEntry = ambiguousRepresentativeBlocksToCsv(entry);
            bufferedWriter.write(csvEntry);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    /**
     * Gets a record ID that represents several blocks and a List of those blocks
     *
     * @param entry
     * @return
     */
    private String ambiguousRepresentativeBlocksToCsv(Map.Entry<Integer, List<BlockDescriptor>> entry) {
        StringBuilder builder = new StringBuilder();
        Integer recordRepresentSeveralBlocks = entry.getKey();
        List<BlockDescriptor> representedBlocks = entry.getValue();

        for (BlockDescriptor blockDescriptor : representedBlocks) {
            for (Integer recordId : blockDescriptor.getMembers()) {
                //adding the root cause for this entry
                builder.append(recordRepresentSeveralBlocks);
                addCsvSeperator(builder);

                //adding current record in Block
                builder.append(recordId);
                addCsvSeperator(builder);

                //adding the content of the block
                List<String> contentOfRecord = blockDescriptor.getContentOfRecord(recordId);
                for (String text : contentOfRecord) {
                    builder.append(text);
                    builder.append(" ");
                }
                builder.deleteCharAt(builder.length() - 1);
                addCsvSeperator(builder);
                builder.append("\r\n");
            }
            builder.append("\r\n");
        }
        return builder.toString();
    }
}
