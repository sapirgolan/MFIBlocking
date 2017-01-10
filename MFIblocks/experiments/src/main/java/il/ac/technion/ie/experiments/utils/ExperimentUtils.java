package il.ac.technion.ie.experiments.utils;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by I062070 on 08/01/2017.
 */
public class ExperimentUtils {
    private static final Logger logger = Logger.getLogger(ExperimentUtils.class);
    private static List<Record> representatives;
    private final static String LINE_SEPARATOR = System.lineSeparator();

    public static String printBlocks(List<BlockWithData> blocks, String title) {
        if (logger.isDebugEnabled()) {
            StringBuilder builder = buildTitle(title);
            sortBlocksByTrueRepID(blocks);
            for (BlockWithData block : blocks) {
                builder.append("block " + block.getTrueRepresentative().getRecordName() + LINE_SEPARATOR);
                builder.append("====================================================" + LINE_SEPARATOR);
                builder.append(getBlockTextRepresentation(block));
                builder.append(LINE_SEPARATOR);
            }
            return builder.toString();
        }
        return null;
    }

    public static String getBlockTextRepresentation(final BlockWithData block) {
        List<Record> members = getBlockRecordsSortedByProbability(block);
        StringBuilder builder = new StringBuilder();
        for (Record member : members) {
            builder.append(block.getMemberProbability(member) + " " + member.getRecordName());
            builder.append(LINE_SEPARATOR);
        }

        return builder.toString();
    }

    private static List<Record> getBlockRecordsSortedByProbability(final BlockWithData block) {
        List<Record> members = block.getMembers();
        Collections.sort(members, new Comparator<Record>() {
            @Override
            public int compare(Record record_1, Record record_2) {
                return Float.compare(block.getMemberProbability(record_1), block.getMemberProbability(record_2)) * -1;
            }
        });
        return members;
    }

    public static void sortBlocksByTrueRepID(List<BlockWithData> blocks) {
        Collections.sort(blocks, new Comparator<BlockWithData>() {
            @Override
            public int compare(BlockWithData block_1, BlockWithData block_2) {
                return Integer.compare(block_1.getTrueRepresentative().getRecordID(), block_2.getTrueRepresentative().getRecordID());
            }
        });
    }

    public static StringBuilder buildTitle(String title) {
        return new StringBuilder(title).append(LINE_SEPARATOR);
    }

    public static List<Record> getRepresentativesSorted() {
        if (representatives == null) {
            representatives = new ArrayList<>();
        } else {
            Collections.sort(representatives, new Comparator<Record>() {
                @Override
                public int compare(Record record_1, Record record_2) {
                    return Integer.compare(record_1.getRecordID(), record_2.getRecordID());
                }
            });
        }
        return representatives;
    }
}
