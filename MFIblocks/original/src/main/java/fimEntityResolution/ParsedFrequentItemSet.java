package fimEntityResolution;

import java.util.List;

public class ParsedFrequentItemSet {
	public ParsedFrequentItemSet(List<Integer> items, int supportSize) {
		this.items = items;
		this.supportSize = supportSize;
	}

	public List<Integer> items;
	public int supportSize;
}
