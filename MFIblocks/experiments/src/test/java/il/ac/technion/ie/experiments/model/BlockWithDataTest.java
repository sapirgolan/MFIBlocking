package il.ac.technion.ie.experiments.model;

import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.model.Record;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BlockWithData.class)
public class BlockWithDataTest {

    @InjectMocks
    private BlockWithData classUnderTest;

    @Mock
    private Record trueRepresentative;

    @Spy
    private List<Record> members = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        PowerMockito.suppress(PowerMockito.constructor(BlockWithData.class));
        classUnderTest = PowerMockito.spy(new BlockWithData(new ArrayList<Record>()));

        MockitoAnnotations.initMocks(this);
    }

    @Test (expected = SizeNotEqualException.class)
    public void testReplaceMembers_throwSizeNotEqualException() throws Exception {
        List newMembers = PowerMockito.mock(List.class);
        PowerMockito.when(newMembers.size()).thenReturn(9);

        classUnderTest.replaceMembers(newMembers);
    }

    @Test
    public void testReplaceMembers() throws Exception {
        //create Existing members
        List<Record> existingMembers = createMockRecords(6, Record.class);
        Record oldRepresentative = PowerMockito.mock(Record.class);
        PowerMockito.when(oldRepresentative.getRecordID()).thenReturn("org");
        existingMembers.add(oldRepresentative);
        List<Record> copyOfExistingMembers = new ArrayList<>(existingMembers);
        Whitebox.setInternalState(classUnderTest, "members", existingMembers);
        //set one member as representative
        Whitebox.setInternalState(classUnderTest, "trueRepresentative", oldRepresentative);

        //create new members
        List<RecordSplit> newMembers = createMockRecords(6, RecordSplit.class);
        RecordSplit newRepresentative = PowerMockito.mock(RecordSplit.class);
        PowerMockito.when(newRepresentative.getRecordID()).thenReturn("org");
        newMembers.add(newRepresentative);
        Collections.shuffle(newMembers);

        classUnderTest.replaceMembers(newMembers);

        MatcherAssert.assertThat(Collections.disjoint(classUnderTest.getMembers(), copyOfExistingMembers), Matchers.is(true));
        MatcherAssert.assertThat(classUnderTest.getTrueRepresentative(), Matchers.isIn(classUnderTest.getMembers()));

    }

    private <T extends Record> List<T> createMockRecords(int numberOfMembers, Class aClass) {

        List<T> records = new ArrayList<>();
        for (int i = 0; i < numberOfMembers; i++) {
            T record = (T) PowerMockito.mock(aClass);
            PowerMockito.when(record.getRecordID()).thenReturn(String.valueOf(i));
            records.add(record);
        }
        return records;
    }
}