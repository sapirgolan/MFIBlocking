package il.ac.technion.ie.model;

import org.apache.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by I062070 on 19/03/2015.
 */
public class Block {
    private static final String CREATE_BLOCK_PATTERN = "Create (%s:Block)";
    private static final String CREATE_RECORD_PATTERN = "Create (%s: Record {id: %d})";
    private static final String ADD_RECORD_PATTERN = "Create (%s) -[:IN {probability:%s}]->(%s)";
    private static final String SET_REPRESENTATIVE_PATTERN = "Create (%s) -[:REPRESENTS]->(%s)";
    private static final String RECORD_PATTERN = "record_%d";
    private static final String BLOCK_PATTERN = "block_%s";

    static final Logger logger = Logger.getLogger(Block.class);

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
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");

        builder.append(System.getProperty("line.separator"));

        builder.append("Block representative is: ");

        Entry<Integer, Float> blockRepr = findBlockRepresentative();
        builder.append(String.format("recordID %d, Probability %s", blockRepr.getKey(), blockRepr.getValue()));
        builder.append(blockRepr.getKey());

        return builder.toString();
    }

    public StringBuilder toCypher(int blockId, Set<Integer> set) {
        StringBuilder builder = new StringBuilder();
        StringBuilder memberBuilder = new StringBuilder();

        String blockName = String.format(BLOCK_PATTERN, blockId);

        builder.append(String.format(CREATE_BLOCK_PATTERN, blockName));
        builder.append(System.getProperty("line.separator"));
        for (Integer member : members) {
            String recordName = String.format(RECORD_PATTERN, member);
            if (!set.contains(member)) {
                builder.append(String.format(CREATE_RECORD_PATTERN, recordName, member));
                builder.append(System.getProperty("line.separator"));
                set.add(member);
            }

            memberBuilder.append(String.format(ADD_RECORD_PATTERN, recordName, this.getMemberProbability(member), blockName));
            memberBuilder.append(System.getProperty("line.separator"));
        }
        //adds members creation statements to main builder
        builder.append(memberBuilder.toString());

        Entry<Integer, Float> blockRepresentative = this.findBlockRepresentative();
        String recordName = String.format(RECORD_PATTERN, blockRepresentative.getKey());

        builder.append(String.format(SET_REPRESENTATIVE_PATTERN, recordName, blockName));
        return builder;
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
}
