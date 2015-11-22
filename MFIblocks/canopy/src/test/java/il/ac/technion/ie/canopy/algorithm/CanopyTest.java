package il.ac.technion.ie.canopy.algorithm;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import il.ac.technion.ie.model.Record;
import org.apache.lucene.index.IndexWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CanopyTest {

    public static final int NUMBER_OF_RECORDS_IN_BIG_FILE = 1000;
    @Spy
    private CanopyInteraction canopyInteraction = new CanopyInteraction();

    private Canopy classUnderTest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = CanopyParametersException.class)
    public void testConstractorT2SmallerThanT1() throws Exception {
        new Canopy(PowerMockito.mock(List.class), 0.6, 0.4);
    }

    @Test
    public void testVerifyAllRecordsAreAdded() throws Exception {
        String pathToBigRecordsFile = ExperimentsUtils.getPathToBigRecordsFile();
        List<Record> records = readRecordsFromTestFile(pathToBigRecordsFile);
        classUnderTest = new Canopy(records, 0.3, 0.6);
        classUnderTest.initSearchEngine(canopyInteraction);
        Mockito.verify(canopyInteraction, Mockito.times(NUMBER_OF_RECORDS_IN_BIG_FILE)).addDoc(Mockito.any(IndexWriter.class), Mockito.anyString(), Mockito.anyString());
    }

    private List<Record> readRecordsFromTestFile(String pathToBigRecordsFile) {
        List<String[]> strings = ExperimentsUtils.readRecordsFromTestFile(pathToBigRecordsFile);
        List<Record> records = new ArrayList<>(strings.size());
        List<String> fieldNames = convertArrayToList(strings.get(0));
        for (int i = 1; i < strings.size(); i++) { //skipping first element since it is the field names
            List<String> values = convertArrayToList(strings.get(i));
            Record record = new Record(fieldNames, values, i);
            records.add(record);
        }
        return records;

    }

    private List<String> convertArrayToList(String[] array) {
        List<String> list = new ArrayList<>(Arrays.asList(array));
        list.remove(0);
        return list;
    }

}