package il.ac.technion.ie.experiments.threads;

import il.ac.technion.ie.experiments.model.ConvexBPContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by I062070 on 20/01/2017.
 */
public class ApacheExecutorTest extends AbstractExecutorTest{
    
    @Before
    public void setUp_ApacheExecutorTest() throws Exception {
        classUnderTest = new ApacheExecutor();
    }

    @Test
    public void testExecute() throws Exception {
        ConvexBPContext convexBPContext = new ConvexBPContext(DCBP_DIR, uaiFile.getAbsolutePath(), outputFile.getName(), 5);
        classUnderTest.execute(convexBPContext);
        outputFile = new File(convexBPContext.getPathToOutputFile());
        Assert.assertTrue("outputfile of convexBP was not created", outputFile.exists());
    }
    
}