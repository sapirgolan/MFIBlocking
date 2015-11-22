package il.ac.technion.ie.probability;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.number.IsCloseTo;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.List;
import java.util.Map;

public class ClusterSimilarityTest {

    @Test
    public void testCalcRecordSimilarityInBlock() throws Exception {
        Integer recordId = 21;
        List<String> mockedList = PowerMockito.mock(List.class);
        Map<Integer, List<String>> map = Maps.newHashMap(ImmutableMap.of(21, mockedList, 22, mockedList));
        SimilarityCalculator similarityCalculator = PowerMockito.mock(SimilarityCalculator.class);

        PowerMockito.when(similarityCalculator.calcRecordsSim(Mockito.anyList(), Mockito.anyList())).thenReturn(1.0F);
        float recordProbability = ClusterSimilarity.calcRecordSimilarityInCluster(recordId, map, similarityCalculator);

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
        float recordProbability = ClusterSimilarity.calcRecordSimilarityInCluster(recordId, map, similarityCalculator);

        MatcherAssert.assertThat((double) recordProbability, IsCloseTo.closeTo(1.5F, 0.001));
    }

    @Test
    public void testCalcRecordProbabilityInBlock_singleItemInBlock() throws Exception {
        Integer recordId = 21;
        List<String> mockedList = PowerMockito.mock(List.class);
        Map<Integer, List<String>> map = Maps.newHashMap(ImmutableMap.of(77, mockedList));
        SimilarityCalculator similarityCalculator = PowerMockito.mock(SimilarityCalculator.class);

        float recordProbability = ClusterSimilarity.calcRecordSimilarityInCluster(recordId, map, similarityCalculator);

        MatcherAssert.assertThat(recordProbability, Matchers.is(1.0F));
    }
}