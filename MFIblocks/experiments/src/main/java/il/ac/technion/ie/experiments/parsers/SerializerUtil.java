package il.ac.technion.ie.experiments.parsers;

import com.google.common.collect.Lists;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Created by I062070 on 22/01/2016.
 */
public class SerializerUtil {

    private static final Logger logger = Logger.getLogger(SerializerUtil.class);

    public static boolean serializeCanopies(File canopiesFile, Collection<CanopyCluster> canopies) {
        return serialize(canopiesFile, canopies);
    }

    public static <T> boolean serialize(File serializeFile, Collection<T> entities) {
        boolean wasSerialized = false;

        try (FileOutputStream outputStream = new FileOutputStream(serializeFile)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
                oos.writeObject(entities);
                oos.close();
            } catch (IOException e) {
                logger.error("Failed to write entities to: '" + serializeFile.getAbsolutePath() + "'", e);
            }
            logger.info("Successfully written entities to: '" + serializeFile.getAbsolutePath() + "'");
            wasSerialized = true;
            outputStream.close();
        } catch (IOException e) {
            logger.error("Failed to open OutputStream from: '" + serializeFile.getAbsolutePath() + "'", e);
        }
        return wasSerialized;
    }

    public static <T> Collection<T> deSerialize(File file) {
        Collection<T> serializedObjects = null;

        try (FileInputStream inputStream = new FileInputStream(file)) {
            try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
                serializedObjects = (Collection<T>) ois.readObject();
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Failed to read serialized Objects to: '" + file.getAbsolutePath() + "'", e);
            }
            logger.debug("Successfully read serialized Objects to: '" + file.getAbsolutePath() + "'");
            inputStream.close();
        } catch (IOException e) {
            logger.error("Failed to open InputStream from: '" + file.getAbsolutePath() + "'", e );
        }
        return serializedObjects;
    }


    public static Collection<CanopyCluster> deSerializeCanopies(File canopiesFile) {
        return SerializerUtil.deSerialize(canopiesFile);
    }
}
