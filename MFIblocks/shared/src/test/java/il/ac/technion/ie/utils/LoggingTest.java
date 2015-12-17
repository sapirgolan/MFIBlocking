package il.ac.technion.ie.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LoggingTest {

    @Rule
    public Logging logging = new Logging();

    static final Logger logger = Logger.getLogger(LoggingTest.class);


    @Test
    public void testLoggerWithThrowable() throws Exception {
        //execute
        try {
            throw new InvalidParameterException("Testing invalid exception");
        } catch (InvalidParameterException e) {
            logger.error("'error', Caught exception", e);
            logger.info("'Info', Caught exception", e);
        }

        //assert
        assertThat(logging.getAllLogs(), not(empty()));
        assertThat(logging.getAllLogsAbove(Level.DEBUG), hasSize(2));
        assertThat(logging.getAllLogsAbove(Level.INFO), hasSize((2)));
        assertThat(logging.getAllLogsAbove(Level.WARN), hasSize((1)));
        assertThat(logging.getAllLogsAbove(Level.ERROR), hasSize((1)));
        assertThat(logging.getAllLogsAbove(Level.TRACE), hasSize((2)));
        assertThat(logging.getAllLogsAbove(Level.FATAL), is(empty()));
    }

    @Test
    public void testLoggerWithoutThrowable() throws Exception {
        //execute
        try {
            throw new InvalidParameterException("Testing invalid exception");
        } catch (InvalidParameterException e) {
            logger.error("'error', Caught exception");
            logger.info("'Info', Caught exception");
        }

        //assert
        assertThat(logging.getAllLogs(), not(empty()));
        assertThat(logging.getAllLogsAbove(Level.DEBUG), hasSize(2));
        assertThat(logging.getAllLogsAbove(Level.INFO), hasSize((2)));
        assertThat(logging.getAllLogsAbove(Level.WARN), hasSize((1)));
        assertThat(logging.getAllLogsAbove(Level.ERROR), hasSize((1)));
        assertThat(logging.getAllLogsAbove(Level.TRACE), hasSize((2)));
        assertThat(logging.getAllLogsAbove(Level.FATAL), is(empty()));
    }
}