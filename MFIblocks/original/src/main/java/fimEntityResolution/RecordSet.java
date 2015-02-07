package fimEntityResolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import scala.collection.mutable.StringBuilder;

import au.com.bytecode.opencsv.CSVReader;

public class RecordSet {
	public static Map<Integer, Record> values;
	public static String[][] originalRecords;
	public static String[] columnNames;
	public static int size;
	public static int minRecordLength = Integer.MAX_VALUE;
	public static int DB_SIZE;
	public static int SCHEMA_SIZE;
	
	
	public static void setRecords(Map<Integer, Record> records){
		values=records;
		size=values.size();
		
	}
	
	public static void loadOriginalRecordsFromCSV(String filename) throws IOException{
		originalRecords=new String[DB_SIZE][SCHEMA_SIZE];
		CSVReader cvsReader = null; 
		
		
		cvsReader = new CSVReader(new FileReader(
					new File(filename)));
		
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
			SCHEMA_SIZE=attNames.length;
			String[] parts=currLine;
			originalRecords[recordId-1]=currLine;
			
			//StringBuilder sb=new StringBuilder();
			
			//for(int i=0 ; i < parts.length ; i++){	
			//	sb.append(parts[i]);
			//	sb.append(",");
			//}
			//originalRecords[recordId-1] = sb.toString();
			recordId++;
		}
		columnNames=attNames;
	}
	public static void readRecords(MfiContext context) {

		String numericRecordsFile = context.getRecordsFile();
		String origRecordsFile = context.getOriginalFile();
		String srcFile = context.getRecordsFile();
		Map<Integer, Record> outputRecords = new HashMap<Integer, Record>();
		try {
			BufferedReader recordsFileReader = new BufferedReader(
					new FileReader(new File(numericRecordsFile)));
			BufferedReader origRecordsFileReader = new BufferedReader(
					new FileReader(new File(origRecordsFile)));
			BufferedReader srcFileReader = null;
			if (srcFile != null && srcFile.length() > 0) {
				srcFileReader = new BufferedReader(new FileReader(new File(
						srcFile)));
			}
			System.out.println("readRecords: srcFile = " + srcFile);
			/*
			 * BufferedReader origRecordsFileReader = new BufferedReader (new
			 * InputStreamReader(new FileInputStream(origRecordsFile),
			 * "UTF16"));
			 */

			String numericLine = "";
			String recordLine = "";
			Pattern ws = Pattern.compile("[\\s]+");
			int recordIndex = 1;
			while (numericLine != null) {
				try {
					numericLine = recordsFileReader.readLine();
					if (numericLine == null) {
						break;
					}
					numericLine = numericLine.trim();
					recordLine = origRecordsFileReader.readLine().trim();
					String src = null;
					if (srcFileReader != null) {
						src = srcFileReader.readLine().trim();
					}
					Record r = new Record(recordIndex, recordLine);
					r.setSrc(src); // in the worst case this is null
					String[] words = ws.split(numericLine);
					if (numericLine.length() > 0) { // very special case when
													// there is an empty line
						for (String word : words) {
							int item = Integer.parseInt(word);
							r.addItem(item);
						}
					}
					minRecordLength = (r.getSize() < minRecordLength) ? r
							.getSize() : minRecordLength;
					outputRecords.put(r.getId(), r);
					recordIndex++;
				} catch (Exception e) {
					System.out.println("Exception while reading line "
							+ recordIndex + ":" + numericLine);
					System.out.println(e);
					break;
				}
			}
			recordsFileReader.close();
			System.out.println("Num of records read: " + outputRecords.size());
			DB_SIZE = outputRecords.size();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RecordSet.setRecords(outputRecords);
		System.out.println("RecordSet.size() " + RecordSet.size);
	}
	
	
}
