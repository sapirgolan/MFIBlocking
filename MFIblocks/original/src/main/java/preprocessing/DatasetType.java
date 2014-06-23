package preprocessing;

public class DatasetType {
	
	private String name;
	private final String CORA = "CORA";
	private final String CENSUS = "CENSUS";
	private final String CDDB = "CDDB";
	private final String MOVIES = "MOVIES";
	private final String RESTS = "RESTS";
	private final String DBPEDIA = "DBPedia";
	private final String[] types = {CORA,CENSUS,CDDB,MOVIES,RESTS,DBPEDIA};
	
	
	public DatasetType (String input){
		int index=Integer.parseInt(input)-1;
		if (types.length > index && index > -1)
			name=types[index];
	}
	
	public String getName(){
		return name;
	}
}
