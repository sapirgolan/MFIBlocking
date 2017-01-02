package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import il.ac.technion.ie.canopy.algorithm.Canopy;
import il.ac.technion.ie.experiments.service.CanopyService;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by I062070 on 15/12/2016.
 */
public class FilesReader {

    private static final Logger logger = Logger.getLogger(ProcessCanopies.class);

    private File root;

    public FilesReader(String rootPath) {

        File file = new File(rootPath);
        if (file.exists()) {
            root = file;
        } else {
            logger.error(rootPath + " folder doesn't exists");
        }
    }

    private Collection<File> listAllCanopies() {
        return FileUtils.listFiles(root, null, true);
    }

    public Table<String, String, Set<File>> getAllCanopies() {
        Table<String, String, Set<File>> canopiesTable = HashBasedTable.create();
        Collection<File> allCanopiesFiles = this.listAllCanopies();
        for (File canopyFile : allCanopiesFiles) {
            String permutationWithParam_columnKey = canopyFile.getParentFile().getName();
            String datasetPermutation_rowKey = canopyFile.getParentFile().getParentFile().getName();
            boolean hasCanopyFiles = canopiesTable.contains(datasetPermutation_rowKey, permutationWithParam_columnKey);
            if (!hasCanopyFiles) {
                canopiesTable.put(datasetPermutation_rowKey, permutationWithParam_columnKey, Sets.newHashSet(canopyFile));
            } else {
                canopiesTable.get(datasetPermutation_rowKey, permutationWithParam_columnKey).add(canopyFile);
            }
        }
        return canopiesTable;
    }
}
