package il.ac.technion.ie.potential.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import il.ac.technion.ie.model.AbstractBlock;

import java.util.List;
import java.util.Map;

/**
 * Created by XPS_Sapir on 14/07/2015.
 */
public class SharedMatrix extends AbstractPotentialMatrix{

    protected BiMap<Integer, Integer> recordToRowsPosMap;
    protected BiMap<Integer, Integer> recordToColumnsPosMap;
    private BlockPair<Integer, Integer> representsBlocks;

    public SharedMatrix(AbstractBlock rowsBlock, AbstractBlock columnsBlock) {
        this.matrix2D = matrixFactory(rowsBlock.size(), columnsBlock.size());
        this.mapRows(rowsBlock);
        this.mapColumns(columnsBlock);
        this.representsBlocks = new BlockPair<>(rowsBlock.getId(), columnsBlock.getId());
    }

    private void mapColumns(AbstractBlock columnsBlock) {
        recordToColumnsPosMap = HashBiMap.create();
        mapRecordToMatrix(columnsBlock, recordToColumnsPosMap);
    }

    private void mapRows(AbstractBlock rowsBlock) {
        recordToRowsPosMap = HashBiMap.create();
        mapRecordToMatrix(rowsBlock, recordToRowsPosMap);
    }

    private void mapRecordToMatrix(AbstractBlock rowsBlock, Map<Integer, Integer> map) {
        List<Integer> members = rowsBlock.getMembers();
        for (int index = 0; index < members.size(); index++) {
            Integer recordId =  members.get(index);
            map.put(recordId, index);
        }
    }

    /**
     * Sets the matrix cell that corresponds to blocks [blockI,blockJ] to the specified value.
     * @param sharedRecord - the id of the shared record
     * @param value - the value that should be stored in A{i,j}
     */
    public void setQuick(int sharedRecord, double value) {
        Integer rowIndex = recordToRowsPosMap.get(sharedRecord);
        Integer columnIndex = recordToColumnsPosMap.get(sharedRecord);
        matrix2D.setQuick(rowIndex, columnIndex, value);
    }

    /**
     * @return number of rows*columns in matrix
     */
    @Override
    public int size() {
        return this.matrix2D.rows() * this.matrix2D.columns();
    }

    @Override
    protected Integer valueInMatrixRowIfValueExists() {
        return -10;
    }

    @Override
    protected int getRecordIDRepresentsRowIndex(int rowIndex) {
        return recordToRowsPosMap.inverse().get(rowIndex);
    }

    @Override
    protected int getRecordIDRepresentsColumnIndex(int columnIndex) {
        return recordToColumnsPosMap.inverse().get(columnIndex);
    }

    public String getName() {
        return "Block_" + representsBlocks.getLeft() + "X" + representsBlocks.getRight();
    }

    public int numberOfRows() {
        return this.matrix2D.rows();
    }
}
