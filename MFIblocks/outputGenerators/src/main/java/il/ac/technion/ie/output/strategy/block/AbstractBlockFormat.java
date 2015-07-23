package il.ac.technion.ie.output.strategy.block;

import il.ac.technion.ie.model.Block;

/**
 * Created by I062070 on 22/07/2015.
 */
public abstract class AbstractBlockFormat implements BlockFormat{
    protected Block block;

    @Override
    final public String format(Block block) {
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
    }

    protected void addBlockID(StringBuilder builder) {
        builder.append(block.getId());
    }

    protected void addCsvSeperator(StringBuilder builder) {
        builder.append("|");
    }

    protected void addCharSeparator(StringBuilder builder) {
        builder.append(",");
    }

    protected abstract void addBlockMembers(StringBuilder builder);

    protected abstract void addBlockProbs(StringBuilder builder);

    protected abstract void addBlockRepresentatives(StringBuilder builder);
}
