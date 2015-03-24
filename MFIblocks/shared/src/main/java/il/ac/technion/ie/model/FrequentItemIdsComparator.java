package il.ac.technion.ie.model;

import il.ac.technion.ie.utils.Utilities;

import java.util.Comparator;

public class FrequentItemIdsComparator implements Comparator<Integer> {
	@Override
	public int compare(Integer o1, Integer o2) {
		FrequentItem fi1 = Utilities.globalItemsMap.get(o1);
		FrequentItem fi2 = Utilities.globalItemsMap.get(o2);
		return fi1.compareTo(fi2);
	}

}
