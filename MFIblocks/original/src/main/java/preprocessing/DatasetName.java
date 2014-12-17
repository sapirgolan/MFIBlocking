package preprocessing;

public class DatasetName {
	private static final String CORA = "CORA";
	private static final String CENSUS = "CENSUS";
	private static final String CDDB = "CDDB";
	private static final String MOVIES = "MOVIES";
	private static final String RESTS = "RESTS";
	private static final String DBPEDIA = "DBPedia";
	private static final String OTHER = "other";
	
	public static final String[] names = {CORA, CENSUS, CDDB, MOVIES, RESTS, DBPEDIA, OTHER};
	
	public static final String[] fileNames = {"coraProfiles",
			"censusProfiles",
			"cddbProfilesNEW",
			"dbpediaMovies,imdbMovies",
			"restaurantProfiles",
			"dbpedia30rc,dbpedia34",
			"other"};

}
