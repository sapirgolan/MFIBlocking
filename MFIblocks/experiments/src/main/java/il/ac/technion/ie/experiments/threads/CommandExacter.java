package il.ac.technion.ie.experiments.threads;

import il.ac.technion.ie.experiments.Utils.ExpFileUtils;
import il.ac.technion.ie.experiments.exception.OSNotSupportedException;
import il.ac.technion.ie.experiments.model.ConvexBPContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by I062070 on 26/09/2015.
 */
public class CommandExacter {

    static final Logger logger = Logger.getLogger(CommandExacter.class);

    public void execute(ConvexBPContext context) throws IOException, OSNotSupportedException, InterruptedException {
        FileOutputStream fileOutputStream = new FileOutputStream(createFileForOutput());
        checkOS();

        Runtime runtime = Runtime.getRuntime();

        String command = createCommand(context);
        logger.debug(String.format("Executing read UAI file by command: '%s'", command));

        long startTime = System.nanoTime();
        Process process = runtime.exec(command, new String[]{}, new File(context.getDir()));
        long afterExecTime = System.currentTimeMillis();
        Thread thread = new Thread(new ProcessTimeoutThread(process, afterExecTime, context.getWaitInterval()));
        thread.start();

        // any error message?
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
        // any output?
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT", fileOutputStream);

        // kick them off
        errorGobbler.start();
        outputGobbler.start();

        // any error???
        // wait, if necessary, until the process object has terminated
        logger.debug("Waiting if needed for previous read job to finish");
        long waitTime = System.nanoTime();
        int exitVal = process.waitFor();
        long endTime = System.nanoTime();
        logger.info("Total execution time: " + TimeUnit.NANOSECONDS.toSeconds(endTime - startTime));
        logger.info("Total wait time time: " + TimeUnit.NANOSECONDS.toSeconds(endTime - waitTime));
        logger.debug("ExitValue: " + exitVal);

        fileOutputStream.flush();
        fileOutputStream.close();

    }

    private String createCommand(ConvexBPContext context) {
        return context.getCommand();
    }

    private File createFileForOutput() throws IOException {
        String outputFilePath = ExpFileUtils.getOutputFilePath("executeCBP", ".txt");
        File file = new File(outputFilePath);
        if (file.exists()) {
            logger.info(String.format("output file of convexBP exists '%s', deleting it", file.getAbsolutePath()));
            FileUtils.forceDelete(file);
        }
        boolean wasFileCreated = file.createNewFile();
        logger.info("Was output file of convexBP created: " + wasFileCreated);
        return file;
    }

    private void checkOS() throws OSNotSupportedException {
        String osName = System.getProperty("os.name");
        if (StringUtils.indexOfIgnoreCase(osName, "windows") == -1) {
            throw new OSNotSupportedException(String.format("Only 'Windows NT' is supported, this OS is: '%s'", osName));
        }
    }
}
