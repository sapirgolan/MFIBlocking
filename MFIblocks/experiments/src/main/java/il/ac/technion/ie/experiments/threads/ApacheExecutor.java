package il.ac.technion.ie.experiments.threads;

import com.google.common.base.Joiner;
import il.ac.technion.ie.experiments.exception.OSNotSupportedException;
import il.ac.technion.ie.experiments.model.ConvexBPContext;
import il.ac.technion.ie.experiments.utils.ExperimentUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by I062070 on 20/01/2017.
 */
public class ApacheExecutor implements IConvexBPExecutor {
    private final int WAIT_TIMEOUT_SECONDS = 760;

    static final Logger logger = Logger.getLogger(ApacheExecutor.class);

    @Override
    public File execute(ConvexBPContext context) throws OSNotSupportedException, IOException, ExecutionException, InterruptedException {
        ExperimentUtils.checkOS();

        CommandLine commandLine = buildCommandLine(context);
        long startTime = System.nanoTime();

        Future<Long> longFuture = ProcessExecutor.runProcess(commandLine, TimeUnit.SECONDS.toMillis(WAIT_TIMEOUT_SECONDS),
                new IProcessOutputHandler() {
                    @Override
                    public void onStandardOutput(String msg) {
                        logger.info(msg);
                    }

                    @Override
                    public void onStandardError(String msg) {
                        logger.error(msg);
                    }
                });

        //wait till execution has finished
        Long exitValue = longFuture.get();
        long endTime = System.nanoTime();

        logger.info(String.format("Total execution time of convexBP: %d", TimeUnit.NANOSECONDS.toSeconds(endTime - startTime)));
        logger.info(String.format("Execution of convexBP has finished with status: %d", exitValue));
        return new File(context.getPathToOutputFile());
    }

    private CommandLine buildCommandLine(ConvexBPContext context) {
        String commandWithArguments = context.getCommand();
        String pathToExecute = StringUtils.substringBetween(commandWithArguments, "\"");
        String commandArguments = StringUtils.substringAfterLast(commandWithArguments, "\"");

        CommandLine commandLine = new CommandLine(pathToExecute);
        commandLine.addArguments(commandArguments, false);
        String log = commandLine.getExecutable() + Joiner.on(' ').join(commandLine.getArguments());
        logger.debug("About to run: " + log);
        return commandLine;
    }
}
