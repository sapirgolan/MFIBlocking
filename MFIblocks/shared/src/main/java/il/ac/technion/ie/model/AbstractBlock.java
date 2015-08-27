package il.ac.technion.ie.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 21/08/2015.
 * Generic version of the Block class.
 * @param <T> the type of the members in the block
 */
public abstract class AbstractBlock<T> {
    protected List<T> members;
    protected Map<T, Float> membersScores;
    protected Map<T, Float> membersProbability;
    protected Map<T, Float> blockRepresentatives;

    public AbstractBlock(List<T> members) {
        membersProbability = new HashMap<>();
        this.members = members;
        membersScores = new HashMap<>();
        blockRepresentatives = null;
    }

    @Override
    public abstract boolean equals(Object obj);

    public float getMemberScore(T memberId) {
        return membersScores.get(memberId);
    }

    public void setMemberProbability(T member, float probability) {
        membersProbability.put(member, probability);
    }

    public float getMemberProbability(T memberId) {
        return membersProbability.get(memberId);
    }

    public double getMemberAvgSimilarity(T memberId) {
        int size = members.size();
        if (size == 1) {
            return 1.0;
        }

        Float totalScore = membersScores.get(memberId);
        return totalScore / (float) (size - 1);
    }

    public Map<T, Float> getBlockRepresentatives() {
        return blockRepresentatives;
    }

    public List<T> getMembers() {
        return members;
    }

    public int size() {
        return this.members.size();
    }

    public Map<T, Float> findBlockRepresentatives() {

        //caching internally blockRepresentatives
        if (blockRepresentatives == null) {
            float maxProb = 0;
            blockRepresentatives = new HashMap<>();
            //find the max service score in the block
            for (Map.Entry<T, Float> entry : membersProbability.entrySet()) {
                Float localProb = entry.getValue();
                maxProb = Math.max(localProb, maxProb);
            }
            Block.logger.debug("Max service of records in this Block is:" + maxProb + ". Block Members: " + members);

            //add all entries that have the max score.
            //More that one entry can the max score
            for (Map.Entry<T, Float> entry : membersProbability.entrySet()) {
                if (maxProb == entry.getValue()) {
                    Block.logger.debug("Adding '" + entry.getKey() + "' as representative of the block");
                    blockRepresentatives.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return blockRepresentatives;
    }

    public boolean hasMember(T recordId) {
        for (T member : members) {
            if (member == recordId) {
                Block.logger.debug("Found " + recordId + " in Block " + this.toString());
                return true;
            }
        }
        Block.logger.debug("Didn't found " + recordId + " in Block" + this.toString());
        return false;
    }

    public abstract int getTrueRepresentativePosition();

    public void setMemberSimScore(T memberId, Float score) {
        if (membersScores.containsKey(memberId)) {
            membersScores.put(memberId, score);
        }
    }
}
