package il.ac.technion.ie.experiments.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by I062070 on 10/09/2015.
 */
public class ExpFileUtils {
    static final Logger logger = Logger.getLogger(ExpFileUtils.class);

    public static File createFile(String pathToFile) {
        File outputFile = new File(pathToFile);
        if (outputFile.exists()) {
            logger.info("Output file exists. Deleting " + pathToFile);
            if (!outputFile.delete()) {
                logger.warn("Failed to delete output file");
            }
        }
        try {
            logger.info("Creating the output file");
            outputFile.createNewFile();
        } catch (IOException e) {
            logger.error("Failed to create output file", e);
        }
        return outputFile;
    }

    public static String getOutputFilePath(String fileName, String fileSuffix) {
        File runningDir = new File(System.getProperty("user.dir"));
        File parentFile = runningDir.getParentFile();
        return parentFile.getAbsolutePath() + File.separator + fileName + fileSuffix;
    }

    public static File createOutputFile(String filename) {
        File expResults = null;
        try {
            expResults = new File(filename);
            if (expResults.exists()) {
                FileUtils.forceDelete(expResults);
            }
            if (!expResults.createNewFile()) {
                expResults = null;
            }
        } catch (IOException e) {
            logger.error("Failed to create file for measurements of Experiment", e);
        }
        return expResults;
    }
}
