package preprocessing;


import il.ac.technion.ie.utils.SerializationUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

//import com.javamex.classmexer.MemoryUtil;
//import com.javamex.classmexer.MemoryUtil.VisibilityFilter;

import DataStructures.Attribute;
import DataStructures.EntityProfile;
import DataStructures.IdDuplicates;

public class ProfileReader {
	//private static final String CLUSTER_ATT_NAME = "class";
	//private static final String SOURCE_ATT_NAME = "source";
	//private static Map<String,List<Integer>> matches;
	private static Lexicon lexicon;
	private static String stopWordsFile;
	private static WordProcessor wordProcessor;	
	public static int DB_Size;
	public static HashSet<IdDuplicates> groundTruth;
	public static ArrayList<EntityProfile>[] entityProfiles;
	public static SortedSet<String> attributeNames;
	public static Map<String, Integer> map;
	public static int[] denseCounter;
	//JS :CONSTs:
	public static final String COMMA=",";
	public static final String DEFAULT_COLUMN_WIEGHT="0.1"; //for DS_Weights file
	public static final String PREFIX_LENGTH="30";   //for DS_Weights file
	public static final int DENSE_THRESHOLD=0;
	//BufferWriters
	private static BufferedWriter numericOutputWriter;
	private static BufferedWriter stringOutputWriter;
	private static BufferedWriter matchWriter;	


