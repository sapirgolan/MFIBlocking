package il.ac.technion.ie.potential.model;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import il.ac.technion.ie.model.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public class AdjustedMatrix extends AbstractPotentialMatrix {

    protected BiMap<Integer, Integer> blockIdToMatPosMap;

    public AdjustedMatrix(List<Block> filteredBlocks) {
        blockIdToMatPosMap = HashBiMap.create();
        createMatrixBlockMappping(filteredBlocks);
        this.matrix2D = matrixFactory(filteredBlocks.size(), filteredBlocks.size());
    }

    private void createMatrixBlockMappping(List<Block> filteredBlocks) {
        int matrixIndex = 0;
        for (Block block : filteredBlocks) {
            blockIdToMatPosMap.put(block.getId(), matrixIndex);
            matrixIndex++;
        }
    }

    public List<Integer> viewRow(int rowId) {
        DoubleMatrix1D row = matrix2D.viewRow(rowId);
        int numberOfCellsInARow = row.size();
        List<Integer> list = new ArrayList<>(numberOfCellsInARow);
        IntArrayList intArrayList = new IntArrayList();
        row.getNonZeros(intArrayList, new DoubleArrayList());

        for (int index = 0; index < numberOfCellsInARow; index++) {
            if (intArrayList.contains(index)) {
                list.add(1);
            } else {
                list.add(0);
            }
        }
        return list;
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
    protected int getRecordIDRepresentsRowIndex(int rowIndex) {
        return blockIdToMatPosMap.inverse().get(rowIndex);
    }

    @Override
    protected int getRecordIDRepresentsColumnIndex(int columnIndex) {
        return blockIdToMatPosMap.inverse().get(columnIndex);
    }
}
