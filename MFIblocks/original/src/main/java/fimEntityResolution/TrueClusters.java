package fimEntityResolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import candidateMatches.CandidatePairs;

public class TrueClusters {

	private static String transactionSeperator = " ";
		
/*	public double minClusterPairScore = Double.MAX_VALUE;
	public double averageClusterPairScore = 0;
	*/
	
//	private BitMatrix groundTruth;
	private CandidatePairs cps;
	

/*	public BitMatrix groundTruth(){
		return groundTruth;
	}*/
	
	public CandidatePairs groundTruthCandidatePairs(){
		return cps;
	}
	
/*
	private void getPairScoreVals(){
		for (Pair pair: truePairs) {
			//CHANGE
			if(!Utilities.globalRecords.containsKey(pair.r1) || !Utilities.globalRecords.containsKey(pair.r2)){
				System.out.println("Either " + pair.r1 + " or " + pair.r2 + " is not in the globalRecords. ");
			}
			double currScore = StringSimTools.softTFIDF(Utilities.globalRecords.get(pair.r1), Utilities.globalRecords.get(pair.r2));
			averageClusterPairScore += currScore;
			minClusterPairScore = Math.min(minClusterPairScore, currScore);			
		}
		averageClusterPairScore=averageClusterPairScore/truePairs.size();
	}
*/
	public TrueClusters(int numOftransactions, String clustersFile){
	//	groundTruth = new BitMatrix(numOftransactions);		
		cps = new CandidatePairs(); //no limit
		int numOfTruePairs = 0;
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
				List<Integer> cluster = new ArrayList<Integer>(transactions.length);
				for (String transaction : transactions) {
					Integer transactionKey = Integer.parseInt(transaction);					
					cluster.add(transactionKey);
				}
				assert (cluster.size() >1);
				
				for(int i=0 ; i < cluster.size() ; i++){
					for(int j=i+1; j < cluster.size() ; j++){						
					//	groundTruth.setPair(cluster.get(i), cluster.get(j));
						cps.setPair(cluster.get(i), cluster.get(j), 0.0);
						numOfTruePairs++;
					}
				}				
			}
		//	getPairScoreVals();
			System.out.println("num of pairs: groundTruth.cardinality "  + numOfTruePairs/*groundTruth.numOfSet()*/);			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	
	private static Set<Pair> getPairs(Collection<Integer> group){
		Set<Pair> pairs = new HashSet<Pair>();
		List<Integer> temp = new ArrayList<Integer>(group.size());
		temp.addAll(group);
		for(int i=0 ; i < temp.size() ; i++){
			for(int j=i+1; j < temp.size() ; j++){
				Pair pair = new Pair(temp.get(i),temp.get(j));								
				pairs.add(pair);				
			}			
		}
		return pairs;
	}
	
}
