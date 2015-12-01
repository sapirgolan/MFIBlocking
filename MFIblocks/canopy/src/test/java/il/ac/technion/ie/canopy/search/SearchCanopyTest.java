package il.ac.technion.ie.canopy.search;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.search.core.SearchEngine;
import il.ac.technion.ie.search.module.SearchResult;
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
    public void testRemoveEmptyStringsFromQueryTerms() throws Exception {
        String allTermsConcatenated = "unk, , , , mr, , aaron, vic, south kibngsville,3806,5, kempthjsoe, , ,5780788,6, NoRole";
        List<String> terms = new ArrayList<>(Splitter.on(',').trimResults().splitToList(allTermsConcatenated));

        Whitebox.invokeMethod(classUnderTest, "removeEmptyStrings", terms);
        assertThat(terms, hasSize(11));
        assertThat(terms, containsInAnyOrder("unk", "mr", "aaron", "vic", "south kibngsville", "5", "6", "NoRole", "5780788", "kempthjsoe", "3806"));
    }

    @Test
    public void testCreateFuzzyQueryWithBlankValues() throws Exception {
        String allTermsConcatenated = "unk, , , , mr, , aaron, vic, south kibngsville,3806,5, kempthjsoe, , ,5780788,6, NoRole";
        List<String> terms = new ArrayList<>(Splitter.on(',').trimResults().splitToList(allTermsConcatenated));

        String queryTerm = Whitebox.invokeMethod(classUnderTest, "concatTermsToFuzzy", terms);
        assertThat(queryTerm, is("unk~0.7 OR mr~0.7 OR aaron~0.7 OR vic~0.7 OR south kibngsville~0.7 OR 3806~0.7 OR 5~0.7 OR kempthjsoe~0.7 OR 5780788~0.7 OR 6~0.7 OR NoRole~0.7"));
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

        List<SearchResult> recordsIDs = searchEngine.searchInIndex(classUnderTest, 1, valuesOne);
        assertThat(recordsIDs, hasSize(1));
        assertThat(recordsIDs.get(0).getID(), is("1"));
    }

    @Test
    public void testInsertOneRecordAndSearchItFuzzy() throws Exception {
        //create Records and Add them
        ArrayList<String> fieldNames = Lists.newArrayList("First", "Last", "middle", "gender");
        ArrayList<String> valuesOne = Lists.newArrayList("Ariel", "Israel", "Leon", "M");
        ArrayList<String> valuesTwo = Lists.newArrayList("Isra", "Lean", "F");
        Record recordOne = new Record(fieldNames, valuesOne, 1);
        searchEngine.addRecords(Lists.newArrayList(recordOne));

        List<SearchResult> recordsIDs = searchEngine.searchInIndex(classUnderTest, 1, valuesTwo);
        assertThat(recordsIDs, hasSize(1));
        assertThat(recordsIDs.get(0).getID(), is("1"));
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

        List<SearchResult> searchResults = searchEngine.searchInIndex(classUnderTest, 2, valuesOne);
        assertThat(searchResults, hasSize(2));
        List<String> recordIDs = extractRecordIDs(searchResults);
        assertThat(recordIDs, contains("1", "2"));
    }

    private List<String> extractRecordIDs(List<SearchResult> searchResults) {
        List<String> recordIDs = new ArrayList<>();
        for (SearchResult searchResult : searchResults) {
            recordIDs.add(searchResult.getID());
        }
        return recordIDs;
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

        List<SearchResult> searchResults = searchEngine.searchInIndex(classUnderTest, 1, valuesOne);
        assertThat(searchResults, hasSize(1));
        List<String> recordIDs = extractRecordIDs(searchResults);
        assertThat(recordIDs, contains("1"));
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

        List<SearchResult> searchResults = searchEngine.searchInIndex(classUnderTest, null, seedValues);
        assertThat(searchResults, hasSize(numberOfRecordsInDataset));
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