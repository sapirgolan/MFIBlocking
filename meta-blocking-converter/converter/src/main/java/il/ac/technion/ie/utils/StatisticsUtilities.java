package il.ac.technion.ie.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author G.A.P. II
 */

public class StatisticsUtilities {
    
    public static double getEntropy(Collection<Double> values) {
        if (values.isEmpty()) {
            return -1.0;
        }
        
        double noOfTerms = 0;
        for (Double frequency : values) {
            noOfTerms += frequency;
        }

        double entropy = 0.0;
        for (Double frequency : values) {
            double currentProbability = frequency / noOfTerms;
            entropy += -currentProbability * Math.log(currentProbability) / Math.log(2);
        }
        
        return entropy;
    }
       
    public static double getFirstQuartile(ArrayList<Double> values) {
        if (values.isEmpty()) {
            return -1.0;
        }
        
        Collections.sort(values);
        switch (values.size() % 4) {
            case 0:
                return values.get(values.size()/4);
            case 1:
                double lower = values.get(values.size()/4);
                double upper = values.get(values.size()/4+1);
                return (lower + upper)/2.0;
            case 2:
                lower = values.get(values.size()/4);
                double middle = values.get(values.size()/4+1);
                upper = values.get(values.size()/4+2);
                return (lower + upper + middle)/3.0;
            case 3:
                lower = values.get(values.size()/4);
                double middle1 = values.get(values.size()/4+1);
                double middle2 = values.get(values.size()/4+2);
                upper = values.get(values.size()/4+3);
                return (lower + upper + middle1 + middle2)/4.0;
        } 
	return -1.0;
    }
    
    public static double getMaxValue(ArrayList<Double> values) {
        if (values.isEmpty()) {
            return -1.0;
        }
        
        double max = -1.0;
        for (Double value : values) {
            if (max < value) {
                max = value;
            }
        }

        return max;
    }

    public static double getMeanValue(ArrayList<Double> values) {
        if (values.isEmpty()) {
            return -1.0;
        }
        
        double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }

        return sum/values.size();
    }

    public static double getMedianValue(ArrayList<Double> values) {
        if (values.isEmpty()) {
            return -1.0;
        }
        
        Collections.sort(values);
        if  (values.size() % 2 == 1) {
            return values.get(values.size()/2+1);
        }

        double lower = values.get(values.size()/2-1);
	double upper = values.get(values.size()/2);

	return (lower + upper)/2.0;
    }

    public static double getMinValue(ArrayList<Double> values) {
        if (values.isEmpty()) {
            return -1.0;
        }
        
        double min = Double.MAX_VALUE;
        for (Double value : values) {
            if (value < min) {
                min = value;
            }
        }

        return min;
    }

    public static double getPearsonCorrelation(ArrayList<Double> list1, ArrayList<Double> list2) {
        if (list1.isEmpty() || list2.isEmpty()) {
            return -2.0;
        }
        
        if (list1.size() != list2.size()) {
            System.err.println("No Pearson Correlation can be estimated between unequally-sized samples");
            return 0.0;
        }

        int n = list1.size();
        Object[] array1 = list1.toArray();
        Object[] array2 = list2.toArray();

        double sumArray1 = 0.0;
        double sumArray2 = 0.0;
        double sumArraysProduct = 0.0;
        double sumArray1Square = 0.0;
        double sumArray2Square = 0.0;
        for (int i = 0; i < n; i++) {
            sumArray1 += (Double) array1[i];
            sumArray2 += (Double) array2[i];
            sumArraysProduct += ((Double) array1[i])*((Double) array2[i]);
            sumArray1Square += Math.pow((Double) array1[i], 2.0);
            sumArray2Square += Math.pow((Double) array2[i], 2.0);
        }

        return (n*sumArraysProduct - sumArray1*sumArray2)/(Math.sqrt(n*sumArray1Square-Math.pow(sumArray1, 2.0)))/(Math.sqrt(n*sumArray2Square-Math.pow(sumArray2, 2.0)));
    }
    
    public static int getSpecificValueInstances(double value, ArrayList<Double> values) {
        if (values.isEmpty()) {
            return -1;
        }
        
        int counter = 0;
        for (Double currentValue : values) {
            if (currentValue == value) {
                counter++;
            }
        }

        return counter;
    }

    public static double getSum(ArrayList<Double> values) {
        if (values.isEmpty()) {
            return -1.0;
        }
        
        double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }

        return sum;
    }
    
    public static double getStandardDeviation(double meanValue, ArrayList<Double> values) {
        if (values.isEmpty()) {
            return -1.0;
        }
        
        double sum = 0.0;
        for (Double value : values) {
            sum += Math.pow(meanValue-value, 2.0);
        }

        return Math.sqrt(sum/values.size());
    }
    
    public static double getThirdQuartile(ArrayList<Double> values) {
        if (values.isEmpty()) {
            return -1.0;
        }
        
        Collections.sort(values);
        switch (values.size() % 4) {
            case 0:
                return values.get(3*values.size()/4);
            case 1:
                double lower = values.get(3*values.size()/4);
                double upper = values.get(3*values.size()/4+1);
                return (lower + upper)/2.0;
            case 2:
                lower = values.get(3*values.size()/4);
                double middle = values.get(3*values.size()/4+1);
                upper = values.get(3*values.size()/4+2);
                return (lower + upper + middle)/3.0;
            case 3:
                lower = values.get(3*values.size()/4);
                double middle1 = values.get(3*values.size()/4+1);
                double middle2 = values.get(3*values.size()/4+2);
                upper = values.get(3*values.size()/4+3);
                return (lower + upper + middle1 + middle2)/4.0;
        } 
        
	return -2.0;
    }
}