package il.ac.technion.ie.experiments.threads;

import il.ac.technion.ie.experiments.model.ConvexBPContext;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class CommandExacterTest {

    private CommandExacter classUnderTest;
    private File outputFile = null;

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
        ConvexBPContext convexBPContext = new ConvexBPContext("C:\\Users\\i062070\\Downloads\\dcBP\\x64\\Debug", "test.uai", "testOut.txt", 2);
        classUnderTest.execute(convexBPContext);
        outputFile = new File(convexBPContext.getPathToOutputFile());
        Assert.assertTrue("outputfile of convexBP was not created", outputFile.exists());
    }
}