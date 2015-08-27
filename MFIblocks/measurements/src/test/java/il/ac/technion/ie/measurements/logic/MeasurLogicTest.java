package il.ac.technion.ie.measurements.logic;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import com.google.common.collect.Lists;
import il.ac.technion.ie.measurements.type.CellType;
import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.model.Block;
import org.easymock.EasyMock;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * Created by XPS_Sapir on 03/06/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(MeasurLogic.class)
public class MeasurLogicTest {
    private final DoubleFactory1D doubleFactory1D = DoubleFactory1D.sparse;
    private final DoubleFactory2D doubleFactory2D = DoubleFactory2D.sparse;
    private MeasurLogic classUnderTest;


    @Before
    public void setup() {
        classUnderTest = new MeasurLogic();
    }

    @Test
    public void testConvertBlocksToMatrix() throws Exception {

    }

    @Test
    public void testUpdateScoreInMatrix_forFirstTime() throws Exception {
        SparseDoubleMatrix2D matrix2D = new SparseDoubleMatrix2D(100, 100);
        List<Integer> members = Arrays.asList(4, 8, 19, 5, 27);
        int recordId = 19;
        Block block = PowerMockito.mock(Block.class);
        PowerMockito.when(block.getMembers()).thenReturn(members);
        PowerMockito.when(block.getMemberScore(Mockito.eq(recordId))).thenReturn((float) 888);
        PowerMockito.when(block.getMemberProbability(Mockito.eq(recordId))).thenReturn((float) 0.456);

        Whitebox.invokeMethod(classUnderTest, "updateScoreInMatrix", matrix2D, recordId, block, CellType.PROBABILITY);

        for (Integer member : members) {
            if (recordId != member) {
                int rawIndex = Whitebox.invokeMethod(classUnderTest, "getMatrixPosFromRecordID", recordId);
                int colIndex = Whitebox.invokeMethod(classUnderTest, "getMatrixPosFromRecordID", member);
                MatcherAssert.assertThat(matrix2D.getQuick(rawIndex, colIndex), closeTo(0.456, 0.0001));
            }
        }
    }

    @Test
    public void testUpdateScoreInMatrix_cellsNotAffected() throws Exception {
        SparseDoubleMatrix2D matrix2D = new SparseDoubleMatrix2D(100, 100);
        List<Integer> members = Arrays.asList(4, 8, 19, 5, 27);
        int recordId = 19;
        Block block = PowerMockito.mock(Block.class);
        PowerMockito.when(block.getMembers()).thenReturn(members);
        PowerMockito.when(block.getMemberScore(Mockito.eq(recordId))).thenReturn((float) 0.999);
        PowerMockito.when(block.getMemberProbability(Mockito.eq(recordId))).thenReturn((float) 0.456);

        Whitebox.invokeMethod(classUnderTest, "updateScoreInMatrix", matrix2D, recordId, block, CellType.PROBABILITY);
        for (int someMember = 1; someMember <= 100; someMember++) {
            if (!members.contains(someMember)) {
                int rawIndex = Whitebox.invokeMethod(classUnderTest, "getMatrixPosFromRecordID", recordId);
                int colIndex = Whitebox.invokeMethod(classUnderTest, "getMatrixPosFromRecordID", someMember);
                MatcherAssert.assertThat(matrix2D.getQuick(rawIndex, colIndex), closeTo(0.0, 0.000001));
            }
        }
    }

