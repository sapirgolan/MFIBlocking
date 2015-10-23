package il.ac.technion.ie.experiments.model;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FebrlMeasuresContextTest {

    private FebrlMeasuresContext classUnderTest;

    @Before
    public void setUp() throws Exception {
        double averageRankedValue = 0.332;
        double averageMrr = 0.343;
        classUnderTest = new FebrlMeasuresContext(averageRankedValue, averageMrr);
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