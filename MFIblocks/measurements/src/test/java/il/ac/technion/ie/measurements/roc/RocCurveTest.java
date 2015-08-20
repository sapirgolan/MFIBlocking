package il.ac.technion.ie.measurements.roc;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import il.ac.technion.ie.measurements.service.iMeasurService;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.TreeMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RocCurve.class)
public class RocCurveTest {

    @InjectMocks
    private static RocCurve classUnderTest;
    private final DoubleFactory1D doubleFactory1D = DoubleFactory1D.sparse;
    @Mock
    private iMeasurService measurService;
    @Spy
    private TreeMap<Double, Double> curveMap = new TreeMap<>();


    @BeforeClass
    public static void before() {
        classUnderTest = Whitebox.newInstance(RocCurve.class);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testApplyThreshold() throws Exception {
        DoubleMatrix1D matrix1D = doubleFactory1D.make(new double[]{0.2, 0, 0.7, 0, 0, 0.6, 0, 0.6, 0.8});

        RocMatcher matcher = new RocMatcher();
        matcher.setThreshold(0.2);
        DoubleMatrix1D applyThreshold = Whitebox.invokeMethod(classUnderTest, "applyThreshold", matrix1D, matcher);
        MatcherAssert.assertThat(applyThreshold.cardinality(), Matchers.is(Matchers.equalTo(4)));
    }

    @Test
    public void testUpdateRocCurve_hasAlreadyValue() throws Exception {
        PowerMockito.when(measurService.calcTruePositiveRate(Mockito.any(DoubleMatrix1D.class), Mockito.any(DoubleMatrix1D.class)))
                .thenReturn(0.4);
        PowerMockito.when(measurService.calcFalsePositiveRate(Mockito.any(DoubleMatrix1D.class), Mockito.any(DoubleMatrix1D.class)))
                .thenReturn(0.1);

        curveMap.put(0.1, 0.8);

        Whitebox.invokeMethod(classUnderTest, "updateRocCurve", PowerMockito.mock(DoubleMatrix1D.class));
        MatcherAssert.assertThat(curveMap.get(0.1), Matchers.is(0.8));
    }

    @Test
    public void testUpdateRocCurve_newValue() throws Exception {
        PowerMockito.when(measurService.calcTruePositiveRate(Mockito.any(DoubleMatrix1D.class), Mockito.any(DoubleMatrix1D.class)))
                .thenReturn(0.4);
        PowerMockito.when(measurService.calcFalsePositiveRate(Mockito.any(DoubleMatrix1D.class), Mockito.any(DoubleMatrix1D.class)))
                .thenReturn(0.1);

        Whitebox.invokeMethod(classUnderTest, "updateRocCurve", PowerMockito.mock(DoubleMatrix1D.class));
        MatcherAssert.assertThat(curveMap.get(0.1), Matchers.is(0.4));
    }
}