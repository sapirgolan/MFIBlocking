package il.ac.technion.ie.experiments.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.canopy.model.CanopyRecord;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.utils.UtilitiesForBlocksAndRecords;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mock;

public class CanopyServiceTest {

    private CanopyService classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new CanopyService();
    }

    @Test
    public void testFetchCanopiesOfSeeds() throws Exception {
        List<Record> recordsFromCsv = UtilitiesForBlocksAndRecords.getRecordsFromCsv();
        ArrayList<BlockWithData> blocksWithDatas = new ArrayList<>();
        blocksWithDatas.add(new BlockWithData(recordsFromCsv.subList(0, 4)));
        Record trueRepBlockOne = recordsFromCsv.get(3);

        CanopyCluster canopy1Real = createCanopy(Lists.newArrayList(recordsFromCsv.subList(2, 4), recordsFromCsv.subList(14, 16)));
        CanopyCluster canopy1Copy = createCanopy(Lists.newArrayList(recordsFromCsv.subList(0, 1), recordsFromCsv.subList(3, 5)));
        BiMap<Record, BlockWithData> allTrueRepresentatives = classUnderTest.getAllTrueRepresentatives(blocksWithDatas);

        Multimap<Record, CanopyCluster> mapping = classUnderTest.fetchCanopiesOfSeeds(Lists.newArrayList(canopy1Copy, canopy1Real), allTrueRepresentatives.keySet());
        assertThat(mapping.keySet(), hasSize(1));
        assertThat(mapping.size(), Matchers.is(2));
        assertThat(mapping.asMap(), Matchers.hasKey(trueRepBlockOne));
        assertThat(mapping.values(), hasSize(2));
        assertThat(mapping.values(), containsInAnyOrder(canopy1Copy, canopy1Real));
    }

    @Test
    public void testSelectCanopiesForRepresentatives() throws Exception {
        List<Record> recordsFromCsv = UtilitiesForBlocksAndRecords.getRecordsFromCsv();
        Map<Record, BlockWithData> repToBlock = new HashMap<>();

        //create mapping trueRep of block #1
        Record trueRepClusterOne = recordsFromCsv.get(3);
        BlockWithData blockWithDataOne = new BlockWithData(recordsFromCsv.subList(0, 4));
        repToBlock.put(trueRepClusterOne, blockWithDataOne);

        //create mapping trueRep of block #1 to its canopies
        Multimap<Record, CanopyCluster> multimap = ArrayListMultimap.create();
        CanopyCluster canopyOfClusterOne = this.createCanopySingleList(recordsFromCsv.subList(1, 4));
        multimap.put(trueRepClusterOne, canopyOfClusterOne);
        multimap.put(trueRepClusterOne, this.createCanopySingleList(recordsFromCsv.subList(3, 6)));
        multimap.put(trueRepClusterOne, this.createCanopySingleList(Lists.newArrayList(recordsFromCsv.get(0), trueRepClusterOne)));

        //create mapping trueRep of block #3
        Record trueRepClusterThree = recordsFromCsv.get(19);
        BlockWithData blockWithDataThree = new BlockWithData(recordsFromCsv.subList(13, 20));
        repToBlock.put(trueRepClusterThree, blockWithDataThree);

        //create mapping trueRep of block #3 to its canopies
        List<Record> someCluster = new ArrayList<>(recordsFromCsv.subList(9, 14));
        someCluster.add(trueRepClusterThree);

        CanopyCluster canopyOfClusterThree = this.createCanopySingleList(recordsFromCsv.subList(13, 20));
        multimap.put(trueRepClusterThree, canopyOfClusterThree);
        multimap.put(trueRepClusterThree, this.createCanopySingleList(someCluster));
        multimap.put(trueRepClusterThree, this.createCanopySingleList(Lists.newArrayList(trueRepClusterThree,
                recordsFromCsv.get(18), recordsFromCsv.get(11))));

        //execute
        BiMap<Record, CanopyCluster> recordCanopyClusterBiMap = classUnderTest.selectCanopiesForRepresentatives(multimap, repToBlock);

        //assertion
        assertThat(recordCanopyClusterBiMap.size(), is(2));
        assertThat(recordCanopyClusterBiMap, hasKey(trueRepClusterOne));
        assertThat(recordCanopyClusterBiMap, hasKey(trueRepClusterThree));
        assertThat(recordCanopyClusterBiMap.get(trueRepClusterOne), is(canopyOfClusterOne));
        assertThat(recordCanopyClusterBiMap.get(trueRepClusterThree), is(canopyOfClusterThree));
    }

    @Test
    public void testCalcIntersection() throws Exception {
        List<Record> recordsFromCsv = UtilitiesForBlocksAndRecords.getRecordsFromCsv();
        List<Record> blockMembers = recordsFromCsv.subList(0, 4);
        CanopyCluster canopyCluster = this.createCanopySingleList(recordsFromCsv.subList(1, 4));

        TreeMap<Integer, CanopyCluster> entry = Whitebox.invokeMethod(classUnderTest, "calcIntersection", blockMembers, canopyCluster);
        assertThat(entry, hasKey(3));
        assertThat(entry.get(3), is(canopyCluster));
    }

    @Test
    public void testMapCanopiesToBlocks() throws Exception {
        int size = 15;
        Map<Record, BlockWithData> recordToBlock = new HashMap<>();
        Map<Record, CanopyCluster> recordToCanopyMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Record record = mock(Record.class);
            BlockWithData block = mock(BlockWithData.class);
            recordToBlock.put(record, block);
            CanopyCluster canopy = mock(CanopyCluster.class);
            recordToCanopyMap.put(record, canopy);
        }
        BiMap<BlockWithData, CanopyCluster> blockToCanopyMap = classUnderTest.mapCanopiesToBlocks(recordToCanopyMap, recordToBlock);
        for (Record record : recordToBlock.keySet()) {
            BlockWithData blockWithData = recordToBlock.get(record);
            CanopyCluster canopyCluster = recordToCanopyMap.get(record);
            assertThat(blockToCanopyMap.get(blockWithData), is(canopyCluster));
            assertThat(blockToCanopyMap.inverse().get(canopyCluster), is(blockWithData));
        }
    }

    private CanopyCluster createCanopy(List<List<Record>> records) throws CanopyParametersException {

        List<CanopyRecord> canopyRecords = new ArrayList<>();
        for (List<Record> sublist : records) {
            for (Record record : sublist) {
                canopyRecords.add(new CanopyRecord(record, 0.0));
            }
        }
        CanopyCluster canopyCluster = new CanopyCluster(canopyRecords, 0.01, 0.2);
        Whitebox.setInternalState(canopyCluster, "allRecords", canopyRecords);

        return canopyCluster;
    }

    private CanopyCluster createCanopySingleList(List<Record> records) throws CanopyParametersException {
        return this.createCanopy(Lists.<List<Record>>newArrayList(records));
    }
}
