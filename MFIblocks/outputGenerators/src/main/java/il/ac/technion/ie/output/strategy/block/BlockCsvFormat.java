package il.ac.technion.ie.output.strategy.block;

import java.util.Map;

/**
 * Created by I062070 on 22/07/2015.
 */
public class BlockCsvFormat extends AbstractBlockFormat {

/*    @Override
    public String format(Block block) {
        this.block = block;
        StringBuilder builder = new StringBuilder();

        addBlockID(builder);
        addCsvSeperator(builder);

        addBlockMembers(builder);
        addCsvSeperator(builder);

        addBlockProbs(builder);
        addCsvSeperator(builder);

        addBlockRepresentatives(builder);
        return builder.toString();
    }*/

    @Override
    protected void addBlockMembers(StringBuilder builder) {
        builder.append("{");
        for (Integer member : block.getMembers()) {
            builder.append(member);
            addCharSeparator(builder);
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
    }

    @Override
    protected void addBlockProbs(StringBuilder builder) {
        builder.append("{");
        for (Integer member : block.getMembers()) {
            builder.append(block.getMemberProbability(member));
            addCharSeparator(builder);
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
    }

    @Override
    protected void addBlockRepresentatives(StringBuilder builder) {
        builder.append(" Block representatives are: ");
        addCsvSeperator(builder);

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

}
