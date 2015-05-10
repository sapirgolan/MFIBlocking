package il.ac.technion.ie.logic;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.reflect.Whitebox;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class SimilarityCalculatorTest {

    private SimilarityCalculator classUnderTest;
    private List<String> left;
    private List<String> right;
    private float expectedSimilarity;

    public SimilarityCalculatorTest(List<String> list1, List<String> list2, float expSimilarity) {
        left = list1;
        right = list2;
        this.expectedSimilarity = expSimilarity;
    }

    @Parameterized.Parameters(name = "{index}: records({0})={1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, null, 0},
                {new ArrayList<String>(), new ArrayList<String>(), 0},
                {Arrays.asList("a", "b", "c", "d"), Arrays.asList("a", "b", "c", "d"), 4},
                {Arrays.asList("avrim blum  merrick furst  michael kearns  and richard j. lipton.", "cryptographic primitives based on hard learning problems"),
                        Arrays.asList("avrim blum  merrick furst  michael kearns  and richard j. lipton.", "cryptographic primitives based on hard learning problems"), 2}
        });
    }

    @Before
    public void setUp() throws Exception {
        classUnderTest = new SimilarityCalculator(new JaroWinkler());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCompareRecords_identical() throws Exception {
        List<String> leftRecord = new ArrayList<>(Arrays.asList("freund  y.  & schapire  r.,,"));
        List<String> rightRecord = new ArrayList<>(Arrays.asList("freund  y.  & schapire  r.,,"));
        double sim = classUnderTest.calcRecordsSim(leftRecord, rightRecord);
        MatcherAssert.assertThat(sim, Matchers.is(1.0));
    }

    @Test
    public void testCompareRecords_notTheSame() throws Exception {
        List<String> leftRecord = new ArrayList<>(Arrays.asList("freund  y.  & schapire  r.,may,"));
        List<String> rightRecord = new ArrayList<>(Arrays.asList("freund  y.  & schapire  r.,,"));
        double sim = classUnderTest.calcRecordsSim(leftRecord, rightRecord);
        MatcherAssert.assertThat(sim, Matchers.allOf(Matchers.greaterThan(0.9), Matchers.lessThan(1.0)));
    }

    @Test
    public void testCalcRecordsSimilarity() throws Exception {
        float similarity = classUnderTest.compareFields(left, right);
        Assert.assertEquals(expectedSimilarity, similarity, 0.0001);
    }

    @Test
    public void testCalcBatchSimilarity_equalLength() throws Exception {
        float similarity = Whitebox.invokeMethod(classUnderTest, "batchCompare", new String[]{"a", "b", "c", "d"}, new String[]{"a", "b", "c", "d"});
        Assert.assertEquals(4, similarity, 0.0001);
    }

    @Test
    public void testCalcBatchSimilarity_notEqualLength_firstIsShort() throws Exception {
        float similarity = Whitebox.invokeMethod(classUnderTest, "batchCompare", new String[]{"a", "b", "c"}, new String[]{"a", "b", "c", "d"});
        Assert.assertEquals(3, similarity, 0.0001);
    }

    @Test
    public void testCalcBatchSimilarity_notEqualLength_secondIsShort() throws Exception {
        float similarity = Whitebox.invokeMethod(classUnderTest, "batchCompare", new String[]{"a", "b", "c", "d"}, new String[]{"a", "b"});
        Assert.assertEquals(2, similarity, 0.0001);
    }
}