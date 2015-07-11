package il.ac.technion.ie.output.writers;


import au.com.bytecode.opencsv.CSVWriter;
import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.*;

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

    public File createRocOutputFile() {
        return this.createUniqueOutputFile("/Roc_", ".txt");
    }
    public File createBlocksOutputFile(String datasetName) {
        return createUniqueOutputFile("/Blocks_"+datasetName, ".txt");
    }

    public File createAmbiguousRepresentativesOutputFile(String datasetName) {
        return createUniqueOutputFile("/AmbiguousRepresentatives_" + datasetName, ".csv");
    }

    public File createCyphrtOutputFile() {
        return createUniqueOutputFile("/CypherCommands_", ".txt");
    }

    private File createUniqueOutputFile(String filePath, String fileExtension) {
        Random randomGenerator = new Random();
        String randomId = String.valueOf(randomGenerator.nextInt(1000));
        return generateOutputFile(filePath + randomId, fileExtension);
    }

    private File generateOutputFile(String fileName, String fileFormat) {
        DateFormat dateFormat = new SimpleDateFormat("_dd_MM_yyyy_HH_mm_ss");
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
            bufferedWriter.write(block.toCsv());
            bufferedWriter.newLine();
        }
        bufferedWriter.write("*****Finish Printing***");
        bufferedWriter.newLine();
        bufferedWriter.close();
    }

    public void writeRocDots(File file, Map<Double, Double> coordinatesForPlot) throws IOException {
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (Entry<Double, Double> entry : coordinatesForPlot.entrySet()) {
            bufferedWriter.write(String.format("%s|%s", entry.getKey(), entry.getValue()));
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    public void writeAmbiguousRepresentatives(File outputFile, Map<Integer, List<BlockDescriptor>> blocksAmbiguousRepresentatives) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (Entry<Integer, List<BlockDescriptor>> entry : blocksAmbiguousRepresentatives.entrySet()) {
            String csvEntry = ambiguousRepresentativeBlocksToCsv(entry);
            bufferedWriter.write(csvEntry);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    /**
     * Gets a record ID that represents several blocks and a List of those blocks
     *
     * @param entry
     * @return
     */
    private String ambiguousRepresentativeBlocksToCsv(Entry<Integer, List<BlockDescriptor>> entry) {
        StringBuilder builder = new StringBuilder();
        Integer recordRepresentSeveralBlocks = entry.getKey();
        List<BlockDescriptor> representedBlocks = entry.getValue();

        for (BlockDescriptor blockDescriptor : representedBlocks) {
            for (Integer recordId : blockDescriptor.getMembers()) {
                //adding the root cause for this entry
                builder.append(recordRepresentSeveralBlocks);
                addCsvSeperator(builder);

                //adding current record in Block
                builder.append(recordId);
                addCsvSeperator(builder);

                //adding the content of the block
                List<String> contentOfRecord = blockDescriptor.getContentOfRecord(recordId);
                for (String text : contentOfRecord) {
                    builder.append(text);
                    builder.append(" ");
                }
                builder.deleteCharAt(builder.length() - 1);
                addCsvSeperator(builder);
                builder.append("\r\n");
            }
            builder.append("\r\n");
        }
        return builder.toString();
    }

    private void addCsvSeperator(StringBuilder builder) {
        builder.append(",");
    }
}
