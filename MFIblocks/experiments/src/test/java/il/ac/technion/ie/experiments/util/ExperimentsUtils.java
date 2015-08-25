package il.ac.technion.ie.experiments.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by I062070 on 24/08/2015.
 */
public class ExperimentsUtils {
    public static List<String> hugeStringToList(String huge) {
        String[] strings = hugeStringToArray(huge);
        return new ArrayList<>( Arrays.asList(strings) );
    }

    public static String[] hugeStringToArray(String huge) {
        return huge.split(",");
    }
}
