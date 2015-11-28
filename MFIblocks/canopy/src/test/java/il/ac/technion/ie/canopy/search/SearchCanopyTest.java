package il.ac.technion.ie.canopy.search;

import com.google.common.collect.Lists;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.search.core.SearchEngine;
import il.ac.technion.ie.search.search.ISearch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.spy;

public class SearchCanopyTest {

    private ISearch classUnderTest;
    private SearchEngine searchEngine;

    @Before
    public void setUp() throws Exception {
        classUnderTest = spy(new SearchCanopy());
        searchEngine = spy(new SearchEngine(new CanopyInteraction()));
    }

    @After
    public void tearDown() throws Exception {
        searchEngine.destroy();
    }

    @Test
    public void testSearch() throws Exception {

    }

    @Test
    public void testCreateFuzzyQuery() throws Exception {
        List<String> terms = Lists.newArrayList("pninit", "ariel");
        String queryTerm = Whitebox.invokeMethod(classUnderTest, "concatTermsToFuzzy", terms);
        assertThat(queryTerm, is("pninit~0.7 OR ariel~0.7"));
    }

    @Test
    public void testInsertOneRecordAndSearchIt() throws Exception {
        //create Records and Add them
        ArrayList<String> fieldNames = Lists.newArrayList("First", "Last", "middle", "gender");
        ArrayList<String> valuesOne = Lists.newArrayList("Ariel", "Israel", "Leon", "M");
//        ArrayList<String> valuesTwo = Lists.newArrayList("Ariel", "Israel", "Leon", "M");
        Record recordOne = new Record(fieldNames, valuesOne, 1);
//        Record recordTwo = new Record(fieldNames, valuesTwo, 2);
        searchEngine.addRecords(Lists.newArrayList(recordOne));

        List<String> recordsIDs = searchEngine.searchInIndex(classUnderTest, 1, valuesOne);
        assertThat(recordsIDs, hasSize(1));
        assertThat(recordsIDs, contains("1"));
    }

    @Test
    public void testInsertOneRecordAndSearchItFuzzy() throws Exception {
        //create Records and Add them
        ArrayList<String> fieldNames = Lists.newArrayList("First", "Last", "middle", "gender");
        ArrayList<String> valuesOne = Lists.newArrayList("Ariel", "Israel", "Leon", "M");
        ArrayList<String> valuesTwo = Lists.newArrayList("Isra", "Lean", "F");
        Record recordOne = new Record(fieldNames, valuesOne, 1);
        searchEngine.addRecords(Lists.newArrayList(recordOne));

        List<String> recordsIDs = searchEngine.searchInIndex(classUnderTest, 1, valuesTwo);
        assertThat(recordsIDs, hasSize(1));
        assertThat(recordsIDs, contains("1"));
    }

    @Test
    public void testInsertTwoDuplicateRecordAndSearchOne() throws Exception {
        //create Records and Add them
        ArrayList<String> fieldNames = Lists.newArrayList("First", "Last", "middle", "gender");
        ArrayList<String> valuesOne = Lists.newArrayList("Ariel", "Israel", "Leon", "M");
        ArrayList<String> valuesTwo = Lists.newArrayList("Areil", "Israeli", "Laon", "M");
        Record recordOne = new Record(fieldNames, valuesOne, 1);
        Record recordTwo = new Record(fieldNames, valuesTwo, 2);
        searchEngine.addRecords(Lists.newArrayList(recordOne, recordTwo));

        List<String> recordsIDs = searchEngine.searchInIndex(classUnderTest, 2, valuesOne);
        assertThat(recordsIDs, hasSize(2));
        assertThat(recordsIDs, contains("1", "2"));
    }

    @Test
    public void testInsertTwoDifferentRecordAndSearchOne() throws Exception {
        //create Records and Add them
        ArrayList<String> fieldNames = Lists.newArrayList("First", "Last", "middle", "gender");
        ArrayList<String> valuesOne = Lists.newArrayList("Ariel", "Israel", "Leon", "M");
        ArrayList<String> valuesTwo = Lists.newArrayList("Natalie", "Cohen", "", "F");
        Record recordOne = new Record(fieldNames, valuesOne, 1);
        Record recordTwo = new Record(fieldNames, valuesTwo, 2);
        searchEngine.addRecords(Lists.newArrayList(recordOne, recordTwo));

        List<String> recordsIDs = searchEngine.searchInIndex(classUnderTest, 1, valuesOne);
        assertThat(recordsIDs, hasSize(1));
        assertThat(recordsIDs, contains("1"));
    }

    @Test
    public void testRetrieveAllDocsFromAllPages() throws Exception {

        List<Record> allRecords = new ArrayList<>();
        List<String> fieldNames = Lists.newArrayList("First", "Last", "middle", "gender", "generatedString");
        List<String> seedValues = Lists.newArrayList("Ariel", "Israel", "Leon", "M", "Gael");
        allRecords.add(new Record(fieldNames, seedValues, 0));
        int numberOfRecordsInDataset = SearchCanopy.DEFAULT_HITS_PER_PAGE * 5;
        for (int i = 1; i < numberOfRecordsInDataset; i++) {
            List<String> valuesForTempRecord = Lists.newArrayList("Ar" + this.generateRandomString(2) + "l", "Isr" + generateRandomString(1) + "el", "Leon", "M", "Gael" + generateRandomString(1));
            allRecords.add(new Record(fieldNames, valuesForTempRecord, i));
        }
        searchEngine.addRecords(allRecords);

        List<String> recordsIDs = searchEngine.searchInIndex(classUnderTest, null, seedValues);
        assertThat(recordsIDs, hasSize(numberOfRecordsInDataset));
    }

    private String generateRandomString(int numberOfChars) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < numberOfChars; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
}