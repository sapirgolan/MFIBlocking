package il.ac.technion.ie.search.core;

import il.ac.technion.ie.search.exception.TooManySearchResults;
import il.ac.technion.ie.search.module.DocInteraction;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class SearchEngine {
    static final Logger logger = Logger.getLogger(SearchEngine.class);
    private StandardAnalyzer standardAnalyzer;
	private Directory index;
    private DocInteraction docInteraction;

    public SearchEngine(DocInteraction docInteraction) {
		standardAnalyzer = new StandardAnalyzer(Version.LUCENE_48);
		index = new RAMDirectory();
        this.docInteraction = docInteraction;
	}
	
	public List<String> getRecordAttributes(String recordId) {
		List<String> attributes = new ArrayList<>();
		try {
			Query query = createQuery(recordId);
			attributes = retriveRecord(query);
		} catch (ParseException e) {
			System.err.println("Failed to create query for recordId: " + recordId);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Failed to perform search operaion for recordId: " + recordId);
			e.printStackTrace();
		} catch (TooManySearchResults e) {
			System.err.println("Didn't find record with given ID: " + recordId);
			e.printStackTrace();
		}
		return attributes;
	}
	
	public void addRecords(String pathToFile){
		//try-with-resources - new in JDK7 (http://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)
		try (BufferedReader reader = connectToFile(pathToFile)) {
			IndexWriter indexWriter = createInderWeiter();
			indexFileContent(reader, indexWriter);
			indexWriter.close();
		} catch (IOException e) {
			System.err.println("Failed to create IndexWriter");
			e.printStackTrace();
		}
	}
	
	private void indexFileContent(BufferedReader bufferedReader, IndexWriter indexWriter) throws IOException {
		String line = bufferedReader.readLine();
        logger.debug("indexing flowing line:" + line);
        //in the file that is being parred and indexed the first row which represents the first record
        //is considered to be 1
        int recordIndex = 1;
        while (line != null) {
			if ( isTermSizeValid(line) ) {
                docInteraction.addDoc(indexWriter, Integer.toString(recordIndex), line);
				recordIndex++;
				line = bufferedReader.readLine();
			}
		}
	}

	private boolean isTermSizeValid(String line) {
		try {
			return (line.getBytes("UTF-8").length < IndexWriter.MAX_TERM_LENGTH);
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}

	//tested
	private BufferedReader connectToFile(String pathToFile) throws FileNotFoundException {
		File filetoRead = new File(pathToFile);
		if (filetoRead.canRead()) {
            return new BufferedReader(new FileReader(filetoRead));
		} else {
			throw new FileNotFoundException(String.format("Could not find file at: %s", pathToFile));
		}
	}

	private IndexWriter createInderWeiter() throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, standardAnalyzer);
        return new IndexWriter(index, config);
	}

	private Query createQuery(String recordId) throws ParseException {
		String querystr = "#" + recordId;
		QueryParser queryParser = new QueryParser(Version.LUCENE_48, "id", standardAnalyzer);
        return queryParser.parse(querystr);
	}
	
	private List<String> retriveRecord(Query query) throws IOException, TooManySearchResults {
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		ScoreDoc[] hits = performSearch(query, searcher);
        return docInteraction.obtainTopResult(searcher, hits);
	}

	private ScoreDoc[] performSearch(Query query, IndexSearcher searcher) throws IOException {
		int hitsPerPage = 1;
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(query, collector);
        return collector.topDocs().scoreDocs;
	}
}
