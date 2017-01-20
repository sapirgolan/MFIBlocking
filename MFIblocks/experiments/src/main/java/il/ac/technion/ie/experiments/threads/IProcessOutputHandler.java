package il.ac.technion.ie.experiments.threads;

/**
 * Created by I062070 on 20/01/2017.
 */
public interface IProcessOutputHandler {
    public void onStandardOutput(String msg);
    public void onStandardError(String msg);
}
