package il.ac.technion.ie.potential.utils;

import il.ac.technion.ie.exception.NotImplementedYetException;
import il.ac.technion.ie.model.Record;

/**
 * Created by I062070 on 26/09/2015.
 */
public class PotentialUtil {
    public static Integer convertToId(Object member) {
        Integer memberId;
        if (member instanceof Integer) {
            memberId = (Integer) member;
        } else if (member instanceof Record) {
            Record record = (Record) member;
            memberId = record.getRecordID();
        } else {
            throw new NotImplementedYetException(String.format("Retrieving recordId from type %s is not supported yet",
                    member.getClass().getSimpleName()));
        }
        return memberId;
    }
}
