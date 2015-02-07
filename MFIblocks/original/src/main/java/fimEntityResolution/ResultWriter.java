package fimEntityResolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import candidateMatches.CandidatePairs;
import candidateMatches.RecordMatches;
import au.com.bytecode.opencsv.CSVWriter;
/**
 * This class will write the results of MFIBlocks to an file
 * @author XPS_Sapir
 *
 */
public class ResultWriter {

	public File createOutputFile() {
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
		Date date = new Date();
		String dateFormated = dateFormat.format(date);
		String workingDir = System.getProperty("user.dir");
		File file = new File(workingDir + "/MFIBlocksResult_" + dateFormated + ".csv");
		return file;
	}
	
	public void writeBlocksIDs(File file, CandidatePairs cps) throws IOException {
		
		FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		//CSVWriter csvWriter=new CSVWriter(fileWriter);
		ConcurrentHashMap<Integer,RecordMatches> allMatches = cps.getAllMatches();
		if (allMatches == null) {
			//csvWriter.close();
			bufferedWriter.close();
			return;
		}
		Iterator<Entry<Integer, RecordMatches>> iterator = allMatches.entrySet().iterator();
		while (iterator.hasNext()) {
			StringBuilder stringBuilder = new StringBuilder();
			Map.Entry<Integer, RecordMatches> entry = (Map.Entry<Integer, RecordMatches>) iterator.next();
			if (entry.getValue() == null || entry.getValue().size() == 0) {
				continue;
			}
			//adds ID of current record
			stringBuilder.append(entry.getKey());
			Set<Integer> keySet = entry.getValue().getCandidateSet().keySet();
			stringBuilder.append(" - " + keySet.toString());
			bufferedWriter.write(stringBuilder.toString());
			bufferedWriter.newLine();
		}
		bufferedWriter.close();
	}
	
	public void writeBlocksStatistics(File file, CandidatePairs cps, MfiContext context) throws IOException {
		FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
		//BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		CSVWriter csvWriter=new CSVWriter(fileWriter);
		ConcurrentHashMap<Integer,RecordMatches> allMatches = cps.getAllMatches();
		if (allMatches == null) {
			csvWriter.close();
			return;
		}
		RecordSet.loadOriginalRecordsFromCSV(context.getOriginalRecordsPath());
		
		Iterator<Entry<Integer, RecordMatches>> iterator = allMatches.entrySet().iterator();
		String[] csvLine = new String[RecordSet.SCHEMA_SIZE];
		StringBuilder stringBuilder=new StringBuilder();
		//stringBuilder.append(RecordSet.columnNames);
		csvWriter.writeNext(RecordSet.columnNames);
		while (iterator.hasNext()) {

			//stringBuilder = new StringBuilder();
			Map.Entry<Integer, RecordMatches> entry = (Map.Entry<Integer, RecordMatches>) iterator.next();
			
			if (entry.getValue() == null || entry.getValue().size() == 0) {
				continue;
			}
			List<Integer> columnsForBlock = new ArrayList<Integer>();
			columnsForBlock.addAll(cps.getColumnsSupport(entry.getKey()));
			if (RecordSet.SCHEMA_SIZE < 5){
				//adds ID of current record
				stringBuilder= new StringBuilder();
				stringBuilder.append("Block #");
				stringBuilder.append(entry.getKey());
				//csvLine[0]=stringBuilder.toString();
				//size of the block
				//stringBuilder=new StringBuilder();
				stringBuilder.append(" Size=");
				stringBuilder.append(entry.getValue().size()+1); //add itself
				//csvLine[1]=stringBuilder.toString();
				//used attributes
				//stringBuilder= new StringBuilder();
				stringBuilder.append(" Used Attributes: ");
				//
				//stringBuilder.append(columnsForBlock.toString());
				//csvLine[2]=stringBuilder.toString();
				//stringBuilder= new StringBuilder();
				//fill the attributes:
				Collections.sort(columnsForBlock);
				for (int id : columnsForBlock){
					stringBuilder.append(RecordSet.columnNames[id]);
					stringBuilder.append(" | ");
				}
				stringBuilder.delete(stringBuilder.lastIndexOf(" | "),stringBuilder.length());
				csvLine[0]=stringBuilder.toString();
				csvWriter.writeNext(csvLine);
			}
			else {
				//adds ID of current record
				stringBuilder= new StringBuilder();
				stringBuilder.append("Block #");
				stringBuilder.append(entry.getKey());
				csvLine[0]=stringBuilder.toString();
				//size of the block
				stringBuilder=new StringBuilder();
				stringBuilder.append("Size=");
				stringBuilder.append(entry.getValue().size()+1); //add itself
				csvLine[1]=stringBuilder.toString();
				//used attributes
				stringBuilder= new StringBuilder();
				stringBuilder.append("Used Attributes: ");
				//
				//stringBuilder.append(columnsForBlock.toString());
				csvLine[2]=stringBuilder.toString();
				stringBuilder= new StringBuilder();
				//fill the attributes:
				Collections.sort(columnsForBlock);
				for (int id : columnsForBlock){
					stringBuilder.append(RecordSet.columnNames[id]);
					stringBuilder.append(" | ");
				}
				stringBuilder.delete(stringBuilder.lastIndexOf(" | "),stringBuilder.length());
				csvLine[3]=stringBuilder.toString();
				//fill the rest of the line with spaces
				for (int i=4;i<RecordSet.SCHEMA_SIZE;i++){
					csvLine[i]=" ";
				}
				csvWriter.writeNext(csvLine);
			}
			
			csvLine=new String[RecordSet.SCHEMA_SIZE];
			for (Integer id : entry.getValue().getCandidateSet().keySet()){
				csvWriter.writeNext(RecordSet.originalRecords[id-1]);
//				stringBuilder = new StringBuilder();
//				stringBuilder.append(RecordSet.originalRecords[id-1]);
//				bufferedWriter.write(stringBuilder.toString());
//				bufferedWriter.newLine();
			}
			//ad the record itself
//			stringBuilder = new StringBuilder();
//			stringBuilder.append(RecordSet.originalRecords[entry.getKey()-1]);
//			bufferedWriter.write(stringBuilder.toString());
//			bufferedWriter.newLine();
			csvWriter.writeNext(RecordSet.originalRecords[entry.getKey()-1]);
		}
		//bufferedWriter.close();
		csvWriter.close();
	}
}
