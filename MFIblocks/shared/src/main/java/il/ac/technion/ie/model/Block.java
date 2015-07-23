package il.ac.technion.ie.model;

import org.apache.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by I062070 on 19/03/2015.
 */
public class Block {
    static final Logger logger = Logger.getLogger(Block.class);
    public static int RANDOM_ID = -1;
    private List<Integer> members;
    private float score;
    private Map<Integer, Float> membersScores;
    private Map<Integer, Float> membersProbability;
    private Map<Integer, Float> blockRepresentatives;
    private int id;

    public Block(List<Integer> members, int blockId) {
        this.members = members;
        score = 0;
        membersScores = new HashMap<>();
        membersProbability = new HashMap<>();
        for (Integer member : members) {
            membersScores.put(member, 0F);
            membersProbability.put(member, 0F);
        }
        blockRepresentatives = null;
        if (blockId == RANDOM_ID) {
            Random random = new Random();
            this.id = random.nextInt(2000) + 10000000;
        } else {
            this.id = blockId;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Block{");
        for (Integer member : members) {
            builder.append(member);
            addCharSeparator(builder);
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");

        builder.append(System.getProperty("line.separator"));

        builder.append("Probs{");
        for (Integer member : members) {
            builder.append(membersProbability.get(member));
            addCharSeparator(builder);
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");

        builder.append(System.getProperty("line.separator"));

        builder.append("Block representative is: ");

        if (blockRepresentatives != null && !blockRepresentatives.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Entry<Integer, Float> blockRepresentative : blockRepresentatives.entrySet()) {
                sb.append(blockRepresentative.getKey());
                addCharSeparator(sb);
            }
            String representatives = sb.substring(0, sb.length() - 1);

            //all entries in the list have the same value. Therefore can use the value of the first entry
            builder.append(String.format("recordIDs %s Probability %s", representatives, blockRepresentatives.values().iterator().next()));
        }

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

    public Map<Integer, Float> findBlockRepresentatives() {

        //caching internally blockRepresentatives
        if (blockRepresentatives == null) {
            float maxProb = 0;
            blockRepresentatives = new HashMap<>();
            //find the max probability score in the block
            for (Entry<Integer, Float> entry : membersProbability.entrySet()) {
                Float localProb = entry.getValue();
                maxProb = Math.max(localProb, maxProb);
            }
            logger.debug("Max probability of records in this Block is:" + maxProb + ". Block Members: " + members);

            //add all entries that have the max score.
            //More that one entry can the max score
            for (Entry<Integer, Float> entry : membersProbability.entrySet()) {
                if (maxProb == entry.getValue()) {
                    logger.debug("Adding '" + entry.getKey() + "' as representative of the block");
                    blockRepresentatives.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return blockRepresentatives;
    }

    public boolean hasMember(int recordId) {
        List<Integer> members = this.getMembers();
        for (Integer member : members) {
            if (member == recordId) {
                logger.debug("Found " + recordId + " in Block " + this.toString());
                return true;
            }
        }
                logger.debug("Didn't found " + recordId + " in Block" + this.toString());
        return false;
    }

    public double getMemberAvgSimilarity(Integer memberId) {
        int size = members.size();
        if (size == 1) {
            return 1.0;
        }

        Float totalScore = membersScores.get(memberId);
        return totalScore / (float) (size - 1);
    }

    private void addCharSeparator(StringBuilder builder) {
        builder.append(",");
    }

    public int size() {
        return this.members.size();
    }

    public int getId() {
        return this.id;
    }

    public Map<Integer, Float> getBlockRepresentatives() {
        return blockRepresentatives;
    }
}
