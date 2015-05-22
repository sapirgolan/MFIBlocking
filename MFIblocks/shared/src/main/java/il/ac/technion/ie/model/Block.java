package il.ac.technion.ie.model;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by I062070 on 19/03/2015.
 */
public class Block {
    private List<Integer> members;
    private float score;
    private Map<Integer, Float> membersScores;
    private Map<Integer, Float> membersProbability;

    public Block(List<Integer> members) {
        this.members = members;
        score = 0;
        membersScores = new HashMap<>();
        membersProbability = new HashMap<>();
        for (Integer member : members) {
            membersScores.put(member, 0F);
            membersProbability.put(member, 0F);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Block{");
        for (Integer member : members) {
            builder.append(member);
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");

        builder.append(System.getProperty("line.separator"));

        builder.append("Probs{");
        for (Integer member : members) {
            builder.append(membersProbability.get(member));
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() -1);
        builder.append("}");

        builder.append(System.getProperty("line.separator"));

        builder.append("Block representative is: ");

        Entry<Integer, Float> blockRepr = findBlockRepresentative();
        builder.append(String.format("recordID %d, Probability %s", blockRepr.getKey(), blockRepr.getValue()));
        builder.append(blockRepr.getKey());

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj instanceof Block) {
            Block other = (Block) obj;
            BitSet itemsBitSet = new BitSet();
            List<Integer> otherMembers = other.getMembers();
            for (Integer otherMember : otherMembers) {
                itemsBitSet.set(otherMember, true);
            }
            for (Integer member : members) {
                itemsBitSet.flip(member);
            }
            isEqual = itemsBitSet.isEmpty();
        }
        return isEqual;
    }

    public List<Integer> getMembers() {
        return members;
    }

    public void setMemberSimScore(Integer memberId, Float score) {
        if (membersScores.containsKey(memberId)) {
            membersScores.put(memberId, score);
        }
    }

    public float getMemberScore(Integer memberId) {
        return membersScores.get(memberId);
    }

    public void setMemberProbability(Integer member, float probability) {
        membersProbability.put(member, probability);
    }

    public float getMemberProbability(Integer memberId) {
        return membersProbability.get(memberId);
    }

    public Entry<Integer, Float> findBlockRepresentative() {
        float maxProb = 0;
        Entry<Integer, Float> blockRepresentative = null;
        for (Entry<Integer, Float> entry : membersProbability.entrySet()) {
            Float localProb = entry.getValue();
            if (localProb.equals(Math.max(localProb, maxProb))) {
                maxProb = localProb;
                blockRepresentative = entry;
            }
        }
        return blockRepresentative;
    }
}
