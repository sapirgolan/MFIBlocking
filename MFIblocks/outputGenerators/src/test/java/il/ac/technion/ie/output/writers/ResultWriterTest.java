package il.ac.technion.ie.output.writers;

import il.ac.technion.ie.model.CandidatePairs;
import il.ac.technion.ie.model.RecordMatches;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;


public class ResultWriterTest {

	private ResultWriter classUnderTest;
	
	@Before
	public void setUp() throws Exception {
		classUnderTest = new ResultWriter();
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateOutputFile() {
		File outputFile = classUnderTest.createNeighborsOutputFile();
		Assert.assertNotNull("Output file was not created", outputFile);
		MatcherAssert.assertThat(outputFile.getAbsolutePath(), Matchers.containsString(System.getProperty("user.dir")));
		
		DateTime dateTime = new DateTime();
		String fileName = outputFile.getName();
		String day = String.valueOf(dateTime.getDayOfMonth());
		String month = String.valueOf(dateTime.getMonthOfYear());
		String year = String.valueOf(dateTime.getYear());
		MatcherAssert.assertThat(fileName, CoreMatchers.allOf(Matchers.containsString(day), Matchers.containsString(month), Matchers.containsString(year)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testWriteSingleBlock() throws IOException {
		File file = new File("result.txt");
		CandidatePairs candidatePairs = Mockito.mock(CandidatePairs.class);
		Mockito.when(candidatePairs.getAllMatches()).thenAnswer(new Answer<ConcurrentHashMap<Integer,RecordMatches>>() {
			//create an answer for getAllMatches() call
			@Override
			public ConcurrentHashMap<Integer, RecordMatches> answer(
					InvocationOnMock invocation) throws Throwable {
				ConcurrentHashMap<Integer, RecordMatches> result = new ConcurrentHashMap<Integer, RecordMatches>();
				RecordMatches recordMatches = new RecordMatches();
				recordMatches.addCandidate(1, 0.2);
				recordMatches.addCandidate(4, 0.3);
				result.put(Integer.valueOf(2), recordMatches);
				return result;
			}
		});

		//classUnderTest.writeBlocksIDs(file, candidatePairs);
		classUnderTest.writeEachRecordNeighbors(file, candidatePairs);
		String fileContent = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		MatcherAssert.assertThat(fileContent, CoreMatchers.allOf(Matchers.containsString("1"), Matchers.containsString("4"), Matchers.containsString("2")));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testWriteSeveralBlocks() throws IOException{
		File file = new File("result.txt");
		CandidatePairs candidatePairs = Mockito.mock(CandidatePairs.class);
		Mockito.when(candidatePairs.getAllMatches()).thenAnswer(new Answer<ConcurrentHashMap<Integer,RecordMatches>>() {
			//create an answer for getAllMatches() call
			@Override
			public ConcurrentHashMap<Integer, RecordMatches> answer(
					InvocationOnMock invocation) throws Throwable {
				ConcurrentHashMap<Integer, RecordMatches> result = new ConcurrentHashMap<Integer, RecordMatches>();
				RecordMatches recordMatchesOne = new RecordMatches();
				recordMatchesOne.addCandidate(1, 0.2);
				recordMatchesOne.addCandidate(4, 0.3);
				result.put(Integer.valueOf(2), recordMatchesOne);
				RecordMatches recordMatchesTwo = new RecordMatches();
				recordMatchesTwo.addCandidate(11, 0.7);
				recordMatchesTwo.addCandidate(14, 0.6);
				recordMatchesTwo.addCandidate(16, 0.9);
				result.put(Integer.valueOf(90), recordMatchesTwo);
				return result;
			}
		});
		
		//classUnderTest.writeBlocksIDs(file, candidatePairs);
		classUnderTest.writeEachRecordNeighbors(file, candidatePairs);
		String fileContent = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		MatcherAssert.assertThat(fileContent, CoreMatchers.allOf(Matchers.containsString("2 - [1, 4]"), Matchers.containsString("90 - [16, 14, 11]")));
	}
	
	@Test
	public void testWriteSkipEmptyBlocks() throws IOException{
		File file = new File("result.txt");
		CandidatePairs candidatePairs = Mockito.mock(CandidatePairs.class);
		Mockito.when(candidatePairs.getAllMatches()).thenAnswer(new Answer<ConcurrentHashMap<Integer,RecordMatches>>() {
			//create an answer for getAllMatches() call
			@Override
			public ConcurrentHashMap<Integer, RecordMatches> answer(
					InvocationOnMock invocation) throws Throwable {
				ConcurrentHashMap<Integer, RecordMatches> result = new ConcurrentHashMap<Integer, RecordMatches>();
				RecordMatches recordMatchesOne = new RecordMatches();
				recordMatchesOne.addCandidate(1, 0.2);
				recordMatchesOne.addCandidate(4, 0.3);
				result.put(Integer.valueOf(2), recordMatchesOne);
				RecordMatches recordMatchesTwo = new RecordMatches();
				result.put(Integer.valueOf(90), recordMatchesTwo);
				return result;
			}
		});
		
		//classUnderTest.writeBlocksIDs(file, candidatePairs);
		classUnderTest.writeEachRecordNeighbors(file, candidatePairs);
		String fileContent = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		MatcherAssert.assertThat(fileContent, Matchers.containsString("2 - [1, 4]"));
		MatcherAssert.assertThat(fileContent, CoreMatchers.not(Matchers.containsString("90")));
	}
	
	private String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

}
