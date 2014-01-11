package preprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CombineCSVFiles {

	private static final String CLUSTER_ATT_NAME = "class";		
	private static final String SOURCE_ATT_NAME = "source";
	

	public static void main(String[] args){
		String mapFile = args[2];
		String firstFile = args[0];
		String secondFile = args[1];
		String outFile = args[3];
		try {
			Map<String,String> IdToClass = MapIdToClass(mapFile);
			CSVReader cvsReader1stFile = new CSVReader(new FileReader(
					new File(firstFile)));
			CSVReader cvsReader2ndFile = new CSVReader(new FileReader(
					new File(secondFile)));
			CSVWriter writer = new CSVWriter(new FileWriter(outFile));
			String[] attnames = cvsReader1stFile.readNext();
			attnames[0] = CLUSTER_ATT_NAME;
			String[] newLine = Arrays.copyOf(attnames, attnames.length + 1);
			newLine[attnames.length] = SOURCE_ATT_NAME;
			writer.writeNext(newLine);
			writeCSVFile(IdToClass,cvsReader1stFile,writer,1);
			writeCSVFile(IdToClass,cvsReader2ndFile,writer,2);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private static void writeCSVFile(Map<String,String> IdToClass, CSVReader toRead, CSVWriter toWrite, int src){
		String[] currLine = null;
		boolean first = true;		
		try {
			while ((currLine = toRead.readNext()) != null) {		
				if(first){ // ignore first line - attribute names						
					first = false;
					continue;
				}
				String classVal = IdToClass.get(currLine[0]);
				if(classVal != null){ // part of a cluster
					currLine[0] = classVal;
				}		
				//write source
				String[] newLine = Arrays.copyOf(currLine, currLine.length + 1);
				newLine[currLine.length] = Integer.toString(src);
				toWrite.writeNext(newLine);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static Map<String,String> MapIdToClass(String mapFile){
		Map<String, String> retVal = new HashMap<String, String>();
		try {
			CSVReader cvsReader = new CSVReader(new FileReader(
					new File(mapFile)));
			String[] currLine = null;
			boolean first = true;
			int currCluster = 1;
			while ((currLine = cvsReader.readNext()) != null) {				
				if(first){ // ignore first line - attribute names						
					first = false;
					continue;
				}
				if(retVal.containsKey(currLine[0]) && retVal.containsKey(currLine[1])){
					System.out.println("pair appeares more than once in the file");
					continue;
				}
				if(retVal.containsKey(currLine[0])){
					retVal.put(currLine[1],retVal.get(currLine[0]));
					continue;
				}
				if(retVal.containsKey(currLine[1])){
					retVal.put(currLine[0],retVal.get(currLine[1]));
					continue;
				}
				//here, neither of the members are in the map
				String currKey = Integer.toString(currCluster);
				retVal.put(currLine[0], currKey);
				retVal.put(currLine[1], currKey);
				currCluster++;
			}			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Number of clusters " + retVal.values().size());
		System.out.println("Number of cluster members " + retVal.keySet().size());
		return retVal;
	}
}
