package preprocessing;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;


public class WordProcessor {
	
	StandardAnalyzer analyzer;
	private final static int MIN_NGRAM_SIZE =3;
	private final static int MAX_NGRAM_SIZE =3;
	
	private int min_ngram_size = MIN_NGRAM_SIZE;
	private int max_ngram_size = MAX_NGRAM_SIZE;
		
	public WordProcessor(File stopwordsFile){
		try {
			analyzer = new StandardAnalyzer(Version.LUCENE_30, stopwordsFile);
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	public WordProcessor(File stopwordsFile, int minNgramSize, int maxNgramSize){
		this(stopwordsFile);
		this.min_ngram_size = minNgramSize;
		this.max_ngram_size = maxNgramSize;
	}
	
	public List<String> processValue(String value){
		List<String> retVal = new ArrayList<String>();
		value = value.replaceAll(replaceExpr, "");
		StringReader sr = new StringReader(value);
		StringReader sr_short = new StringReader(value);
		TokenStream ts = analyzer.tokenStream(value, sr);
		TokenStream ts_shortWords = analyzer.tokenStream(value, sr_short);
		
		NGramTokenFilter ngtf = new NGramTokenFilter(ts,min_ngram_size,max_ngram_size);		
		try {
			while(ts_shortWords.incrementToken()){
				TermAttribute m = ts_shortWords.getAttribute(TermAttribute.class);
				if(m.term().length() < min_ngram_size){
					retVal.add(m.term().trim().toLowerCase());
				}
			}
			while(ngtf.incrementToken()){
				TermAttribute m = ts.getAttribute(TermAttribute.class);
				retVal.add(m.term().trim().toLowerCase());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}
	
	public final static String replaceExpr = "-|\\|/|\\/|\\.|,|\'|(|)";
	public List<String> removeStopwordsAndSpecialChars(String value){
		List<String> retVal = new ArrayList<String>();
		value = value.replaceAll(replaceExpr, "");
		
		StringReader sr = new StringReader(value);
		TokenStream ts = analyzer.tokenStream(value, sr);		
		try {
			while(ts.incrementToken()){
				TermAttribute m = ts.getAttribute(TermAttribute.class);
				retVal.add(m.term());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
