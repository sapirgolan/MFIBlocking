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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import DataStructures.Attribute;
import DataStructures.EntityProfile;
import DataStructures.IdDuplicates;

public class ProfileReader {

	private static LexiconProfiles lexicon;
	private static String stopWordsFile;
	private static WordProcessor wordProcessor;	
	public static HashSet<IdDuplicates> groundTruth;
	public static ArrayList<EntityProfile>[] entityProfiles;
	public static SortedSet<String> attributeNames;
	public static Map<String, Integer> map;
	public static int[] denseCounter;
	public static Set<ComparableColumnsDensity> sortedDensity;
	//Constants:
	public static final String COMMA=",";
	public static final String DEFAULT_COLUMN_WIEGHT="0.9"; //for DS_Weights file
	public static final String PREFIX_LENGTH="100";   //for DS_Weights file
	public static int COLUMNS=10;
	public static String MOVIES_DS_FILE="DS_weights_movies.properties";
	public static String CDDB_DS_FILE="DS_weights_CDDB.properties";
	public static String RESTS_DS_FILE="DS_weights_RESTS.properties";
	public static DBSize dbSize;
	private static DatasetType dataset;
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

	public static void loadEntityProfileSets(String[] filePaths){

		for (int i=0;i<filePaths.length;i++){
			entityProfiles[i] = loadEntityProfile(filePaths[i]);
			dbSize.setSize(i, entityProfiles[i].size());
			System.out.println("File "+(i+1)+" loaded. Size: " +dbSize.getSize(i));
		}
		
		if (dbSize.getLimit() < Integer.MAX_VALUE) {
			long start = System.currentTimeMillis();
			reduceProfiles(filePaths);
			System.out.println("Profiles were reduced to required limit relative to their sizes in "+(System.currentTimeMillis()-start)/1000.0 + " seconds");
			start = System.currentTimeMillis();
			reduceGroundTruth();
			System.out.println("Ground truth was reduced relative to dataset in "+(System.currentTimeMillis()-start)/1000.0 + " seconds");
			
		}	
	}

	private static void reduceGroundTruth() {
		HashSet<IdDuplicates> newGroundTruth=new HashSet<IdDuplicates>();
		for (IdDuplicates pair : groundTruth){
			if (pair.getEntityId1() < dbSize.getSize(0) && pair.getEntityId2() < dbSize.getSize(1)){
				newGroundTruth.add(new IdDuplicates(pair.getEntityId1(), pair.getEntityId2()));
			}
		}
		System.out.println("original="+groundTruth.size());
		System.out.println("new="+newGroundTruth.size());
		groundTruth=newGroundTruth;
		
	}

