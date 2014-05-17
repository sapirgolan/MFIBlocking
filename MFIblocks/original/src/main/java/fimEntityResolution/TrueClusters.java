package fimEntityResolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import candidateMatches.CandidatePairs;

public class TrueClusters {

	private static String transactionSeperator = " ";
	private static int cardinality;	
	
	private CandidatePairs cps;
	

	
	public CandidatePairs groundTruthCandidatePairs(){
		return cps;
	}
	
	public TrueClusters(int numOftransactions, String clustersFile){
		cps = new CandidatePairs(); //no limit
		int numOfTruePairs = 0;
		BufferedReader clustersFilereader = null;
		try {
			clustersFilereader = new BufferedReader(new FileReader(
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
						cps.setPair(cluster.get(i), cluster.get(j), 0.0);
						numOfTruePairs++;
					}
				}				
			}
			cardinality=numOfTruePairs;
			System.out.println("num of pairs: groundTruth.cardinality "  + numOfTruePairs/*groundTruth.numOfSet()*/);			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (clustersFilereader!= null) {
				try {
					clustersFilereader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public int getCardinality(){
		return cardinality;
	}
}
