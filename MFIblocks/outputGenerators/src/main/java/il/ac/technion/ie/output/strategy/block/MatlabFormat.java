package il.ac.technion.ie.output.strategy.block;

import java.util.Map;

/**
 * Created by I062070 on 22/07/2015.
 */
public class MatlabFormat extends AbstractBlockFormat {
    @Override
    protected void addBlockMembers(StringBuilder builder) {
        for (Integer member : block.getMembers()) {
            builder.append(member);
            addCharSeparator(builder);
        }
        builder.deleteCharAt(builder.length() - 1);
    }

    @Override
    protected void addBlockProbs(StringBuilder builder) {
        for (Integer member : block.getMembers()) {
            builder.append(block.getMemberProbability(member));
            addCharSeparator(builder);
        }
        builder.deleteCharAt(builder.length() - 1);
    }

    @Override
    protected void addBlockRepresentatives(StringBuilder builder) {
        //add representatives
        Map<Integer, Float> blockRepresentatives = block.getBlockRepresentatives();
        if (blockRepresentatives != null) {
            for (Map.Entry<Integer, Float> blockRepresentative : blockRepresentatives.entrySet()) {
                builder.append(blockRepresentative.getKey());
                addCharSeparator(builder);
            }
            builder.deleteCharAt(builder.length() - 1);
        }
    }

    @Override
    protected void addCharSeparator(StringBuilder builder) {
        builder.append(" ");
    }

    @Override
    protected void addCsvSeperator(StringBuilder builder) {
        builder.append(",");
    }
}
