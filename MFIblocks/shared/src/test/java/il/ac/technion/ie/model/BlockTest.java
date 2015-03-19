package il.ac.technion.ie.model;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class BlockTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testEquals_true() throws Exception {
        Block blockOne = new Block(Arrays.asList(1,2,3));
        Block blockTwo = new Block(Arrays.asList(1,2,3));
        MatcherAssert.assertThat(blockOne, Is.is(IsEqual.equalTo(blockTwo)));
    }

    @Test
    public void testEquals_sameDifferentOrder() throws Exception {
        Block blockOne = new Block(Arrays.asList(1,2,3));
        Block blockTwo = new Block(Arrays.asList(2,3,1));
        MatcherAssert.assertThat(blockOne, Is.is(IsEqual.equalTo(blockTwo)));
    }

    @Test
    public void testEquals_secondSmaller() throws Exception {
        Block blockOne = new Block(Arrays.asList(1,2,3));
        Block blockTwo = new Block(Arrays.asList(1,2));
        MatcherAssert.assertThat(blockOne, IsNot.not(IsEqual.equalTo(blockTwo)));
    }

    @Test
    public void testEquals_secondBigger() throws Exception {
        Block blockOne = new Block(Arrays.asList(1,2));
        Block blockTwo = new Block(Arrays.asList(1,2,3));
        MatcherAssert.assertThat(blockOne, IsNot.not(IsEqual.equalTo(blockTwo)));
    }

    @Test
    public void testEquals_firstAndLastMembersAreTheSame() throws Exception {
        Block blockOne = new Block(Arrays.asList(1,7,220,2));
        Block blockTwo = new Block(Arrays.asList(1,9,2));
        MatcherAssert.assertThat(blockOne, IsNot.not(IsEqual.equalTo(blockTwo)));
    }
}