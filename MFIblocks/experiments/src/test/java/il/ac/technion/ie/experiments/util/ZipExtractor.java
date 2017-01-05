package il.ac.technion.ie.experiments.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class ZipExtractor {


    public static void extractZipFromResources(File rootFolder, String zipPathInResources) throws IOException, URISyntaxException, ZipException {
        File tesZip = new File(ZipExtractor.class.getResource(zipPathInResources).toURI());
        ZipFile zipFile = new ZipFile(tesZip);
        zipFile.extractAll(rootFolder.getAbsolutePath());
    }
}