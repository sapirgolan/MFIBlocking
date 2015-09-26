package il.ac.technion.ie.experiments.threads;

import java.util.concurrent.TimeUnit;

/**
 * Created by I062070 on 26/09/2015.
 */
public class ProcessTimeoutThread implements Runnable {
    private Process process;
    private long timeoutValueInSeconds;
    private long afterExecTime;

    public ProcessTimeoutThread(Process aProcess, long afterExecTime, long timeoutValueInSeconds) {
        this.process = aProcess;
        this.timeoutValueInSeconds = timeoutValueInSeconds;
        this.afterExecTime = afterExecTime;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            long timeSinceExecution = System.currentTimeMillis() - afterExecTime;
            Thread.sleep(TimeUnit.SECONDS.toMillis(timeoutValueInSeconds) - timeSinceExecution);
            process.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
