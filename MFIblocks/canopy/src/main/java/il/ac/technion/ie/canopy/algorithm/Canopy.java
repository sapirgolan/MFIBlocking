package il.ac.technion.ie.canopy.algorithm;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.canopy.model.CanopyRecord;
import il.ac.technion.ie.canopy.search.SearchCanopy;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.search.core.SearchEngine;
import il.ac.technion.ie.search.module.DocInteraction;
import il.ac.technion.ie.search.module.SearchResult;
import il.ac.technion.ie.search.search.ISearch;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by I062070 on 21/11/2015.
 * Based on the article "Efficient Clustering of
 * High-Dimensional Data Sets
 * with Application to Reference Matching" at http://www.kamalnigam.com/papers/canopy-kdd00.pdf
 * <p/>
 * There are two parameters: T2, T1 and T2 > T1
 *
 * http://webdam.inria.fr/Jorge/html/wdmch18.html
 */
public class Canopy {

    private static final Logger logger = Logger.getLogger(Canopy.class);


    private final Map<Integer, Record> records;
    private final double T2;
    private final double T1;
    private final ISearch searcher;
    private SearchEngine searchEngine;

    public Canopy(List<Record> records, double t1, double t2) throws CanopyParametersException {
        Canopy.assertT1andT2(t1, t2);
        this.records = new HashMap<>(records.size());
        for (Record record : records) {
            this.records.put(record.getRecordID(), record);
        }
        T2 = t2;
        T1 = t1;
        this.searcher = new SearchCanopy();
    }

    public static void assertT1andT2(double t1, double t2) throws CanopyParametersException {
        if (t2 >= t1) {
            throw new CanopyParametersException(String.format("The value of T1 (%s) must be bigger than the value of T2 (%s)", t1, t2));
        }
    }

    public synchronized void initSearchEngine(DocInteraction canopyInteraction) {
        searchEngine = new SearchEngine(canopyInteraction);
        searchEngine.addRecords(records.values());
    }

    public void createCanopies() {
        List<Record> recordsPool = new ArrayList<>(records.values());
        List<CanopyCluster> canopies = new ArrayList<>();
        while (!recordsPool.isEmpty()) {
            Record rootRecord = sampleRecordRandomly(recordsPool);
            List<SearchResult> searchResults = searchEngine.searchInIndex(searcher, SearchCanopy.DEFAULT_HITS_PER_PAGE, rootRecord.getEntries());
            List<CanopyRecord> candidateRecordsForCanopy = fetchRecordsBasedOnIDs(searchResults);
            try {
                CanopyCluster canopyCluster = new CanopyCluster(candidateRecordsForCanopy, T2, T1);
                canopyCluster.removeRecordsBelowT2();
                canopyCluster.removeRecordsBelowT1();
                List<CanopyRecord> tightedRecords = canopyCluster.getTightRecords();
                removeRecords(recordsPool, rootRecord, tightedRecords);
                canopies.add(canopyCluster);
            } catch (CanopyParametersException e) {
                logger.error("Failed to create Canopy", e);
            }
        }

    }

    private void removeRecords(Collection<Record> recordsPool, Record rootRecord, Collection<? extends Record> tightedRecords) {
        recordsPool.remove(rootRecord);
        recordsPool.removeAll(tightedRecords);
    }

    private List<CanopyRecord> fetchRecordsBasedOnIDs(List<SearchResult> searchResults) {
        ArrayList<CanopyRecord> list = new ArrayList<>();
        for (SearchResult searchResult : searchResults) {
            try {
                int intID = Integer.parseInt(searchResult.getID());
                Record record = records.get(intID);
                if (record != null) {
                    list.add(new CanopyRecord(record, searchResult.getScore()));
                } else {
                    logger.warn(String.format("Didn't find a records that corresponds to ID: '%d'", intID));
                }
            } catch (NumberFormatException e) {
                logger.error(String.format("Failed to retrieve record corresponds to ID '%s'", searchResult), e);
            }
        }
        return list;
    }

    private Record sampleRecordRandomly(List<Record> recordsPool) {
        Collections.shuffle(recordsPool);
        Random random = new Random();
        int randomIndex = random.nextInt(recordsPool.size());
        return recordsPool.get(randomIndex);
    }
}
