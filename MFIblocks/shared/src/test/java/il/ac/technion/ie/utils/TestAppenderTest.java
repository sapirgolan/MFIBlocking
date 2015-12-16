package il.ac.technion.ie.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestAppenderTest extends AbstractAppenderTest {

    @Test
    public void test() {
        Logger.getLogger(TestAppender.class).info("Test");

        final List<LoggingEvent> log = appender.getLog();
        final LoggingEvent firstLogEntry = log.get(0);
        assertThat(firstLogEntry.getLevel(), is(Level.INFO));
        assertThat((String) firstLogEntry.getMessage(), is("Test"));
        assertThat(firstLogEntry.getLoggerName(), is(TestAppender.class.getCanonicalName()));
    }

}