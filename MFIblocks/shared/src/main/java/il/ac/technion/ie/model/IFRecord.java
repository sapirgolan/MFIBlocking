package il.ac.technion.ie.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface IFRecord extends Serializable{

	public String getRecordStr();
	public void addItem(int itemId);
	public Map<Integer,Integer> getItemsToFrequency();
	public int getId();
	public String getSrc();
	public void setSrc(String src);
	/**
	 * Write only the items which passed the support constraints
	 * @param appropriateItems
	 * @return
	 */
	public String getNumericline(Set<Integer> appropriateItems);
	public String toString();
}
