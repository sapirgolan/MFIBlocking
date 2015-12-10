package il.ac.technion.ie.canopy.algorithm;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.canopy.model.CanopyRecord;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.search.module.SearchResult;
import il.ac.technion.ie.utils.UtilitiesForBlocksAndRecords;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.reflect.Whitebox;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class CanopyTest {

    public static final int NUMBER_OF_RECORDS_IN_BIG_FILE = 1000;
    @Spy
    private CanopyInteraction canopyInteraction = new CanopyInteraction();

    private Canopy classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = Whitebox.newInstance(Canopy.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = CanopyParametersException.class)
    public void testConstractorT1SmallerThanT2() throws Exception {
        new Canopy(mock(List.class), 0.4, 0.5);
    }

    @Test
    public void testVerifyAllRecordsAreAdded() throws Exception {
        String pathToBigRecordsFile = ExperimentsUtils.getPathToBigRecordsFile();
        List<Record> records = UtilitiesForBlocksAndRecords.createRecordsFromTestFile(pathToBigRecordsFile);
        classUnderTest = new Canopy(records, 0.6, 0.3);
        classUnderTest.initSearchEngine(canopyInteraction);
        Mockito.verify(canopyInteraction, Mockito.times(NUMBER_OF_RECORDS_IN_BIG_FILE)).addDoc(Mockito.any(IndexWriter.class), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testSampleRecordRandomly() throws Exception {
        List<Record> objects = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Record record = mock(Record.class);
            when(record.getRecordID()).thenReturn(i);
            objects.add(record);
        }
        classUnderTest = Whitebox.newInstance(Canopy.class);
        Record first = Whitebox.invokeMethod(classUnderTest, "sampleRecordRandomly", objects);
        Record second = Whitebox.invokeMethod(classUnderTest, "sampleRecordRandomly", objects);
        assertThat(first, is(not(second)));
    }

    @Test
    public void testFetchRecordsBasedOnIDs() throws Exception {
        String pathToBigRecordsFile = ExperimentsUtils.getPathToBigRecordsFile();
        List<Record> records = UtilitiesForBlocksAndRecords.createRecordsFromTestFile(pathToBigRecordsFile);
        classUnderTest = new Canopy(records, 0.6, 0.3);

        List<Integer> iDsRandomly = createIDsRandomly(records);
        List<SearchResult> searchResults = convertIdsToSearchResults(iDsRandomly);
        List<Record> fetchRecordsBasedOnIDs = Whitebox.invokeMethod(classUnderTest, "fetchRecordsBasedOnIDs", searchResults);
        assertThat(fetchRecordsBasedOnIDs, hasSize(iDsRandomly.size()));
        for (int i = 0; i < fetchRecordsBasedOnIDs.size(); i++) {
            Record record = fetchRecordsBasedOnIDs.get(i);
            assertThat(iDsRandomly, hasItem(record.getRecordID()));
        }
    }

    @Test
    public void testRemoveRecords() throws Exception {
        List<Record> pool = getRecordsFromCsv();
        Record root = pool.get(10);
        List<Record> list = new ArrayList<>(pool.subList(5, 9));
        int expectedSize = pool.size() - list.size() - 1;

        Whitebox.invokeMethod(classUnderTest, "removeRecords", pool, root, list);

        assertThat(pool, hasSize(expectedSize));
        assertThat(pool, not(containsInAnyOrder(list.toArray())));
    }

    /**
     * This test verify that in a given List<Records> list, one can remove records from it
     * by:
     * 1) supplying an instance of the list
     * 2) supplying subclass instance of the list that was constructed by an instance of the list
     * 3) supplying subclass instance of the list that was constructed with the same values as an instance of the list
     *
     * @throws Exception
     */
    @Test
    public void testRemoveRecordsWhereRemovedOneAreNotSubsetFromSameSource() throws Exception {
        List<String> fieldsName = Lists.newArrayList("First", "Last", "Gender");
        List<Record> originRecords = new ArrayList<>();
        Record davidZ = new Record(fieldsName, Lists.newArrayList("David", "Zuaretz", "M"), 1);
        Record pavelNedved = new Record(fieldsName, Lists.newArrayList("Pavel", "Nedved", "M"), 4);
        Record needToRemain = new Record(fieldsName, Lists.newArrayList("Robert", "Lev", "M"), 3);

        originRecords.add(davidZ);
        originRecords.add(new Record(fieldsName, Lists.newArrayList("Yael", "Sidi", "F"), 2));
        originRecords.add(pavelNedved);
        originRecords.add(needToRemain);

        //remove records contain 2 records from origin
        List<CanopyRecord> removeRecords = new ArrayList<>();
        removeRecords.add(new CanopyRecord(pavelNedved, 0.2));
        removeRecords.add(new CanopyRecord(new Record(fieldsName, Lists.newArrayList("Yael", "Sidi", "F"), 2), 0.4));
        removeRecords.add(new CanopyRecord(new Record(fieldsName, Lists.newArrayList("Yael", "Mekel", "F"), 6), 0.1));

        Whitebox.invokeMethod(classUnderTest, "removeRecords", originRecords, davidZ, removeRecords);

        assertThat(originRecords, hasSize(1));
        assertThat(originRecords, contains(needToRemain));
    }

    @Test
    public void testCreateCanopies() throws Exception {
        String pathToBigRecordsFile = ExperimentsUtils.getPathToBigRecordsFile();
        List<Record> records = UtilitiesForBlocksAndRecords.createRecordsFromTestFile(pathToBigRecordsFile);
        classUnderTest = new Canopy(records, 0.15, 0.05);
        classUnderTest.initSearchEngine(new CanopyInteraction());
        List<CanopyCluster> canopies = classUnderTest.createCanopies();
        assertThat(canopies, not(empty()));
        assertThat(canopies, not(hasSize(records.size())));
        assertThat(canopies.size(), lessThan(records.size()));
    }

    @Test
    public void testRetainLegalCandidates() throws Exception {
        List<String> fieldsName = Lists.newArrayList("First", "Last", "Gender");
        List<Record> recordsPool = new ArrayList<>();
        Record recordOne = new Record(fieldsName, Lists.newArrayList("David", "Zuaretz", "M"), 1);
        Record recordTwo = new Record(fieldsName, Lists.newArrayList("Pavel", "Nedved", "M"), 4);
        Record recordThree = new Record(fieldsName, Lists.newArrayList("Robert", "Lev", "M"), 3);
        Record recordFour = new Record(fieldsName, Lists.newArrayList("Yael", "Sidi", "F"), 2);

        recordsPool.add(recordOne);
        recordsPool.add(recordFour);
        recordsPool.add(recordTwo);
        recordsPool.add(recordThree);

        //remove records contain 2 records from origin
        List<CanopyRecord> candidateRecordsForCanopy = new ArrayList<>();
        CanopyRecord recordThatNeedToRemain = new CanopyRecord(recordTwo, 0.2);
        candidateRecordsForCanopy.add(recordThatNeedToRemain);
        candidateRecordsForCanopy.add(new CanopyRecord(new Record(fieldsName, Lists.newArrayList("Yael", "Mekel", "F"), 9), 0.4));

        Whitebox.invokeMethod(classUnderTest, "retainLegalCandidates", candidateRecordsForCanopy, recordsPool);

        assertThat(candidateRecordsForCanopy, hasSize(1));
        assertThat(candidateRecordsForCanopy, contains(recordThatNeedToRemain));
    }

    @Test
    public void testRetainLegalCandidates_notContainsFromPool() throws Exception {
        List<String> fieldsName = Lists.newArrayList("First", "Last", "Gender");
        List<Record> recordsPool = new ArrayList<>();
        Record recordOne = new Record(fieldsName, Lists.newArrayList("David", "Zuaretz", "M"), 1);
        Record recordTwo = new Record(fieldsName, Lists.newArrayList("Pavel", "Nedved", "M"), 4);
        Record recordThree = new Record(fieldsName, Lists.newArrayList("Robert", "Lev", "M"), 3);
        Record recordFour = new Record(fieldsName, Lists.newArrayList("Yael", "Sidi", "F"), 2);

        recordsPool.add(recordOne);
        recordsPool.add(recordFour);
        recordsPool.add(recordTwo);
        recordsPool.add(recordThree);

        //remove records contain 2 records from origin
        List<CanopyRecord> candidateRecordsForCanopy = new ArrayList<>();
        candidateRecordsForCanopy.add(new CanopyRecord(new Record(fieldsName, Lists.newArrayList("David", "Robert", "F"), 6), 0.4));
        candidateRecordsForCanopy.add(new CanopyRecord(new Record(fieldsName, Lists.newArrayList("Yael", "Mekel", "F"), 9), 0.4));

        Whitebox.invokeMethod(classUnderTest, "retainLegalCandidates", candidateRecordsForCanopy, recordsPool);

        assertThat(candidateRecordsForCanopy, hasSize(0));
    }

    private List<SearchResult> convertIdsToSearchResults(List<Integer> iDsRandomly) {
        UniformRealDistribution distribution = new UniformRealDistribution();
        List<SearchResult> list = new ArrayList<>(iDsRandomly.size());

        for (Integer id : iDsRandomly) {
            list.add(new SearchResult(String.valueOf(id), distribution.sample()));
        }
        return list;
    }

    private List<Integer> createIDsRandomly(List<Record> records) {
        Random random = new Random();
        int randomIndex = random.nextInt(records.size());
        int secondRandomIndex = random.nextInt(records.size());
        int lower = Math.min(randomIndex, secondRandomIndex);
        int upper = Math.max(randomIndex, secondRandomIndex);
        int[] rangeNumbersPrimitive = Ints.toArray(ContiguousSet.create(Range.closed(lower, upper), DiscreteDomain.integers()));
        Integer[] rangeNumbers = ArrayUtils.toObject(rangeNumbersPrimitive);

        return new ArrayList<>(Arrays.asList(rangeNumbers));
    }

    private List<Record> getRecordsFromCsv() throws URISyntaxException {
        //read records from CSV file
        String pathToSmallRecordsFile = ExperimentsUtils.getPathToSmallRecordsFile();
        List<Record> records = UtilitiesForBlocksAndRecords.createRecordsFromTestFile(pathToSmallRecordsFile);
        assertThat(records, hasSize(20));
        return records;
    }
}