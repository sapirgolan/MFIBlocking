package preprocessing;

public class ExperimentsRunner {

	public static final String PROFILES_DIRECTORY="../data/profiles/";
	
	/**
	 * //1. memory < in MB > 
		//2. datasets
		//3. q-grams
		//4. thresholds
		//5. limit
	 */
	public static void main(String[] args) {
		//java -Xmx40000M -jar CreateFiles-1.jar ../data/profiles/dbpedia30rc,../data/profiles/dbpedia34 3gram/DBPedia_numeric.txt 3gram/DBPedia_match.txt ../data/groundtruth/dbpediaIdGroundTruth stopwords.txt 3gram/DBPedia_lexicon_3grams.txt 3gram/DBPedia_NoSW.txt 3 0.0 6 1000000 > DBPedia_3grams.out
		
		String memoryLimit=args[0];
		int dataset=Integer.parseInt(args[1]);
		String[] qgrams=args[2].split(",");
		String[] thresholds=args[3].split(",");
		String datasetLimit=args[4];
		String comandLine = "java -Xmx"+memoryLimit+"-jar CreateFiles-1.jar ";
		
		if (dataset==4 || dataset==6){
			String[] fileNames=DatasetName.names[dataset].split(",");
			for (int i=0; i<fileNames.length;i++){
				comandLine+=PROFILES_DIRECTORY+fileNames[i];
				if (i<fileNames.length-1) comandLine+=",";
			}	
		}
		else {
			comandLine+=PROFILES_DIRECTORY+DatasetName.names[dataset];
		}
		comandLine+=" ";
		for (String qgram : qgrams){
			for (String th : thresholds){
				;
			}
		}
		
		
		
		
	}

}