    @Test
    public void testUpdateScoreInMatrix_forSecondTime() throws Exception {
        SparseDoubleMatrix2D matrix2D = new SparseDoubleMatrix2D(100, 100);
        int recordId = 19;
        List<Integer> members = Arrays.asList(4, 8, 19, 5, 27);
        Block block = PowerMockito.mock(Block.class);
        PowerMockito.when(block.getMembers()).thenReturn(members);
        PowerMockito.when(block.getMemberScore(Mockito.eq(recordId))).thenReturn((float) 0.92);
        PowerMockito.when(block.getMemberProbability(Mockito.eq(recordId))).thenReturn((float) 0.32);

        Whitebox.invokeMethod(classUnderTest, "updateScoreInMatrix", matrix2D, recordId, block, CellType.PROBABILITY);
        members = Arrays.asList(19, 5, 27);
        Whitebox.invokeMethod(classUnderTest, "updateScoreInMatrix", matrix2D, recordId, block, CellType.PROBABILITY);

        int rawIndex = Whitebox.invokeMethod(classUnderTest, "getMatrixPosFromRecordID", recordId);
        int colIndex = Whitebox.invokeMethod(classUnderTest, "getMatrixPosFromRecordID", 5);
        MatcherAssert.assertThat(matrix2D.getQuick(rawIndex, colIndex), closeTo(0.64, 0.0001));
        colIndex = Whitebox.invokeMethod(classUnderTest, "getMatrixPosFromRecordID", 27);
        MatcherAssert.assertThat(matrix2D.getQuick(rawIndex, colIndex), closeTo(0.64, 0.0001));
    }

    @Test
    public void testBuildSimilarityVectorFromMatrix() throws Exception {
        DoubleMatrix2D matrix2D = doubleFactory2D.descending(10, 10);
        DoubleMatrix1D matrix1D = classUnderTest.buildSimilarityVectorFromMatrix(matrix2D);
        MatcherAssert.assertThat(matrix1D.size(), is(100));
        for (int i = 0; i < 100; i++) {
            MatcherAssert.assertThat(matrix1D.getQuick(i), Matchers.is((double) (99 - i)));
        }
    }

    @Test
    public void testCalcNonBinaryRecall() throws Exception {
        DoubleMatrix1D algVector = doubleFactory1D.make(new double[]{0.2, 0, 0.7, 0, 0, 0.6, 0, 0.6, 1.2});
        DoubleMatrix1D trueMatch = doubleFactory1D.make(new double[]{0, 0, 1, 0, 0, 1, 0, 0, 1});

        double nonBinaryRecall = classUnderTest.calcNonBinaryRecall(algVector, trueMatch);
        MatcherAssert.assertThat(nonBinaryRecall, closeTo((2.5 / 3.0), 0.0001));
    }

    @Test
    public void testCalcNonBinaryPrecision() throws Exception {
        DoubleMatrix1D algVector = doubleFactory1D.make(new double[]{0, 0.2, 0.7, 0, 0, 0.6, 1.2, 0.6, 0});
        DoubleMatrix1D trueMatch = doubleFactory1D.make(new double[]{0, 0, 1, 0, 0, 1, 1, 0, 0});

        double nonBinaryRecall = classUnderTest.calcNonBinaryPrecision(algVector, trueMatch);
        MatcherAssert.assertThat(nonBinaryRecall, closeTo((2.5 / 3.3), 0.0001));
    }

    @Test
    public void testCalcTruePositiveRate() throws Exception {
        classUnderTest = PowerMock.createPartialMock(MeasurLogic.class, "calcFalseNegative", "calcTruePositive");
        PowerMock.expectPrivate(classUnderTest, "calcTruePositive", EasyMock.anyObject(), EasyMock.anyObject()).andReturn(7);
        PowerMock.expectPrivate(classUnderTest, "calcFalseNegative", EasyMock.anyObject(), EasyMock.anyObject()).andReturn(6);
//        PowerMockito.doNothing().when(MeasurLogicTest.class,"checkString");
        PowerMock.replay(classUnderTest);

        double truePositiveRate = classUnderTest.calcTruePositiveRate(doubleFactory1D.make(new double[0]), doubleFactory1D.make(new double[0]));
        MatcherAssert.assertThat(truePositiveRate, closeTo(0.5384615384615385, 0.00001));
    }

