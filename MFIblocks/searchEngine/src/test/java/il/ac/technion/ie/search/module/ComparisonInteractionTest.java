package il.ac.technion.ie.search.module;

import il.ac.technion.ie.search.exception.TooManySearchResults;
import org.apache.lucene.document.Document;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Document.class, ComparisonInteraction.class})
public class ComparisonInteractionTest {

    private ComparisonInteraction classUnderTest;

    @Before
    public void setup() {
        classUnderTest = new ComparisonInteraction();
    }

    @Test(expected = TooManySearchResults.class)
    public void testObtainTopResult_ToManyDocs() throws Exception {
        IndexSearcher indexSearcher = PowerMockito.mock(IndexSearcher.class);
        ScoreDoc[] docs = new ScoreDoc[]{new ScoreDoc(0, 0), new ScoreDoc(1, 0.1f)};
        Whitebox.invokeMethod(classUnderTest, "obtainTopResult", indexSearcher, docs);
    }

    @Test
    public void testSeperateQgrams() throws Exception {
        List<String> qgrams = Whitebox.invokeMethod(classUnderTest, "seperateQgrams", "1 2 3 ");
        Set<String> expected = new HashSet<>(Arrays.asList("1", "2", "3"));
        Assert.assertTrue("don't have all qgrams", qgrams.containsAll(expected));
    }

    @Test
    public void testSeperateQgrams_noItems() throws Exception {
        List<String> qgrams = Whitebox.invokeMethod(classUnderTest, "seperateQgrams", "");
        Assert.assertTrue("Didn't obtain empty result", qgrams.isEmpty());
    }

    @Test
    public void testSeperateQgrams_space() throws Exception {
        List<String> qgrams = Whitebox.invokeMethod(classUnderTest, "seperateQgrams", " ");
        Assert.assertTrue("Didn't obtain empty result", qgrams.isEmpty());
    }

    @Test(expected = TooManySearchResults.class)
    public void testObtainTopResult_noDocs() throws Exception {
        IndexSearcher indexSearcher = PowerMockito.mock(IndexSearcher.class);
        ScoreDoc[] docs = new ScoreDoc[0];
        Whitebox.invokeMethod(classUnderTest, "obtainTopResult", indexSearcher, docs);
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

        List<String> qgrams = Whitebox.invokeMethod(classUnderTest, "obtainTopResult", indexSearcher, docs);

        Set<String> expected = new HashSet<>(Arrays.asList("1", "4", "8"));
        Assert.assertTrue("don't have all qgrams", qgrams.containsAll(expected));
    }
}