	@SuppressWarnings("unchecked")
	public static ArrayList<EntityProfile> loadEntityProfile(String filePath) {

		File file = new File(filePath);
		if (file.exists()) {
			ArrayList<EntityProfile> profileList = (ArrayList<EntityProfile>) SerializationUtilities.loadSerializedObject(filePath);
			return profileList;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static HashSet<IdDuplicates> loadGroundTruth(String filePath) {

		File file = new File(filePath);
		if (file.exists()) {
			HashSet<IdDuplicates> groundList = (HashSet<IdDuplicates>) SerializationUtilities.loadSerializedObject(filePath);
			return groundList;
		}
		return null;
	}
	/**
	 * Input parameters:
	 * 1. input profiles files (separated by commas)
	 * 2. output numeric file path  
	 * 3. output match file path 
	 * 4. input ground truth file path 
	 * 5. input stopwords file path
	 * 6. output lexicon file path
	 * 7. output records file path
	 * 8. n-grams parameter {3,4,5,..}
	 * 9. pruning threshold parameter
	 * 10. [source map file path]
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long start = System.currentTimeMillis();
		System.out.println("Started...");
		//reading inputs:
		String[] inputFiles = args[0].split(COMMA);
		String numericOutFilePath = args[1];
		String matchOutFilePath = args[2];
		String groundTruthOutFilePath = args[3];
		stopWordsFile = args[4];
		String lexiconOutFilePath = args[5];
		String recordOutFilePath = args[6];
		int n_gramsParam = Integer.parseInt(args[7]);	
		double idfThreshParam = Double.parseDouble(args[8]);
		String sourceMapFilePath = (args.length > 9 ? args[9] : null);
		
		//loading data
		DB_Size=0;
		entityProfiles=new ArrayList[inputFiles.length];
		for (int i=0;i<inputFiles.length;i++){
			entityProfiles[i] = loadEntityProfile(inputFiles[i]);
			System.out.println("File "+(i+1)+" loaded.");
			DB_Size+=entityProfiles[i].size();
		}
		System.out.println("Processing file with " + DB_Size + " records");
		
		groundTruth=loadGroundTruth(groundTruthOutFilePath);
		System.out.println("Ground truth file loaded.");

		//1. extract names from attributes list AND 
		attributeNames = new TreeSet<String>();
		long attributeCounter=0;
		for (ArrayList<EntityProfile> profiles:entityProfiles){
			for (EntityProfile entityProfile : profiles){
				HashSet<Attribute> attributes = entityProfile.getAttributes();
				for (Attribute attribute : attributes) {
					attributeNames.add( attribute.getName() );
					attributeCounter++;
				}
			}
		}

		System.out.println("Attribute names were extracted. Total: "+attributeCounter+" attributes");
		//2. build the map for numeric column indexing
		map = buildMapIndex(attributeNames);
		
		wordProcessor = new WordProcessor(new File(stopWordsFile),n_gramsParam,n_gramsParam);
		
		//3. choose dense columns only
		start = System.currentTimeMillis();
		denseCounter=new int[map.size()];
		for (ArrayList<EntityProfile> profiles:entityProfiles){
			for (EntityProfile entityProfile : profiles){
				HashSet<Attribute> attributes = entityProfile.getAttributes();
				for (Attribute attribute : attributes) {
					if (attribute.getValue()!=null && getCleanString(attribute.getValue())!=null) 
						denseCounter[map.get(attribute.getName())]++;
				}
			}
		}
		System.out.println("Column density was calculated in "+(System.currentTimeMillis()-start)/1000.0 + " seconds");
		
		//4. create DS_weights.properties file
		File DS_weightsFile= createDS_weightsFile();
		writeMapToDS_weightsFile(DS_weightsFile);
		//5. construct lexicon object
		lexicon = new Lexicon(DS_weightsFile);
		DS_weightsFile=null;
		
		numericOutputWriter = new BufferedWriter(new FileWriter(new File(numericOutFilePath)));
		matchWriter = new BufferedWriter( new FileWriter(new File(matchOutFilePath)));
		stringOutputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recordOutFilePath)));
		BufferedWriter sourceWriter = null;
		if(sourceMapFilePath != null && sourceMapFilePath.length() > 0){
			sourceWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sourceMapFilePath)));
		}
		
		try {
			createMatchingFile(inputFiles.length);
			System.out.println("MatchingFile was created");
			groundTruth=null; //JS: not used anymore 20140506
			removeTooFrequentItems(DB_Size,idfThreshParam );
			System.out.println("Frequent items were removed!");
			
			int recordId = 1;
			//boolean previousEntityList=true;
			for (ArrayList<EntityProfile> profiles : entityProfiles){
//				if (previousEntityList) {
//					previousEntityList=false;
//				}
//				else entityProfiles[0]=null;
				for (EntityProfile entityProfile : profiles){
					
					StringBuilder cleanStringBuilder = new StringBuilder();			
					StringBuilder numericStringBuilder = new StringBuilder();
					
					for (Attribute attribute : entityProfile.getAttributes()){
						String toWrite = getCleanString(attribute.getValue());
						if(lexicon.getPrefixLengthForColumn(map.get(attribute.getName())) > 0){						
							toWrite = toWrite.substring(0, 
									Math.min(lexicon.getPrefixLengthForColumn(map.get(attribute.getName())),toWrite.length()));						
						}		
						if(lexicon.getColumnWeight(map.get(attribute.getName())) > 0){
							cleanStringBuilder.append(toWrite).append(" ");
						}

						String ngramIdString = getNGramIdString(recordId, toWrite,map.get(attribute.getName()), true);
						if(ngramIdString.trim().length() > 0){
							numericStringBuilder.append(ngramIdString).append(" ");
						}
					}

					String numericString = numericStringBuilder.toString().trim();
					numericOutputWriter.write(numericString.trim());
					numericOutputWriter.newLine();
					stringOutputWriter.write(cleanStringBuilder.toString().trim());
					stringOutputWriter.newLine();
					
					
					recordId++;
					
				}
			}

			numericOutputWriter.close();
			stringOutputWriter.close();
			lexicon.exportToPropFile(lexiconOutFilePath);
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
		System.out.println("Time to convert files: " + (System.currentTimeMillis()-start)/1000.0 + " seconds");
		
	}

	private static File createDS_weightsFile() {
		File file = new File("DS_weights" + ".properties");
		int index = 0;
		while (file.exists() &&  !file.delete()) {
			file = new File ("DS_weights" + "_" +  index + ".properties");
			index++;
		}
		return file;
	}
	
	private static Map<String, Integer> buildMapIndex(SortedSet<String> fieldsNames) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		int index = 0;
		for (String fieldsName : fieldsNames) {
			map.put(fieldsName, index);
			index++;
		}
		return map;
	}
	
	private static void createMatchingFile(int numOfInputFiles) throws IOException{
		if (numOfInputFiles==2){ //JS for DBPedia 20140506
			long linesNumPrefix=entityProfiles[0].size();
			for (IdDuplicates groundPair:groundTruth){
				StringBuilder sb = new StringBuilder();
				sb.append(groundPair.getEntityId1()).append(" ");
				sb.append(groundPair.getEntityId2()+linesNumPrefix).append(" ");
				matchWriter.write(sb.toString());
				matchWriter.newLine();
			}
		}
		else{
			for (IdDuplicates groundPair:groundTruth){
				StringBuilder sb = new StringBuilder();
				sb.append(groundPair.getEntityId1()).append(" ");
				sb.append(groundPair.getEntityId2()).append(" ");
				matchWriter.write(sb.toString());
				matchWriter.newLine();
			}
		}
		
		matchWriter.close();
		System.out.println("total number of pairs in match file: " + groundTruth.size());
	}

	private static void removeTooFrequentItems( double DBSize, double IDFThresh){
		int recordId = 1;
		for (ArrayList<EntityProfile> profiles :entityProfiles){
			for (EntityProfile entityProfile : profiles){
				for (Attribute attribute : entityProfile.getAttributes()){
					try{
						if(lexicon.getColumnWeight(map.get(attribute.getName())) <= 0){
							continue;
						}
					}
					catch(NullPointerException npex){
						System.out.println("JS: There are some extra-parts in line "+recordId);
						continue;
					}
					String toWrite = getCleanString(attribute.getValue());	
					if(lexicon.getPrefixLengthForColumn(map.get(attribute.getName())) > 0){						
						toWrite = toWrite.substring(0, 
								Math.min(lexicon.getPrefixLengthForColumn(map.get(attribute.getName())),toWrite.length()));						
					}
					//uses functions that in the end call to Lexicon.addWord that updates lexicon
					getNGramIdString(recordId, toWrite,map.get(attribute.getName()), false);				
				}
				recordId++;
				
			}
			System.out.println("removeTooFrequentItems: "+recordId+" records done.");
		}
		//now remove high frequency items
		lexicon.removeFrequentItems(DBSize, IDFThresh);
	}

	private static String getCleanString(String value){
		List<String> swFreeTerms = wordProcessor.removeStopwordsAndSpecialChars(value);
		return WordProcessor.concatListMembers(swFreeTerms);
	}
	
	
	private static String getNGramIdString(int recordId, String value, int propertyId, boolean removedFrequent){
		List<Integer> NGramsIds = new ArrayList<Integer>();
		//String[] values = value.split("\\s+");
		String[] values = value.split("[+\\-*/\\^ .,?!]+"); //JS 20140506 for DBPEDIA only
		for(int i=0; i < values.length ;i++){
			if (values[i]==null) continue; //JS 20140506
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
	
	private static void writeMapToDS_weightsFile(File file) {
		try {
			Writer writer = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(writer);	
			int counter=0;
			String columnWeight=""; //JS: 20140508 if the density of the column is less than threshold the column's weight is zero.
			for (String field: attributeNames){
				if (denseCounter[map.get(field)]>DENSE_THRESHOLD){
					columnWeight=DEFAULT_COLUMN_WIEGHT;
				}
				else columnWeight="0";
				bufferedWriter.write("#"+field);
				bufferedWriter.newLine();
				bufferedWriter.write(counter+"="+counter+","+columnWeight+","+PREFIX_LENGTH);
				bufferedWriter.newLine();
				counter++;
			}
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				System.out.println("Failed to close BufferedWriter");
				e.printStackTrace();
			}	
		}
		catch (IOException e3) {
			System.out.println("Failed to create filewriter");
			e3.printStackTrace();
		}
	}

}

