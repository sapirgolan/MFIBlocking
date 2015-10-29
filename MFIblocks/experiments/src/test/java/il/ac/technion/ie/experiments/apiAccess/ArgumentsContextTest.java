package il.ac.technion.ie.experiments.apiAccess;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ArgumentsContextTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private File tempFile;

    @Before
    public void setUp() throws Exception {
        tempFile = File.createTempFile("temporerFile", null);
    }

    @Test
    public void testReceivePathToDataset() throws Exception {
        ArgumentsContext argumentsContext = new ArgumentsContext(tempFile.getAbsolutePath()).invoke();
        assertThat(argumentsContext.getPathToDataset(), is(tempFile.getAbsolutePath()));
    }


    @Test
    public void testSystemExitWhenNoArgumentsPassed() throws Exception {
        exit.expectSystemExitWithStatus(-1);
        ArgumentsContext argumentsContext = new ArgumentsContext(null);
        argumentsContext.invoke();
    }

    @Test
    public void testReceiveThreshold() throws Exception {
        ArgumentsContext argumentsContext = new ArgumentsContext(tempFile.getAbsolutePath(), "0.2").invoke();
        assertThat(argumentsContext.getThreshold(), closeTo(0.2, 0.0001));
    }

    @Test
    public void testThresholdIsZeroWhenNotPassAsParameter() throws Exception {
        ArgumentsContext argumentsContext = new ArgumentsContext(tempFile.getAbsolutePath()).invoke();
        assertThat(argumentsContext.getThreshold(), closeTo(0.0, 0.0001));
    }

    @Test
    public void testReceiveMultipleThresholds() throws Exception {
        ArgumentsContext argumentsContext = new ArgumentsContext(tempFile.getAbsolutePath(), "0.2, 0.3").invoke();
        assertThat(argumentsContext.getThreshold(), closeTo(0.2, 0.0001));
        assertThat(argumentsContext.getThresholds(), containsInAnyOrder(closeTo(0.2, 0.0001), closeTo(0.3, 0.0001)));
    }

    @Test
    public void testReceiveMultipleValuesNotThreshold() throws Exception {
        ArgumentsContext argumentsContext = new ArgumentsContext(tempFile.getAbsolutePath(), "0.2, 0.3, p.9").invoke();
        assertThat(argumentsContext.getThreshold(), closeTo(0.2, 0.0001));
        assertThat(argumentsContext.getThresholds(), containsInAnyOrder(closeTo(0.2, 0.0001), closeTo(0.3, 0.0001)));
    }
}