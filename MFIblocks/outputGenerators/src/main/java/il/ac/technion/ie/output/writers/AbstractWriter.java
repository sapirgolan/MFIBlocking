package il.ac.technion.ie.output.writers;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by XPS_Sapir on 11/07/2015.
 */
public abstract class AbstractWriter {

    protected File createUniqueOutputFile(String filePath, String fileExtension) {
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

    protected void addCsvSeperator(StringBuilder builder) {
        builder.append(",");
    }

    public abstract File generateFile(String fileName, String fileExtention);

    public abstract void writeResults(File file, Object... other) throws IOException;
}
