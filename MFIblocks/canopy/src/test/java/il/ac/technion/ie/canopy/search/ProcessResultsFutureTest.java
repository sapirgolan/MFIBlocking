package il.ac.technion.ie.canopy.search;

import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.utils.MockIterator;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Document.class, ScoreDoc.class})
public class ProcessResultsFutureTest {

    public static final int NUMBER_OF_DOCS = 15;

    @Mock
    private List<ScoreDoc> scoreDocs;
    @Mock
    private IndexSearcher searcher;

    @InjectMocks
    private ProcessResultsFuture classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new ProcessResultsFuture(new ScoreDoc[1], searcher);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCall() throws Exception {
        List<String> expected = new ArrayList<>();
        //mock actual documents
        List<ScoreDoc> docs = new ArrayList<>();
        for (int i = 1; i <= NUMBER_OF_DOCS; i++) {
            ScoreDoc scoreDoc = new ScoreDoc(i, i / NUMBER_OF_DOCS);
            Document document = mock(Document.class);
            when(searcher.doc(i)).thenReturn(document);
            when(document.get(CanopyInteraction.ID)).thenReturn(String.valueOf(-1 * i));
            expected.add(String.valueOf(-1 * i));
            docs.add(scoreDoc);
        }
        //mock size of documents
        when(scoreDocs.size()).thenReturn(NUMBER_OF_DOCS);
        //mock iteration
        MockIterator.mockIterable(scoreDocs, docs);

        List<String> docsIDs = classUnderTest.call();
        assertThat(docsIDs, hasSize(NUMBER_OF_DOCS));
        assertThat(docsIDs, contains(expected.toArray(new String[expected.size()])));

    }
}