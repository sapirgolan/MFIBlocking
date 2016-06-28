package il.ac.technion.ie.experiments.apiAccess;

import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by I062070 on 22/02/2016.
 */
public class TimeLogger {

    static final Logger logger = Logger.getLogger(TimeLogger.class);

    public static void logDurationInSeconds(long startTime, String message) {
        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toSeconds(endTime - startTime);
        logger.info(String.format("%s: %d seconds", message, duration));
    }
}
