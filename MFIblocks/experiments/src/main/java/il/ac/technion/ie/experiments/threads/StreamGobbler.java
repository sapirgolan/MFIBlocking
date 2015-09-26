package il.ac.technion.ie.experiments.threads;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by I062070 on 26/09/2015.
 */
public class StreamGobbler extends Thread {

    static final Logger logger = Logger.getLogger(StreamGobbler.class);

    private InputStream is;
    private String type;
    private OutputStream outputStream;

    StreamGobbler(InputStream is, String type) {
        this(is, type, null);
    }

    StreamGobbler(InputStream is, String type, OutputStream redirect) {
        this.is = is;
        this.type = type;
        this.outputStream = redirect;
    }

    public void run() {
        try {
            StringBuilder builder = new StringBuilder();
            PrintWriter printWriter = null;
            if (outputStream != null) {
                printWriter = new PrintWriter(outputStream);
            }

            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (printWriter != null) {
                    printWriter.println(line);
                }
                builder.append(line);
            }
            logger.info(type + ">" + builder.toString());

            if (printWriter != null)
                printWriter.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
