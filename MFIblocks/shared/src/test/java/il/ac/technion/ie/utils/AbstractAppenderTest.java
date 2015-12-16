package il.ac.technion.ie.utils;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

/**
 * Created by I062070 on 16/12/2015.
 */
public abstract class AbstractAppenderTest {
    protected TestAppender appender;
    protected final Logger logger = Logger.getRootLogger();

    @Before
    public void setUpAbstractAppender() throws Exception {
        appender = new TestAppender();
        logger.addAppender(appender);
    }

    @After
    public void tearDownAbstractAppender() throws Exception {
        logger.removeAppender(appender);
    }


}
