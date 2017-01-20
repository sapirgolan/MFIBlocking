package il.ac.technion.ie.experiments.utils;

import il.ac.technion.ie.experiments.exception.OSNotSupportedException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import org.apache.commons.lang.StringUtils;
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
            sortBlocksByTrueRep(blocks);
            for (BlockWithData block : blocks) {
                Record trueRepresentative = block.getTrueRepresentative();
                if (trueRepresentative == null) {
                    builder.append("block without a true representative" + LINE_SEPARATOR);
                } else {
                    builder.append(String.format("block %s, #%d%s", trueRepresentative.getRecordName(), block.getId(), LINE_SEPARATOR));
                }
                builder.append("====================================================" + LINE_SEPARATOR);
                builder.append(getBlockTextRepresentation(block));
                builder.append(LINE_SEPARATOR);
            }
            String blocksTextRepresentation = builder.toString();
            logger.debug(blocksTextRepresentation);
            return blocksTextRepresentation;
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
        List<Record> members = new ArrayList<>(block.getMembers());
        try {
            Collections.sort(members, new Comparator<Record>() {
                @Override
                public int compare(Record record_1, Record record_2) {
                    return Float.compare(block.getMemberProbability(record_1), block.getMemberProbability(record_2)) * -1;
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return members;
    }

    public static void sortBlocksByTrueRep(List<BlockWithData> blocks) {
        try {
            Collections.sort(blocks, new Comparator<BlockWithData>() {
                @Override
                public int compare(BlockWithData block_1, BlockWithData block_2) {
                    Record block_1TrueRepresentative = block_1.getTrueRepresentative();
                    Record block_2TrueRepresentative = block_2.getTrueRepresentative();
                    if (block_1TrueRepresentative == null & block_2TrueRepresentative == null) {
                        return 0;
                    }
                    if (block_1TrueRepresentative == null) {
                        return 1;
                    }
                    if (block_2TrueRepresentative == null) {
                        return -1;
                    }
                    int block1_RepName = Integer.parseInt(StringUtils.substringBetween(block_1TrueRepresentative.getRecordName(), "-"));
                    int block2_RepName = Integer.parseInt(StringUtils.substringBetween(block_2TrueRepresentative.getRecordName(), "-"));
                    return Integer.compare(block1_RepName, block2_RepName);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
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

    public static String humanReadableByteCount() {
        Long bytes = Runtime.getRuntime().totalMemory();
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static void checkOS() throws OSNotSupportedException {
        String osName = System.getProperty("os.name");
        if (StringUtils.indexOfIgnoreCase(osName, "windows") == -1) {
            throw new OSNotSupportedException(String.format("Only 'Windows NT' is supported, this OS is: '%s'", osName));
        }
    }
}
