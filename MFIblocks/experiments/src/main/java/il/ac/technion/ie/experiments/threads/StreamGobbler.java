package il.ac.technion.ie.experiments.threads;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by I062070 on 26/09/2015.
 */
public class StreamGobbler extends Thread {

    static final Logger logger = Logger.getLogger(StreamGobbler.class);
    private final static String NEW_LINE = System.getProperty("line.separator");

    private InputStream is;
    private ChanelType type;

    StreamGobbler(InputStream is, ChanelType type) {
        this.is = is;
        this.type = type;
    }

    public void run() {
        try {
            StringBuilder log = new StringBuilder();
            StringBuilder builder = new StringBuilder();

            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                log.append(NEW_LINE);
                builder.append(line);
            }
            if (ChanelType.ERROR.equals(type)) {
                logger.error(log.toString());
            } else {
                logger.info(log.toString());
            }
            logger.info(type + ">" + builder.toString());

        } catch (IOException ioe) {
            logger.error("Failed to run Stream Gobbler", ioe);
        }
    }

    public enum ChanelType {
        ERROR,
        OUTPUT
    }
}
