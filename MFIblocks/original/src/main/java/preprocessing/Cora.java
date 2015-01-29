package preprocessing;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Cora {

	/**
	 * @param args
	 */
	private static Map<String,Set<Integer>> matches;
	private static BufferedWriter numericOutputWriter;
	private static BufferedWriter stringOutputWriter;
	private static BufferedWriter matchWriter;
	private static Set<String> distinctRecords;
	private static LexiconProfiles lexicon;
	private static String swFile;
	private static WordProcessor wordProcessor;
	
	public static void main(String[] args) {
		String xmlInputFile = args[0];
		matches = new HashMap<String,Set<Integer>>();	
		distinctRecords = new HashSet<String>();
		String outputFile = args[1];
		String matchFile = args[2];
		String paramsFile = args[3];
		swFile = args[4];
		String lexiconOutFile = args[5];
		String publicationsOutFile = args[6];
		 
		
		lexicon = new LexiconProfiles(paramsFile);
		wordProcessor = new WordProcessor(new File(swFile));
		
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		File file = new File(xmlInputFile);
	    try {
	    	numericOutputWriter = new BufferedWriter(new FileWriter
					(new File(outputFile)));
	    	matchWriter = new BufferedWriter(new FileWriter
					(new File(matchFile)));
	    	stringOutputWriter = new BufferedWriter(new FileWriter
					(new File(publicationsOutFile)));
	    	
			DocumentBuilder builder = fact.newDocumentBuilder();
			Document doc = builder.parse(file);
			Element root = doc.getDocumentElement();	        
	        System.out.println("Root Node: " + root.getNodeName());
	        NodeList publications = doc.getElementsByTagName("publication");
	        System.out.println("number of publications: " + publications.getLength());
	       
	        int pubId = 1;
	        for(int i = 0; i < publications.getLength() ; i++){
	        	Element publicationElem = (Element)publications.item(i);
	        	String publication = getPublicationString(publicationElem);
	        	if(distinctRecords.contains(publication)){
	        		continue; //eliminate duplicates
	        	}
	        	distinctRecords.add(publication);
	        	addPublicationToMatch(publicationElem.getAttribute("id"),
	        			publication,pubId);
	        	
	        	String numericPublication = getPublicationNGramIdString(pubId, publicationElem);
	        	//String numericPublication = getPublicationWordIdString(pubId, publicationElem);
	        	pubId++;
	        	numericOutputWriter.write(numericPublication);
				numericOutputWriter.newLine();
				stringOutputWriter.write(publication);
				stringOutputWriter.newLine();
	        }
	        System.out.println("number of distinct publications: " + distinctRecords.size());
	        StringBuilder sb = new StringBuilder();
	        //write the matches
	        for (Map.Entry<String,Set<Integer>> match : matches.entrySet()) {
	        	sb = new StringBuilder();	        	
				Set<Integer> cluster = match.getValue();
			//	sb.append(match.getKey()).append(" ").append(cluster.size()).append(": ");
				if(cluster.size() < 2) continue;
				for (Integer integer : cluster) {
					sb.append(integer).append(" ");
				}
				matchWriter.write(sb.toString());
				matchWriter.newLine();
			}
	        numericOutputWriter.close();
	        stringOutputWriter.close();
	        matchWriter.close();	        
	        lexicon.exportToPropFile(lexiconOutFile);
	        
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	private static void addPublicationToMatch(String pubId, String publication, int index){
		Set<Integer> pub_matches = null;
		if(matches.containsKey(pubId)){
			pub_matches = matches.get(pubId);
		}
		else{
			pub_matches = new HashSet<Integer>();
		}
		pub_matches.add(index);
		matches.put(pubId, pub_matches);
	}
	
	@SuppressWarnings("unused")
	private static String getPublicationWordIdString(int recordId, Element publication){		
		List<Integer> publicationIds = new ArrayList<Integer>();
		
		NodeList authors = publication.getElementsByTagName("author");
		String authorsVal = concatNodeValues(authors);
		publicationIds.addAll(getNGramIds(getWords(authorsVal),recordId,1));
		
		NodeList titleWords = publication.getElementsByTagName("title");
		String titlesVal = concatNodeValues(titleWords);
		publicationIds.addAll(getNGramIds(getWords(titlesVal),recordId,2));		
		
		Element venueNode =  (Element) publication.getElementsByTagName("venue").item(0);
		NodeList venueNameWords = venueNode.getElementsByTagName("name");
		String venueNameVals = concatNodeValues(venueNameWords);
		publicationIds.addAll(getNGramIds(getWords(venueNameVals),recordId,3));		
		
		NodeList dateNodes = venueNode.getElementsByTagName("date");
		if(dateNodes != null){
			String venueDateVals = concatNodeValues(dateNodes);
			publicationIds.addAll(getNGramIds(getWords(venueDateVals),recordId,3));
		}
		
		NodeList volNode = venueNode.getElementsByTagName("vol");
		if(volNode != null){
			String volNodeVals = concatNodeValues(volNode);
			publicationIds.addAll(getNGramIds(getWords(volNodeVals),recordId,3));						
		}				
		
		return WordProcessor.concatListMembers(publicationIds);		
	}
	
	
	private static String getPublicationNGramIdString(int recordId, Element publication){		
		List<Integer> publicationNGramsIds = new ArrayList<Integer>();
		
		NodeList authors = publication.getElementsByTagName("author");
		String authorsVal = concatNodeValues(authors);		
		publicationNGramsIds.addAll(getNGramIds(getNGrams(authorsVal),recordId,1));
		
		NodeList titleWords = publication.getElementsByTagName("title");
		String titlesVal = concatNodeValues(titleWords);
		publicationNGramsIds.addAll(getNGramIds(getNGrams(titlesVal),recordId,2));
		
		Element venueNode =  (Element) publication.getElementsByTagName("venue").item(0);
		NodeList venueNameWords = venueNode.getElementsByTagName("name");
		String venueNameVals = concatNodeValues(venueNameWords);
		publicationNGramsIds.addAll(getNGramIds(getNGrams(venueNameVals),recordId,3));
		
		NodeList dateNodes = venueNode.getElementsByTagName("date");
		if(dateNodes != null){
			String venueDateVals = concatNodeValues(dateNodes);
			publicationNGramsIds.addAll(getNGramIds(getNGrams(venueDateVals),recordId,3));			
		}
		
		NodeList volNode = venueNode.getElementsByTagName("vol");
		if(volNode != null){
			String volNodeVals = concatNodeValues(volNode);
			publicationNGramsIds.addAll(getNGramIds(getNGrams(volNodeVals),recordId,3));			
		}				
		
		return WordProcessor.concatListMembers(publicationNGramsIds);		
	}
	
	@SuppressWarnings("unused")
	private static String getPublicationNGramString(int recordId, Element publication){
		
		List<String> publicationNGrams = new ArrayList<String>();
		//currPublication.append(pubId).append(": ");
		NodeList authors = publication.getElementsByTagName("author");
		String authorsVal = concatNodeValues(authors);
		publicationNGrams.addAll(getNGrams(authorsVal));
		
		NodeList titleWords = publication.getElementsByTagName("title");
		String titlesVal = concatNodeValues(titleWords);
		publicationNGrams.addAll(getNGrams(titlesVal));
					
		Element venueNode =  (Element) publication.getElementsByTagName("venue").item(0);
		NodeList venueNameWords = venueNode.getElementsByTagName("name");
		String venueNameVals = concatNodeValues(venueNameWords);
		publicationNGrams.addAll(getNGrams(venueNameVals));
		
		NodeList dateNodes = venueNode.getElementsByTagName("date");
		if(dateNodes != null){
			String venueDateVals = concatNodeValues(dateNodes);
			publicationNGrams.addAll(getNGrams(venueDateVals));
		}
		
		NodeList volNode = venueNode.getElementsByTagName("vol");
		if(volNode != null){
			String volNodeVals = concatNodeValues(volNode);
			publicationNGrams.addAll(getNGrams(volNodeVals));

		}				
		return WordProcessor.concatListMembers(publicationNGrams);
	}

	private static String getPublicationString(Element publication){
		StringBuilder retVal = new StringBuilder();
		
		NodeList authors = publication.getElementsByTagName("author");
		String authorsVal = concatNodeValues(authors).trim();
		retVal.append(authorsVal).append(" ");
		
		NodeList titleWords = publication.getElementsByTagName("title");
		String titlesVal = concatNodeValues(titleWords).trim();
		retVal.append(titlesVal).append(" ");
		
		Element venueNode =  (Element) publication.getElementsByTagName("venue").item(0);
		NodeList venueNameWords = venueNode.getElementsByTagName("name");
		String venueNameVals = concatNodeValues(venueNameWords).trim();
		retVal.append(venueNameVals).append(" ");
		
		NodeList dateNodes = venueNode.getElementsByTagName("date");
		if(dateNodes != null){
			String venueDateVals = concatNodeValues(dateNodes).trim();
			retVal.append(venueDateVals).append(" ");
		}
		
		NodeList volNode = venueNode.getElementsByTagName("vol");
		if(volNode != null){
			String volNodeVals = concatNodeValues(volNode).trim();
			retVal.append(volNodeVals);

		}			
		
		String concatedString = retVal.toString().trim();
		List<String> swFreeTerms = wordProcessor.removeStopwordsAndSpecialChars(concatedString);
		return WordProcessor.concatListMembers(swFreeTerms);
		
		
	}
	
	private static String concatNodeValues(NodeList nl){
		StringBuilder sb = new StringBuilder();
		for(int i=0 ; i < nl.getLength() ; i++){
			sb.append(nl.item(i).getTextContent().toLowerCase()).append(" ");
		}
		return sb.toString();
	}
	
	
	
	private static List<String> getNGrams(String value){
		return wordProcessor.processValue(value);
	}
	
	private static List<String> getWords(String value){
		return wordProcessor.removeStopwordsAndSpecialChars(value);
	}
	
	
	private static List<Integer> getNGramIds(List<String> nGrams, int recordId, int attributeId){		
		List<Integer> retVal = new ArrayList<Integer>(nGrams.size());		
		for (String nGram : nGrams) {
			retVal.add(lexicon.addWord(attributeId, recordId, nGram));
		}
		return retVal;
	}
	
	
}