	public static void reduceProfiles(String[] filePaths){
		for (int i=0;i<filePaths.length;i++){
			int newSize = dbSize.getRelativeSize(i);
			ArrayList<EntityProfile> entityProfileTemp=entityProfiles[i];
			entityProfiles[i]=new ArrayList<EntityProfile>();
			for (int j=0; j < newSize;j++){
				entityProfiles[i].add(entityProfileTemp.get(j));
			}
		}
		for (int i=0;i<filePaths.length;i++){
			dbSize.setSize(i, entityProfiles[i].size());
			System.out.println("DEBUG: Size of file "+i+ " is "+dbSize.getSize(i));
		}
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
	 * 10. dataset type
	 * 11. [limit for data-set size]
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		Runtime.getRuntime().gc();
		long globalStart = System.currentTimeMillis();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		System.out.println("Started on : " + dateFormat.format(new Date()));	
		
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
		dataset = new DatasetType(args[9]);
		long start = System.currentTimeMillis();

		//loading data
		entityProfiles = new ArrayList[inputFiles.length];
		dbSize = new DBSize(inputFiles.length);
		if (args.length>10) {
			dbSize.setLimit(Integer.parseInt(args[10]));
		}
		groundTruth = loadGroundTruth(groundTruthOutFilePath);
		System.out.println("Ground truth file loaded.");
		loadEntityProfileSets(inputFiles);
		System.out.println("Processing file with " + dbSize.getTotalSize() + " records");
		System.out.println("Time to load profiles "+ (System.currentTimeMillis()-start)/1000.0 + " seconds");
		
		//================testing DBPEDIA 20140701====START=========
//				extractMatchingTuples(1000,true);
//				System.out.println("DBSize was roughly reduced to :" + 1000 + " tuples");
//				dbSize.setSize(0, entityProfiles[0].size());
		//dbSize.setSize(1, entityProfiles[1].size());
		//================testing DBPEDIA 20140701=====END==========
		
		start = System.currentTimeMillis();
		//1. extract names from attributes list AND 
		attributeNames = new TreeSet<String>();
		long attributeCounter=0;
		for (int i=0 ; i<entityProfiles.length ; i++){
			for(int j=0 ; j<entityProfiles[i].size() ; j++){
				HashSet<Attribute> attributes = entityProfiles[i].get(j).getAttributes();
				for (Attribute attribute : attributes) {
					attributeNames.add( attribute.getName() );
					attributeCounter++;
				}
			}
		}
		
		//special case for datasets
		if (dataset.getName().equalsIgnoreCase("DBPEDIA")){
			System.out.println("Number of columns is set to "+COLUMNS+" .");
		}

		System.out.println("Attribute names were extracted. Total: " + attributeCounter + " attributes");
		System.out.println("Time to extract attributess " + (System.currentTimeMillis()-start)/1000.0 + " seconds");
		
		//2. build the map for numeric column indexing
		map = buildMapIndex(attributeNames);
		wordProcessor = new WordProcessor(new File(stopWordsFile),n_gramsParam,n_gramsParam);
		
		//Special case for DBPEDIA dataset
		if (dataset.getName().equalsIgnoreCase("DBPEDIA")){
			//3. choose dense columns only
			start = System.currentTimeMillis();
			int maximalAttributeNumber=0;
			denseCounter=new int[attributeNames.size()];
			sortedDensity=new TreeSet<ComparableColumnsDensity>();
			for (ArrayList<EntityProfile> profiles:entityProfiles){
				for (EntityProfile entityProfile : profiles){
					HashSet<Attribute> attributes = entityProfile.getAttributes();
					maximalAttributeNumber=Math.max(maximalAttributeNumber, entityProfile.getAttributes().size());
					for (Attribute attribute : attributes) {
						if (attribute.getValue()!=null && getCleanString(attribute.getValue())!=null) 
							denseCounter[map.get(attribute.getName())]++;							
					}
				}
			}
			for (int i=0; i<attributeNames.size();i++){ 
				sortedDensity.add(new ComparableColumnsDensity(i, denseCounter[i])); 

			}
			System.out.println("Time to calculate density: "+(System.currentTimeMillis()-start)/1000.0 + " seconds");
			System.out.println("Maximum number of attributes in profile: "+maximalAttributeNumber);
		}
		if (dataset.getName().equalsIgnoreCase("MOVIES")){
			lexicon = new LexiconProfiles(new File (MOVIES_DS_FILE));
		}
		else if (dataset.getName().equalsIgnoreCase("CDDB")){
			lexicon = new LexiconProfiles(new File (CDDB_DS_FILE));
		}
//		else if (dataset.getName().equalsIgnoreCase("RESTS")){ //20141224 test for custom DS_WEIGHTS.properties file
//			lexicon = new Lexicon(new File (RESTS_DS_FILE));
//		}
		else {
			//4. create DS_weights.properties file
			start = System.currentTimeMillis();
			File DS_weightsFile= createDS_weightsFile();
			System.out.println(DS_weightsFile.getAbsolutePath().toString());
			writeMapToDS_weightsFile(DS_weightsFile);
			System.out.println("Time to create DS_weights.properties file: "+(System.currentTimeMillis()-start)/1000.0 + " seconds");
			//5. construct lexicon object
			lexicon = new LexiconProfiles(DS_weightsFile);
			DS_weightsFile = null;

		}
		

		numericOutputWriter = new BufferedWriter(new FileWriter(new File(numericOutFilePath)));
		matchWriter = new BufferedWriter( new FileWriter(new File(matchOutFilePath)));
		stringOutputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recordOutFilePath)));
		try {
			createMatchingFile(inputFiles.length);
			System.out.println("MatchingFile was created");
			groundTruth=null; //JS: not used anymore 20140506
			removeTooFrequentItems(dbSize.getTotalSize(),idfThreshParam );
			System.out.println("Frequent items were removed!");			
			int recordId = 1;
			for (int i=0;i<entityProfiles.length;i++){
				for (int j=0;j<entityProfiles[i].size();j++){
					StringBuilder cleanStringBuilder = new StringBuilder();			
					StringBuilder numericStringBuilder = new StringBuilder();

					for (Attribute attribute : entityProfiles[i].get(j).getAttributes()){
						String toWrite = getCleanString(attribute.getValue());
						if(lexicon.getPrefixLengthForColumn(map.get(attribute.getName())) > 0){						
							toWrite = toWrite.substring(0, 
									Math.min(lexicon.getPrefixLengthForColumn(map.get(attribute.getName())),toWrite.length()));						
						}		
						if(lexicon.getColumnWeight(map.get(attribute.getName())) > 0){
							cleanStringBuilder.append(toWrite).append(" ");
						}
						//else System.out.println("DEBUG: value " + toWrite +" is from attribute "+ attribute.getName()+ " and it's weight is 0");

						String ngramIdString = getNGramIdString(recordId, toWrite,map.get(attribute.getName()), true);
						if(ngramIdString.trim().length() > 0){
							numericStringBuilder.append(ngramIdString).append(" ");
						}
						attribute=null;
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
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Total time to convert files: " + (System.currentTimeMillis()-globalStart)/1000.0 + " seconds");

	}

	//JS: use to reduce dbpedia file to required limit only from ground truth
	private static void extractMatchingTuples(int limit,boolean createGroundTruth) {
		if (dataset.getName().equalsIgnoreCase("MOVIES") || dataset.getName().equalsIgnoreCase("DBPEDIA")){
			int count=0;
			HashSet<IdDuplicates> newGroundTruth=new HashSet<>();
			ArrayList<EntityProfile> extractedEntityProfiles = new ArrayList<>() ;
			for (IdDuplicates pair : groundTruth){
				int id1=pair.getEntityId1();
				int id2=pair.getEntityId2();
				if (entityProfiles[0].size() > id1 && entityProfiles[1].size() > id2) {
					extractedEntityProfiles.add(entityProfiles[0].get(id1));
					extractedEntityProfiles.add(entityProfiles[1].get(id2));
					count+=2;
					newGroundTruth.add(new IdDuplicates(count-1, count));
				}
				else {
					System.out.println("Index of dublicates is out of range: id1=" + id1+", id2="+id2);
				}
				if (count >= limit)
					break;
			}
			entityProfiles[1]=extractedEntityProfiles;
			entityProfiles[0]=new ArrayList<>();
			if (createGroundTruth) 
				groundTruth=newGroundTruth;
		}
		else{
			int count=0;
			HashSet<IdDuplicates> newGroundTruth=new HashSet<>();
			ArrayList<EntityProfile> extractedEntityProfiles = new ArrayList<>() ;
			for (IdDuplicates pair : groundTruth){
				int id1=pair.getEntityId1();
				int id2=pair.getEntityId2();
				if (entityProfiles[0].size() > id1 && entityProfiles[0].size() > id2) {
					extractedEntityProfiles.add(entityProfiles[0].get(id1));
					extractedEntityProfiles.add(entityProfiles[0].get(id2));
					count+=2;
					newGroundTruth.add(new IdDuplicates(count-1, count));
				}
				else {
					System.out.println("Index of dublicates is out of range: id1=" + id1+", id2="+id2);
				}
				if (count >= limit)
					break;
			}
			entityProfiles[0]=extractedEntityProfiles;
			//entityProfiles[1]=new ArrayList<>();
			if (createGroundTruth) 
				groundTruth=newGroundTruth;
		}

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
		if (numOfInputFiles==2){ //for two-files inputs 20140506
			long linesNumPrefix=entityProfiles[0].size();
			for (IdDuplicates groundPair:groundTruth){
				StringBuilder sb = new StringBuilder();
				sb.append(groundPair.getEntityId1()+1).append(" ");
				sb.append(groundPair.getEntityId2()+1+linesNumPrefix).append(" ");
				matchWriter.write(sb.toString());
				matchWriter.newLine();
			}
		}
		else{
			for (IdDuplicates groundPair:groundTruth){
				StringBuilder sb = new StringBuilder();
				//20140713 adding 1 to match index (records start from 1 and index from 0)
				sb.append(groundPair.getEntityId1()+1).append(" ");
				sb.append(groundPair.getEntityId2()+1).append(" ");
				matchWriter.write(sb.toString());
				matchWriter.newLine();
			}
		}

		matchWriter.close();
		System.out.println("total number of pairs in match file: " + groundTruth.size());
	}

	private static void removeTooFrequentItems( double DBSize, double IDFThresh){
		int recordId = 1;

		for (int i=0;i<entityProfiles.length;i++){
			for (int j=0;j<entityProfiles[i].size();j++){

				for (Attribute attribute : entityProfiles[i].get(j).getAttributes()){
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
					//if (IS_DBPedia) attribute=null;  //JS: 20140509
				}
				recordId++;
				//if (IS_DBPedia)  entityProfiles[i].add(j, null); // JS: 20140509

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
		String[] values = value.split("[+\\-*/\\^ .,?!]+");
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
			if (dataset.getName().equalsIgnoreCase("DBPEDIA")){

				String columnWeight=""; //JS: 20140508 if the density of the column is less than threshold the column's weight is zero.
				List<Integer> denseColumnsIDs=new ArrayList<Integer>();
				int i=0; 
				for(ComparableColumnsDensity object: sortedDensity){
					if(i<COLUMNS){
						System.out.println("Density "+ (i+1) +" is "+object.density);
						denseColumnsIDs.add(i, object.columnID);
						i++;
					}
					else break;

				}
				for (String field: attributeNames){
					if (denseColumnsIDs.contains(map.get(field))){
						columnWeight=DEFAULT_COLUMN_WIEGHT;
					}
					else columnWeight="0";
					bufferedWriter.write("#"+field);
					bufferedWriter.newLine();
					bufferedWriter.write(counter+"="+counter+","+columnWeight+","+PREFIX_LENGTH);
					bufferedWriter.newLine();
					counter++;
				}	
			}
			else {
				for (String field: attributeNames){
					bufferedWriter.write("#"+field);
					bufferedWriter.newLine();
					bufferedWriter.write(counter+"="+counter+","+DEFAULT_COLUMN_WIEGHT+","+PREFIX_LENGTH);
					bufferedWriter.newLine();
					counter++;
				}
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

