package il.ac.technion.ie.experiments.parsers;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.FuzzyService;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UaiBuilder.class, FuzzyService.class})
public class UaiBuilderIntegrationTest {

    private UaiBuilder classUnderTest;
    private File outputFile;

    @Before
    public void setUp() throws Exception {
        List<BlockWithData> fuzzyBlocks = ExperimentsUtils.createFuzzyBlocks();
        classUnderTest = PowerMockito.spy(new UaiBuilder(fuzzyBlocks));

        outputFile = Whitebox.invokeMethod(classUnderTest, "createOutputFile");
    }

    @After
    public void tearDown() throws Exception {
        if (null != outputFile) {
            FileUtils.forceDelete(outputFile);
        }
    }

    @Test
    public void testCreateUaiFile_cleanBlocks() throws Exception {
        //mocking
        PowerMockito.doReturn(outputFile).when(classUnderTest, "createOutputFile");

        //execution
        classUnderTest.createUaiFile();

        //assertion
        Assert.assertThat(FileUtils.sizeOf(outputFile), Matchers.greaterThan(100L));
    }
}