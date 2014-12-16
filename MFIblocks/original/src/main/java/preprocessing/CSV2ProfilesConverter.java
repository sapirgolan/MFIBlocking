package preprocessing;

import il.ac.technion.ie.utils.SerializationUtilities;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import DataStructures.EntityProfile;
import au.com.bytecode.opencsv.CSVReader;
/***
 * Converts input CSV file into "Profiles" format (George Papadakis) = serialized  ArrayList<EntityProfile>.
 * @author Jonathan Svirsky
 * 20141213
 *
 */
public class CSV2ProfilesConverter {

	/**
	 * @param:
	 * 1. input file in CSV format
	 * 2. output file/path 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ArrayList<EntityProfile> entityProfiles=new ArrayList<EntityProfile>();
		String inputFile = args[0];
		String outputFile = args[1];
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
		SerializationUtilities.storeSerializedObject(entityProfiles, outputFile);
	}

}
