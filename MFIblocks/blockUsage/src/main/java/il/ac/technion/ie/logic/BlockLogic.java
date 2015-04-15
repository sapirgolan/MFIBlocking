package il.ac.technion.ie.logic;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by I062070 on 13/03/2015.
 */
public class BlockLogic implements iBlockLogic {

    static final Logger logger = Logger.getLogger(BlockLogic.class);
    private iFindBlockAlgorithm algorithm;


    public BlockLogic() {
        this.algorithm = new FindBlockAlgorithm();
    }

    @Override
    public List<Block> findBlocks(CandidatePairs candidatePairs) {
        ConcurrentHashMap<Integer, RecordMatches> matches = candidatePairs.getAllMatches();
        Set<Integer> recordsIds = matches.keySet();

        List<NeighborsVector> neighborsVectors = buildNeighborVectors(matches, recordsIds);

        algorithm.sort(neighborsVectors, new NeighborsVectorsCompare());
        return algorithm.findBlocks(neighborsVectors);
    }

    @Override
    public void calcProbabilityOnRecords(final List<Block> blocks, MfiContext context) {
        Multimap<Integer, String> termsOfEachRecord = obtainTerms(blocks, context);
        tokenizeTerms(termsOfEachRecord);
        calcProbability();
    }

    private void calcProbability() {
    }

    private void tokenizeTerms(Multimap<Integer, String> termsOfEachRecord) {
    }

    /**
     * Each record is identified by an ID {@link java.lang.Integer}.
     * The method obtains the terms that appear in each record, tokenize them
     * and return a {@link Multimap} of it.
     *
     * @param blocks
     * @param context
     * @return
     */

    private Multimap<Integer, String> obtainTerms(List<Block> blocks, MfiContext context) {
        //TODO: for each ID retrieve terms
        //TODO: tokenize each term
        ArrayListMultimap<Integer, String> arrayListMultimap = ArrayListMultimap.create();
        String recordsPath = context.getOriginalRecordsPath();
        Map<Integer, String> records = retriveRecords(recordsPath);
        return arrayListMultimap;
    }

    private Map<Integer, String> retriveRecords(String recordsPath) {
        int lineNumber = 1;
        Map<Integer, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(recordsPath))))) {
            String line = reader.readLine();
            while (line != null) {
                map.put(lineNumber, line);
                lineNumber++;
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            logger.error(recordsPath + " was not found in system", e);
        } catch (IOException e) {
            logger.error("Failed to read lines form " + recordsPath, e);
        }
        return map;
    }

    private List<NeighborsVector> buildNeighborVectors(ConcurrentHashMap<Integer, RecordMatches> matches, Set<Integer> recordsIds) {
        int numberOfRecords = recordsIds.size();
        List<NeighborsVector> neighborsVectors = new ArrayList<>(numberOfRecords);
        for (Integer recordsId : recordsIds) {
            NeighborsVector vector = new NeighborsVector(recordsId, numberOfRecords);
            Set<Integer> matchedIds = matches.get(recordsId).getMatchedIds();
            for (Integer matchedId : matchedIds) {
                vector.exitsNeighbor(matchedId);
            }
            neighborsVectors.add(vector);
        }
        logger.debug("Finished building 'NeighborVectors' for matched input");
        return neighborsVectors;
    }

}
