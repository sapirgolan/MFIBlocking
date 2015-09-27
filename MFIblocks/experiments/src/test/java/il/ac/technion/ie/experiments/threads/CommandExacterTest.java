package il.ac.technion.ie.experiments.threads;

import il.ac.technion.ie.experiments.model.ConvexBPContext;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
}