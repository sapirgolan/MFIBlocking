package il.ac.technion.ie.experiments.model;

import com.google.common.collect.*;
import il.ac.technion.ie.potential.model.SharedMatrix;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UaiVariableContext.class})
public class UaiVariableContextTest {

    @InjectMocks
    private UaiVariableContext classUnderTest;

    private Map<Integer, SharedMatrix> variableIdToSharedMatrixMap;

    @Before
    public void setUp() throws Exception {
        classUnderTest = Whitebox.newInstance(UaiVariableContext.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetVariablesIdsWithSharedMatricesSorted() throws Exception {
        SharedMatrix sharedMatrix = PowerMockito.mock(SharedMatrix.class);
        variableIdToSharedMatrixMap = Maps.newHashMap(
                Collections.unmodifiableMap(ImmutableMap.of(4, sharedMatrix, 2, sharedMatrix, 1, sharedMatrix, 6, sharedMatrix)));
        Whitebox.setInternalState(classUnderTest, "variableIdToSharedMatrixMap", variableIdToSharedMatrixMap);
        List<Integer> idsWithSharedMatricesSorted = classUnderTest.getVariablesIdsWithSharedMatricesSorted();

        assertThat(idsWithSharedMatricesSorted, Matchers.contains(1, 2, 4, 6));
    }

    @Test
    public void testGetSizeAndIndexOfVariables() throws Exception {
        TreeMultimap<Integer, Integer> variableIdToBlocksMultimap = TreeMultimap.create();
        variableIdToBlocksMultimap.put(0, 11);
        variableIdToBlocksMultimap.put(1, 12);
        variableIdToBlocksMultimap.put(2, 13);
        variableIdToBlocksMultimap.put(3, 14);
        variableIdToBlocksMultimap.put(4, 15);
        variableIdToBlocksMultimap.put(5, 16);
        variableIdToBlocksMultimap.putAll(6, Lists.newArrayList(16, 15));
        variableIdToBlocksMultimap.putAll(7, Lists.newArrayList(12, 11));
        Whitebox.setInternalState(classUnderTest, "variableIdToBlocksMultimap", variableIdToBlocksMultimap);

        BiMap<Integer, Integer> variableIdToBlockId = HashBiMap.create(
                Maps.newHashMap(new ImmutableMap.Builder<Integer, Integer>()
                        .put(0, 11) //One k-v pair
                        .put(1, 12) //One k-v pair
                        .put(2, 13) //One k-v pair
                        .put(3, 14) //One k-v pair
                        .put(4, 15) //One k-v pair
                        .put(5, 16) //One k-v pair
                        .build())
        );
        Whitebox.setInternalState(classUnderTest, "variableIdToBlockId", variableIdToBlockId);

        Multimap<Integer, Integer> sizeAndIndexOfVariables = classUnderTest.getSizeAndIndexOfVariables();
        Collection<Integer> variablesIDsOfBlocks = sizeAndIndexOfVariables.asMap().get(1);
        Collection<Integer> variablesIDsOfCliques = sizeAndIndexOfVariables.asMap().get(2);

        assertThat(variablesIDsOfBlocks, contains(0, 1, 2, 3, 4, 5));
        assertThat(variablesIDsOfCliques, contains(4, 5, 0, 1));


    }
}