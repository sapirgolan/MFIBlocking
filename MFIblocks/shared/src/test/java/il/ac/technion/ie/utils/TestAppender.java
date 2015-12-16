package il.ac.technion.ie.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by I062070 on 16/12/2015.
 */
public class TestAppender extends AppenderSkeleton {
    private final List<LoggingEvent> log = new ArrayList<>();
    private final Multimap<Level, LoggingEvent> logsMap = ArrayListMultimap.create();

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    protected void append(final LoggingEvent loggingEvent) {
        logsMap.put(loggingEvent.getLevel(), loggingEvent);
        log.add(loggingEvent);
    }

    @Override
    public void close() {
    }

    public List<LoggingEvent> getLog() {
        return new ArrayList<>(log);
    }

    public List<LoggingEvent> getLogByLevel(Level level) {
        return new ArrayList<>(logsMap.get(level));
    }
}
