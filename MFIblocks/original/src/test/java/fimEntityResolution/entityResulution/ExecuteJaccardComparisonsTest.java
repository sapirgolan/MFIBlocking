package fimEntityResolution.entityResulution;

import static org.mockito.Mockito.spy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import candidateMatches.CandidatePairs;

public class ExecuteJaccardComparisonsTest {

	private ExecuteJaccardComparisons jaccardComparisons;
	private ExecuteJaccardComparisons spyJaccardComparisons ;
	@Before
	public void setUp() throws Exception {
		jaccardComparisons = new ExecuteJaccardComparisons();
		spyJaccardComparisons = spy(jaccardComparisons);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMeasureComparisonExecution() {
		CandidatePairs trueIds = new CandidatePairs();
		trueIds.setPair(1, 2, 0.1);
		trueIds.setPair(1, 3, 0.1);
		
		CandidatePairs algorithmIds = new CandidatePairs();
		algorithmIds.setPair(1, 2, 0.1);
		algorithmIds.setPair(1, 3, 0.1);
		
		long startingTime = System.currentTimeMillis();
		long executionTime = spyJaccardComparisons.measureComparisonExecution(trueIds, algorithmIds);
		Assert.assertTrue(" Method didn't execute", startingTime!=executionTime);
		
	}

}
