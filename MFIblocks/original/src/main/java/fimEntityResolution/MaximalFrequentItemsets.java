package fimEntityResolution;

import il.ac.technion.ie.model.FrequentItemset;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MaximalFrequentItemsets {

	Set<FrequentItemset> maximalFrequentItemsets;
	public static String newline = System.getProperty("line.separator");
	
	public MaximalFrequentItemsets(){
		maximalFrequentItemsets = new HashSet<FrequentItemset>();
	}
	
/*	public void addFrequentItemset(FrequentItemset itemset){
		if(itemset.getSupport().size() < 2)
			return;
		Set<FrequentItemset> toDelete = new HashSet<FrequentItemset>();
		for (FrequentItemset member : maximalFrequentItemsets) {
			if(member.contains(itemset)){
				return; // already contained
			}
			if(itemset.contains(member)){
				toDelete.add(member);
			}
		}
		maximalFrequentItemsets.removeAll(toDelete);
		maximalFrequentItemsets.add(itemset);
	}
	*/
	
	public Collection<FrequentItemset> getItemsets(){
		return maximalFrequentItemsets;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (FrequentItemset itemset : maximalFrequentItemsets) {
			sb.append(itemset.toString()).append(newline);
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
}
