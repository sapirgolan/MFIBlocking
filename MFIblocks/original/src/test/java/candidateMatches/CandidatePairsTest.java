package candidateMatches;

import il.ac.technion.ie.model.CandidatePairs;
import il.ac.technion.ie.model.RecordMatches;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class CandidatePairsTest {

	CandidatePairs candidatePairs ;
	CandidatePairs candidatePairsSpy ;
	
	@Before
	public void setUp() throws Exception {
		candidatePairs = new CandidatePairs();
		candidatePairsSpy = spy(candidatePairs);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Ignore
	public void testCalcTrueAndFalsePositives_Simple() {
		CandidatePairs actualCPs = mock(CandidatePairs.class);
		doAnswer(new Answer<Object>() {
			@Override
			public Set<Entry<Integer, RecordMatches>> answer(InvocationOnMock invocation) throws Throwable {
				Map<Integer, RecordMatches> map = new HashMap<Integer, RecordMatches>();
				RecordMatches recordMatches = new RecordMatches(5);
				recordMatches.addCandidate(7, 0.4);
				recordMatches.addCandidate(2, 0.3);
				map.put(9, recordMatches);
				return map.entrySet();
			}
		}).when(actualCPs).getAllMatchedEntries();
		
		
		CandidatePairs trueCPs = spy(new CandidatePairs(1));
		trueCPs.setPair(9, 7, 0.4);
		
		long[] calcTrueAndFalsePositives = candidatePairsSpy.calcTrueAndFalsePositives(actualCPs);
		assertThat(1l, is(calcTrueAndFalsePositives[0]) );
		assertThat(1l, is(calcTrueAndFalsePositives[1]) );
//		assertThat("Number of True Positive should be 1", 1==calcTrueAndFalsePositives[0]);
//		assertThat("Number of False Positive should be 1", 1==calcTrueAndFalsePositives[1]);
	}
	
	@Test
	@Ignore
	public void testCalcTrueAndFalsePositives_clustersNotSemetric() {
		
		CandidatePairs actualCPs = spy(new CandidatePairs(2));
		//creating cluster {9 - [7, 2]}
		//creating cluster {7 - [9]}
		//creating cluster {2 - [9]} - will be updated later
		actualCPs.setPair(9, 7, 0.3);
		actualCPs.setPair(9, 2, 0.3);
		
		//creating cluster {5 - [2]}
		//creating cluster {4 - [2]}
		//updating cluster of 2 and remove 9 from it so now it will be: {2 - [5, 4]}
		actualCPs.setPair(2, 5, 0.4);
		actualCPs.setPair(2, 4, 0.7);
		
		
		CandidatePairs trueCPs = spy(new CandidatePairs(1));
		trueCPs.setPair(9, 2, 0.3);
		
		long[] calcTrueAndFalsePositives = candidatePairsSpy.calcTrueAndFalsePositives( actualCPs);
		assertThat(1l, is(calcTrueAndFalsePositives[0]) );
		assertThat(6l, is(calcTrueAndFalsePositives[1]) );
	}

}
