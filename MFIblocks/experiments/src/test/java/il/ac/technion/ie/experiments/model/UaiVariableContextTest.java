package il.ac.technion.ie.experiments.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import il.ac.technion.ie.potential.model.SharedMatrix;
import org.hamcrest.MatcherAssert;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UaiVariableContext.class})
public class UaiVariableContextTest {

    @InjectMocks
    private UaiVariableContext classUnderTest;

    private Map<Integer, SharedMatrix> variableIdToSharedMatrixMap;

    private TreeMap<Integer, Integer> variableIdToSizeMap;

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
        variableIdToSizeMap = Maps.newTreeMap(Collections.unmodifiableSortedMap(new TreeMap<Integer, Integer>() {{
            put(4, 9);
            put(1, 9);
            put(2, 9);
            put(3, 9);
            put(5, 9);
            put(6, 9);
            put(7, 9);
        }}));
        Whitebox.setInternalState(classUnderTest, "variableIdToSharedMatrixMap", variableIdToSharedMatrixMap);
        Whitebox.setInternalState(classUnderTest, "variableIdToSizeMap", variableIdToSizeMap);
        List<Integer> idsWithSharedMatricesSorted = classUnderTest.getVariablesIdsWithSharedMatricesSorted();

        MatcherAssert.assertThat(idsWithSharedMatricesSorted, Matchers.contains(1, 2, 4, 6));
    }
}