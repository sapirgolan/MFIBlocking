package il.ac.technion.ie.utils;

import java.util.Collections;
import java.util.Map;

/**
 * Created by I062070 on 22/11/2015.
 */
public class MathUtils {
    public static <T> void normilize(Map<T, Float> idToValue) {
        Float maxValue = Collections.max(idToValue.values());
        Float minValue = Collections.min(idToValue.values());
        for (Map.Entry<T, Float> entry : idToValue.entrySet()) {
            float normValue = (entry.getValue() - minValue) / (maxValue - minValue);
            entry.setValue(normValue);
        }
    }
}