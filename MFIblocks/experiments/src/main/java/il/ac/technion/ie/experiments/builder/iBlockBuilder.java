package il.ac.technion.ie.experiments.builder;

import com.univocity.parsers.csv.CsvParser;
import il.ac.technion.ie.experiments.model.BlockWithData;

import java.util.List;

/**
 * Created by I062070 on 22/08/2015.
 */
public interface iBlockBuilder {
    List<BlockWithData> build(CsvParser parser, List<String> fieldsNames);
}
