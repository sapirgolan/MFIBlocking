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


    /**
     * This method generate a parser that starts an iterator-style parsing cycle that
     * does not rely in a {@link com.univocity.parsers.common.processor.RowProcessor}.
     * <p/>
     * To initialize a {@code CsvParser} with a default {@link com.univocity.parsers.common.processor.RowProcessor}
     * use {@link il.ac.technion.ie.experiments.parsers.DatasetParser#getParserForFile(String, com.univocity.parsers.csv.CsvParserSettings)} set the
     * parser in the {@link CsvParserSettings}
     *
     * @param pathToFile Full path in File System to a CSV file that contains records
     * @return an instance of {@link com.univocity.parsers.csv.CsvParser}
     */
    public CsvParser getParserForFile(String pathToFile) {
        // The settings object provides many configuration options
        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        CsvParser parser = createParser(parserSettings);
        // the 'parse' method will parse the file and delegate each parsed row to the RowProcessor you defined
        parser.beginParsing(getReader(pathToFile));

        return parser;
    }

    /**
     * This method generate a parser that parses the entirety of a given input and delegates each parsed row to
     * an instance of {@link com.univocity.parsers.common.processor.RowProcessor} that is defined in
     * <code>parserSettings</code>
     *
     * @param pathToFile     Full path in File System to a CSV file that contains records
     * @param parserSettings a custom configuration for the {@code CsvParser} that is being returned
     * @return an instance of {@link com.univocity.parsers.csv.CsvParser}
     */
    public CsvParser getParserForFile(String pathToFile, CsvParserSettings parserSettings) {
        CsvParser parser = createParser(parserSettings);
        parser.parse(getReader(pathToFile));
        return parser;
    }

    private CsvParser createParser(CsvParserSettings parserSettings) {
        return new CsvParser(parserSettings);
    }

    public CsvWriter preparOutputFile(String pathToFile) {

        File file = ExpFileUtils.createFile(pathToFile);
        return this.preparOutputFile(file);
    }

    public CsvWriter preparOutputFile(File file) {
        CsvWriter writer = null;

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
