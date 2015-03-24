package il.ac.technion.ie.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Created by I062070 on 22/03/2015.
 */
public class SparkContextWrapper {
    private static JavaSparkContext ourInstance = null;
    static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SparkContextWrapper.class);

    public static JavaSparkContext getJavaSparkContext() {
        if (ourInstance == null) {
            int numOfCores = Runtime.getRuntime().availableProcessors();
            SparkConf conf = new SparkConf();
            conf.setMaster("local["+numOfCores+"]");
            conf.setAppName("MFIBlocks");
            ourInstance = new JavaSparkContext(conf);
            logger.debug("SPARK HOME= " + ourInstance.getSparkHome());
            System.out.println("SPARK HOME= " + ourInstance.getSparkHome());
        }
        return ourInstance;
    }

    private SparkContextWrapper() {
    }
}
