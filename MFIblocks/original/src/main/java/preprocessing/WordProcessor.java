package preprocessing;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;


public class WordProcessor {
	
	StandardAnalyzer analyzer;
	private final static int MIN_NGRAM_SIZE =3;
	private final static int MAX_NGRAM_SIZE =3;
	
	private int min_ngram_size = MIN_NGRAM_SIZE;
	private int max_ngram_size = MAX_NGRAM_SIZE;
		
	public WordProcessor(File stopwordsFile){
		analyzer = new StandardAnalyzer(Version.LUCENE_48);
	}
	
	public WordProcessor(File stopwordsFile, int minNgramSize, int maxNgramSize){
		this(stopwordsFile);
		this.min_ngram_size = minNgramSize;
		this.max_ngram_size = maxNgramSize;
	}
	
	public List<String> processValue(String value){
		List<String> retVal = new ArrayList<String>();
		try {
			value = value.replaceAll(replaceExpr, "");
			StringReader sr = new StringReader(value);
			//JS: TokenStream protocol requirement: 1. define, 2. reset, 3. increment, 4.end, 5. close  20140515
			
			StringReader sr_short = new StringReader(value);
			TokenStream ts_shortWords = analyzer.tokenStream(value, sr_short);
			ts_shortWords.reset();
			while(ts_shortWords.incrementToken()){
				ts_shortWords.getAttribute(CharTermAttribute.class);
				String term = convertTokenStreamToString(ts_shortWords);
				if(term.length() < min_ngram_size){
					retVal.add(term.trim().toLowerCase());
				}
			}
			ts_shortWords.end();
			ts_shortWords.close();
			//JS: TokenStream protocol requirement: 1. define, 2. reset, 3. increment, 4.end, 5. close  20140515
			TokenStream ts = analyzer.tokenStream(value, sr);
			ts.reset();
			NGramTokenFilter ngtf = new NGramTokenFilter(Version.LUCENE_48, ts, min_ngram_size, max_ngram_size);
			while(ngtf.incrementToken()){
				String term = convertTokenStreamToString(ngtf);
				retVal.add(term.trim().toLowerCase());
			}
			
			ts.end();
			ts.close();
			
			
		} catch (IOException e) {
			System.err.println("Failed to parse: " + value);
			e.printStackTrace();
		}
		return retVal;
	}

	private String convertTokenStreamToString(TokenStream ts_shortWords) {
		CharTermAttribute m = ts_shortWords.getAttribute(CharTermAttribute.class);
		//String term = new String(m.buffer());
		return m.toString();
	}
	
//	private String convertTokenStreamToString(TokenStream ts_shortWords) {
//		CharTermAttribute m = ts_shortWords.getAttribute(CharTermAttribute.class);
//		String term = new String(m.buffer());
//		return term;
//	}
	
	
	public final static String replaceExpr = "-|\\|/|\\/|\\.|,|\'|(|)";
	public List<String> removeStopwordsAndSpecialChars(String value){
		List<String> retVal = new ArrayList<String>();
		try {
			value = value.replaceAll(replaceExpr, "");
			
			StringReader sr = new StringReader(value);
			
			TokenStream ts = analyzer.tokenStream(value, sr);		
			ts.reset();
			while(ts.incrementToken()){
				String term = convertTokenStreamToString(ts);
				retVal.add(term);
			}
			ts.end();
			ts.close();
		} catch (IOException e) {
			System.err.println("Failed to parse: " + value);
			e.printStackTrace();
		}
		return retVal;
	}
	
//	public static void main(String[] args){
//		File f = new File("D:\\Batya\\EntityResolution\\tools\\cora-all-id\\stopwords.txt");
//		WordProcessor wp = new WordProcessor(f);
//		String value = "sony vegas 6";
//		List<String> ngrams = wp.processValue(value);
//		System.out.println(ngrams);
//		
//	}
	
	public static <T> String concatListMembers(List<T> list){
		StringBuilder sb = new StringBuilder();
		for (Object object : list) {
			sb.append(object.toString().trim()).append(" ");
		}
		return sb.toString();
	}
}
