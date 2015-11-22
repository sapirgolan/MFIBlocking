package il.ac.technion.ie.canopy.algorithm;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.search.core.SearchEngine;
import il.ac.technion.ie.search.module.DocInteraction;

import java.util.List;

/**
 * Created by I062070 on 21/11/2015.
 * Based on the article "Efficient Clustering of
 * High-Dimensional Data Sets
 * with Application to Reference Matching" at http://www.kamalnigam.com/papers/canopy-kdd00.pdf
 * <p/>
 * There are two parameters: T2, T1 and T2 > T1
 */
public class Canopy {

    private final List<Record> records;
    private final double T2;
    private final double T1;
    private SearchEngine searchEngine;

    public Canopy(List<Record> records, double t1, double t2) throws CanopyParametersException {
        if (t1 >= t2) {
            throw new CanopyParametersException(String.format("The value of T2 (%s) must be bigger than the value of T1 (%s)", t2, t1));
        }
        this.records = records;
        T2 = t2;
        T1 = t1;

    }

    public synchronized void initSearchEngine(DocInteraction canopyInteraction) {
        searchEngine = new SearchEngine(canopyInteraction);
        searchEngine.addRecords(records);
    }
}
