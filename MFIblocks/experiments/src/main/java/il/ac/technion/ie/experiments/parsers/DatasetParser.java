package il.ac.technion.ie.experiments.parsers;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import il.ac.technion.ie.experiments.Utils.ExpFileUtils;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by I062070 on 21/08/2015.
 */
public class DatasetParser {

    static final Logger logger = Logger.getLogger(DatasetParser.class);

    private Reader getReader(String pathToFile) {
        try {
            FileInputStream fileInputStream = new FileInputStream(pathToFile);
            return new InputStreamReader(fileInputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to retrieve stream from input at: " + pathToFile, e);
        } catch (FileNotFoundException e) {
            logger.error("File doesn't exists at: " + pathToFile, e);
        }
        return null;
    }

    public CsvParser getParserForFile(String pathToFile) {
        // The settings object provides many configuration options
        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        CsvParser parser = new CsvParser(parserSettings);
        // the 'parse' method will parse the file and delegate each parsed row to the RowProcessor you defined
        parser.beginParsing(getReader(pathToFile));

        return parser;
    }

    public CsvWriter preparOutputFile(String pathToFile) {
        CsvWriter writer = null;

        File file = ExpFileUtils.createFile(pathToFile);
        if (!file.exists() || !file.canWrite()) {
            logger.warn("Can't write output to file. Either it doesn't exists or nothing can be written");
            return null;
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            Writer outputWriter = new OutputStreamWriter(fileOutputStream);

            writer = new CsvWriter(outputWriter, new CsvWriterSettings());
        } catch (FileNotFoundException e) {
            logger.error("Output File doesn't exists", e);
        }
        return writer;
    }
}
