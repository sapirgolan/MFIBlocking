package il.ac.technion.ie.potential.model;

/**
 * Created by XPS_Sapir on 10/07/2015.
 */
public class MatrixCell<V> {
    private int rowPos;
    private int colPos;
    private V value;

    public MatrixCell(int rowPos, int colPos, V value) {
        this.rowPos = rowPos;
        this.colPos = colPos;
        this.value = value;
    }

    public int getRowPos() {
        return rowPos;
    }

    public int getColPos() {
        return colPos;
    }

    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MatrixCell) {
            MatrixCell other = (MatrixCell) obj;
            if (rowPos == other.getRowPos() && colPos == other.getColPos() ){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Cell {%d,%d} = %s", rowPos, colPos, value);
    }
}
