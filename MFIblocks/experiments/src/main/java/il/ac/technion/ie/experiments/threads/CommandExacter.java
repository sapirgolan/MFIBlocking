package il.ac.technion.ie.experiments.threads;

import il.ac.technion.ie.experiments.exception.OSNotSupportedException;
import il.ac.technion.ie.experiments.model.ConvexBPContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by I062070 on 26/09/2015.
 */
public class CommandExacter {

    static final Logger logger = Logger.getLogger(CommandExacter.class);
    public static final int MAX_NUMBER_OF_EXECUTIONS = 3;
    private ConvexBPContext context;

    public File execute(ConvexBPContext context) throws IOException, OSNotSupportedException, InterruptedException {
        File outputFile;
        int numberOfExecutions = 1;
        this.context = context;
        checkOS();
        Runtime runtime = Runtime.getRuntime();
        String command = createCommand();

        do {
            logger.info(String.format("Executing convexBP algorithm for #%d time", numberOfExecutions));
            logger.debug(String.format("Executing read UAI file by command: '%s'", command));

            long startTime = System.nanoTime();
            logger.info(String.format("Running convexBP with following command%s", command));
            Process process = runtime.exec(command, new String[]{}, new File(this.context.getDir()));
            long afterExecTime = System.currentTimeMillis();
            Thread thread = new Thread(new ProcessTimeoutThread(process, afterExecTime, this.context.getWaitInterval()));
            thread.start();

            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), StreamGobbler.ChanelType.ERROR);
            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), StreamGobbler.ChanelType.OUTPUT);

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // wait, if necessary, until the process object has terminated
            logger.debug("Waiting if needed for previous read job to finish");
            long waitTime = System.nanoTime();
            int exitVal = process.waitFor();
            long endTime = System.nanoTime();
            logger.info(String.format("Total execution & waitTime of convexBP: %d, %d",
                    TimeUnit.NANOSECONDS.toSeconds(endTime - startTime), TimeUnit.NANOSECONDS.toSeconds(endTime - waitTime)));
            logger.debug("ExitValue: " + exitVal);
            outputFile = new File(this.context.getPathToOutputFile());
            numberOfExecutions++;
        } while (keepJobRunning(outputFile, numberOfExecutions));
        return outputFile;
    }

    private boolean keepJobRunning(File outputFile, int numberOfExecutions) {
        if (outputFileNotPrepared(outputFile) && numberOfExecutions <= MAX_NUMBER_OF_EXECUTIONS) {
            if (numberOfExecutions == (MAX_NUMBER_OF_EXECUTIONS)) {
                logger.warn("Failed to run convexBP for" + MAX_NUMBER_OF_EXECUTIONS + "times");
                File badUaiFile = this.copyBadUaiFile();
                if (badUaiFile.canRead()) {
                    logger.info("Uai file that wasn't executed was copied to " + badUaiFile.getAbsolutePath());
                }
            }
            return true;
        }
        return false;
    }

    private boolean outputFileNotPrepared(File outputFile) {
        return (outputFile == null || !outputFile.exists());
    }

    private String createCommand() {
        return context.getCommand();
    }

    private void checkOS() throws OSNotSupportedException {
        String osName = System.getProperty("os.name");
        if (StringUtils.indexOfIgnoreCase(osName, "windows") == -1) {
            throw new OSNotSupportedException(String.format("Only 'Windows NT' is supported, this OS is: '%s'", osName));
        }
    }

    private File copyBadUaiFile()  {
        File uaiFile = new File(context.getDir() + File.separator + context.getUaiFileName());
        String absolutePath = uaiFile.getAbsolutePath();
        File destFile = new File(buildBadUaiFileName(absolutePath));
        try {
            FileUtils.copyFile(uaiFile, destFile);
        } catch (IOException e) {
            logger.error("Failed to copy UAI file that was not executed.", e);
        }

        return destFile;
    }

    private String buildBadUaiFileName(String absolutePath) {
        StringBuilder builder = new StringBuilder();
        builder.append(System.getProperty("java.io.tmpdir"));
        builder.append(File.separator);
        builder.append(FilenameUtils.getBaseName(absolutePath));
        builder.append('_').append(context.getThreshold());
        builder.append('.').append(FilenameUtils.getExtension(absolutePath));

        return builder.toString();
    }
}
