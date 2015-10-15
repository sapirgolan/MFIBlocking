package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class MeasurementsTest {

    @InjectMocks
    private Measurements classUnderTest;

    @Spy
    private iMeasurService measurService = new MeasurService();


    @Before
    public void setUp() throws Exception {
        classUnderTest = new Measurements();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCalculate_valueExist() {
        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.213);
        assertThat(classUnderTest.getRankedValueByThreshold(0.213), notNullValue());
        assertThat(classUnderTest.getMRRByThreshold(0.213), notNullValue());
    }

    @Test
    public void testCalculate_whenRankedValueThatIsCalculated() throws Exception {
        //mocking
        when(measurService.calcRankedValue(Mockito.anyList())).thenReturn(0.3);
        when(measurService.calcMRR(Mockito.anyList())).thenReturn(0.7);

        //execute
        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.213);
        assertThat(classUnderTest.getRankedValueByThreshold(0.213), closeTo(0.3, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.213), closeTo(0.7, 0.0001));
    }

    @Test
    public void testCalculate_calculateTwice() throws Exception {
        //mocking
        when(measurService.calcRankedValue(Mockito.anyList())).thenReturn(0.3, 0.5);
        when(measurService.calcMRR(Mockito.anyList())).thenReturn(0.7, 0.6);

        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.213);
        assertThat(classUnderTest.getRankedValueByThreshold(0.213), closeTo(0.3, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.213), closeTo(0.7, 0.0001));


        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.213);
        assertThat(classUnderTest.getRankedValueByThreshold(0.213), closeTo(0.5, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.213), closeTo(0.6, 0.0001));
    }

    @Test
    public void testGetRankedValueByThresholdTwice() throws Exception {
        //mocking
        when(measurService.calcRankedValue(Mockito.anyList())).thenReturn(0.3, 0.5);
        when(measurService.calcMRR(Mockito.anyList())).thenReturn(0.7, 0.6);

        //execution
        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.213);
        classUnderTest.calculate(new ArrayList<BlockWithData>(), 0.323);

        //assert
        assertThat(classUnderTest.getRankedValueByThreshold(0.323), closeTo(0.5, 0.0001));
        assertThat(classUnderTest.getRankedValueByThreshold(0.213), closeTo(0.3, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.213), closeTo(0.7, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.323), closeTo(0.6, 0.0001));
    }

    @Test
    public void testCalculate_ListIsNull() throws Exception {
        classUnderTest.calculate(null, 0.8);

        //assert
        assertThat(classUnderTest.getRankedValueByThreshold(0.8), closeTo(-1.0, 0.0001));
        assertThat(classUnderTest.getMRRByThreshold(0.8), closeTo(-1.0, 0.0001));
    }
}