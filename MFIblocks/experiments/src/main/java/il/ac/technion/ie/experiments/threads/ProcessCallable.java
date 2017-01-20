package il.ac.technion.ie.experiments.threads;

import il.ac.technion.ie.experiments.threads.streams.MyLogOutputStream;
import org.apache.commons.exec.*;

import java.util.concurrent.Callable;

/**
 * Created by I062070 on 20/01/2017.
 */
public class ProcessCallable implements Callable<Long> {

    public static final Long WATCHDOG_EXIST_VALUE = -999L;

    private long watchdogTimeout;
    private IProcessOutputHandler handler;
    private CommandLine commandline;

    public ProcessCallable(long watchdogTimeout, IProcessOutputHandler handler, CommandLine commandline) {
        this.watchdogTimeout = watchdogTimeout;
        this.handler = handler;
        this.commandline = commandline;
    }

    @Override
    public Long call() throws Exception {
        ExecuteWatchdog watchDog = new ExecuteWatchdog(watchdogTimeout);
        Executor executor = buildProcessExecuter(watchDog);
        Long exitValue;
        try {
            exitValue =  new Long(executor.execute(commandline));

        } catch (ExecuteException e) {
            exitValue =  new Long(e.getExitValue());
        }
        boolean wasProcessKilled = watchDog.killedProcess();
        if(wasProcessKilled){
            exitValue = WATCHDOG_EXIST_VALUE;
        }

        return exitValue;
    }

    private Executor buildProcessExecuter(ExecuteWatchdog watchDog) {
        Executor executor = new DefaultExecutor();
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        executor.setWatchdog(watchDog);
        executor.setStreamHandler(new PumpStreamHandler(new MyLogOutputStream(handler, true),new MyLogOutputStream(handler, false)));
        return executor;
    }
}
