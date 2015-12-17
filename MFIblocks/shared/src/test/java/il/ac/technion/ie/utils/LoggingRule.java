package il.ac.technion.ie.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.LevelRangeFilter;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.List;

/**
 * Created by I062070 on 17/12/2015.
 */
public class LoggingRule implements TestRule {
    private static final String MESSAGE_WITHOUT_THROWABLE = "%s: %s, %s.";
    private static final String MESSAGE_WITH_THROWABLE = MESSAGE_WITHOUT_THROWABLE + " %s";
    protected TestAppender appender;
    protected final Logger logger = Logger.getRootLogger();

    @Override
    public Statement apply(Statement base, Description description) {
        return statement(base);
    }

    private Statement statement(final Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } finally {
                    after();
                }
            }
        };
    }

    protected void before() throws Throwable {
        appender = new TestAppender();
        logger.addAppender(appender);
    }

    protected void after() {
        for (LoggingEvent event : appender.getLog()) {
            if (event.getThrowableInformation() != null) {
                System.out.printf(MESSAGE_WITH_THROWABLE,
                        event.getLoggerName(), event.getLevel(), event.getMessage(), event.getThrowableInformation().getThrowable().getMessage());
                System.out.println();
            } else {
                System.out.printf(MESSAGE_WITHOUT_THROWABLE,
                        event.getLoggerName(), event.getLevel(), event.getMessage());
                System.out.println();
            }
        }
        logger.removeAppender(appender);
    }

    public List<LoggingEvent> getAllLogs() {
        return appender.getLog();
    }

    public List<LoggingEvent> getAllLogsAbove(Level level) {
        Filter rangeFilter = createFilter(level);
        return appender.getLogsByLevel(rangeFilter);
    }

    public LevelRangeFilter createFilter(Level level) {
        LevelRangeFilter rangeFilter = new LevelRangeFilter();
        rangeFilter.setLevelMin(level);
        rangeFilter.setLevelMax(Level.FATAL);
        rangeFilter.setAcceptOnMatch(true);
        return rangeFilter;
    }
}
