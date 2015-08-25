package il.ac.technion.ie.experiments.logic;

import com.univocity.parsers.csv.CsvParser;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.Record;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FebrlBlockBuilder.class, CsvParser.class})
public class FebrlBlockBuilderTest {

    private FebrlBlockBuilder classUnderTest;

    @Before
    public void setUp() throws Exception {
        this.classUnderTest = PowerMockito.spy(new FebrlBlockBuilder());
    }

    @Test
    public void testGetBlockIdFromRecord() throws Exception {
        Record record = PowerMockito.mock(Record.class);
        PowerMockito.when(record.getRecordID()).thenReturn("rec-101-dup-0").thenReturn("rec-104-org").thenReturn("very bad record");

        String blockId = Whitebox.invokeMethod(classUnderTest, "getBlockIdFromRecord", record);
        MatcherAssert.assertThat(blockId, Matchers.is("101"));

        blockId = Whitebox.invokeMethod(classUnderTest, "getBlockIdFromRecord", record);
        MatcherAssert.assertThat(blockId, Matchers.is("104"));

        blockId = Whitebox.invokeMethod(classUnderTest, "getBlockIdFromRecord", record);
        MatcherAssert.assertThat(blockId, Matchers.isEmptyOrNullString());
    }

    @Test
    public void testBuild() throws Exception {
        CsvParser csvParser = PowerMockito.mock(CsvParser.class);
        final List<String> fieldsNames = ExperimentsUtils.hugeStringToList("rec_id, culture, sex, age, date_of_birth");
        PowerMockito.when(csvParser.parseNext()).thenReturn(ExperimentsUtils.hugeStringToArray("rec-0-dup-0, unk, f,22,19930508"))
                .thenReturn(ExperimentsUtils.hugeStringToArray("rec-0-org,   unk, f,22,19930508"))
                .thenReturn(ExperimentsUtils.hugeStringToArray("rec-103-org,   unk, f,25, "))
                .thenReturn(ExperimentsUtils.hugeStringToArray("rec-104-dup-0, eng, f,2, 1989 0 08"))
                .thenReturn(ExperimentsUtils.hugeStringToArray("rec-104-dup-1, eng, f,2, l989 1008"))
                .thenReturn(ExperimentsUtils.hugeStringToArray("rec-104-dup-2, eng, f,2, 19891 08"))
                .thenReturn(ExperimentsUtils.hugeStringToArray("rec-104-org,   eng, f,26,19891008"))
                .thenReturn(null);

        List<BlockWithData> blocks = classUnderTest.build(csvParser, fieldsNames);

        //assert number of blocks
        MatcherAssert.assertThat(blocks, Matchers.hasSize(3));

        //assertion on first block
        List<Record> blockOf_0 = blocks.get(0).getMembers();
        MatcherAssert.assertThat(blockOf_0, Matchers.hasSize(2));
        Record record = blockOf_0.get(1);
        String recordID = Whitebox.invokeMethod(classUnderTest, "getBlockIdFromRecord", record);
        MatcherAssert.assertThat(recordID, Matchers.equalTo("0"));

        //assertion on block
        List<Record> blockOf_103 = blocks.get(1).getMembers();
        MatcherAssert.assertThat(blockOf_103, Matchers.hasSize(1));
        record = blockOf_103.get(0);
         recordID = Whitebox.invokeMethod(classUnderTest, "getBlockIdFromRecord", record);
        MatcherAssert.assertThat(recordID, Matchers.equalTo("103"));

        //assertion on block 104
        List<Record> blockOf_104 = blocks.get(2).getMembers();
        MatcherAssert.assertThat(blockOf_104, Matchers.hasSize(4));
        record = blockOf_104.get(3);
        recordID = Whitebox.invokeMethod(classUnderTest, "getBlockIdFromRecord", record);
        MatcherAssert.assertThat(recordID, Matchers.equalTo("104"));
    }
}