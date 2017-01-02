package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.parsers.SerializerUtil;
import il.ac.technion.ie.experiments.service.CanopyService;
import il.ac.technion.ie.model.Record;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by I062070 on 23/10/2016.
 */
public class ProcessCanopies {

    private static final Logger logger = Logger.getLogger(ProcessCanopies.class);

    private CanopyService canopyService;
    private BiMap<File, Collection<CanopyCluster>> fileToCanopies;
    private Table<String, String, Set<File>> allCanopies;

    public ProcessCanopies() {
        canopyService = new CanopyService();
    }

    public void execute(String pathToDirFolder) {
        this.readAndParseCanopiesFromDir(pathToDirFolder);
        //for each dataset
            //for each permutation
                //for random generation of canopies
        this.calculateBaselineResults(null);
        this.getBaselineRepresentatives(null);
        this.executeConvexBP(null);
        this.getBcbpRepresentatives(null);
        this.calculateMeasurements(null);
    }

    private void readAndParseCanopiesFromDir(String dirPath) {
        FilesReader filesReader = new FilesReader(dirPath);
        allCanopies = filesReader.getAllCanopies();
        fileToCanopies = HashBiMap.create(allCanopies.size());
        //each combination of (parameterUnderTest + ParamValue) has several seized canopies
        for (Set<File> files : allCanopies.values()) {
            for (File file : files) {
                Collection<CanopyCluster> canopyClusters = SerializerUtil.deSerializeCanopies(file);
                fileToCanopies.put(file, canopyClusters);
            }
        }
    }

    private void calculateBaselineResults(List<BlockWithData> blocks) {

    }
    private Multimap<Record, BlockWithData> getBaselineRepresentatives(List<BlockWithData> blocks) {
        return null;
    }

    private boolean executeConvexBP(List<BlockWithData> blocks) {
        return false;
    }

    private Multimap<Record, BlockWithData> getBcbpRepresentatives(List<BlockWithData> blocks) {
        return null;
    }

    private void calculateMeasurements(List<BlockWithData> blocks) {

    }
}
