package il.ac.technion.ie.utils;

import com.google.common.base.Splitter;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
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

            ClassLoader classLoader = MathUtils.class.getClassLoader();
            try {
                String fileToString = IOUtils.toString(classLoader.getResourceAsStream("primeNumbers.txt"));
                List<String> primeNumbersStr = Splitter.on(' ').trimResults().omitEmptyStrings().splitToList(fileToString);
                for (String primeNumberStr : primeNumbersStr) {
                    try {
                        Integer integer = Integer.valueOf(primeNumberStr);
                        primeNumbers.add(integer);
                    } catch (NumberFormatException e) {
                        logger.error("Failed to parse " + primeNumberStr + " to integer", e);
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to read file of prime numbers", e);
                return null;
            }
        }
        return primeNumbers.subList(0, howManyNumbers);
    }
}

