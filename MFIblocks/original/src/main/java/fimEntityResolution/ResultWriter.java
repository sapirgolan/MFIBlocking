package fimEntityResolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import candidateMatches.CandidatePairs;
import candidateMatches.RecordMatches;

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
		File file = new File(workingDir + "/MFIBlocksResult_" + dateFormated + ".txt");
		return file;
	}
	
	public void writeBlocksIDs(File file, CandidatePairs cps) throws IOException {
		FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		ConcurrentHashMap<Integer,RecordMatches> allMatches = cps.getAllMatches();
		if (allMatches == null) {
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
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		ConcurrentHashMap<Integer,RecordMatches> allMatches = cps.getAllMatches();
		if (allMatches == null) {
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
			stringBuilder.append(" ");
			stringBuilder.append("size=");
			stringBuilder.append(entry.getValue().size());
			bufferedWriter.write(stringBuilder.toString());
			bufferedWriter.newLine();
			RecordSet.loadOriginalRecordsFromCSV(context.getOriginalRecordsPath());
			for (Integer id : entry.getValue().getCandidateSet().keySet()){
				stringBuilder = new StringBuilder();
				stringBuilder.append(RecordSet.originalRecords[id-1]);
				bufferedWriter.write(stringBuilder.toString());
				bufferedWriter.newLine();
			}
			//Set<Integer> keySet = entry.getValue().getCandidateSet().keySet();
			//stringBuilder.append(" - " + keySet.toString());
			//bufferedWriter.write(stringBuilder.toString());
			//bufferedWriter.newLine();
		}
		bufferedWriter.close();
	}
}
