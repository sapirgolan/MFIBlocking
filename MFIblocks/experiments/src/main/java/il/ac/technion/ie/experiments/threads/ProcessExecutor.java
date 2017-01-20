package il.ac.technion.ie.experiments.threads;

import org.apache.commons.exec.CommandLine;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by I062070 on 20/01/2017.
 */
public class ProcessExecutor {
    public static Future<Long> runProcess(final CommandLine commandline, final long watchdogTimeout, final IProcessOutputHandler outputHandler) throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> result = executor.submit(new ProcessCallable(watchdogTimeout, outputHandler, commandline));
        executor.shutdown();
        return result;
    }
}
