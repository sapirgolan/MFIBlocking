package il.ac.technion.ie.experiments.threads;

import il.ac.technion.ie.experiments.service.ExprimentsService;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by I062070 on 20/01/2017.
 */
public class AbstractExecutorTest {
    protected static final String DCBP_DIR = ExprimentsService.DCBP_DIR;
    protected static File outputFile = null;
    protected static File uaiFile;
    protected IConvexBPExecutor classUnderTest;

    @BeforeClass
    public static void beforeClass() throws URISyntaxException, IOException {
        FileUtils.copyFileToDirectory(ExperimentsUtils.getUaiFile(), new File(DCBP_DIR));

        uaiFile = new File(DCBP_DIR + File.separator + "uaiFile.uai");
        outputFile = new File(DCBP_DIR + File.separator + "testOut.txt");
        if (uaiFile.exists()) {
            FileUtils.forceDeleteOnExit(uaiFile);
        }
    }

    @Before
    public void setUp() throws Exception {
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
}
