package il.ac.technion.ie.potential.model;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import il.ac.technion.ie.model.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public class AdjustedMatrix {
    private DoubleMatrix2D matrix2D;
    private BiMap<Integer, Integer> blockIdToMatPosMap;

    public AdjustedMatrix(List<Block> filteredBlocks) {
        this.matrix2D = DoubleFactory2D.sparse.make(filteredBlocks.size(), filteredBlocks.size());
        blockIdToMatPosMap = HashBiMap.create();
        createMatrixBlockMappping(filteredBlocks);
    }

    private void createMatrixBlockMappping(List<Block> filteredBlocks) {
        int matrixIndex = 0;
        for (Block block : filteredBlocks) {
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

    /**
     * Returns the number of cells having non-zero values.
     * @return number of cells having non-zero values
     */
    public int cardinality() {
        return this.matrix2D.cardinality();
    }

    /**
     * @return number of rows\columns in matrix
     */
    public int size() {
        return this.matrix2D.rows();
    }

    public List<MatrixCell<Double>> getCellsCongaingNonZeroValue() {
        List<MatrixCell<Double>> matrixCells = new ArrayList<>();
        IntArrayList rowList = new IntArrayList();
        IntArrayList columnList = new IntArrayList();
        DoubleArrayList valueList = new DoubleArrayList();
        matrix2D.getNonZeros(rowList, columnList, valueList);
        for (int i = 0; i < columnList.size(); i++) {
            matrixCells.add(new MatrixCell<>(blockIdToMatPosMap.inverse().get(rowList.getQuick(i)),
                                            blockIdToMatPosMap.inverse().get(columnList.getQuick(i)),
                                            valueList.getQuick(i)));
        }
        return matrixCells;
    }

    public List<Integer> viewRow(int rowId) {
        DoubleMatrix1D row = matrix2D.viewRow(rowId);
        int numberOfCellsInARow = row.size();
        List<Integer> list = new ArrayList<Integer>(numberOfCellsInARow);
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
}