    @Test
    public void testCalcFalsePositiveRate() throws Exception {
        classUnderTest = PowerMock.createPartialMock(MeasurLogic.class, "calcTrueNegative", "calcFalsePositive");
        PowerMock.expectPrivate(classUnderTest, "calcTrueNegative", EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyInt()).andReturn(11);
        PowerMock.expectPrivate(classUnderTest, "calcFalsePositive", EasyMock.anyObject(), EasyMock.anyObject()).andReturn(26);
        PowerMock.replay(classUnderTest);

        double falsePositiveRate = classUnderTest.calcFalsePositiveRate(doubleFactory1D.make(new double[0]), doubleFactory1D.make(new double[0]));
        MatcherAssert.assertThat(falsePositiveRate, closeTo(0.7027027027027027, 0.00001));
    }

    @Test
    public void testCalcTruePositive() throws Exception {
        BitSet result = createBitSet(10, Arrays.asList(0, 4, 5));
        BitSet trueMatch = createBitSet(10, Arrays.asList(1, 3, 4, 5));
        int truePositive = Whitebox.invokeMethod(classUnderTest, "calcTruePositive", result, trueMatch);
        MatcherAssert.assertThat(truePositive, is(equalTo(2)));
    }

    @Test
    public void testCalcFalseNegative() throws Exception {
        BitSet result = createBitSet(10, Arrays.asList(0, 4, 5));
        BitSet trueMatch = createBitSet(10, Arrays.asList(1, 2, 3, 4, 5));
        int truePositive = Whitebox.invokeMethod(classUnderTest, "calcFalseNegative", result, trueMatch);
        MatcherAssert.assertThat(truePositive, is(equalTo(3)));
    }

    @Test
    public void testCalcFalsePositive() throws Exception {
        BitSet result = createBitSet(10, Arrays.asList(0, 4, 5, 6, 7, 8));
        BitSet trueMatch = createBitSet(10, Arrays.asList(1, 3, 4, 5));
        int falsePositive = Whitebox.invokeMethod(classUnderTest, "calcFalsePositive", result, trueMatch);
        MatcherAssert.assertThat(falsePositive, is(equalTo(4)));
    }

    @Test
    public void testCalcTrueNegative() throws Exception {
        BitSet result = createBitSet(10, Arrays.asList(0, 4, 5));
        BitSet trueMatch = createBitSet(10, Arrays.asList(1, 3, 4, 5));
        int trueNegative = Whitebox.invokeMethod(classUnderTest, "calcTrueNegative", result, trueMatch, 10);
        MatcherAssert.assertThat(trueNegative, is(equalTo(5)));
    }

    @Test
    public void testCalcRankedValue_singleBlock() throws Exception {
        AbstractBlock singleBlock = buildMockBlockRankedValue(1, 1);

        List<AbstractBlock> blocks = Lists.newArrayList(singleBlock);
        double rankedValue = classUnderTest.calcRankedValue(blocks);
        MatcherAssert.assertThat(rankedValue, closeTo(0.0, 0.0000001));
    }

    @Test
    public void testCalcRankedValue() throws Exception {
        AbstractBlock block1 = buildMockBlockRankedValue(1, 1);
        AbstractBlock block2 = buildMockBlockRankedValue(1, 2);
        AbstractBlock block3 = buildMockBlockRankedValue(3, 6);
        AbstractBlock block4 = buildMockBlockRankedValue(4, 4);

        double rankedValue = classUnderTest.calcRankedValue(Lists.newArrayList(block1, block2, block3, block4));
        MatcherAssert.assertThat(rankedValue, closeTo(0.35, 0.0000001));
    }

    private AbstractBlock buildMockBlockRankedValue(Integer trueRepPosition, Integer blockSize) {
        AbstractBlock block1 = PowerMockito.mock(AbstractBlock.class);
        PowerMockito.when(block1.getTrueRepresentativePosition()).thenReturn(trueRepPosition);
        PowerMockito.when(block1.size()).thenReturn(blockSize);
        return block1;
    }

    private BitSet createBitSet(int size, List<Integer> trueIndices) {
        BitSet bitSet = new BitSet(size);
        for (Integer integer : trueIndices) {
            bitSet.set(integer);
        }
        return bitSet;
    }
}