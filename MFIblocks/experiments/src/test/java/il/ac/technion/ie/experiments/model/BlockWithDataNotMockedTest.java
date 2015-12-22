package il.ac.technion.ie.experiments.model;

import com.google.common.collect.Lists;
import il.ac.technion.ie.model.Record;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mock;

public class BlockWithDataNotMockedTest {

    private BlockWithData classUnderTest;


    @Test
    public void testFindBlockRepresentativesAfterProbChange() throws Exception {
        //prepare
        Record recordOne = mock(Record.class);
        Record recordTwo = mock(Record.class);
        Record recordThree = mock(Record.class);
        classUnderTest = new BlockWithData(Lists.newArrayList(recordOne, recordTwo, recordThree));
        classUnderTest.setMemberProbability(recordOne, (float) 0.4);
        classUnderTest.setMemberProbability(recordTwo, (float) 0.4);
        classUnderTest.setMemberProbability(recordThree, (float) 0.2);

        //first execution
        Map<Record, Float> blockRepresentatives = classUnderTest.findBlockRepresentatives();
        //first assertion
        assertThat(blockRepresentatives.size(), is(2));
        assertThat(blockRepresentatives, hasKey(recordOne));
        assertThat(blockRepresentatives, hasKey(recordTwo));

        //prepare for second execution
        classUnderTest.setMemberProbability(recordOne, (float) 0.5);
        classUnderTest.setMemberProbability(recordTwo, (float) 0.3);
        //second execution
        blockRepresentatives = classUnderTest.findBlockRepresentatives();
        //second assertion
        assertThat(blockRepresentatives.size(), is(2));
        assertThat(blockRepresentatives, hasKey(recordOne));
        assertThat(blockRepresentatives, hasKey(recordTwo));

        //third execution
        blockRepresentatives = classUnderTest.reFindBlockRepresentatives();
        //third assertion
        assertThat(blockRepresentatives.size(), is(1));
        assertThat(blockRepresentatives, hasKey(recordOne));
        assertThat(blockRepresentatives, not(hasKey(recordTwo)));
    }
}