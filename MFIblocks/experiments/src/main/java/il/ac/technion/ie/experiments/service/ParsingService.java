package il.ac.technion.ie.experiments.service;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvWriter;
import il.ac.technion.ie.experiments.builder.FebrlBlockBuilder;
import il.ac.technion.ie.experiments.builder.iBlockBuilder;
import il.ac.technion.ie.experiments.parsers.DatasetParser;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.Record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by I062070 on 22/08/2015.
 */
public class ParsingService {

    private DatasetParser dataParser;
    private iBlockBuilder blockBuilder;

    public ParsingService() {
        this.dataParser = new DatasetParser();
        blockBuilder = new FebrlBlockBuilder();
    }
    public List<BlockWithData> parseDataset(String pathToFile) {
        List<BlockWithData> blocksWithData = new ArrayList<>();
        CsvParser parser = dataParser.getParserForFile(pathToFile);
        String[] fields = parser.parseNext();

        if (fields != null) {
            List<String> fieldsNames = new ArrayList<>(Arrays.asList(fields));
            blocksWithData = blockBuilder.build(parser, fieldsNames);
        }
        return blocksWithData;
    }

    public void writeBlocks(List<BlockWithData> blocks, String pathToFile) {
        CsvWriter csvWriter = dataParser.preparOutputFile(pathToFile);
        if (csvWriter != null) {
            // Write the record headers of this file
            List<String> fieldsNames = getBlockFieldsNames(blocks);
            fieldsNames.add("Probability");
            csvWriter.writeHeaders(fieldsNames);

            // Let's write the rows one by one
            for (BlockWithData block : blocks) {
                for (Record record : block.getMembers()) {
                    for (String recordEntry : record.getEntries()) {
                        csvWriter.writeValue(recordEntry);
                    }
                    csvWriter.writeValues(block.getMemberProbability(record));
                    csvWriter.writeValuesToRow();
                }
            }
            // Here we just tell the writer to write everything and close the given output Writer instance.
            csvWriter.close();
        }

    }

    private List<String> getBlockFieldsNames(List<BlockWithData> blocks) {
        if (blocks != null && !blocks.isEmpty()) {
            final BlockWithData blockWithData = blocks.get(0);
            return blockWithData.getFieldNames();
        }
        return null;
    }
}
