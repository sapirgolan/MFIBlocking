package il.ac.technion.ie.canopy.utils;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;

/**
 * Created by I062070 on 05/12/2015.
 */
public class CanopyUtils {
    public static void assertT1andT2(double t1, double t2) throws CanopyParametersException {
        if (t2 >= t1) {
            throw new CanopyParametersException(String.format("The value of T1 (%s) must be bigger than the value of T2 (%s)", t1, t2));
        }
    }
}
