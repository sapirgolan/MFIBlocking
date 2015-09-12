package il.ac.technion.ie.experiments.builder;

import com.univocity.parsers.csv.CsvParser;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by I062070 on 22/08/2015.
 */
public class FebrlBlockBuilder implements iBlockBuilder {

    private static final String EMPTY_STRING = "";
    private static final int FIRST_RECORD_ID = 1;
    private static final int STARTING_RECORD_INDEX = 2;
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
        // we have already initialized the first record, therefore the index starts at 2
        int recordIndex = STARTING_RECORD_INDEX;
        while (row != null && currentBlockId != null) {
            Record record = this.createRecord(row, recordIndex);

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
            recordIndex++;
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
            Record record = this.createRecord(row, FIRST_RECORD_ID);
            blockId = getBlockIdFromRecord(record);
            recordsForBlock.add(record);
        }
        return blockId;
    }

    private String getBlockIdFromRecord(Record record) {
        return StringUtils.substringBetween(record.getRecordName(), "rec-", "-");
    }

    private Record createRecord(String[] row, int id) {
        List<String> values = new ArrayList<>(Arrays.asList(row));
        for (int i = 0; i < values.size(); i++) {
            String val = values.get(i);
            if (val == null) {
                values.set(i, EMPTY_STRING);
            }
        }
        return new Record(fieldsNames, values, id);
    }
}
