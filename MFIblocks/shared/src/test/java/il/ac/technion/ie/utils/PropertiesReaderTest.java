package il.ac.technion.ie.utils;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

public class PropertiesReaderTest {

    @Test
    public void testGetFileds_singleValue() throws Exception {

        List<String> fields = PropertiesReader.getFields("key");
        MatcherAssert.assertThat(fields, Matchers.hasSize(1));
        MatcherAssert.assertThat(fields, Matchers.contains("value"));
    }

    @Test
    public void testGetFileds_list() throws Exception {
        List<String> fields = PropertiesReader.getFields("tokens_on_a_line");
        MatcherAssert.assertThat(fields, Matchers.hasSize(2));
        MatcherAssert.assertThat(fields, Matchers.contains("first token", "second token"));
    }
}