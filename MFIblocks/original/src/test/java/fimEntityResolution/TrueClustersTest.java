package fimEntityResolution;

import il.ac.technion.ie.model.CandidateMatch;
import il.ac.technion.ie.model.RecordMatches;
import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest(TrueClusters.class)
public class TrueClustersTest {
	
	@InjectMocks
	private TrueClusters classUnderTest;
	
	@Before
	public void setUp() throws Exception {
		classUnderTest = new TrueClusters();
		MockitoAnnotations.initMocks(this);
	}
	
	
	@Test
	public void tetstAssosiateRecordsAsMatching() throws Exception {
		List<Integer> clusterMembers = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7,8,9));
		Whitebox.invokeMethod(classUnderTest, "assosiateRecordsAsMatching", clusterMembers);
		Assert.assertEquals("Verify number of pairs", 36, classUnderTest.getCardinality());
		
		ConcurrentHashMap<Integer,RecordMatches> allMatches = classUnderTest.getGroundTruthCandidatePairs().getAllMatches();
		Set<Integer> keySet = allMatches.keySet();
		Assert.assertEquals("Verify there is an entry for each record", 9, keySet.size());
		
		for (Integer recordKey : keySet) {
			Collection<CandidateMatch> recordsThatWereAssignedWithKey = allMatches.get(recordKey).getCandidateMatches();
			clusterMembers.remove(recordKey);
			for (CandidateMatch candidateMatch : recordsThatWereAssignedWithKey) {
				assertThat(candidateMatch.getRecordId(), isIn(clusterMembers));
			}
			clusterMembers.add(recordKey);
		}
	}
	
	@Test
	public void test_noSingleToneClusterAreCreated() throws IOException {
		Logger mockedLogger = PowerMockito.mock(Logger.class);
		Whitebox.setInternalState(classUnderTest.getClass(), "logger", mockedLogger);
		
		List<String> clusterMembers = new ArrayList<>(Arrays.asList("1 2 3 4","2 5 6"));
		
		PowerMock.mockStatic(Files.class);
		EasyMock.expect(Files.readAllLines(EasyMock.anyObject(Path.class), EasyMock.anyObject(Charset.class))).andReturn(clusterMembers);
		PowerMock.replayAll();
		
		classUnderTest.findClustersAssingments("");
		Mockito.verify(mockedLogger, Mockito.never()).fatal(Mockito.anyString());
	}
	
	
	
	@Test
	public void testFindClustersAssingments() throws Exception {
		List<String> clusterMembers = new ArrayList<>(Arrays.asList("1 2 3 4","2 5 6"));
		
		PowerMock.mockStatic(Files.class);
		EasyMock.expect(Files.readAllLines(EasyMock.anyObject(Path.class), EasyMock.anyObject(Charset.class))).andReturn(clusterMembers);
		PowerMock.replayAll();
		
		classUnderTest.findClustersAssingments("");
		
		ConcurrentHashMap<Integer,RecordMatches> allMatches = classUnderTest.getGroundTruthCandidatePairs().getAllMatches();
		
		//assert cluster members of 2
		Collection<CandidateMatch> clusterMembersRecieved = allMatches.get(2).getCandidateMatches();
		ArrayList<Integer> clusterMembersOfTwo = new ArrayList<Integer>();
		for (CandidateMatch candidateMatch : clusterMembersRecieved) {
			assertThat("True cluster assignment has a score", candidateMatch.getScore(), is(0.0));
			clusterMembersOfTwo.add(candidateMatch.getRecordId());
		}
		assertThat("not all cluster Members were added", clusterMembersOfTwo, containsInAnyOrder(1,3,4,5,6));
		
		//assert cluster members of 6
		clusterMembersRecieved = allMatches.get(6).getCandidateMatches();
		ArrayList<Integer> clusterMembersOfSix = new ArrayList<Integer>();
		for (CandidateMatch candidateMatch : clusterMembersRecieved) {
			assertThat("True cluster assignment has a score", candidateMatch.getScore(), is(0.0));
			clusterMembersOfSix.add(candidateMatch.getRecordId());
		}
		assertThat("not all cluster Members were added", clusterMembersOfSix, containsInAnyOrder(5,2));
	}

}
