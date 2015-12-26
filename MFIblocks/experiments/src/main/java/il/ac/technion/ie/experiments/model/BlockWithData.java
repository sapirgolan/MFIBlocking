package il.ac.technion.ie.experiments.model;

import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.model.Record;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by I062070 on 21/08/2015.
 */
public class BlockWithData extends AbstractBlock<Record>{

    private static final Logger logger = Logger.getLogger(BlockWithData.class);

    private Record trueRepresentative;

    public BlockWithData(List<Record> members) {
        super(members);
        for (Record member : members) {
            if (checkAndSetRepresentative(member)) {
                break;
            }
        }
        Collections.sort(this.members, new RecordComparator());
        this.id = this.hashCode();
    }

    private boolean checkAndSetRepresentative(Record record) {
        if (record != null && !StringUtils.isEmpty(record.getRecordName())) {
            if (record.getRecordName().endsWith("org")) {
                trueRepresentative = record;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockWithData) {
            BlockWithData other = (BlockWithData) obj;
            List<Integer> thisRecordIds = this.getRecordIds();
            List<Integer> otherRecordIds = other.getRecordIds();
            if (thisRecordIds.size() == otherRecordIds.size() && thisRecordIds.containsAll(otherRecordIds)) {
                return true;
            }
        }
        return false;
    }

    private List<Integer> getRecordIds() {
        List<Integer> recordIds = new ArrayList<>();
        for (Record member : members) {
            recordIds.add(member.getRecordID());
        }
        return recordIds;
    }

    @Override
    public int getTrueRepresentativePosition() {
        ArrayList<Pair> pairs = new ArrayList<>();
        if (nomProbabilityAssigned()) {
            logger.error("Cannot execute method 'getTrueRepresentativePosition()' since there are no probabilities." +
                    "This happen since you didn't invoke 'setMemberSimScore()' before");
            return 0;
        }
        for (Map.Entry<Record, Float> entry : membersProbability.entrySet()) {
            pairs.add(new Pair(entry.getKey(), entry.getValue()));
        }
        Collections.sort(pairs, new Comparator<Pair>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                Double o1Score = o1.getScore();
                Double o2Score = o2.getScore();
                return o1Score.compareTo(o2Score);
            }
        });
        Collections.reverse(pairs);

        HashSet<Double> seenScores = new HashSet<>();
        for (Pair pair : pairs) {
            seenScores.add(pair.getScore());
            if (pair.getRecord() == trueRepresentative) {
                break;
            }
        }
        return seenScores.size();

    }

    @Override
    public void setMemberSimScore(Record memberId, Float score) {
        membersScores.put(memberId, score);
    }

    public boolean isRepresentative(Record record) {
        return trueRepresentative.equals(record);
    }

    public Record getTrueRepresentative() {
        return trueRepresentative;
    }

    /**
     * Given a list of new members this method replace the given block members.
     * It also modify the block representative (reference it to one of the newRecords)
     *
     * @param newRecords
     * @throws SizeNotEqualException
     */
    public void replaceMembers(List<RecordSplit> newRecords) throws SizeNotEqualException {
        if (newRecords == null || newRecords.size() != members.size()) {
            String message = "Cannot replace records in Blocks Since size of new Records is not as equal to existing records";
            throw new SizeNotEqualException(message);
        }
        Map<Integer, Record> recordIdToRecordMap = new HashMap<>();
        for (Record record : members) {
            recordIdToRecordMap.put(record.getRecordID(), record);
        }

        for (RecordSplit newRecord : newRecords) {
            Record recordToReplace = recordIdToRecordMap.get(newRecord.getRecordID());
            Collections.replaceAll(members, recordToReplace, newRecord);
            checkAndSetRepresentative(newRecord);
        }
    }

    private class Pair{
        private Record record;
        private double score;

        public Pair(Record record, double score) {
            this.record = record;
            this.score = score;
        }

        public double getScore() {
            return score;
        }

        public Record getRecord() {
            return record;
        }
    }

    public List<String> getFieldNames() {
        return trueRepresentative.getFieldNames();
    }

    public List<Record> getSortedMembers() {
        Collections.sort(members, new RecordComparator());
        return members;
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        String result;
        if (nomProbabilityAssigned()) {
            result = String.format("%d; %s", size(), trueRepresentative);
        } else {
            result = String.format("%d; %s at pos %d", size(), trueRepresentative, getTrueRepresentativePosition());
        }
        return result;
    }

    private boolean nomProbabilityAssigned() {
        return membersProbability.entrySet().isEmpty();
    }
}
