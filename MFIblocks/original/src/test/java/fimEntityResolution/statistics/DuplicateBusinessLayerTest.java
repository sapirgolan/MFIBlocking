package fimEntityResolution.statistics;

import il.ac.technion.ie.model.CandidatePairs;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class DuplicateBusinessLayerTest {

	private DuplicateBusinessLayer classUnderTest;
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetNumberOfDuplicates_OneDuplicate() {
		CandidatePairs groundTruth = new CandidatePairs(2);
		groundTruth.setPair(1, 2, 0.1);
		
		CandidatePairs algorithmOutput = new CandidatePairs(2);
		classUnderTest = new DuplicateBusinessLayer(groundTruth, algorithmOutput);
		assertThat("Didn't find 1 duplicate", 1, is(classUnderTest.getNumberOfDuplicatesInDataset()));
	}
	
	@Test
	public void testGetNumberOfDuplicates_OneDuplicatesWithThreeEntities() {
		CandidatePairs groundTruth = new CandidatePairs(3);
		//This is the same entity
		groundTruth.setPair(1, 2, 0.1);
		groundTruth.setPair(2, 3, 0.3);
		
		CandidatePairs algorithmOutput = new CandidatePairs(2);
		classUnderTest = new DuplicateBusinessLayer(groundTruth, algorithmOutput);
		Assert.assertEquals(1, classUnderTest.getNumberOfDuplicatesInDataset());
	}
	
	@Test
	public void testGetNumberOfDuplicates_TwoDuplicates() {
		CandidatePairs groundTruth = new CandidatePairs(3);
		//This is the same entity
		groundTruth.setPair(1, 2, 0.1);
		groundTruth.setPair(5, 4, 0.3);
		
		CandidatePairs algorithmOutput = new CandidatePairs(2);
		classUnderTest = new DuplicateBusinessLayer(groundTruth, algorithmOutput);
		Assert.assertEquals(2, classUnderTest.getNumberOfDuplicatesInDataset());
	}

	@Test 
	public void testGetNumberOfDuplicatesFound_OneDuplicate() {
		CandidatePairs groundTruth = new CandidatePairs(2);
		//This is the same entity
		groundTruth.setPair(1, 2, 0.1);
		
		CandidatePairs algorithmOutput = new CandidatePairs(2);
		algorithmOutput.setPair(1, 2, 0.1);
		
		classUnderTest = new DuplicateBusinessLayer(groundTruth, algorithmOutput);
		Assert.assertEquals(1, classUnderTest.getNumberOfDuplicatesFound() );
	}
	
	@Test 
	public void testGetNumberOfDuplicatesFound_NoDuplicate() {
		CandidatePairs groundTruth = new CandidatePairs(2);
		//This is the same entity
		groundTruth.setPair(1, 2, 0.1);
		
		CandidatePairs algorithmOutput = new CandidatePairs(2);
		algorithmOutput.setPair(1, 3, 0.1);
		
		classUnderTest = new DuplicateBusinessLayer(groundTruth, algorithmOutput);
		Assert.assertEquals(0, classUnderTest.getNumberOfDuplicatesFound() );
	}
	

	@Test 
	public void testGetNumberOfDuplicatesFound_OneDuplicateManyNot() {
		CandidatePairs groundTruth = new CandidatePairs(2);
		//This is the same entity
		groundTruth.setPair(1, 2, 0.1);
		
		CandidatePairs algorithmOutput = new CandidatePairs(3);
		algorithmOutput.setPair(1, 3, 0.1);
		algorithmOutput.setPair(4, 2, 0.4);
		algorithmOutput.setPair(11, 20, 0.9);
		algorithmOutput.setPair(2, 1, 0.1);
		
		classUnderTest = new DuplicateBusinessLayer(groundTruth, algorithmOutput);
		Assert.assertEquals(1, classUnderTest.getNumberOfDuplicatesFound() );
	}
	
	@Test 
	public void testGetNumberOfDuplicatesFound_OneDuplicates_OutOfSize() {
		CandidatePairs groundTruth = new CandidatePairs(2);
		//This is the same entity
		groundTruth.setPair(1, 2, 0.1);
		
		CandidatePairs algorithmOutput = new CandidatePairs(3);
		algorithmOutput.setPair(1, 3, 0.2);
		algorithmOutput.setPair(4, 2, 0.4);
		algorithmOutput.setPair(11, 20, 0.9);
		algorithmOutput.setPair(2, 1, 0.1);
		algorithmOutput.setPair(1, 90, 0.7);
		algorithmOutput.setPair(1, 15, 0.7);
		
		classUnderTest = new DuplicateBusinessLayer(groundTruth, algorithmOutput);
		Assert.assertEquals(1, classUnderTest.getNumberOfDuplicatesFound() );
	}
	
	
}
