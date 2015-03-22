package fimEntityResolution;


import au.com.bytecode.opencsv.CSVWriter;
import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.CandidatePairs;
import il.ac.technion.ie.model.RecordMatches;
import il.ac.technion.ie.model.RecordSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class will write the results of MFIBlocks to an file
 * @author XPS_Sapir
 *
 */
public class ResultWriter {

	public File createNeighborsOutputFile() {
        return generateOutputFile("/MFIBlocksResult_", ".csv");
	}

    public File createBlocksOutputFile() {
        Random randomGenerator = new Random();
        String randomId = String.valueOf(randomGenerator.nextInt(1000));
        return generateOutputFile("/Blocks_" + randomId, ".txt");
	}

    private File generateOutputFile(String fileName, String fileFormat) {
        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
        Date date = new Date();
        String dateFormatted = dateFormat.format(date);
        String workingDir = System.getProperty("user.dir");
        return new File(workingDir + fileName + dateFormatted + fileFormat);
    }

    public void writeEachRecordNeighbors(File file, CandidatePairs cps) throws IOException {
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
			Map.Entry<Integer, RecordMatches> entry = iterator.next();
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
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        ConcurrentHashMap<Integer, RecordMatches> allMatches = cps.getAllMatches();
        if (allMatches == null) {
            csvWriter.close();
            return;
        }
        RecordSet.loadOriginalRecordsFromCSV(context.getOriginalRecordsPath());

        Iterator<Entry<Integer, RecordMatches>> iterator = allMatches.entrySet().iterator();
        csvWriter.writeNext(RecordSet.columnNames);
        while (iterator.hasNext()) {

            Map.Entry<Integer, RecordMatches> entry = iterator.next();
            if (entry.getValue() == null || entry.getValue().size() == 0) {
                continue;
            }

            List<Integer> columnsForBlock = new ArrayList<>();
            columnsForBlock.addAll(cps.getColumnsSupport(entry.getKey()));
            if (RecordSet.SCHEMA_SIZE < 5) {
                writeInOneCell(csvWriter, entry, columnsForBlock);
            } else {
                writeInManyCells(csvWriter, entry, columnsForBlock);
            }

            for (Integer id : entry.getValue().getCandidateSet().keySet()) {
                csvWriter.writeNext(RecordSet.originalRecords[id - 1]);
            }
            //ad the record itself
            csvWriter.writeNext(RecordSet.originalRecords[entry.getKey() - 1]);
        }
        csvWriter.close();
    }

    private void writeInManyCells(CSVWriter csvWriter, Entry<Integer, RecordMatches> entry, List<Integer> columnsForBlock) {
        String[] csvLine = new String[RecordSet.SCHEMA_SIZE];
        StringBuilder stringBuilder = new StringBuilder();
        //adds ID of current record
        writeRootRecordID(stringBuilder, entry);
        csvLine[0] = stringBuilder.toString();

        //size of the block
        stringBuilder = new StringBuilder();
        writeNumberOfNeighbors(stringBuilder, entry);
        csvLine[1] = stringBuilder.toString();

        //used attributes
        stringBuilder = new StringBuilder();
        stringBuilder.append("Used Attributes: ");
        csvLine[2] = stringBuilder.toString();

        stringBuilder = new StringBuilder();
        //fill the attributes:
        writeNeighbors(stringBuilder, columnsForBlock);
        csvLine[3] = stringBuilder.toString();
        //fill the rest of the line with spaces
        for (int i = 4; i < RecordSet.SCHEMA_SIZE; i++) {
            csvLine[i] = " ";
        }
        csvWriter.writeNext(csvLine);
    }

    private void writeInOneCell(CSVWriter csvWriter, Entry<Integer, RecordMatches> entry, List<Integer> columnsForBlock) {
        StringBuilder stringBuilder = new StringBuilder();
        writeRootRecordID(stringBuilder, entry);
        writeNumberOfNeighbors(stringBuilder, entry);
        stringBuilder.append(" Used Attributes: ");
        writeNeighbors(stringBuilder, columnsForBlock);
        csvWriter.writeNext(stringBuilder.toString());
    }

    private void writeNeighbors(StringBuilder stringBuilder, List<Integer> columnsForBlock) {
        Collections.sort(columnsForBlock);
        for (int id : columnsForBlock) {
            stringBuilder.append(RecordSet.columnNames[id]);
            stringBuilder.append(" | ");
        }
        stringBuilder.delete(stringBuilder.lastIndexOf(" | "), stringBuilder.length());
    }

    private void writeNumberOfNeighbors(StringBuilder stringBuilder, Entry<Integer, RecordMatches> entry) {
        stringBuilder.append("Size=");
        stringBuilder.append(entry.getValue().size() + 1); //add itself
    }

    private void writeRootRecordID(StringBuilder stringBuilder, Entry<Integer, RecordMatches> entry) {
        stringBuilder.append("Block #");
        stringBuilder.append(entry.getKey());
    }

    public void writeBlocks(File file, List<Block> blocks) throws IOException {
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write("*****Printing blocks***");
        bufferedWriter.newLine();
        for (Block block : blocks) {
            bufferedWriter.write(block.toString());
            bufferedWriter.newLine();
        }
        bufferedWriter.write("*****Finish Printing***");
        bufferedWriter.newLine();
        bufferedWriter.close();
    }
}
