package il.ac.technion.ie.experiments.model;

import il.ac.technion.ie.experiments.service.IMeasurements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FebrlMeasuresContextTest {


    private FebrlMeasuresContext classUnderTest;

    private IMeasurements measurements = PowerMockito.mock(IMeasurements.class);

    @Before
    public void setUp() throws Exception {
        //mocking
        PowerMockito.when(measurements.getAverageRankedValue(Mockito.anyDouble())).thenReturn(0.332);
        PowerMockito.when(measurements.getAverageMRR(Mockito.anyDouble())).thenReturn(0.343);

        classUnderTest = new FebrlMeasuresContext(measurements, 0.4);
    }

    @Test
    public void testGetAverageRankedValue() throws Exception {
        assertThat(classUnderTest.getAverageRankedValue(), closeTo(0.332, 0.000001));
    }

    @Test
    public void testGetAverageMRR() throws Exception {
        assertThat(classUnderTest.getAverageMRR(), closeTo(0.343, 0.00001));

    }
}