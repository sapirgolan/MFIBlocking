MFIBlockingParallelized
===========

MFI Blocking repository

This project uses MAVEN, so after you checkout the code you have to install maven (If you havn't done it already):
http://maven.apache.org/download.cgi

Also install the maven plugin for eclipse: http://www.eclipse.org/m2e/download/

If maven is not in your path you can open command line and run "env.bat", it will add it as long as the command line is opened.
for MFIBlocks project you have to "install" 3 jars.
inorder to do it simply run the following commands from the command line:


mvn install:install-file -Dfile=.\src\main\resources\Classmexer\classmexer.jar -DgroupId=com.javamex -DartifactId=Classmexer -Dversion=0.03 -Dpackaging=jar
mvn install:install-file -Dfile=.\src\main\resources\sbs\SBS.jar -DgroupId=org.enerj.core -DartifactId=SBS -Dversion=0.0.1 -Dpackaging=jar
mvn install:install-file -Dfile=.\src\main\resources\simmetrics\simmetrics.jar -DgroupId=uk.ac.shef.wit -DartifactId=simmetrics -Dversion=1.6.2 -Dpackaging=jar

In order to run MFIBlocks Add the following VM arguments to Java Virtaul Machine (JVM):
-Xmx1300M
-javaagent:.\src\main\resources\Classmexer\classmexer.jar
-Djava.library.path=.\src\main\resources\hyperic-sigar-1.6.4\sigar-bin\lib

For illustration:
java -Xmx1300M -javaagent:.\src\main\resources\Classmexer\classmexer.jar -Djava.library.path=.\src\main\resources\hyperic-sigar-1.6.4\sigar-bin\lib -jar EntityResolution.jar ...(EntityResolution parameters)

