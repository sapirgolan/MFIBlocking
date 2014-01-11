package preprocessing;

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

import au.com.bytecode.opencsv.CSVReader;

public class csvFile {

	private static final String CLUSTER_ATT_NAME = "class";
	private static final String SOURCE_ATT_NAME = "source";

	private static Map<String,List<Integer>> matches;
	private static BufferedWriter numericOutputWriter;
	private static BufferedWriter stringOutputWriter;
	private static BufferedWriter matchWriter;	
	private static Lexicon lexicon;
	private static String swFile;
	private static WordProcessor wordProcessor;	
	public static int DB_Size;
	
	public static void main(String[] args){
		String inputFile = args[0];
		matches = new HashMap<String,List<Integer>>();		
		String numericOutputFile = args[1];
		String matchFile = args[2];
		String paramsFile = args[3];
		swFile = args[4];
		String lexiconOutFile = args[5];
		String recordOutFile = args[6];
		int NGramSize = Integer.parseInt(args[7]);		
		double DBSize = Double.parseDouble(args[8]);
		DB_Size =  (int)DBSize;
		double IDFThresh = Double.parseDouble(args[9]);
		String sourceMapFile = (args.length > 10 ? args[10] : null);
		System.out.println("Processing file with " + DBSize + " records");
		
		lexicon = new Lexicon(new File(paramsFile));
		wordProcessor = new WordProcessor(new File(swFile),NGramSize,NGramSize);
		
		try {
			numericOutputWriter = new BufferedWriter(new FileWriter(new File(
					numericOutputFile)));
			matchWriter = new BufferedWriter(
					new FileWriter(new File(matchFile)));
			stringOutputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recordOutFile)));
			BufferedWriter sourceWriter = null;
			if(sourceMapFile != null && sourceMapFile.length() > 0){
				sourceWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sourceMapFile)));
			}
			CSVReader cvsReader = null; 
			
			
			cvsReader = new CSVReader(new FileReader(
						new File(inputFile)));
			removeTooFrequentItems(lexicon,cvsReader, DBSize,IDFThresh );
			cvsReader.close();
			
			
			cvsReader = new CSVReader(new FileReader(
					new File(inputFile)));
			String[] currLine = null;
			int recordId = 1;
			boolean first = true;
			String[] attNames= null;
			while ((currLine = cvsReader.readNext()) != null) {				
				if(first){
					attNames = currLine;	
					first = false;
					continue;
				}
				String[] parts = currLine;	
				int clusterAttIndex = getClusterFieldIndex(attNames);
				int sourceAttIndex = getSourceFieldIndex(attNames);
				if(sourceAttIndex >= 0 && sourceWriter != null){
					sourceWriter.write(parts[sourceAttIndex]);
					sourceWriter.newLine();
				}
				String clusterId = parts[clusterAttIndex];
				StringBuilder cleanStringBuilder = new StringBuilder();			
				StringBuilder numericStringBuilder = new StringBuilder();
				for(int i=0 ; i < parts.length ; i++){	
					if(i == clusterAttIndex|| i == sourceAttIndex){
						continue; // do not want to write this as part of the file
					}					
					String toWrite = getCleanString(parts[i]);			
					if(lexicon.getPrefixLengthForColumn(i) > 0){						
						toWrite = toWrite.substring(0, 
								Math.min(lexicon.getPrefixLengthForColumn(i),toWrite.length()));						
					}		
					if(lexicon.getColumnWeight(i) > 0){
						cleanStringBuilder.append(toWrite).append(" ");
					}
					
					String ngramIdString = getNGramIdString(recordId, toWrite,i, true);
					if(ngramIdString.trim().length() > 0){
						numericStringBuilder.append(ngramIdString).append(" ");
					}
				
				}
				String numericString = numericStringBuilder.toString().trim();
				numericOutputWriter.write(numericString.trim());
				numericOutputWriter.newLine();
				stringOutputWriter.write(cleanStringBuilder.toString().trim());
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
	        if(sourceWriter != null){
	        	sourceWriter.close();
	        }
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private static void removeTooFrequentItems(Lexicon lexicon, CSVReader reader, double DBSize, double IDFThresh){
		String[] currLine = null;
		int recordId = 1;
		boolean first = true;
		String[] attNames= null;
		try {
			while ((currLine = reader.readNext()) != null) {				
				if(first){
					attNames = currLine;	
					first = false;
					continue;
				}
				String[] parts = currLine;	
				int clusterAttIndex = getClusterFieldIndex(attNames);
				int sourceAttIndex = getSourceFieldIndex(attNames);
				
				for(int i=0 ; i < parts.length ; i++){	
					if(i == clusterAttIndex|| i == sourceAttIndex){
						continue; // do not want to write this as part of the file
					}					
					if(lexicon.getColumnWeight(i) <= 0){
						continue;
					}
					String toWrite = getCleanString(parts[i]);	
					if(lexicon.getPrefixLengthForColumn(i) > 0){						
						toWrite = toWrite.substring(0, 
								Math.min(lexicon.getPrefixLengthForColumn(i),toWrite.length()));						
					}		
					getNGramIdString(recordId, toWrite,i, false);				
						
				}
				recordId++;
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		//now remove high frequency items
		lexicon.removeFrequentItems(DBSize, IDFThresh);
		
	}
	
	private static int sourceFieldIndex = -1;
	private static int getSourceFieldIndex(String[] atts){
		if(sourceFieldIndex > 0){
			return sourceFieldIndex;
		}		
		for (int i=0 ; i < atts.length ; i++) {
			if(atts[i].equals(SOURCE_ATT_NAME)){
				sourceFieldIndex = i;
				break;
			}
		}
		return sourceFieldIndex;
		
	}
	
	private static int clusterFieldIndex = -1;
	private static int getClusterFieldIndex(String[] atts){
		if(clusterFieldIndex > 0){
			return clusterFieldIndex;
		}		
		for (int i=0 ; i < atts.length ; i++) {
			if(atts[i].equals(CLUSTER_ATT_NAME)){
				clusterFieldIndex = i;
				break;
			}
		}
		return clusterFieldIndex;
		
	}
	private static String getCleanStringNoWS(String value){
		List<String> swFreeTerms = wordProcessor.removeStopwordsAndSpecialChars(value);
		StringBuilder sb = new StringBuilder();
		for (String string : swFreeTerms) {
			sb.append(string);
		}
		return sb.toString();
	}
	private static String getCleanString(String value){
		List<String> swFreeTerms = wordProcessor.removeStopwordsAndSpecialChars(value);
		return WordProcessor.concatListMembers(swFreeTerms);
	}
	
	private static String getNGramIdString(int recordId, String value, int propertyId, boolean removedFrequent){
		List<Integer> NGramsIds = new ArrayList<Integer>();
		String[] values = value.split("\\s+");
		for(int i=0; i < values.length ;i++){			
			List<String> valueNgrams = getNGrams(values[i]);
			NGramsIds.addAll(getNGramIds(valueNgrams,recordId,propertyId, removedFrequent)); //generic id
		}
		StringBuilder sb = new StringBuilder();
		for (Integer ngramId : NGramsIds) {
			sb.append(ngramId).append(" ");
		}
		return sb.toString().trim();		
		
	}
	
	private static List<String> getNGrams(String value){
		return wordProcessor.processValue(value);
	}
	

	private static List<Integer> getNGramIds(List<String> nGrams, int recordId, int attributeId, boolean removedFrequent){		
		List<Integer> retVal = new ArrayList<Integer>(nGrams.size());		
		for (String nGram : nGrams) {
			int wordId = 0;
			if(!removedFrequent)
				wordId = lexicon.addWord(attributeId, recordId, nGram);
			else{
				//check if ngram falls in expected support range
				wordId = lexicon.wordExists(attributeId, nGram);
			}
			if(wordId >= 0 ){
				retVal.add(wordId);
			}
		}
		return retVal;
	}
}
