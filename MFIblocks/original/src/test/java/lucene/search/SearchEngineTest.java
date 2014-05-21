package lucene.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import fimEntityResolution.exception.TooManySearchResults;

/*import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;*/

@RunWith(PowerMockRunner.class)
@PrepareForTest(Document.class)
public class SearchEngineTest {
	private SearchEngine engine;
	
	@Before
	public void setUp(){
		engine = new SearchEngine();
	}

	@Test
	public void testSeperateQgrams() throws Exception {
		List<String> qgrams = Whitebox.<List<String>>invokeMethod(engine, "seperateQgrams", "1 2 3 ");
		Set<String> expected = new HashSet<String>(Arrays.asList("1", "2", "3"));
		Assert.assertTrue("don't have all qgrams", qgrams.containsAll(expected));
	}
	
	@Test
	public void testSeperateQgrams_noItems() throws Exception {
		List<String> qgrams = Whitebox.<List<String>>invokeMethod(engine, "seperateQgrams", "");
		Assert.assertTrue("Didn't obtain empty result", qgrams.isEmpty());
	}
	
	@Test
	public void testSeperateQgrams_space() throws Exception {
		List<String> qgrams = Whitebox.<List<String>>invokeMethod(engine, "seperateQgrams", " ");
		Assert.assertTrue("Didn't obtain empty result", qgrams.isEmpty());
	}
	
	@Test (expected = TooManySearchResults.class)
	public void testObtainTopResult_noDocs() throws Exception {
		IndexSearcher indexSearcher = PowerMockito.mock(IndexSearcher.class);
		ScoreDoc[] docs = new ScoreDoc[0];
		Whitebox.invokeMethod(engine, "obtainTopResult", indexSearcher, docs);
	}
	
	@Test (expected = TooManySearchResults.class)
	public void testObtainTopResult_ToManyDocs() throws Exception {
		IndexSearcher indexSearcher = PowerMockito.mock(IndexSearcher.class);
		ScoreDoc[] docs = new ScoreDoc[]{new ScoreDoc(0, 0), new ScoreDoc(1, 0.1f)};
		Whitebox.invokeMethod(engine, "obtainTopResult", indexSearcher, docs);
	}
	
	@Test
	public void testObtainTopResult_oneDocs() throws Exception {
		// We use PowerMock.createMock(..) to create the mock object.
		Document document = PowerMock.createMock(Document.class);
		EasyMock.expect(document.get("qgrams")).andReturn(" 1 8 4 ");
		
		ScoreDoc[] docs = new ScoreDoc[]{new ScoreDoc(0, 0)};
		IndexSearcher indexSearcher = PowerMockito.mock(IndexSearcher.class);
		PowerMockito.when(indexSearcher.doc(0)).thenReturn(document);
		
		// PowerMock.replay(..) must be used.
		PowerMock.replay(document);
		
		List<String> qgrams = Whitebox.<List<String>>invokeMethod(engine, "obtainTopResult", indexSearcher, docs);
		
		Set<String> expected = new HashSet<String>(Arrays.asList("1", "4", "8"));
		Assert.assertTrue("don't have all qgrams", qgrams.containsAll(expected));
	}
	
	@Test (expected = FileNotFoundException.class)
	public void testconnectToFile_fileNotExists() throws Exception{
		Whitebox.invokeMethod(engine, "connectToFile", "xxxxxx");
	}
	
	@Test
	public void testConnectToFile() throws Exception {
		BufferedReader reader = getBufferReaderToResutrantsQgramsFile();
		Assert.assertNotNull("Failed to obtain reader to file", reader);
		reader.close();
	}

	private BufferedReader getBufferReaderToResutrantsQgramsFile() throws URISyntaxException, Exception {
		String pathToFile = "/resturants_ids.txt";
		URL resourceUrl = getClass().getResource(pathToFile);
		File file = new File(resourceUrl.toURI());
		BufferedReader reader = Whitebox.<BufferedReader>invokeMethod(engine, "connectToFile", file.getAbsolutePath());
		return reader;
	}
	
	@Test
	public void testIndexFileContent() throws Exception {
		IndexWriter indexWriter = Whitebox.<IndexWriter>invokeMethod(engine, "createInderWeiter");
		BufferedReader bufferedReader = getBufferReaderToResutrantsQgramsFile();
		
		int numDocsBeforeAddingToIndex = indexWriter.numDocs();
		Whitebox.invokeMethod(engine, "indexFileContent", bufferedReader, indexWriter);
		int numDocsAfterAddingToIndex = indexWriter.numDocs();
		Assert.assertNotEquals("didn't index any documents",numDocsBeforeAddingToIndex, numDocsAfterAddingToIndex);
	}
	

}
