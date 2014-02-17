package fimEntityResolution;

import java.util.List;

public class ParsedFrequentItemSet {
	public List<Integer> items;
	public int supportSize;
	
	public ParsedFrequentItemSet(List<Integer> items, int supportSize) {
		this.items = items;
		this.supportSize = supportSize;
	}

	
	
}
