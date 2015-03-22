package il.ac.technion.ie.data.structure;

import java.io.Serializable;
import java.util.List;

public interface SetPairIF extends Serializable {

	public void setPair(int i, int j, double score);
	public void setColumnsSupport(List<Integer> items, int recordID1, int recordID2);
}
