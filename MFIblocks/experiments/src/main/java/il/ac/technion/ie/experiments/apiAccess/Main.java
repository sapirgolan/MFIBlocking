package il.ac.technion.ie.experiments.apiAccess;

import il.ac.technion.ie.experiments.service.ParsingService;
import il.ac.technion.ie.experiments.service.ProbabilityService;

/**
 * Created by I062070 on 27/08/2015.
 */
public class Main {

    private ParsingService parsingService;
    private ProbabilityService probabilityService;

    public Main() {
        parsingService = new ParsingService();
        probabilityService = new ProbabilityService();
    }
}
