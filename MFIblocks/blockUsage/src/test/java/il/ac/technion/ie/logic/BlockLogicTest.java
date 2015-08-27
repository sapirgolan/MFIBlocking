package il.ac.technion.ie.logic;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.lprobability.SimilarityCalculator;
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
        blocks.add(new Block(Arrays.asList(2, 4, 3, 5), Block.RANDOM_ID));
        blocks.add(new Block(Arrays.asList(1), Block.RANDOM_ID));

        recordsFileName = "dataset.csv";
        PowerMockito.when(context.getOriginalRecordsPath()).thenReturn(this.getRecordsFilePath());
        PowerMockito.when(context.getDatasetName()).thenReturn("cora");

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
        PowerMockito.when(context.getDatasetName()).thenReturn("cora");
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
    public void testFindBlocks_fromTrueMatch_TwoBlocks() throws Exception {
        List<Integer> recordsIDsBlockOne = Arrays.asList(1, 2, 3, 4, 5);
        CandidatePairs pairs = createBlock(recordsIDsBlockOne);

        List<Integer> recordsIDsBlockTwo = Arrays.asList(6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18);
        pairs.addAll(createBlock(recordsIDsBlockTwo));

        List<Block> blocks = classUnderTest.findBlocks(pairs, 18);

        MatcherAssert.assertThat(blocks, hasSize(2));
        MatcherAssert.assertThat(blocks.get(0).getMembers(), containsInAnyOrder(recordsIDsBlockOne.toArray()));
        MatcherAssert.assertThat(blocks.get(1).getMembers(), containsInAnyOrder(recordsIDsBlockTwo.toArray()));

    }

    @Test
    public void testFindBlocks_fromTrueMatch_OneBlock() throws Exception {
        List<Integer> recordsIDsBlockOne = Arrays.asList(1, 2);
        CandidatePairs pairs = createBlock(recordsIDsBlockOne);
        MatcherAssert.assertThat(pairs.getAllMatches().size(), is(2));

        List<Block> blocks = classUnderTest.findBlocks(pairs, 2);

        MatcherAssert.assertThat(blocks, hasSize(1));
        MatcherAssert.assertThat(blocks.get(0).getMembers(), containsInAnyOrder(recordsIDsBlockOne.toArray()));
    }


    @Test
    public void testFindBlocks_fromTrueMatch_severalBlocks_sameSize() throws Exception {
        List<Integer> recordsIDsBlockOne = Arrays.asList(1, 2);
        List<Integer> recordsIDsBlockTwo = Arrays.asList(3, 4);
        List<Integer> recordsIDsBlockThree = Arrays.asList(5, 6);
        List<Integer> recordsIDsBlockFour = Arrays.asList(7, 8);

        CandidatePairs pairs = createBlock(recordsIDsBlockOne);
        pairs.addAll(createBlock(recordsIDsBlockTwo));
        pairs.addAll(createBlock(recordsIDsBlockThree));
        pairs.addAll(createBlock(recordsIDsBlockFour));

        List<Block> blocks = classUnderTest.findBlocks(pairs, 8);

        MatcherAssert.assertThat(blocks, hasSize(4));
        MatcherAssert.assertThat(blocks.get(0).getMembers(), containsInAnyOrder(recordsIDsBlockOne.toArray()));
        MatcherAssert.assertThat(blocks.get(1).getMembers(), containsInAnyOrder(recordsIDsBlockTwo.toArray()));
        MatcherAssert.assertThat(blocks.get(2).getMembers(), containsInAnyOrder(recordsIDsBlockThree.toArray()));
        MatcherAssert.assertThat(blocks.get(3).getMembers(), containsInAnyOrder(recordsIDsBlockFour.toArray()));
    }

    /**
     * The real algorithm creates two CandidatePairs for each pair.
     * For the block that holds records 180 & 181 following pairs will be created {180,181} and {181,180}
     * @param recordsIDs
     * @return
     */
    private CandidatePairs createBlock(List<Integer> recordsIDs) {
        CandidatePairs candidatePairs = new CandidatePairs();
        for (int i = 0; i < recordsIDs.size(); i++) {
            Integer outer = recordsIDs.get(i);
            for (int j = 0; j < recordsIDs.size(); j++) {
                Integer inner = recordsIDs.get(j);
                candidatePairs.setPair(outer, inner, 0);
            }
        }
        return candidatePairs;
    }

	@Test
    public void testFindBlocksOfRecord() throws Exception {
        int searchRecord = 1;
        List<Block> blocks = new ArrayList<>();
        blocks.add(new Block(Arrays.asList(1, 2, 4, 3, 5), Block.RANDOM_ID));
        blocks.add(new Block(Arrays.asList(5, 1, 7), Block.RANDOM_ID));
        blocks.add(new Block(Arrays.asList(22, 29, 30), Block.RANDOM_ID));
        blocks.add(new Block(Arrays.asList(7, 9, 1), Block.RANDOM_ID));

        List<Block> blocksOfRecord = classUnderTest.findBlocksOfRecord(blocks, searchRecord);
        List<Block> expectedBlocks = new ArrayList<>(blocks);
        expectedBlocks.remove(2);

        MatcherAssert.assertThat(blocksOfRecord, hasSize(3));
        MatcherAssert.assertThat(blocksOfRecord, containsInAnyOrder(expectedBlocks.toArray()));
    }

    @Test
    public void testFindMissingRecordsFromBlocks_inBetween() throws Exception {
        List<Block> blocks = new ArrayList<>();
        blocks.add(new Block(Arrays.asList(1, 2, 4, 3, 5), Block.RANDOM_ID));
        blocks.add(new Block(Arrays.asList(5, 7), Block.RANDOM_ID));

        List<Integer> missingRecords = Whitebox.invokeMethod(classUnderTest, "findMissingRecordsFromBlocks", blocks, 7);
        MatcherAssert.assertThat(missingRecords, contains(6));
    }

    @Test
    public void testFindMissingRecordsFromBlocks_none() throws Exception {
        List<Block> blocks = new ArrayList<>();
        blocks.add(new Block(Arrays.asList(1, 2, 4, 3, 5), Block.RANDOM_ID));
        blocks.add(new Block(Arrays.asList(5, 6), Block.RANDOM_ID));

        List<Integer> missingRecords = Whitebox.invokeMethod(classUnderTest, "findMissingRecordsFromBlocks", blocks, 6);
        MatcherAssert.assertThat(missingRecords, is(empty()));
    }

    @Test
    public void testFindMissingRecordsFromBlocks_several() throws Exception {
        List<Block> blocks = new ArrayList<>();
        blocks.add(new Block(Arrays.asList(2, 4, 3, 5), Block.RANDOM_ID));
        blocks.add(new Block(Arrays.asList(5, 6), Block.RANDOM_ID));
        blocks.add(new Block(Arrays.asList(3, 8), Block.RANDOM_ID));

        List<Integer> missingRecords = Whitebox.invokeMethod(classUnderTest, "findMissingRecordsFromBlocks", blocks, 10);
        MatcherAssert.assertThat(missingRecords, contains(1, 7, 9, 10));
    }

    @Test
    public void testCreateBlocksForMissingRecords() throws Exception {
        List<Integer> integers = Arrays.asList(8, 11, 12, 20);
        List<Block> singletoneBlocks = Whitebox.invokeMethod(classUnderTest, "createBlocksForMissingRecords", integers);
        MatcherAssert.assertThat(singletoneBlocks.size(), is(4));
        MatcherAssert.assertThat(singletoneBlocks, containsInAnyOrder(
                new Block(Arrays.asList(8), Block.RANDOM_ID),
                new Block(Arrays.asList(11), Block.RANDOM_ID),
                new Block(Arrays.asList(12), Block.RANDOM_ID),
                new Block(Arrays.asList(20), Block.RANDOM_ID)
        ));
    }

    @Test
    public void testFindBlocks_integration() throws Exception {
        List<Integer> recordsIDsBlockOne = new ArrayList<>(Arrays.asList(3, 2));
        List<Integer> recordsIDsBlockTwo = new ArrayList<>(Arrays.asList(5, 6));

        CandidatePairs pairs = createBlock(recordsIDsBlockOne);
        pairs.addAll(createBlock(recordsIDsBlockTwo));

        List<Block> blocks = classUnderTest.findBlocks(pairs, 8);

        MatcherAssert.assertThat(blocks, hasSize(6));
        MatcherAssert.assertThat(blocks.get(0).getMembers(), containsInAnyOrder(recordsIDsBlockOne.toArray()));
        MatcherAssert.assertThat(blocks.get(1).getMembers(), containsInAnyOrder(recordsIDsBlockTwo.toArray()));

        for (int recordId = 1; recordId <= 8; recordId++) {
            if (!recordsIDsBlockOne.contains(recordId) && !recordsIDsBlockTwo.contains(recordId)) {
                List<Block> blocksOfRecord = classUnderTest.findBlocksOfRecord(blocks, recordId);
                MatcherAssert.assertThat(blocksOfRecord, allOf(hasSize(1)));
                MatcherAssert.assertThat(blocksOfRecord, contains(new Block(Arrays.asList(recordId), Block.RANDOM_ID)));
            }
        }
    }

    @Test
    public void testSetRecordsInBlocksAsTrueMatch() throws Exception {
        List<Block> blocks = new ArrayList<>();
        blocks.add(new Block(Arrays.asList(2, 4, 3, 5), Block.RANDOM_ID));
        blocks.add(new Block(Arrays.asList(5, 6), Block.RANDOM_ID));
        blocks.add(new Block(Arrays.asList(3, 8), Block.RANDOM_ID));

        classUnderTest.setRecordsInBlocksAsTrueMatch(blocks);

        for (Block block : blocks) {
            for (Integer memberId : block.getMembers()) {
                MatcherAssert.assertThat((double) block.getMemberProbability(memberId), closeTo(1.0, 0.00001));
                MatcherAssert.assertThat(block.getMemberAvgSimilarity(memberId), closeTo(1.0, 0.00001));
            }
        }
    }

    @Test
    public void testUpdateBlockRepresentativesMap_addOneNewRepresentative() throws Exception {
        Map<Integer, List<Block>> map = new HashMap<>();
        Block block = new Block(Arrays.asList(1, 7, 22, 2), Block.RANDOM_ID);
        block.setMemberProbability(1, 0.26F);
        block.setMemberProbability(7, 0.27F);
        block.setMemberProbability(22, 0.24F);
        block.setMemberProbability(2, 0.23F);

        Whitebox.invokeMethod(classUnderTest, "updateBlockRepresentativesMap", map, block);
        MatcherAssert.assertThat(map.get(7), contains(block));
    }

    @Test
    public void testUpdateBlockRepresentativesMap_addTwoNewRepresentatives() throws Exception {
        Map<Integer, List<Block>> map = new HashMap<>();
        Block block = new Block(Arrays.asList(1, 7, 22, 2), Block.RANDOM_ID);
        block.setMemberProbability(1, 0.26F);
        block.setMemberProbability(7, 0.26F);
        block.setMemberProbability(22, 0.25F);
        block.setMemberProbability(2, 0.23F);

        Whitebox.invokeMethod(classUnderTest, "updateBlockRepresentativesMap", map, block);
        MatcherAssert.assertThat(map.get(7), contains(block));
        MatcherAssert.assertThat(map.get(1), contains(block));
    }

    @Test
    public void testUpdateBlockRepresentativesMap_addBlockToExistingRepresentative() throws Exception {
        Map<Integer, List<Block>> map = new HashMap<>();
        Block existingBlock = PowerMockito.mock(Block.class);
        List<Block> blocks = new ArrayList<>(Arrays.asList(existingBlock));
        map.put(7, blocks);

        Block block = new Block(Arrays.asList(1, 7, 22, 2), Block.RANDOM_ID);
        block.setMemberProbability(1, 0.26F);
        block.setMemberProbability(7, 0.26F);
        block.setMemberProbability(22, 0.25F);
        block.setMemberProbability(2, 0.23F);

        Whitebox.invokeMethod(classUnderTest, "updateBlockRepresentativesMap", map, block);
        MatcherAssert.assertThat(map.get(1), contains(block));
        MatcherAssert.assertThat(map.get(7), containsInAnyOrder(block, existingBlock));
    }

    @Test
    public void testFindRecordsWithSeveralBlocks() throws Exception {
        List<Block> listOneSizeOne = PowerMockito.mock(List.class);
        PowerMockito.when(listOneSizeOne.size()).thenReturn(1);
        List<Block> listTwoSizeTwo = PowerMockito.mock(List.class);
        PowerMockito.when(listTwoSizeTwo.size()).thenReturn(2);
        List<Block> listThreeSizeOne = PowerMockito.mock(List.class);
        PowerMockito.when(listThreeSizeOne.size()).thenReturn(1);
        List<Block> listFourSizeThree = PowerMockito.mock(List.class);
        PowerMockito.when(listFourSizeThree.size()).thenReturn(3);
        Map<Integer, List<Block>> map = Maps.newHashMap(ImmutableMap.of(
                127, listOneSizeOne,
                158, listTwoSizeTwo,
                7, listThreeSizeOne,
                2, listFourSizeThree));

        Whitebox.invokeMethod(classUnderTest, "findRecordsWithSeveralBlocks", map);
        MatcherAssert.assertThat(map.size(), Matchers.is(2));
        MatcherAssert.assertThat(map, allOf(hasEntry(158, listTwoSizeTwo), hasEntry(2, listFourSizeThree)));
    }
}

