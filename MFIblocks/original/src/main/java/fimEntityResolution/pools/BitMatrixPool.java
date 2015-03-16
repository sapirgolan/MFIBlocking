package fimEntityResolution.pools;

import il.ac.technion.ie.data.structure.BitMatrix;

import java.util.LinkedList;


public class BitMatrixPool {

	private LinkedList<BitMatrix> matrices = new LinkedList<BitMatrix>();
	private int created = 0;
	private static BitMatrixPool self = null;
	
	private BitMatrixPool(){};
	
	public static BitMatrixPool getInstance(){
		if(self == null){
			self = new BitMatrixPool();
		}
		return self;
	}
	
	public int getNumCreated(){
		return created;
	}
	
	//assumption is all bms are the same size
	public BitMatrix getBM(int size){	
		synchronized(this){
			if(matrices.size() > 0){
				BitMatrix toReturn = matrices.remove();
				toReturn.clearAll();
				if(toReturn.numOfSet() > 0){				
					System.out.println("about to return a matrix with " + toReturn.numOfSet() + " bits set right after calling clearAll!!");
				}
				return toReturn;
			}
			created++;
			System.out.println("num of bms created is " + created);
		}
		return new BitMatrix(size);
	
		
	}
	
	public void returnMatrix(BitMatrix toReturn){	
		toReturn.clearAll();		
		synchronized(this){
			matrices.add(toReturn);
		}
	}
	
	public void restart(){
		System.out.println(" Clearing " + matrices.size() + " BMs");
		matrices = null;
		matrices =  new LinkedList<BitMatrix>();
	}
}
