package fimEntityResolution;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class will enable to efficiently store FIs.
 * FIs will be sorted according to their support size, and will be inserted to the tree.
 * Each node will contain its value and the BitSet representing the support from the root
 * down to its node. 
 * 
 * @author batya
 *
 */
public class FITree {
	private int value; //value of the node
	private List<FITree> children; //children of this node
	private FITree parent = null;
	private static FITree root;
	private final static FrequentItemIdsComparator comparator = new FrequentItemIdsComparator();
	private static int numOfnodes = 0;
	
	public int hashCode(){
		return value;		
	}
	

	public void clearAll(){
		root = null;
	}
	public int getValue(){
		return value;
	}
	
	public boolean hasChildren(){
		return !children.isEmpty();
	}
	
	public boolean hasParent(){
		return (parent!=null);
	}
	
	
	public Collection<FITree> getChildren(){
		return children;
	}
	
	public FITree getParent(){
		return parent;
	}
	
	public static FITree getInstance(){
		return getRoot();
	}
	
	
	private FITree(int value){
		this.value = value;
		children = new LinkedList<FITree>();
	}
	
	private static FITree getRoot(){
		if(root == null){			
			root = new FITree(0);
			numOfnodes++;
		}
		return root;
	}
	
	public void insert(List<Integer> items){
		Collections.sort(items,comparator);
		insertItemIds(items);		
	}
	
	
	
	private FITree getChildNode(int item){
		for(int i=0 ; i < children.size() ; i++){
			FITree currChild = children.get(i);
			if(currChild.value == item)
				return currChild;
		}		
		return null;
	}
	
	
	
	
	private void insertItemIds(List<Integer> sortedItems){
		FITree parentNode = getRoot();
		for (Integer itemId : sortedItems) {
			FITree childNode = parentNode.getChildNode(itemId);
			if(childNode == null){
				childNode = new FITree(itemId);
				parentNode.children.add(childNode);
				childNode.parent = parentNode;
				numOfnodes++;
			}
			parentNode = childNode;
			
		}			
	}
	
	public int getNumOfNodes(){
		return  numOfnodes;
	}
	public static void main(String[] args){
	
	}
	

	
}
