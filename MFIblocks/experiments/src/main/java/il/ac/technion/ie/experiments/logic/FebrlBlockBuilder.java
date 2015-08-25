package il.ac.technion.ie.experiments.logic;

import com.univocity.parsers.csv.CsvParser;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.Record;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by I062070 on 22/08/2015.
 */
public class FebrlBlockBuilder implements iBlockBuilder {

    private List<String> fieldsNames;
    @Override
    public List<BlockWithData> build(CsvParser parser, List<String> fieldsNames) {
        this.fieldsNames = fieldsNames;

        List<BlockWithData> blocksWithData = new ArrayList<>();
        List<Record> recordsForBlock = new ArrayList<>();
        String[] row = parser.parseNext();
        String currentBlockId;
        //while there are more rows in dataset
        currentBlockId = initForFirstRecordOnly(row, recordsForBlock);
        row = parser.parseNext();
        while (row != null && currentBlockId != null) {
            Record record = this.createRecord(row);

            String blockId = getBlockIdFromRecord(record);
            if (blockId.equals(currentBlockId)) {
                recordsForBlock.add(record);
            } else {
                createBlockFromRecords(blocksWithData, recordsForBlock);
                recordsForBlock = new ArrayList<>();
                recordsForBlock.add(record);
                currentBlockId = blockId;
            }
            row = parser.parseNext();
        }
        if (!recordsForBlock.isEmpty()) {
            createBlockFromRecords(blocksWithData, recordsForBlock);
        }

        return blocksWithData;
    }

    private void createBlockFromRecords(List<BlockWithData> blocksWithData, List<Record> recordsForBlock) {
        blocksWithData.add(new BlockWithData(recordsForBlock));
    }

    private String initForFirstRecordOnly(String[] row, List<Record> recordsForBlock) {
        String blockId = null;
        if (row != null) {
            Record record = this.createRecord(row);
            blockId = getBlockIdFromRecord(record);
            recordsForBlock.add(record);
        }
        return blockId;
    }

    private String getBlockIdFromRecord(Record record) {
        return StringUtils.substringBetween(record.getRecordID(), "rec-", "-");
    }

    private Record createRecord(String[] row) {
        List<String> values = new ArrayList<>(Arrays.asList(row));
        return new Record(fieldsNames, values);
    }
}
