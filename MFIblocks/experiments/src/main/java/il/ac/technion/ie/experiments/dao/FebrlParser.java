package il.ac.technion.ie.experiments.dao;

import com.univocity.parsers.csv.CsvParser;
import il.ac.technion.ie.experiments.builder.FebrlBlockBuilder;
import il.ac.technion.ie.experiments.builder.iBlockBuilder;
import il.ac.technion.ie.experiments.model.BlockWithData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by I062070 on 22/08/2015.
 */
public class FebrlParser {

    private DatasetParser dataParser;
    private iBlockBuilder blockBuilder;

    public FebrlParser() {
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
}
