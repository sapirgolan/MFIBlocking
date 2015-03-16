package fimEntityResolution.entityResulution;

import il.ac.technion.ie.model.CandidatePairs;
import lucene.search.SearchEngine;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;

import static org.mockito.Matchers.eq;


public class JaccardComparisonsTest {

	private JaccardComparisons jaccardComparisons;
	@Before
	public void setUp() throws Exception {
		SearchEngine engine = PowerMockito.mock(SearchEngine.class);
		jaccardComparisons = new JaccardComparisons(engine);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMeasureComparisonExecution() {
		
		CandidatePairs algorithmIds = new CandidatePairs();
		algorithmIds.setPair(1, 2, 0.1);
		algorithmIds.setPair(1, 3, 0.1);
		
		SearchEngine engine = PowerMockito.mock(SearchEngine.class);
		PowerMockito.when(engine.getRecordAttributes(eq("1"))).thenReturn(Arrays.asList("a", "b", "c"));
		PowerMockito.when(engine.getRecordAttributes(eq("2"))).thenReturn(Arrays.asList("a", "d", "e"));
		PowerMockito.when(engine.getRecordAttributes(eq("3"))).thenReturn(Arrays.asList("d", "c", "b"));
		Whitebox.setInternalState(jaccardComparisons, "engine", engine);
		
		long startingTime = System.currentTimeMillis();
		long executionTime = jaccardComparisons.measureComparisonExecution(algorithmIds);
		Assert.assertTrue(" Method didn't execute", startingTime!=executionTime);
		
	}

}
