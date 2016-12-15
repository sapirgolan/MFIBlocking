package il.ac.technion.ie.experiments.experimentRunners;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;

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

}
