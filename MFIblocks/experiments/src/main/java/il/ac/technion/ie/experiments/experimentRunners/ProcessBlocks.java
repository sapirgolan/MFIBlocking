package il.ac.technion.ie.experiments.experimentRunners;

import il.ac.technion.ie.experiments.model.BlocksMapper;

import java.io.File;
import java.util.Collection;

/**
 * Created by I062070 on 10/02/2017.
 */
public class ProcessBlocks {

    public void runExperiments(String pathToBlocksFolder, String pathToOriginalDatasetFile) {
        BlocksMapper blocksMapper =  new FilesReader(pathToBlocksFolder).getAllBlocks();
        Collection<File> allDatasetPermutations = new FilesReader(pathToOriginalDatasetFile).getAllDatasets();


    }
}
