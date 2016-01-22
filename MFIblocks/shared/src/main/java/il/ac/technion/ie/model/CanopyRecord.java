package il.ac.technion.ie.model;

import java.io.Serializable;

/**
 * Created by I062070 on 28/11/2015.
 */
public class CanopyRecord extends Record implements Serializable {
    protected double score;

    public CanopyRecord(Record record, double score) {
        this.fields = record.getFields();
        this.recordID = record.getRecordID();
        this.recordName = record.getRecordName();
        this.score = score;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public double getScore() {
        return score;
    }
}
