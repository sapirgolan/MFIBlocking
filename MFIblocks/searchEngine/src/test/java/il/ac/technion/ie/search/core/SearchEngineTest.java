package il.ac.technion.ie.search.core;

import il.ac.technion.ie.search.module.ComparisonInteraction;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;*/

@RunWith(PowerMockRunner.class)
@PrepareForTest({Document.class, SearchEngine.class})
public class SearchEngineTest {
    private SearchEngine engine;

    @Before
    public void setUp() {
        engine = new SearchEngine(new ComparisonInteraction());
    }



    @Test(expected = FileNotFoundException.class)
    public void testconnectToFile_fileNotExists() throws Exception {
        Whitebox.invokeMethod(engine, "connectToFile", "xxxxxx");
    }

    @Test
    public void testConnectToFile() throws Exception {
        BufferedReader reader = getBufferReaderToResutrantsQgramsFile();
        Assert.assertNotNull("Failed to obtain reader to file", reader);
        reader.close();
    }

    private BufferedReader getBufferReaderToResutrantsQgramsFile() throws Exception {
        File file = getResturantsFile();
        return Whitebox.invokeMethod(engine, "connectToFile", file.getAbsolutePath());
    }

    private File getResturantsFile() throws URISyntaxException {
        String pathToFile = "/resturants_ids.txt";
        URL resourceUrl = getClass().getResource(pathToFile);
        return new File(resourceUrl.toURI());
    }

    @Test
    public void testIndexFileContent() throws Exception {
        IndexWriter indexWriter = Whitebox.invokeMethod(engine, "createInderWeiter");
        BufferedReader bufferedReader = getBufferReaderToResutrantsQgramsFile();

        int numDocsBeforeAddingToIndex = indexWriter.numDocs();
        Whitebox.invokeMethod(engine, "indexFileContent", bufferedReader, indexWriter);
        int numDocsAfterAddingToIndex = indexWriter.numDocs();
        Assert.assertNotEquals("didn't index any documents", numDocsBeforeAddingToIndex, numDocsAfterAddingToIndex);
    }

    @Test
    public void testGetRecordAttributes_EndToEnd() throws URISyntaxException, IOException {
        File resturantsFile = getResturantsFile();
        engine.addRecords(resturantsFile.getCanonicalPath());
        List<String> recordAttributes = engine.getRecordAttributes(Integer.toString(49));
        ArrayList<String> expected = new ArrayList<String>(Arrays.asList("587", "35", "36", "37", "38", "39", "173", "404", "405", "588", "589", "590", "591"));
        Assert.assertTrue("Didn't get all records", recordAttributes.containsAll(expected));

        recordAttributes.removeAll(expected);
        Assert.assertTrue("Obtained more records than needed", recordAttributes.size() == 0);
    }

    @Test
    public void testGetRecordAttributes_recordNotExists() throws URISyntaxException, IOException {
        //suppress Indexing phase
        PowerMock.suppress(PowerMock.method(SearchEngine.class, "indexFileContent"));
        engine = new SearchEngine(new ComparisonInteraction());

        File resturantsFile = getResturantsFile();
        engine.addRecords(resturantsFile.getCanonicalPath());
        List<String> recordAttributes = engine.getRecordAttributes(Integer.toString(49));
        Assert.assertTrue("Obtained attributes for recordId that was not indexed", recordAttributes.isEmpty());
    }

    @Test
    public void testIsTermSizeValid_shortTerm() throws Exception {
        Boolean isTermSizeValid = Whitebox.<Boolean>invokeMethod(engine, "isTermSizeValid", "This is a short Term");
        Assert.assertTrue("Short term was classified as long term", isTermSizeValid);

    }


}
