package il.ac.technion.ie.potential.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import il.ac.technion.ie.model.AbstractBlock;

import java.util.List;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public class AdjustedMatrix extends AbstractPotentialMatrix {

    protected BiMap<Integer, Integer> blockIdToMatPosMap;

    public AdjustedMatrix(List<? extends AbstractBlock> filteredBlocks) {
        createMatrixBlockMapping(filteredBlocks);
        this.matrix2D = matrixFactory(filteredBlocks.size(), filteredBlocks.size());
    }

    private void createMatrixBlockMapping(List<? extends AbstractBlock> filteredBlocks) {
        blockIdToMatPosMap = HashBiMap.create();
        int matrixIndex = 0;
        for (AbstractBlock block : filteredBlocks) {
            blockIdToMatPosMap.put(block.getId(), matrixIndex);
            matrixIndex++;
        }
    }

    /**
     * Sets the matrix cell that corresponds to blocks [blockI,blockJ] to the specified value.
     * @param blockI - the id of block corresponding to row i
     * @param blockJ - the id of block corresponding to column j
     * @param value - the value that should be stored in A{i,j} and A{j,i}
     */
    public void  setQuick(int blockI, int blockJ, double value) {
        Integer matrixPosI = blockIdToMatPosMap.get(blockI);
        Integer matrixPosJ = blockIdToMatPosMap.get(blockJ);
        matrix2D.setQuick(matrixPosI, matrixPosJ, value);
        matrix2D.setQuick(matrixPosJ, matrixPosI, value);
    }

    @Override
    protected Integer valueInMatrixRowIfValueExists() {
        return 1;
    }

    @Override
    protected int getRecordIDRepresentsRowIndex(int rowIndex) {
        return blockIdToMatPosMap.inverse().get(rowIndex);
    }

    @Override
    protected int getRecordIDRepresentsColumnIndex(int columnIndex) {
        return blockIdToMatPosMap.inverse().get(columnIndex);
    }
}
