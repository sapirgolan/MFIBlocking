package il.ac.technion.ie.experiments.parsers;

import il.ac.technion.ie.canopy.model.CanopyCluster;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;

/**
 * Created by I062070 on 22/01/2016.
 */
public class SerializerUtil {

    private static final Logger logger = Logger.getLogger(SerializerUtil.class);

    public static boolean serializeCanopies(File canopiesFile, List<CanopyCluster> canopies) {
        boolean wasSerialized = false;

        try (FileOutputStream outputStream = new FileOutputStream(canopiesFile)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
                oos.writeObject(canopies);
                oos.close();
            } catch (IOException e) {
                logger.error("Failed to write canopies to: '" + canopiesFile.getAbsolutePath() + "'", e);
            }
            logger.info("Successfully written canopies to: '" + canopiesFile.getAbsolutePath() + "'");
            wasSerialized = true;
            outputStream.close();
        } catch (IOException e) {
            logger.error("Failed to open OutputStream from: '" + canopiesFile.getAbsolutePath() + "'", e);
        }
        return wasSerialized;
    }

    public static List<CanopyCluster> deSerializeCanopies(File canopiesFile) {
        List<CanopyCluster> canopies = null;

        try (FileInputStream inputStream = new FileInputStream(canopiesFile)) {
            try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
                canopies = (List<CanopyCluster>) ois.readObject();
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Failed to read canopies to: '" + canopiesFile.getAbsolutePath() + "'", e);
            }
            logger.debug("Successfully read canopies to: '" + canopiesFile.getAbsolutePath() + "'");
            inputStream.close();
        } catch (IOException e) {
            logger.error("Failed to open InputStream from: '" + canopiesFile.getAbsolutePath() + "'", e );
        }
        return canopies;
    }
}
