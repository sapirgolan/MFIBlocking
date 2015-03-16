package il.ac.technion.ie.model;

import il.ac.technion.ie.context.MfiContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RecordSet {
	public static Map<Integer, Record> values;
	public static int size;
	public static int minRecordLength = Integer.MAX_VALUE;
	public static int DB_SIZE;
	
	public static void setRecords(Map<Integer, Record> records){
        values = records;
        size = values.size();
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
