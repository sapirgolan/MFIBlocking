package il.ac.technion.ie.experiments.apiAccess;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ArgumentsContextTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void receivePathToDataset() throws Exception {
        File datasetFile = temporaryFolder.newFile("temporerFile");
        ArgumentsContext argumentsContext = new ArgumentsContext(datasetFile.getAbsolutePath()).invoke();
        assertThat(argumentsContext.getPathToDataset(), is(datasetFile.getAbsolutePath()));
    }

    @Test
    public void systemExitWhenNoArgumentsPassed() throws Exception {
        exit.expectSystemExitWithStatus(-1);
        ArgumentsContext argumentsContext = new ArgumentsContext(null);
        argumentsContext.invoke();
    }

    @Test
    public void receivePathToDadasetAndCanopies() throws Exception {
        File datasetRootFoler = temporaryFolder.newFolder("rootDataset");
        File canopiesRootFolder = temporaryFolder.newFolder("canopiesDataset");
        ArgumentsContext argumentsContext = new ArgumentsContext(datasetRootFoler.getAbsolutePath(), canopiesRootFolder.getAbsolutePath()).invoke();

        assertThat(argumentsContext.getPathToDataset(), is(datasetRootFoler.getAbsolutePath()));
        assertThat(argumentsContext.getPathToSerializedFiles(), is(canopiesRootFolder.getAbsolutePath()));
        assertThat(argumentsContext.isProfilingMode(), is(false));
    }

    @Test
    public void createCanopiesInPerf() throws Exception {
        File datasetRootFoler = temporaryFolder.newFolder("rootDataset");
        ArgumentsContext argumentsContext = new ArgumentsContext(datasetRootFoler.getAbsolutePath(), "perf").invoke();

        assertThat(argumentsContext.getPathToDataset(), is(datasetRootFoler.getAbsolutePath()));
        assertThat(argumentsContext.getPathToSerializedFiles(), nullValue());
        assertThat(argumentsContext.isProfilingMode(), is(true));
    }
}