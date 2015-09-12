package il.ac.technion.ie.experiments.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import il.ac.technion.ie.experiments.util.ExperimentsUtils;
import il.ac.technion.ie.model.Record;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

@RunWith(Parameterized.class)
public class BlockWithDataParametrizedTest {

    private final List<Double> probabilities;
    private final int expectedPosition;
    private BlockWithData classUnderTest;
    private List<Record> records;
    private List<String> fieldNames = Arrays.asList("rec_id", "culture", "sex",  "age", "date_of_birth",
                                                "title", "given_name", "surname", "state");

    public BlockWithDataParametrizedTest(List<Double> probabilities, int expectedPosition) {
        this.probabilities = probabilities;
        this.expectedPosition = expectedPosition;
    }

    @Parameterized.Parameters(name = "{index}: probabilities({0}); TruePos{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Arrays.asList(0.2, 0.3, 0.5), 1},
                {Arrays.asList(0.2, 0.7, 0.1), 3},
                {Arrays.asList(0.1, 0.6, 0.3), 2}
        });
    }

    @Test
    public void testGetTrueRepresentativePosition() throws Exception {
        //prepare
        List<String> value1 = ExperimentsUtils.hugeStringToList("rec-100-dup-0, usa, m, 3l, , m s, ch|o  , washin ton, q");
        List<String> value2 = ExperimentsUtils.hugeStringToList("rec-100-dup-1, usa, r,3, , ms, ch   e, washington, ql  ");
        List<String> value3 = ExperimentsUtils.hugeStringToList("rec-100-org,   usa, m,31, , ms, chloe, washington, qld");
        initRecords(value1, value2, value3);

        HashMap<String, Double> probabilitiesMap = Maps.newHashMap(ImmutableMap.<String, Double>builder().
                put("rec-100-dup-0", probabilities.get(0)).
                put("rec-100-dup-1", probabilities.get(1)).
                put("rec-100-org", probabilities.get(2)).
                build());

        createBlock(probabilitiesMap);

        //execute
        int trueRepresentativePosition = classUnderTest.getTrueRepresentativePosition();

        //assertion
        MatcherAssert.assertThat(trueRepresentativePosition, Matchers.is(expectedPosition));

    }

    private void initRecords(List<String>... values) {
        records = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            List<String> fieldsValues = values[i];
            records.add(new Record(fieldNames, fieldsValues, i));
        }
    }

    private void createBlock(Map<String, Double> map) {
        classUnderTest = new BlockWithData(records);
        for (Record record : records) {
            Double score = map.get(record.getRecordName());
            classUnderTest.setMemberProbability(record, score.floatValue());
        }
    }

}