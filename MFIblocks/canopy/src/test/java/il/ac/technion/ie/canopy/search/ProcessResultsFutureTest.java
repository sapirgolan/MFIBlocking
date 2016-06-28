package il.ac.technion.ie.canopy.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Document.class, ScoreDoc.class})
public class ProcessResultsFutureTest {

    public static final int NUMBER_OF_DOCS = 15;

    @Mock
    private List<ScoreDoc> scoreDocs;
    @Mock
    private IndexSearcher searcher;

/*    @InjectMocks
    private ProcessResultsFuture classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new ProcessResultsFuture(new ScoreDoc[1], searcher);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCall() throws Exception {
        List<SearchResult> expected = new ArrayList<>();
        //mock actual documents
        List<ScoreDoc> docs = new ArrayList<>();
        for (int i = 1; i <= NUMBER_OF_DOCS; i++) {
            ScoreDoc scoreDoc = new ScoreDoc(i, i / NUMBER_OF_DOCS);
            Document document = mock(Document.class);
            when(searcher.doc(i)).thenReturn(document);
            when(document.get(CanopyInteraction.ID)).thenReturn(String.valueOf(-1 * i));
            ;
            expected.add(new SearchResult(String.valueOf(-1 * i), 0));
            docs.add(scoreDoc);
        }
        //mock size of documents
        when(scoreDocs.size()).thenReturn(NUMBER_OF_DOCS);
        //mock iteration
        MockIterator.mockIterable(scoreDocs, docs);

        List<SearchResult> results = classUnderTest.call();
        assertThat(results, hasSize(NUMBER_OF_DOCS));
        assertThat(results, contains(expected.toArray(new SearchResult[expected.size()])));

    }*/
}