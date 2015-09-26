package il.ac.technion.ie.experiments.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
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

    public static String getPathToRecordsFile() throws URISyntaxException {
        String pathToFile = "/20Records.csv";
        File file = getFileFromResourceDir(pathToFile);
        return file.getAbsolutePath();
    }

    public static File getUaiFile() throws URISyntaxException {
        String pathToFile = "/uaiFile.uai";
        return getFileFromResourceDir(pathToFile);
    }

    private static File getFileFromResourceDir(String pathToFile) throws URISyntaxException {
        URL resourceUrl = ExperimentsUtils.class.getResource(pathToFile);
        return new File(resourceUrl.toURI());
    }
}
