package il.ac.technion.ie.experiments.threads;

import il.ac.technion.ie.experiments.model.ConvexBPContext;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CommandExacterTest {

    public static final String DCBP_DIR = "C:\\Users\\i062070\\Downloads\\dcBP\\x64\\Debug";
    private CommandExacter classUnderTest;
    private File outputFile = null;

    @BeforeClass
    public static void beforeClass() throws URISyntaxException, IOException {
        FileUtils.copyFileToDirectory(ExperimentsUtils.getUaiFile(), new File(DCBP_DIR));

        File uaiFile = new File(DCBP_DIR + File.separator + "uaiFile.uai");
        if (uaiFile.exists()) {
            FileUtils.forceDeleteOnExit(uaiFile);
        }
    }

    @Before
    public void setUp() throws Exception {
        classUnderTest = new CommandExacter();
        if (outputFile != null) {
            if (outputFile.exists()) {
                FileUtils.forceDelete(outputFile);
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        if (outputFile != null) {
            if (outputFile.exists()) {
                FileUtils.forceDelete(outputFile);
            }
        }
    }

    @Test
    public void testExecute() throws Exception {
        ConvexBPContext convexBPContext = new ConvexBPContext(DCBP_DIR, "uaiFile.uai", "testOut.txt", 2);
        classUnderTest.execute(convexBPContext);
        outputFile = new File(convexBPContext.getPathToOutputFile());
        Assert.assertTrue("outputfile of convexBP was not created", outputFile.exists());
    }

    @Test
    public void testCopyUaiFileOfBadExecution() throws Exception {
        File uaiFile = ExperimentsUtils.getUaiFile();
        ConvexBPContext convexBPContext = new ConvexBPContext(uaiFile.getParent(), uaiFile.getName(), "", 0);
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