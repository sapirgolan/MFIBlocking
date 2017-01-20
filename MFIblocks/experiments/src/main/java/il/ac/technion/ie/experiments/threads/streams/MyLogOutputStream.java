package il.ac.technion.ie.experiments.threads.streams;

import il.ac.technion.ie.experiments.threads.IProcessOutputHandler;
import org.apache.commons.exec.LogOutputStream;

/**
 * Created by I062070 on 20/01/2017.
 */
public class MyLogOutputStream extends LogOutputStream {

    private IProcessOutputHandler handler;
    private boolean forewordToStandardOutput;

    public MyLogOutputStream(IProcessOutputHandler handler, boolean forewordToStandardOutput) {
        this.handler = handler;
        this.forewordToStandardOutput = forewordToStandardOutput;
    }

    @Override
    protected void processLine(String line, int logLevel) {
        if (forewordToStandardOutput){
            handler.onStandardOutput(line);
        }
        else{
            handler.onStandardError(line);
        }
    }
}
