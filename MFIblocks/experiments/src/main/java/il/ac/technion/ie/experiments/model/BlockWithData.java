package il.ac.technion.ie.experiments.model;

import il.ac.technion.ie.model.AbstractBlock;

import java.util.*;

/**
 * Created by I062070 on 21/08/2015.
 */
public class BlockWithData extends AbstractBlock<Record>{

    private Record trueRepresentative;

    public BlockWithData(List<Record> members) {
        super(members);
        for (Record member : members) {
            if (member.getRecordID().endsWith("org")) {
                trueRepresentative = member;
                break;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockWithData) {
            BlockWithData other = (BlockWithData) obj;
            List<String> thisRecordIds = this.getRecordIds();
            List<String> otherRecordIds = other.getRecordIds();
            if (thisRecordIds.size() == otherRecordIds.size() && thisRecordIds.containsAll(otherRecordIds)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getRecordIds() {
        List<String> recordIds = new ArrayList<>();
        for (Record member : members) {
            recordIds.add(member.getRecordID());
        }
        return recordIds;
    }

    @Override
    public int getTrueRepresentativePosition() {
        ArrayList<Pair> pairs = new ArrayList<>();
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
}
