package il.ac.technion.ie.experiments.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by I062070 on 10/09/2015.
 */
public class ExpFileUtils {
    private static final Logger logger = Logger.getLogger(ExpFileUtils.class);

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
        File expResults;
        try {
            expResults = new File(FileUtils.getUserDirectoryPath(), filename);
            if (expResults.exists()) {
                FileUtils.forceDelete(expResults);
            }
            if (!expResults.createNewFile()) {
                expResults = null;
            }
        } catch (IOException e) {
            logger.error("Failed to create file for measurements of Experiment", e);
            return ExpFileUtils.createOutputFile(filename + System.nanoTime());
        }
        return expResults;
    }

    public static File createOrGetFolder(String folderName) {
        File file;
        file = new File(FileUtils.getUserDirectoryPath(), folderName);
        if (!file.exists()) { //if file doesn't exists - create it
            if (!file.mkdir()) { //if failed to create
                file = null;
            }
        }
        return file;
    }

    public static File createBlocksFile(File parent, final String prefix) throws IOException {
        Collection<File> files = FileUtils.listFiles(parent, new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return StringUtils.startsWith(file.getName(), prefix);
            }

            @Override
            public boolean accept(File dir, String name) {
                return StringUtils.startsWith(name, prefix);
            }
        }, null);
        File file;

        if (files.isEmpty()) {
            file = new File(parent, prefix + "_0");
        } else {
            ArrayList<Integer> indexes = new ArrayList<>();
            Pattern p = Pattern.compile("^" + prefix + "_(\\d)$");
            for (File existingFile : files) {
                Matcher matcher = p.matcher(existingFile.getName());
                if (matcher.find()) {
                    indexes.add(Integer.parseInt(matcher.group(1)));
                }
            }

            Integer max = Collections.max(indexes);
            file = new File(parent, prefix + "_" + (++max));
        }
        file.createNewFile();
        return file;
    }
}
