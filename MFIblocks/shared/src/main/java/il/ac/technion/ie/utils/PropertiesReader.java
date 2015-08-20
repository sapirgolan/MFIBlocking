package il.ac.technion.ie.utils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by I062070 on 20/06/2015.
 */
public class PropertiesReader {

    public static List<String> getFields(String scenario) throws ConfigurationException {
        PropertiesConfiguration propertiesConfiguration = init();
        String[] stringArray = propertiesConfiguration.getStringArray(scenario);
        return new ArrayList<>(Arrays.asList(stringArray));
    }

    private static PropertiesConfiguration init() throws ConfigurationException {
        URL resource = PropertiesReader.class.getClass().getResource("/fieldsName.properties");
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(resource);
        return propertiesConfiguration;
    }
}
