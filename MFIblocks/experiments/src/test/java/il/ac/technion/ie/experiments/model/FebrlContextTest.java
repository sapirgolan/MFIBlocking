package il.ac.technion.ie.experiments.model;

import il.ac.technion.ie.experiments.service.IMeasurements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class FebrlContextTest {

    private FebrlContext classUnderTest;
    private IMeasurements measurements;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new FebrlContext();
        measurements = mock(IMeasurements.class);
    }

    @Test
    public void testAdd_addThreeDatasets() throws Exception {
        when(measurements.getFebrlMeasuresContext(Mockito.anyDouble())).thenReturn(new FebrlMeasuresContext(0.2, 0.5));

        List<BlockWithData> blocks = mock(List.class);
        List<BlockWithData> blocks1 = mock(List.class);
        List<BlockWithData> blocks2 = mock(List.class);

        classUnderTest.add(0.3, blocks, measurements);
        classUnderTest.add(0.3, blocks1, measurements);
        classUnderTest.add(0.3, blocks2, measurements);


        //assert on size
        assertThat(classUnderTest.getDataSet(0.3), hasSize(3));
        //assert on content
        assertThat(classUnderTest.getDataSet(0.3), containsInAnyOrder(blocks, blocks1, blocks2));

        assertThat(classUnderTest.getMeasurments(0.3, blocks), not(nullValue()));
        assertThat(classUnderTest.getMeasurments(0.3, blocks1), not(nullValue()));
        assertThat(classUnderTest.getMeasurments(0.3, blocks2), not(nullValue()));
    }

    @Test
    public void testAdd_addTwoDatasetsWithTwoThresholds() throws Exception {
        when(measurements.getFebrlMeasuresContext(Mockito.anyDouble())).thenReturn(new FebrlMeasuresContext(0.2, 0.5));

        List<BlockWithData> blocks = mock(List.class);
        List<BlockWithData> blocks1 = mock(List.class);

        classUnderTest.add(0.3, blocks, measurements);
        classUnderTest.add(0.4, blocks1, measurements);

        //assert on size
        assertThat(classUnderTest.getDataSet(0.3), hasSize(1));
        assertThat(classUnderTest.getDataSet(0.4), hasSize(1));
        //assert on content
        assertThat(classUnderTest.getDataSet(0.3), allOf(contains(blocks), not(contains(blocks1))));
        assertThat(classUnderTest.getDataSet(0.4), allOf(contains(blocks1), not(contains(blocks))));

        assertThat(classUnderTest.getMeasurments(0.3, blocks), not(nullValue()));
        assertThat(classUnderTest.getMeasurments(0.4, blocks1), not(nullValue()));
    }
}