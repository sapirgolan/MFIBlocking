package fimEntityResolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Clusters {
 
	HashMap<Integer, Set<Set<Integer>>> transactionsClusters;
	Set<Set<Integer>> clusters;
	int minSup;
	
	private static String transactionSeperator = " ";
	
	public HashMap<Integer, Set<Set<Integer>>> getTransactionsClusters(){
		return transactionsClusters;
	}
	
	public Set<Set<Integer>> getClusters(){
		return clusters;
	}
	
	public Clusters(int numOftransactions, int minSup){
		transactionsClusters = new HashMap<Integer, Set<Set<Integer>>>(numOftransactions);
		clusters = new HashSet<Set<Integer>>();
		this.minSup = minSup;
	}
	public Clusters(int numOftransactions, String clustersFile, int minSup){
		this(numOftransactions,minSup);
		try {
			BufferedReader clustersFilereader = new BufferedReader(new FileReader(
					new File(clustersFile)));
			String currLine = "";
			while (currLine != null) {
				try {
					currLine = clustersFilereader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (currLine == null) {
					break;
				}
				if(currLine.length() == 0){
					continue;
				}
				String[] transactions = currLine.trim().split(transactionSeperator);
				Set<Integer> cluster = new HashSet<Integer>(transactions.length);
				for (String transaction : transactions) {
					Integer transactionKey = Integer.parseInt(transaction);					
					cluster.add(transactionKey);
				}
				clusters.add(cluster);
				mapClusterToTransactions(cluster);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void addCluster(Set<Integer> newCluster){
		clusters.add(newCluster);
		mapClusterToTransactions(newCluster);
		
	}
	
	private void mapClusterToTransactions(Set<Integer> cluster){
		for (Integer transactionKey : cluster) {
			Set<Set<Integer>> transactionClusters = transactionsClusters.get(transactionKey);
			if(transactionClusters == null){
				transactionClusters = new HashSet<Set<Integer>>();
			}
			transactionClusters.add(cluster);
			transactionsClusters.put(transactionKey, transactionClusters);
		}
		
	}
	
	protected Set<Integer> selectBestClusterForTransaction(int transaction, Set<Set<Integer>> clusters){
		return null;
	}
/*	public void makeClustersDisjoint(){
		Set<Integer> transactions = transactionsClusters.keySet();
		for (Integer transaction : transactions) {
			Set<Set<Integer>> clusters = transactionsClusters.get(transaction);
			if(clusters.size() < 2)
				continue;
			
			Set<Integer> selectedCluster = selectBestClusterForTransaction(transaction, clusters);
			Set<Set<Integer>> newClusters = new HashSet<Set<Integer>>(1);
			newClusters.add(selectedCluster);
			transactionsClusters.put(transaction, newClusters);
			
			for (Set<Integer> cluster : clusters) {
				if(cluster.equals(selectedCluster))
					continue;
				cluster.remove(transaction);
				if(cluster.size()<minSup){// remove cluster from transactions
					for (Integer currTrans : cluster) {
						Set<Set<Integer>> transClusters = transactionsClusters.get(currTrans);
						transClusters.remove(cluster);
						if(transClusters.size() == 0){
							transactionsClusters.remove(currTrans);
						}
					}
					
				}
			}
		}
	}*/
}
