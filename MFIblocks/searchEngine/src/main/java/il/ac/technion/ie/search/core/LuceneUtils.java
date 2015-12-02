package il.ac.technion.ie.search.core;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;

import java.io.IOException;

/**
 * Created by I062070 on 02/12/2015.
 */
public class LuceneUtils {
    private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);

    static final Logger logger = Logger.getLogger(LuceneUtils.class);


    public static String tokenTerm(String inputTerm) {
        StringBuilder builder = new StringBuilder();
        try {
            TokenStream tokenStream = analyzer.tokenStream(null, inputTerm);
            OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                int startOffset = offsetAttribute.startOffset();
                int endOffset = offsetAttribute.endOffset();
                String term = charTermAttribute.toString();
                builder.append(term).append(' ');
            }
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            logger.error("Failed to tokenize " + inputTerm, e);
        }
        if (builder.length() != 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
