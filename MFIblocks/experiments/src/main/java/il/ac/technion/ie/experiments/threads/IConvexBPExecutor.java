package il.ac.technion.ie.experiments.threads;

import il.ac.technion.ie.experiments.exception.OSNotSupportedException;
import il.ac.technion.ie.experiments.model.ConvexBPContext;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by I062070 on 20/01/2017.
 */
public interface IConvexBPExecutor {
    public File execute(ConvexBPContext context) throws IOException, OSNotSupportedException, InterruptedException, ExecutionException;
}
