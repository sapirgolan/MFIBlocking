package il.ac.technion.ie.model;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 19/03/2015.
 */
public class Block {
    private List<Integer> members;
    private float score;
    private Map<Integer, Float> membersScores;

    public Block(List<Integer> members) {
        this.members = members;
        score = 0;
        membersScores = new HashMap<>();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Block{");
        for (Integer member : members) {
            builder.append(member);
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() -1);
        builder.append("}");
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
}
