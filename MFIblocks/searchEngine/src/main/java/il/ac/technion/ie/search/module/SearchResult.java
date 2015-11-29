package il.ac.technion.ie.search.module;

/**
 * Created by I062070 on 28/11/2015.
 */
public class SearchResult {

    private String documentID;
    private double score;

    public SearchResult(String documentID, double score) {
        this.documentID = documentID;
        this.score = score;
    }

    public String getID() {
        return documentID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SearchResult) {
            SearchResult other = (SearchResult) obj;
            return documentID.equals(other.getID());
        }
        return super.equals(obj);
    }

    public double getScore() {
        return score;
    }
}
