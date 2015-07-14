package il.ac.technion.ie.potential.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import il.ac.technion.ie.model.Block;

import java.util.List;
import java.util.Map;

/**
 * Created by XPS_Sapir on 14/07/2015.
 */
public class SharedMatrix extends AbstractPotentialMatrix{

    protected BiMap<Integer, Integer> recordToRowsPosMap;
    protected BiMap<Integer, Integer> recordToColumnsPosMap;

    public SharedMatrix(Block rowsBlock, Block columnsBlock) {
        this.matrix2D = matrixFactory(rowsBlock.size(), columnsBlock.size());
        this.mapRows(rowsBlock);
        this.mapColumns(columnsBlock);
    }

    private void mapColumns(Block columnsBlock) {
        recordToColumnsPosMap = HashBiMap.create();
        mapRecordToMatrix(columnsBlock, recordToColumnsPosMap);
    }

    private void mapRows(Block rowsBlock) {
        recordToRowsPosMap = HashBiMap.create();
        mapRecordToMatrix(rowsBlock, recordToRowsPosMap);
    }

    private void mapRecordToMatrix(Block rowsBlock, Map<Integer, Integer> map) {
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
}
