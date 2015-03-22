package preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Restaurants {
	
	private static Set<String> distinctRecords;
	private static LexiconProfiles lexicon;
	private static String swFile;
	private static WordProcessor wordProcessor;
	
	public static void main(String args[]){
		String input = args[0];		//input file	
		String numericOutFile = args[1];		
		String paramsFile = args[2];
		swFile = args[3];
		String lexiconOutFile = args[4];
		String noSWOutFile = args[5];
		int ngrams = (args.length > 6 ? Integer.parseInt(args[6]):0);
		lexicon = new LexiconProfiles(new File(paramsFile));
		wordProcessor = ((ngrams == 0) ? new WordProcessor(new File(swFile)):
			new WordProcessor(new File(swFile),ngrams,ngrams));
		distinctRecords = new HashSet<String>();
		BufferedReader recordsFileReader = null;
		BufferedWriter numericOutputWriter = null;
		BufferedWriter stringOutputWriter = null;
		
		try{
			recordsFileReader = new BufferedReader(new FileReader(
					new File(input)));	
			numericOutputWriter = new BufferedWriter(new FileWriter
					(new File(numericOutFile)));    
			stringOutputWriter = new BufferedWriter(new FileWriter
					(new File(noSWOutFile)));
			String recordLine = "";
			int recordId = 1;
			while(recordLine != null){
				String restaurantLine = recordsFileReader.readLine();
				if(restaurantLine == null)
					break;
				List<String> swFreeTerms = wordProcessor.removeStopwordsAndSpecialChars(restaurantLine);
				String restaurantLineNoSW = WordProcessor.concatListMembers(swFreeTerms);					
	        	distinctRecords.add(restaurantLineNoSW);
	        	List<String> nGrams = wordProcessor.processValue(restaurantLineNoSW);
	        	List<Integer> nGramIds = lexicon.getNGramIds(nGrams,recordId++,1);
	        	String numericRestaurant = WordProcessor.concatListMembers(nGramIds);
	        	numericOutputWriter.write(numericRestaurant);
	        	numericOutputWriter.newLine();
	        	stringOutputWriter.write(restaurantLineNoSW);
	        	stringOutputWriter.newLine();	        	
			}
			lexicon.exportToPropFile(lexiconOutFile);
		}
		catch(Exception e){
			System.out.println(e);
		}
		finally{
			try {
				recordsFileReader.close();
				numericOutputWriter.close();
				stringOutputWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	
	
	
}
