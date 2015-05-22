package fimEntityResolution.statistics;

/**
 * Created by I062070 on 21/05/2015.
 */
public class Timer {

    private long startTime;
    private long actionStartTime;

    public Timer() {
        startTime = System.currentTimeMillis();
        actionStartTime = 0;
    }

    public void startActionTimeMeassurment() {
        actionStartTime = System.currentTimeMillis();
    }

    public long getActionTimeDuration() {
        return System.currentTimeMillis() - actionStartTime;
    }

    public long getStartTime() {
        return startTime;
    }
}
