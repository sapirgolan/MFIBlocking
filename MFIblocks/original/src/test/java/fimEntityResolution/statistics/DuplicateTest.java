package fimEntityResolution.statistics;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import candidateMatches.RecordMatches;

public class DuplicateTest {

	private Duplicate classUnderTest;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWasDuplicateEntityDetected_OnePositiveIteration() {
		classUnderTest = new Duplicate(Arrays.asList(1,2,3));
		
		RecordMatches recordMatchesMocked = mock(RecordMatches.class);
		when(recordMatchesMocked.getMatchedIds()).then(new Answer<Object>() {
			@Override
			public Set<Integer> answer(InvocationOnMock invocation) throws Throwable {
				HashSet<Integer> set = new HashSet<Integer>();
				set.add(2);
				set.add(3);
				return set;
			}
		});
		
		classUnderTest.decideIfDetected(recordMatchesMocked, 1);
		assertThat(true, is(classUnderTest.wasDuplicateDetected()));
	}
	
	@Test
	public void testWasDuplicateEntityDetected_OneNegativeIteration() {
		classUnderTest = new Duplicate(Arrays.asList(1,2,3));
		
		RecordMatches recordMatchesMocked = mock(RecordMatches.class);
		when(recordMatchesMocked.getMatchedIds()).then(new Answer<Object>() {
			@Override
			public Set<Integer> answer(InvocationOnMock invocation) throws Throwable {
				HashSet<Integer> set = new HashSet<Integer>();
				set.add(1);
				set.add(2);
				return set;
			}
		});
		
		classUnderTest.decideIfDetected(recordMatchesMocked,4);
		assertThat(false, is(classUnderTest.wasDuplicateDetected()));
	}
	
	@Test
	public void testWasDuplicateEntityDetected_SeveralIterations() {
		classUnderTest = new Duplicate(Arrays.asList(1,2,3));
		RecordMatches recordMatchesToIdOne = mock(RecordMatches.class);
		when(recordMatchesToIdOne.getMatchedIds()).then(new Answer<Object>() {
			@Override
			public Set<Integer> answer(InvocationOnMock invocation) throws Throwable {
				HashSet<Integer> set = new HashSet<Integer>();
				set.add(1);
				set.add(2);
				return set;
			}
		});
		
		RecordMatches recordMatchesToIdTwo = mock(RecordMatches.class);
		when(recordMatchesToIdTwo.getMatchedIds()).then(new Answer<Object>() {
			@Override
			public Set<Integer> answer(InvocationOnMock invocation) throws Throwable {
				HashSet<Integer> set = new HashSet<Integer>();
				set.add(1);
				return set;
			}
		});
		
		RecordMatches recordMatchesToIdThree = mock(RecordMatches.class);
		when(recordMatchesToIdThree.getMatchedIds()).then(new Answer<Object>() {
			@Override
			public Set<Integer> answer(InvocationOnMock invocation) throws Throwable {
				HashSet<Integer> set = new HashSet<Integer>();
				set.add(1);
				set.add(2);
				return set;
			}
		});
		
		classUnderTest.decideIfDetected(recordMatchesToIdOne,1);
		classUnderTest.decideIfDetected(recordMatchesToIdTwo,2);
		classUnderTest.decideIfDetected(recordMatchesToIdThree,3);

		assertThat(true, is(classUnderTest.wasDuplicateDetected()));
	}
	
	@Test
	public void testWasDuplicateEntityDetected_SeveralIterations_OneDetection() {
		classUnderTest = new Duplicate(Arrays.asList(1,2,3));
		RecordMatches recordMatchesToIdOne = mock(RecordMatches.class);
		when(recordMatchesToIdOne.getMatchedIds()).then(new Answer<Object>() {
			@Override
			public Set<Integer> answer(InvocationOnMock invocation) throws Throwable {
				HashSet<Integer> set = new HashSet<Integer>();
				set.add(1);
				set.add(5);
				return set;
			}
		});
		
		RecordMatches recordMatchesToIdTwo = mock(RecordMatches.class);
		when(recordMatchesToIdTwo.getMatchedIds()).then(new Answer<Object>() {
			@Override
			public Set<Integer> answer(InvocationOnMock invocation) throws Throwable {
				HashSet<Integer> set = new HashSet<Integer>();
				set.add(1);
				return set;
			}
		});
		
		RecordMatches recordMatchesToIdThree = mock(RecordMatches.class);
		when(recordMatchesToIdThree.getMatchedIds()).then(new Answer<Object>() {
			@Override
			public Set<Integer> answer(InvocationOnMock invocation) throws Throwable {
				HashSet<Integer> set = new HashSet<Integer>();
				set.add(1);
				set.add(2);
				return set;
			}
		});
		
		classUnderTest.decideIfDetected(recordMatchesToIdOne,1);
		classUnderTest.decideIfDetected(recordMatchesToIdTwo,2);
		classUnderTest.decideIfDetected(recordMatchesToIdThree,3);

		assertThat(true, is(classUnderTest.wasDuplicateDetected()));
	}
	
}
