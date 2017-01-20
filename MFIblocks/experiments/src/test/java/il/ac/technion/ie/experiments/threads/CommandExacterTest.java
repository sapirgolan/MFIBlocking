package il.ac.technion.ie.experiments.threads;

import il.ac.technion.ie.experiments.model.ConvexBPContext;
import il.ac.technion.ie.experiments.service.ExprimentsService;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CommandExacterTest extends AbstractExecutorTest {

    @Before
    public void setUp_CommandExacterTest() throws Exception {
        classUnderTest = new CommandExacter();
    }

    @Test
    public void testExecute() throws Exception {
        ConvexBPContext convexBPContext = new ConvexBPContext(DCBP_DIR, uaiFile.getAbsolutePath(), outputFile.getAbsolutePath(), 5);
        classUnderTest.execute(convexBPContext);
        outputFile = new File(convexBPContext.getPathToOutputFile());
        Assert.assertTrue("outputfile of convexBP was not created", outputFile.exists());
    }

    @Test
    public void testCopyUaiFileOfBadExecution() throws Exception {
        File uaiFile = ExperimentsUtils.getUaiFile();
        ConvexBPContext convexBPContext = new ConvexBPContext(uaiFile.getParent(), uaiFile.getAbsolutePath(), "", 0);
        double threshold = 0.434;
        convexBPContext.setThreshold(threshold);
        Whitebox.setInternalState(classUnderTest, convexBPContext);

        //execute
        File badUaiFile = Whitebox.invokeMethod(classUnderTest, "copyBadUaiFile");
        assertThat(badUaiFile.exists(), is(true));
        assertThat(badUaiFile.getAbsolutePath(), containsString(System.getProperty("java.io.tmpdir")));
        assertThat(badUaiFile.getName(), allOf(containsString(".uai"), containsString(String.valueOf(threshold))));
        FileUtils.forceDeleteOnExit(badUaiFile);

    }
}