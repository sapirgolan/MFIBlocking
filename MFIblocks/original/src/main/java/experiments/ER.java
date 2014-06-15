package experiments;

import java.io.IOException;

import preprocessing.ProfileReader;

public class ER {
	
	public static void main(String[] args) throws IOException{
		//1. files creation step
		final String profilesFiles="Data/profiles/10Kprofiles";
		final String numericFile="3gram/DBPedia_numeric.txt"; 
		final String matchFile="3gram/DBPedia_match.txt";
		final String groundTruthFile="Data/groundtruth/10KIdDuplicates";
		final String stopWordsFile="stopwords.txt";
		final String lexiconFile="3gram/DBPedia_lexicon_3grams.txt";
		final String recordsFile="3gram/DBPedia_NoSW.txt";
		final String nGramParameter="3"; 
		final String pruningThreshold="0.0";
		String[] parameters = new String[]{profilesFiles,numericFile,matchFile,groundTruthFile,stopWordsFile,
				lexiconFile,recordsFile,nGramParameter,pruningThreshold};
		//rofileReader profileReader = new ProfileReader(parameters);
		
		//2. blocking step
		//3. ER step
	}
	
	
}
