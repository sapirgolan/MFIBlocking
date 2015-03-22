package preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecondString {

	private static Map<String,List<Integer>> matches;
	private static BufferedWriter numericOutputWriter;
	private static BufferedWriter stringOutputWriter;
	private static BufferedWriter matchWriter;	
	private static LexiconProfiles lexicon;
	private static String swFile;
	private static WordProcessor wordProcessor;
	
	private final static String regexExp = "\\t+";
	
	public static void main(String[] args){
		String inputFile = args[0];
		matches = new HashMap<String,List<Integer>>();		
		String numericOutputFile = args[1];
		String matchFile = args[2];
		String paramsFile = args[3];
		swFile = args[4];
		String lexiconOutFile = args[5];
		String recordOutFile = args[6];		
		
		lexicon = new LexiconProfiles(new File(paramsFile));
		wordProcessor = new WordProcessor(new File(swFile));
		
		try {
			numericOutputWriter = new BufferedWriter(new FileWriter(new File(
					numericOutputFile)));
			matchWriter = new BufferedWriter(
					new FileWriter(new File(matchFile)));
			stringOutputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recordOutFile)));
			
			BufferedReader inputReader = new BufferedReader(new FileReader(
					new File(inputFile)));
			String currLine = "";
			int recordId = 1;
			while (currLine != null) {						
				currLine = inputReader.readLine();	
				if(currLine == null)
					break;
				String[] parts = currLine.split(regexExp);		
				String clusterId = parts[1].toLowerCase().trim();
				String concatedImportentparts ="";
				if(parts.length <= 3){//source, cluster id, and item
					concatedImportentparts = parts[2];
				}
				else{
					StringBuilder sb = new StringBuilder();
					for (int i=2 ; i < parts.length ; i++) {
						sb.append(parts[i]).append(" ");
					}
					concatedImportentparts = sb.toString();
				}
				String cleanString = getCleanString(concatedImportentparts.trim());
				StringBuilder numericStringBuilder = new StringBuilder();
				for(int i=2 ; i < parts.length ; i++){					 
					numericStringBuilder.append(getNGramIdString(recordId, parts[i],i-1)).append(" ");
				}
				String numericString = numericStringBuilder.toString().trim();
				numericOutputWriter.write(numericString.trim());
				numericOutputWriter.newLine();
				stringOutputWriter.write(cleanString.trim());
				stringOutputWriter.newLine();
				
				List<Integer> cluster = null;
				if(matches.containsKey(clusterId)){
					cluster = matches.get(clusterId);
				}
				else{
					cluster = new ArrayList<Integer>();
				}
				cluster.add(recordId);
				matches.put(clusterId, cluster);
				recordId++;
			}
			 //write the matches	       
	        int numOfpairs = 0;
	        for (List<Integer> cluster : matches.values()) {
	        	StringBuilder sb = new StringBuilder();
	        	Collections.sort(cluster);
				if(cluster.size() < 2) continue;
				numOfpairs += cluster.size()*(cluster.size()-1)*0.5;
				for (Integer integer : cluster) {
					sb.append(integer).append(" ");
				}
				matchWriter.write(sb.toString());
				matchWriter.newLine();
			}
	        numericOutputWriter.close();
	        stringOutputWriter.close();
	        matchWriter.close();	        
	        lexicon.exportToPropFile(lexiconOutFile);
	        System.out.println("total number of pairs in match file: " + numOfpairs);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static String getCleanString(String value){
		List<String> swFreeTerms = wordProcessor.removeStopwordsAndSpecialChars(value);
		return WordProcessor.concatListMembers(swFreeTerms);
	}
	
	private static String getNGramIdString(int recordId, String value, int propertyId){
		List<Integer> NGramsIds = new ArrayList<Integer>();
		String[] values = value.split("\\s+");
		for(int i=0; i < values.length ;i++){			
			List<String> valueNgrams = getNGrams(values[i]);
			NGramsIds.addAll(getNGramIds(valueNgrams,recordId,propertyId)); //generic id
		}
		return WordProcessor.concatListMembers(NGramsIds);		
	}
	
	private static List<String> getNGrams(String value){
		return wordProcessor.processValue(value);
	}
	private static List<Integer> getNGramIds(List<String> nGrams, int recordId, int attributeId){		
		List<Integer> retVal = new ArrayList<Integer>(nGrams.size());		
		for (String nGram : nGrams) {
			retVal.add(lexicon.addWord(attributeId, recordId, nGram));
		}
		return retVal;
	}
}
