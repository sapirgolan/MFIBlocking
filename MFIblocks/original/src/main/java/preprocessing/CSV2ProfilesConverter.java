package preprocessing;

import il.ac.technion.ie.utils.SerializationUtilities;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import DataStructures.EntityProfile;
import DataStructures.IdDuplicates;
import au.com.bytecode.opencsv.CSVReader;
/***
 * Converts input CSV file into "Profiles" format (George Papadakis) = serialized  ArrayList<EntityProfile>.
 * (treats the records and golden truth)
 * @author Jonathan Svirsky
 * 20141213
 *
 */
public class CSV2ProfilesConverter {

	public static final int CENSUS_FIRST_FILE_SIZE=449;
	/**
	 * @param:
	 * 1. input file in CSV format
	 * 2. input file in CSV format
	 * 3. match file
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		createProfiles(args[0]);
		createProfiles(args[1]);
		createTruthMatch(args[2]);
		
			
		
		
	}

	private static void createTruthMatch(String filename) throws NumberFormatException, IOException {
		HashSet<IdDuplicates> outputformat=new HashSet<IdDuplicates>();
		
		String inputFile = filename;
		CSVReader cvsReader = null;
		cvsReader = new CSVReader(new FileReader(new File(inputFile)));
		String[] currLine = null;
		while ((currLine = cvsReader.readNext()) != null) {				
			String[] parts = currLine;
			IdDuplicates pair = new IdDuplicates(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])-CENSUS_FIRST_FILE_SIZE);
			outputformat.add(pair);
		}
		cvsReader.close();
		SerializationUtilities.storeSerializedObject(outputformat, filename + "_goundTruth");
		
	}

	private static void createProfiles(String filename) throws IOException {
		ArrayList<EntityProfile> entityProfiles=new ArrayList<EntityProfile>();
		String inputFile = filename;
		//String outputFile = args[1];
		CSVReader cvsReader = null;
		cvsReader = new CSVReader(new FileReader(new File(inputFile)));
		String[] currLine = null;
		boolean first = true;
		String[] attNames= null;
		while ((currLine = cvsReader.readNext()) != null) {				
			if(first){
				attNames = currLine;	
				for (String att:attNames){
					System.out.println(att);
				}
				first = false;
				continue;
			}
			String[] parts = currLine;
			EntityProfile currentProfile=new EntityProfile("test");
			for(int i=0 ; i < parts.length ; i++){		
				currentProfile.addAttribute(attNames[i], parts[i]);
			}
			entityProfiles.add(currentProfile);
		}
		cvsReader.close();
		SerializationUtilities.storeSerializedObject(entityProfiles, filename + "_profiles");
		
	}

}
