package il.ac.technion.ie.experiments.parsers;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class ReadBinaryFileTest {
    private ReadBinaryFile classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new ReadBinaryFile();
    }

    @Test
    public void testReadFile_FileInput() throws Exception {
        File binaryFile = ExperimentsUtils.getBinaryFile();
        Map<Integer, Double> newProbabilities = classUnderTest.readFile(binaryFile);

        //assertion
        int[] array = Ints.toArray(ContiguousSet.create(Range.closed(1, 44), DiscreteDomain.integers()));
        Arrays.sort(array);
        Integer[] integers = ArrayUtils.toObject(array);

        Assert.assertThat(newProbabilities.size(), is(equalTo(44)));
        Assert.assertThat(newProbabilities.keySet(), contains(integers));
    }

    @Test
    public void testReadFile_pathInput() throws Exception {
        String binaryFilePath = ExperimentsUtils.getBinaryFile().getAbsolutePath();
        Map<Integer, Double> newProbabilities = classUnderTest.readFile(binaryFilePath);

        //assertion
        int[] array = Ints.toArray(ContiguousSet.create(Range.closed(1, 44), DiscreteDomain.integers()));
        Arrays.sort(array);
        Integer[] integers = ArrayUtils.toObject(array);

        Assert.assertThat(newProbabilities.size(), is(equalTo(44)));
        Assert.assertThat(newProbabilities.keySet(), contains(integers));
    }
}