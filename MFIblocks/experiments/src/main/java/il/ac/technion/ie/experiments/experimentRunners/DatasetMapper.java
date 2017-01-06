package il.ac.technion.ie.experiments.experimentRunners;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collection;

/**
 * Created by I062070 on 06/01/2017.
 */
public class DatasetMapper {

    public static File getDatasetFile(String permutation, Collection<File> allDatasetPermutations) {
        String permuataionParamenter = StringUtils.substringAfterLast(permutation, "_");
        for (File datasetPermutation : allDatasetPermutations) {
            String filePermutation = StringUtils.substringBetween(datasetPermutation.getName(), "=", ".");
            if (permuataionParamenter.equals(filePermutation)) {
                return datasetPermutation;
            }
        }
        return null;
    }
}
