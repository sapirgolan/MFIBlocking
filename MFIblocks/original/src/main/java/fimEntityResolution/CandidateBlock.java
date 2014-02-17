package fimEntityResolution;

import java.util.List;
/***
 * Extends ParsedFrequentItemSet by adding next parameters:
 * 1. boolean minSupPredicate for nonFIs calculations (3.3 sparse neighborhood condition)
 * 2. boolean isFIPredicate for numOfFIs calculations (3.3 sparse neighborhood condition)
 * 3. boolean supportConditionPredicate for tooLarge calculations (3.1 support condition )
 * 4. boolean clusterJaccardPredicate for scorePruned calculations (3.2 ClusterJaccard score)
 * @author Jonathan Svirsky
 */
public class CandidateBlock extends ParsedFrequentItemSet {
	
	public boolean minSupPredicate; //JS: for nonFIs calculations (3.3 sparse neighborhood condition)
	public boolean isFIPredicate; //JS: for numOfFIs calculations (3.3 sparse neighborhood condition)
	public boolean supportConditionPredicate; //JS: for tooLarge calculations (3.1 support condition )
	public boolean clusterJaccardPredicate; //JS: for scorePruned calculations (3.2 ClusterJaccard score)
	
	public CandidateBlock(List<Integer> items, int supportSize) {
		super(items, supportSize);
		this.items = items;
		this.supportSize = supportSize;
		this.minSupPredicate=false;
		this.isFIPredicate=false;
		this.supportConditionPredicate=false;
		this.clusterJaccardPredicate=false;
	}

}
