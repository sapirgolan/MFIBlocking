package il.ac.technion.ie.search.core;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LuceneUtilsTest {

    @Test
    public void testTokenTermWithCapital() throws Exception {
        assertThat(LuceneUtils.tokenTerm("David"), is("david"));
    }

    @Test
    public void testTokenTwoDifferentTerms() throws Exception {
        assertThat(LuceneUtils.tokenTerm("David"), is("david"));
        assertThat(LuceneUtils.tokenTerm("rover"), is("rover"));
    }

    @Test
    public void testTokenWithSpecialChars() throws Exception {
        assertThat(LuceneUtils.tokenTerm(" inalinga c/van park"), is("inalinga c van park"));
    }

    @Test
    public void testTokenWordToBeRemoved() throws Exception {
        assertThat(LuceneUtils.tokenTerm(" a"), is(""));
    }
}