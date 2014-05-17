package fimEntityResolution.comparison;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProfileComparisonTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Set<Integer> trueMatchedIds = new HashSet<Integer>(Arrays.asList(1,2,3));
		Set<Integer> algorithmMatchedIds = new HashSet<Integer>(Arrays.asList(3,4,5,6));
		
		double jaccardSimilarity = ProfileComparison.getJaccardSimilarity(trueMatchedIds, algorithmMatchedIds);
		Assert.assertEquals("jaccardSimilarity didn't find single simillarity", (double)1/6, jaccardSimilarity, 0);
	}

}
