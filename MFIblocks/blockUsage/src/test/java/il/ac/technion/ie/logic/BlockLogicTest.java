package il.ac.technion.ie.logic;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.CandidatePairs;
import il.ac.technion.ie.model.NeighborsVector;
import il.ac.technion.ie.model.RecordMatches;
import il.ac.technion.ie.search.core.SearchEngine;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.number.IsCloseTo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class BlockLogicTest {

    private BlockLogic classUnderTest;
    private MfiContext context;
    private String recordsFileName = "NoSw.txt";

    @Before
    public void setUp() throws Exception {
        classUnderTest = new BlockLogic();
        context = PowerMockito.mock(MfiContext.class);
        PowerMockito.when(context.getOriginalRecordsPath()).thenReturn(recordsFileName);
    }
    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testBuildNeighborVectors() throws Exception {
        Set<Integer> recordsIds = Sets.newHashSet(1, 2, 3, 4, 5);
        final RecordMatches recordMatches = PowerMockito.mock(RecordMatches.class);

        ConcurrentHashMap matches = PowerMockito.mock(ConcurrentHashMap.class);
        PowerMockito.when(matches.get(Mockito.anyObject())).then(new Answer<RecordMatches>() {
            @Override
            public RecordMatches answer(InvocationOnMock invocation) throws Throwable {
                int id = (int) invocation.getArguments()[0];
                switch (id) {
                    case 1:
                        PowerMockito.when(recordMatches.getMatchedIds()).thenReturn(Sets.newHashSet(2, 3, 5));
                        break;
                    case 2:
                        PowerMockito.when(recordMatches.getMatchedIds()).thenReturn(Sets.newHashSet(1, 3, 4));
                        break;
                    case 3:
                        PowerMockito.when(recordMatches.getMatchedIds()).thenReturn(Sets.newHashSet(1, 2, 4));
                        break;
                    case 4:
                        PowerMockito.when(recordMatches.getMatchedIds()).thenReturn(Sets.newHashSet(2, 3));
                        break;
                    case 5:
                        PowerMockito.when(recordMatches.getMatchedIds()).thenReturn(Sets.newHashSet(1));
                        break;
                }
                return recordMatches;
            }
        });
        List<NeighborsVector> list = Whitebox.invokeMethod(classUnderTest, matches, recordsIds);

        MatcherAssert.assertThat(list.get(0).getNeighbors(), Matchers.containsInAnyOrder(1, 2, 3, 5));
        MatcherAssert.assertThat(list.get(1).getNeighbors(), Matchers.containsInAnyOrder(1, 2, 3, 4));
        MatcherAssert.assertThat(list.get(2).getNeighbors(), Matchers.containsInAnyOrder(1, 2, 3, 4));
        MatcherAssert.assertThat(list.get(3).getNeighbors(), Matchers.containsInAnyOrder(2, 3, 4));
        MatcherAssert.assertThat(list.get(4).getNeighbors(), Matchers.containsInAnyOrder(1, 5));
    }

    private String getRecordsFilePath() {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(recordsFileName).getFile();
    }

    @Test
    public void testCalcProbabilityOnRecords() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(new Block(Arrays.asList(2, 4, 3, 5)));
        blocks.add(new Block(Arrays.asList(1)));

        recordsFileName = "dataset.csv";
        PowerMockito.when(context.getOriginalRecordsPath()).thenReturn(this.getRecordsFilePath());

        classUnderTest.calcProbabilityOnRecords(blocks, context);

        for (Block block : blocks) {
            List<Integer> blockMembers = block.getMembers();
            if (blockMembers.size() == 1) {
                MatcherAssert.assertThat(block.getMemberScore(1), is(1F));
            } else {
                float totalProbsInBlock = 0;
                for (Integer blockMember : blockMembers) {
                    float memberProb = block.getMemberProbability(blockMember);
                    MatcherAssert.assertThat(memberProb, Matchers.allOf(greaterThan(0F), lessThan(1F)));
                    totalProbsInBlock += memberProb;
                }
                MatcherAssert.assertThat((double) totalProbsInBlock, closeTo(1.0F, 0.001));
            }
        }
    }

    @Test
    public void testGetMembersFields() throws Exception {

        List<String> authors = Arrays.asList("p. auer  n. cesa-bianchi  y. freund  and r. e. schapire ",
                "a. blum  m. furst  m. j. kearns  and richard j. lipton.",
                "avrim blum  merrick furst  michael kearns  and richard j. lipton.");

        List<Integer> blockMembers = new ArrayList(Arrays.asList(1, 3, 2));
        recordsFileName = "dataset.csv";
        PowerMockito.when(context.getOriginalRecordsPath()).thenReturn(this.getRecordsFilePath());
        SearchEngine searchEngine = Whitebox.invokeMethod(classUnderTest, "buildSearchEngineForRecords", context);

        //test
        Map<Integer, List<String>> membersFields = Whitebox.invokeMethod(classUnderTest, "getMembersAtributes", blockMembers, searchEngine);
        MatcherAssert.assertThat(membersFields.size(), is(3));


        for (Map.Entry<Integer, List<String>> entry : membersFields.entrySet()) {
            List<String> fields = entry.getValue();
            MatcherAssert.assertThat(fields, Matchers.hasSize(6));
            int authorIndexInList = entry.getKey() - 1;
            MatcherAssert.assertThat(fields, Matchers.hasItem(authors.get(authorIndexInList)));
        }
    }

    @Test
    public void testCalcRecordSimilarityInBlock() throws Exception {
        Integer recordId = 21;
        List<String> mockedList = PowerMockito.mock(List.class);
        Map<Integer, List<String>> map = Maps.newHashMap(ImmutableMap.of(21, mockedList, 22, mockedList));
        SimilarityCalculator similarityCalculator = PowerMockito.mock(SimilarityCalculator.class);

        PowerMockito.when(similarityCalculator.calcRecordsSim(Mockito.anyList(), Mockito.anyList())).thenReturn(1.0F);
        float recordProbability = Whitebox.invokeMethod(classUnderTest, "calcRecordSimilarityInBlock", recordId, map, similarityCalculator);

        MatcherAssert.assertThat(recordProbability, Matchers.is(1.0F));
    }

    @Test
    public void testCalcRecordSimilarityInBlock_manyItems() throws Exception {
        Integer recordId = 21;
        List<String> mockedList = PowerMockito.mock(List.class);
        Map<Integer, List<String>> map = Maps.newHashMap(ImmutableMap.of(21, mockedList,
                22, mockedList, 2, mockedList, 4, mockedList, 1, mockedList));
        SimilarityCalculator similarityCalculator = PowerMockito.mock(SimilarityCalculator.class);

        PowerMockito.when(similarityCalculator.calcRecordsSim(Mockito.anyList(), Mockito.anyList())).thenReturn(0.5F, 0.4F, 0F, 0.6F);
        float recordProbability = Whitebox.invokeMethod(classUnderTest, "calcRecordSimilarityInBlock", recordId, map, similarityCalculator);

        MatcherAssert.assertThat((double) recordProbability, IsCloseTo.closeTo(1.5F, 0.001));
    }

    @Test
    public void testCalcRecordProbabilityInBlock_singleItemInBlock() throws Exception {
        Integer recordId = 21;
        List<String> mockedList = PowerMockito.mock(List.class);
        Map<Integer, List<String>> map = Maps.newHashMap(ImmutableMap.of(77, mockedList));
        SimilarityCalculator similarityCalculator = PowerMockito.mock(SimilarityCalculator.class);

        float recordProbability = Whitebox.invokeMethod(classUnderTest, "calcRecordSimilarityInBlock", recordId, map, similarityCalculator);

        MatcherAssert.assertThat(recordProbability, Matchers.is(1.0F));
    }

    @Test
    public void testFindBlocks_fromTrueMatch() throws Exception {
        List<Integer> recordsIDsBlockOne = Arrays.asList(1160, 1161, 1162, 1163, 1164);
        CandidatePairs pairs = createBlock(recordsIDsBlockOne);

        List<Integer> recordsIDsBlockTwo = Arrays.asList(460, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472);
        pairs.addAll(createBlock(recordsIDsBlockTwo));


        List<Block> blocks = classUnderTest.findBlocks(pairs);

        MatcherAssert.assertThat(blocks, hasSize(2));
        MatcherAssert.assertThat(blocks.get(0).getMembers(), containsInAnyOrder(recordsIDsBlockOne.toArray()));
        MatcherAssert.assertThat(blocks.get(1).getMembers(), containsInAnyOrder(recordsIDsBlockTwo.toArray()));

    }

    private CandidatePairs createBlock(List<Integer> recordsIDs) {
        CandidatePairs candidatePairs = new CandidatePairs();
        for (int i = 0; i < recordsIDs.size(); i++) {
            Integer outer = recordsIDs.get(i);
            for (int j = i; j < recordsIDs.size(); j++) {
                Integer inner = recordsIDs.get(j);
                if (outer != inner) {
                    candidatePairs.setPair(outer, inner, 0);
                }

            }
        }
        return candidatePairs;
    }

	@Test
    public void testFindBlocksOfRecord() throws Exception {
        int searchRecord = 1;
        List<Block> blocks = new ArrayList<>();
        blocks.add(new Block(Arrays.asList(1, 2, 4, 3, 5)));
        blocks.add(new Block(Arrays.asList(5, 1, 7)));
        blocks.add(new Block(Arrays.asList(22, 29, 30)));
        blocks.add(new Block(Arrays.asList(7, 9, 1)));

        List<Block> blocksOfRecord = classUnderTest.findBlocksOfRecord(blocks, searchRecord);
        List<Block> expectedBlocks = new ArrayList<>(blocks);
        expectedBlocks.remove(2);

        MatcherAssert.assertThat(blocksOfRecord, hasSize(3));
        MatcherAssert.assertThat(blocksOfRecord, containsInAnyOrder(expectedBlocks.toArray()));
    }
}
