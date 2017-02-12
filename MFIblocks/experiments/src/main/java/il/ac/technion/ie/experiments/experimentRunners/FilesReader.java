package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import il.ac.technion.ie.experiments.model.BlocksMapper;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.Set;

/**
 * Created by I062070 on 15/12/2016.
 */
public class FilesReader {

    private static final Logger logger = Logger.getLogger(ProcessCanopies.class);

    private File root;

    public FilesReader(String rootDir) {

        File file = new File(rootDir);
        if (file.exists()) {
            root = file;
        } else {
            logger.error(rootDir + " folder doesn't exists");
        }
    }

    protected Collection<File> listAllFiles() {
        return FileUtils.listFiles(root, null, true);
    }


    /**
     * The structure of the returned object is
     * --01_NumberOfOriginalRecords (scenario)
     * ----permutationWith_parameter=25 (permutation)
     * ------canopy_0 (files that fits to scenario + permutation)
     * ------canopy_1 (files that fits to scenario + permutation)
     *
     * @return
     */
    public Table<String, String, Set<File>> getAllCanopies() {
        Table<String, String, Set<File>> canopiesTable = HashBasedTable.create();
        Collection<File> allCanopiesFiles = this.listAllFiles();
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

    public Collection<File> getAllDatasets() {
        return this.listAllFiles();
    }

    public BlocksMapper getAllBlocks() {
        Collection<File> allBlockFiles = this.listAllFiles();
        BlocksMapper blocksMapper = new BlocksMapper();
        for (File blockFile : allBlockFiles) {
            blocksMapper.add(blockFile);
        }
        return blocksMapper;
    }
}
