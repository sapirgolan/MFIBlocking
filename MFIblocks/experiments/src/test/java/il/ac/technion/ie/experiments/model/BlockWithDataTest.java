package il.ac.technion.ie.experiments.model;

import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.model.Record;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.when;

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
        when(newMembers.size()).thenReturn(9);

        classUnderTest.replaceMembers(newMembers);
    }

    @Test
    public void testReplaceMembers() throws Exception {
        //create Existing members
        List<Record> existingMembers = createMockRecords(6, Record.class);
        Record oldRepresentative = PowerMockito.mock(Record.class);
        when(oldRepresentative.getRecordID()).thenReturn(77);
        when(oldRepresentative.getRecordName()).thenReturn("org");
        existingMembers.add(oldRepresentative);
        List<Record> copyOfExistingMembers = new ArrayList<>(existingMembers);

        //set internal members
        Whitebox.setInternalState(classUnderTest, "members", existingMembers);
        //set one member as representative
        Whitebox.setInternalState(classUnderTest, "trueRepresentative", oldRepresentative);

        //create new members
        List<RecordSplit> newMembers = createMockRecords(6, RecordSplit.class);
        RecordSplit newRepresentative = PowerMockito.mock(RecordSplit.class);
        when(newRepresentative.getRecordID()).thenReturn(77);
        when(newRepresentative.getRecordName()).thenReturn("org");
        newMembers.add(newRepresentative);
        Collections.shuffle(newMembers);

        classUnderTest.replaceMembers(newMembers);

        assertThat(classUnderTest.getMembers(), not(containsInAnyOrder(copyOfExistingMembers.toArray(new Record[copyOfExistingMembers.size()]))));
        assertThat(classUnderTest.getTrueRepresentative(), Matchers.isIn(classUnderTest.getMembers()));

    }

    private <T extends Record> List<T> createMockRecords(int numberOfMembers, Class aClass) {

        List<T> records = new ArrayList<>();
        for (int i = 0; i < numberOfMembers; i++) {
            T record = (T) PowerMockito.mock(aClass);
            when(record.getRecordID()).thenReturn(i);
            records.add(record);
        }
        return records;
    }
}