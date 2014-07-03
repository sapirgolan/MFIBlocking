package preprocessing;

public class DatasetType {
	
	private String name;
	
	
	private final String[] types = DatasetName.names;
	
	
	public DatasetType (String input){
		int index=Integer.parseInt(input)-1;
		if (types.length > index && index > -1)
			name=types[index];
	}
	
	public String getName(){
		return name;
	}
	
	
}
