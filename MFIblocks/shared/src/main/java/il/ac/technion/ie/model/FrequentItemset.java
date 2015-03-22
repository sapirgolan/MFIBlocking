package il.ac.technion.ie.model;

import java.util.BitSet;


public class FrequentItemset{

	private BitSet items;
	private BitSet support;
	private String toString;
	
	

	FrequentItemset(BitSet items, BitSet support){
		this.items = items;
		this.support = support;
		toString = "items=" + items.toString() + " support="+support.toString(); 
	}
	
	public BitSet getItems(){
		return items;
	}
	
	public BitSet getSupport(){
		return support;
	}
	
	
	public boolean contains(FrequentItemset other){
		BitSet otherItems = other.getItems();
		for(Integer index=otherItems.nextSetBit(0); index>=0;
		index=otherItems.nextSetBit(index+1)){
			if(!items.get(index))
				return false;
		}
		return true;
	}

    public boolean equals(Object obj){
		if(obj.hashCode() != this.hashCode())
			return false;
		FrequentItemset other = (FrequentItemset)obj;
		return (other.getItems().equals(this.getItems()) && 
					other.getSupport().equals(this.getSupport()));
	}
	public int hashCode(){
		return toString.hashCode();
	}

	public String toString(){
		return toString;
	}
}
