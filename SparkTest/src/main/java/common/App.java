package common;

import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;

public class App {
  public static void main(String[] args) {
    String logFile = "C:/workspace/spark-0.8.1-incubating/README.md"; // Should be some file on your system
    JavaSparkContext sc = new JavaSparkContext("local", "App",
      "C:/workspace/spark-0.8.1-incubating/README.md", new String[]{"target/SparkTest-1.0-SNAPSHOT.jar"});
    JavaRDD<String> logData = sc.textFile(logFile).cache();

    long numAs = logData.filter(new Function<String, Boolean>() {
      public Boolean call(String s) { return s.contains("a"); }
    }).count();

    long numBs = logData.filter(new Function<String, Boolean>() {
      public Boolean call(String s) { return s.contains("b"); }
    }).count();

    System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);
  }
}