package lucene.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import fimEntityResolution.exception.TooManySearchResults;

public class SearchEngine {
	private StandardAnalyzer standardAnalyzer;
	private Directory index;
	
	public SearchEngine() {
		standardAnalyzer = new StandardAnalyzer(Version.LUCENE_48);
		index = new RAMDirectory();
	}
	
	public void addRecords(String pathToFile){
		//try-with-resources - new in JDK7 (http://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)
		try (BufferedReader reader = connectToFile(pathToFile)) {
			IndexWriter indexWriter = createInderWeiter();
			indexFileContent(reader, indexWriter);
		} catch (IOException e) {
			System.err.println("Failed to create IndexWriter");
			e.printStackTrace();
		}
	}
	
	//tested
	private void indexFileContent(BufferedReader bufferedReader, IndexWriter indexWriter) throws IOException {
		String line = bufferedReader.readLine();
		int recordIndex = 1;
		while (line != null) {
			addDoc(indexWriter, Integer.toString(recordIndex), line);
			recordIndex++;
			line = bufferedReader.readLine();
		}
	}

	//tested
	private BufferedReader connectToFile(String pathToFile) throws FileNotFoundException {
		File filetoRead = new File(pathToFile);
		if (filetoRead.canRead()) {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filetoRead));
			return bufferedReader;
		} else {
			throw new FileNotFoundException(String.format("Could not find file at: %s", pathToFile));
		}
	}

	private IndexWriter createInderWeiter() throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, standardAnalyzer);
		IndexWriter indexWriter = new IndexWriter(index, config);
		return indexWriter;
	}
	
	private void addDoc(IndexWriter indexWriter, String recordId, String qgrams) throws IOException {
		recordId = "#" + recordId;
		Document doc = new Document();
		doc.add(new TextField("id", recordId, Field.Store.YES));
		doc.add(new StringField("qgrams", qgrams, Field.Store.YES));
		indexWriter.addDocument(doc);
	}
	
	private Query createQuery(String recordId) throws ParseException {
		String querystr = "#" + recordId;
		QueryParser queryParser = new QueryParser(Version.LUCENE_48, "id", standardAnalyzer);
		Query query = queryParser.parse(querystr);
		return query;
	}
	
	private void retriveRecord(Query query) throws IOException, TooManySearchResults {
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		ScoreDoc[] hits = performSearch(query, searcher);
		obtainTopResult(searcher, hits);
	}

	//tested
	private List<String> obtainTopResult(IndexSearcher searcher, ScoreDoc[] hits)
			throws IOException, TooManySearchResults {
		if (hits.length !=1) {
			throw new TooManySearchResults(String.format("Search has obtained %d results", hits.length));
		}
		int docId = hits[0].doc;
	    Document document = searcher.doc(docId);
	    String concatonatedQgrams = document.get("qgrams");
	    List<String> qgrams = seperateQgrams(concatonatedQgrams);
	    return qgrams;
	}

	//tested
	private List<String> seperateQgrams(String result) {
		List<String> qgrams = new ArrayList<String>();
		if (result!=null) {
			result = result.trim();
		}
		if (!StringUtils.isEmpty(result)) {
			String[] strings = result.split(" ");
			qgrams = new ArrayList<String>(Arrays.asList(strings));
		}
		return qgrams;
	}

	private ScoreDoc[] performSearch(Query query, IndexSearcher searcher) throws IOException {
		int hitsPerPage = 1;
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		return hits;
	}
}
