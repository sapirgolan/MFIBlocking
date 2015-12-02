package il.ac.technion.ie.utils;

import com.google.common.base.Splitter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 22/11/2015.
 */
public class MathUtils {

    private static List<Integer> primeNumbers;

    static final Logger logger = Logger.getLogger(MathUtils.class);


    public static <T> void normilize(Map<T, Float> idToValue) {
        Float maxValue = Collections.max(idToValue.values());
        Float minValue = Collections.min(idToValue.values());
        for (Map.Entry<T, Float> entry : idToValue.entrySet()) {
            float normValue = (entry.getValue() - minValue) / (maxValue - minValue);
            entry.setValue(normValue);
        }
    }

    public static List<Integer> getPrimeNumbers(int howManyNumbers) {
        if (primeNumbers == null) {
            primeNumbers = new ArrayList<>();
            URL resourceUrl = MathUtils.class.getResource("/primeNumbers.txt");
            try {
                File file = new File(resourceUrl.toURI());
                String fileToString = FileUtils.readFileToString(file);
                List<String> primeNumbersStr = Splitter.on(' ').trimResults().omitEmptyStrings().splitToList(fileToString);
                for (String primeNumberStr : primeNumbersStr) {
                    try {
                        Integer integer = Integer.valueOf(primeNumberStr);
                        primeNumbers.add(integer);
                    } catch (NumberFormatException e) {
                        logger.error("Failed to parse " + primeNumberStr + " to integer", e);
                    }
                }
            } catch (URISyntaxException | IOException e) {
                logger.error("Failed to read file of prime numbers", e);
                return null;
            }
        }
        return primeNumbers.subList(0, howManyNumbers);
    }
}

