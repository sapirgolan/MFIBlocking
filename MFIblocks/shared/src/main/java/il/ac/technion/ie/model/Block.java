package il.ac.technion.ie.model;

import il.ac.technion.ie.exception.NotImplementedYetException;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by I062070 on 19/03/2015.
 */
public class Block extends AbstractBlock<Integer> {
    static final Logger logger = Logger.getLogger(Block.class);
    public static int RANDOM_ID = -1;

    public Block(List<Integer> members, int blockId) {
        super(members);
        for (Integer member : members) {
            membersScores.put(member, 0F);
            setMemberProbability(member, 0F);
        }
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
            builder.append(getMemberProbability(member));
            addCharSeparator(builder);
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");

        builder.append(System.getProperty("line.separator"));

        builder.append("Block representative is: ");

        if (getBlockRepresentatives() != null && !getBlockRepresentatives().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Entry<Integer, Float> blockRepresentative : getBlockRepresentatives().entrySet()) {
                sb.append(blockRepresentative.getKey());
                addCharSeparator(sb);
            }
            String representatives = sb.substring(0, sb.length() - 1);

            //all entries in the list have the same value. Therefore can use the value of the first entry
            builder.append(String.format("recordIDs %s Probability %s", representatives, getBlockRepresentatives().values().iterator().next()));
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

    private void addCharSeparator(StringBuilder builder) {
        builder.append(",");
    }

    @Override
    public int getTrueRepresentativePosition() {
        throw new NotImplementedYetException("This method is not implemented since the True Representative is unknown");
    }
}
