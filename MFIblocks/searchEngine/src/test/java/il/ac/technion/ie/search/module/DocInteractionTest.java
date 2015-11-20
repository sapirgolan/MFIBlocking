package il.ac.technion.ie.search.module;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DocInteraction.class)
public class DocInteractionTest {

    private ComparisonInteraction classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new ComparisonInteraction();
    }

    @Test
    public void testSeperateQgrams() throws Exception {
        List<String> qgrams = Whitebox.invokeMethod(classUnderTest, "separateContentBySpace", "1 2 3 ");
        Set<String> expected = new HashSet<>(Arrays.asList("1", "2", "3"));
        Assert.assertTrue("don't have all qgrams", qgrams.containsAll(expected));
    }

    @Test
    public void testSeperateQgrams_noItems() throws Exception {
        List<String> qgrams = Whitebox.invokeMethod(classUnderTest, "separateContentBySpace", "");
        Assert.assertTrue("Didn't obtain empty result", qgrams.isEmpty());
    }

    @Test
    public void testSeperateQgrams_space() throws Exception {
        List<String> qgrams = Whitebox.invokeMethod(classUnderTest, "separateContentBySpace", " ");
        Assert.assertTrue("Didn't obtain empty result", qgrams.isEmpty());
    }
}
