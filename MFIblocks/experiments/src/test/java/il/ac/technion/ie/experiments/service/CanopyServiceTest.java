package il.ac.technion.ie.experiments.service;

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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
}
