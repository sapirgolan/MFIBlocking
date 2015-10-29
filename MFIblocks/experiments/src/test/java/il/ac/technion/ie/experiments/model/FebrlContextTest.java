package il.ac.technion.ie.experiments.model;

import il.ac.technion.ie.experiments.service.IMeasurements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

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

        int datasetOne = 5;
        int datasetTwo = 6;
        int datasetThree = 7;

        classUnderTest.add(0.3, datasetOne, measurements);
        classUnderTest.add(0.3, datasetTwo, measurements);
        classUnderTest.add(0.3, datasetThree, measurements);


        //assert on size
        assertThat(classUnderTest.getDataSet(0.3), hasSize(3));
        //assert on content
        assertThat(classUnderTest.getDataSet(0.3), containsInAnyOrder(datasetOne, datasetTwo, datasetThree));

        assertThat(classUnderTest.getMeasurments(0.3, datasetOne), not(nullValue()));
        assertThat(classUnderTest.getMeasurments(0.3, datasetTwo), not(nullValue()));
        assertThat(classUnderTest.getMeasurments(0.3, datasetThree), not(nullValue()));
    }

    @Test
    public void testAdd_addTwoDatasetsWithTwoThresholds() throws Exception {
        when(measurements.getFebrlMeasuresContext(Mockito.anyDouble())).thenReturn(new FebrlMeasuresContext(0.2, 0.5));

        int datasetOne = 5;
        int datasetTwo = 2;

        classUnderTest.add(0.3, datasetOne, measurements);
        classUnderTest.add(0.4, datasetTwo, measurements);

        //assert on size
        assertThat(classUnderTest.getDataSet(0.3), hasSize(1));
        assertThat(classUnderTest.getDataSet(0.4), hasSize(1));
        //assert on content
        assertThat(classUnderTest.getDataSet(0.3), allOf(contains(datasetOne), not(contains(datasetTwo))));
        assertThat(classUnderTest.getDataSet(0.4), allOf(contains(datasetTwo), not(contains(datasetOne))));

        assertThat(classUnderTest.getMeasurments(0.3, datasetOne), not(nullValue()));
        assertThat(classUnderTest.getMeasurments(0.4, datasetTwo), not(nullValue()));
    }

    @Test
    public void testGetMeasurments_AllFebrlMeasuresContext() throws Exception {
        when(measurements.getFebrlMeasuresContext(Mockito.anyDouble())).
                thenReturn(new FebrlMeasuresContext(0.2, 0.5)).
                thenReturn(new FebrlMeasuresContext(0.3, 0.6)).
                thenReturn(new FebrlMeasuresContext(0.1, 0.4)).
                thenReturn(new FebrlMeasuresContext(0.7, 0.8));

        int datasetOne = 2;
        int datasetTwo = 9;

        classUnderTest.add(0.3, datasetOne, measurements);
        classUnderTest.add(0.3, datasetTwo, measurements);
        classUnderTest.add(0.5, datasetOne, measurements);

        Map<Integer, FebrlMeasuresContext> measurments = classUnderTest.getMeasurments(0.3);
        //assert for threshold 0.3
        assertThat(measurments.size(), is(2));
        assertThat(measurments.keySet(), containsInAnyOrder(datasetOne, datasetTwo));
        FebrlMeasuresContext febrlMeasuresContext = measurments.get(datasetOne);
        assertThat(febrlMeasuresContext.getAverageRankedValue(), closeTo(0.2, 0.001));
        assertThat(febrlMeasuresContext.getAverageMRR(), closeTo(0.5, 0.001));

        febrlMeasuresContext = measurments.get(datasetTwo);
        assertThat(febrlMeasuresContext.getAverageRankedValue(), closeTo(0.3, 0.001));
        assertThat(febrlMeasuresContext.getAverageMRR(), closeTo(0.6, 0.001));

        //assert for threshold 0.5
        measurments = classUnderTest.getMeasurments(0.5);
        assertThat(measurments.size(), is(1));
    }
}