package preprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CDs {

	private static Map<String,List<Integer>> matches;
	private static BufferedWriter numericOutputWriter;
	private static BufferedWriter stringOutputWriter;
	private static BufferedWriter matchWriter;	
	private static Lexicon lexicon;
	private static String swFile;
	private static WordProcessor wordProcessor;

	public static void main(String[] args) {
		String xmlInputFile = args[0];
		matches = new HashMap<String,List<Integer>>();		
		String numericOutputFile = args[1];
		String matchFile = args[2];
		String paramsFile = args[3];
		swFile = args[4];
		String lexiconOutFile = args[5];
		String discOutFile = args[6];
		String inputMatchFile = args[7];
		
		lexicon = new Lexicon(paramsFile);
		wordProcessor = new WordProcessor(new File(swFile));

		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		File file = new File(xmlInputFile);
		

		try {
			numericOutputWriter = new BufferedWriter(new FileWriter(new File(
					numericOutputFile)));
			matchWriter = new BufferedWriter(
					new FileWriter(new File(matchFile)));
			stringOutputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(discOutFile),"UTF16"));

			DocumentBuilder builder = fact.newDocumentBuilder();
			Document doc = builder.parse(file);
			Element root = doc.getDocumentElement();	        
	        System.out.println("Root Node: " + root.getNodeName());
	        NodeList discs = doc.getElementsByTagName("disc");
	        System.out.println("number of discs: " + discs.getLength());
	        int discId = 1;
	        Map<String,Integer> discToId = new HashMap<String, Integer>();
	        for(int i = 0; i < discs.getLength() ; i++){
	        	Element discElem = (Element)discs.item(i);
	        	String disc = getDiscString(discElem);
	        	String numericDisc = getDiscNGramIdString(discId, discElem);
	        	discToId.put(disc, discId);
	        	discId++;
	        	numericOutputWriter.write(numericDisc);
				numericOutputWriter.newLine();
				stringOutputWriter.write(disc.trim());
				stringOutputWriter.newLine();
		        
	        }
	        Set<List<Integer>> matches = readMatchFile(inputMatchFile,discToId);
	        
	        //write the matches	       
	        int numOfpairs = 0;
	        for (List<Integer> match : matches) {
	        	StringBuilder sb = new StringBuilder();
	        	Collections.sort(match);
				if(match.size() < 2) continue;
				numOfpairs += match.size()*(match.size()-1)*0.5;
				for (Integer integer : match) {
					sb.append(integer).append(" ");
				}
				matchWriter.write(sb.toString());
				matchWriter.newLine();
			}
	        numericOutputWriter.close();
	        stringOutputWriter.close();
	        matchWriter.close();	        
	        lexicon.exportToPropFile(lexiconOutFile);
	        System.out.println("total number of pairs in match file: " + numOfpairs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	private static Set<List<Integer>> readMatchFile(String xmlMatchFile,Map<String,Integer> discToId){
		Set<List<Integer>> retVal = new HashSet<List<Integer>>();
		File file = new File(xmlMatchFile);
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = fact.newDocumentBuilder();
			Document doc = builder.parse(file);
			Element root = doc.getDocumentElement();	        
	        System.out.println("Root Node: " + root.getNodeName());
	        NodeList pairs = doc.getElementsByTagName("pair");
	        System.out.println("number of pairs: " + pairs.getLength());
	        Set<Integer> skipped= new HashSet<Integer>();
	        for(int i=0 ; i < pairs.getLength() ; i++){
	        	Element pairElem = (Element)pairs.item(i);
	        	NodeList discPairs = pairElem.getElementsByTagName("disc");
	        	if(discPairs.getLength() != 2){
	        		System.out.println("found pair of size " + discPairs.getLength());
	        	}
	        	String disc1 = getDiscString((Element)discPairs.item(0));
	        	String disc2 = getDiscString((Element)discPairs.item(1));
	        	int disc1Id = -1;
	        	int disc2Id = -1;
	        	if(discToId.containsKey(disc1)){
	        		disc1Id = discToId.get(disc1);
	        	}
	        	else{
	        		System.out.println("No id for disc " + disc1);
	        	}
	        	if(discToId.containsKey(disc2)){
	        		disc2Id = discToId.get(disc2);
	        	}
	        	else{
	        		System.out.println("No id for disc " + disc2);
	        	}
	        	if(disc1Id < 0 || disc2Id < 0){
	        		skipped.add(i);
	        		continue;	        		
	        	}
	        	List<Integer> newlist = new ArrayList<Integer>(2);
	        	newlist.add(disc1Id);
	        	newlist.add(disc2Id);
	        	retVal.add(newlist);
	        }
	       System.out.println("Number of pairs writtern: " + retVal.size());
	       System.out.println("Skipped pairs: " + skipped.toString());
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
		 return retVal;
	}
	
	@SuppressWarnings("unused")
	private static void addDiscToMatch(String discId, int index){
		if(discId == null || discId.length() == 0 || discId.equals(" ")){
		//	return;
		}
		List<Integer> disc_matches = null;
		if(matches.containsKey(discId)){
			disc_matches = matches.get(discId);
		}
		else{
			disc_matches = new ArrayList<Integer>();
		}
		disc_matches.add(index);
		matches.put(discId, disc_matches);
	}
	
	private static String concatNodeValues(NodeList nl){
		return concatNodeValues(nl,nl.getLength());
	}
	
	private static String concatNodeValues(NodeList nl, int numOfNodes){
		StringBuilder sb = new StringBuilder();
		int maxIndex = Math.min(numOfNodes, nl.getLength());
		for(int i=0 ; i < maxIndex ; i++){
			sb.append(nl.item(i).getTextContent().toLowerCase().trim()).append(" ");
		}
		return sb.toString();
	}

	private static Element getSingleElement(Element root, String tagname){
		NodeList elements = root.getElementsByTagName(tagname);
		if(elements != null && elements.getLength() > 0){
			return (Element) elements.item(0);
		}
		return null;
	}
	private static String getDiscNGramIdString(int recordId, Element disc){		
		Set<Integer> discNGramsIds = new HashSet<Integer>();
		
		NodeList artists = disc.getElementsByTagName("artist");
		String artistsVal = concatNodeValues(artists);		
		discNGramsIds.addAll(getNGramIds(getNGrams(artistsVal),recordId,1));
		
		NodeList titleWords = disc.getElementsByTagName("dtitle");
		String titlesVal = concatNodeValues(titleWords);
		discNGramsIds.addAll(getNGramIds(getNGrams(titlesVal),recordId,2));
		
		
		Element categoryNode =  getSingleElement(disc,"category");
		if(categoryNode != null){
			List<String> categoryNgrams = getNGrams(categoryNode.getTextContent().toLowerCase());
			discNGramsIds.addAll(getNGramIds(categoryNgrams,recordId,3));
		}
		
		Element yearNode =  getSingleElement(disc,"year");
		if(yearNode != null){
			List<String> yearNgrams = getNGrams(yearNode.getTextContent().toLowerCase());
			discNGramsIds.addAll(getNGramIds(yearNgrams,recordId,4));
		}
		
		Element genreNode =  getSingleElement(disc,"genre");
		if(genreNode != null){
			List<String> genreNgrams = getNGrams(genreNode.getTextContent().toLowerCase());
			discNGramsIds.addAll(getNGramIds(genreNgrams,recordId,5));
		} 		
		
		Element tracks = getSingleElement(disc,"tracks");
		if(tracks != null){
			NodeList trackTitlesNode = tracks.getElementsByTagName("title");
			String trackTitles = concatNodeValues(trackTitlesNode,4).trim();
			discNGramsIds.addAll(getNGramIds(getNGrams(trackTitles),recordId,6));		
		}
		
		List<Integer> finalList = new ArrayList<Integer>(discNGramsIds);
		return WordProcessor.concatListMembers(finalList);		
	}
	
	private static List<String> getNGrams(String value){
		return wordProcessor.processValue(value);
	}
	
	private static List<Integer> getNGramIds(List<String> nGrams, int recordId, int attributeId){		
		List<Integer> retVal = new ArrayList<Integer>(nGrams.size());		
		for (String nGram : nGrams) {
			retVal.add(lexicon.addWord(attributeId, recordId, nGram));
		}
		return retVal;
	}
	
	private static String getDiscString(Element disc){
		StringBuilder retVal = new StringBuilder();
		
		NodeList artists = disc.getElementsByTagName("artist");
		String artistsVal = concatNodeValues(artists).trim();		
		retVal.append(artistsVal).append(" ");
		
		NodeList titleWords = disc.getElementsByTagName("dtitle");
		String titlesVal = concatNodeValues(titleWords).trim();
		retVal.append(titlesVal).append(" ");
		
		Element categoryNode =  getSingleElement(disc,"category");
		if(categoryNode != null){
			retVal.append(categoryNode.getTextContent().toLowerCase().trim()).append(" ");
		}
		
		Element yearNode =  getSingleElement(disc,"year");
		if(yearNode != null){
			retVal.append(yearNode.getTextContent().toLowerCase().trim()).append(" ");
		}
	
		Element genreNode =  getSingleElement(disc,"genre");
		if(genreNode != null){
			retVal.append(genreNode.getTextContent().toLowerCase().trim()).append(" ");
		}		
		
		
		Element tracks = getSingleElement(disc,"tracks");
		if(tracks != null){
			NodeList trackTitlesNode = tracks.getElementsByTagName("title");
			String trackTitles = concatNodeValues(trackTitlesNode,4).trim();
			retVal.append(trackTitles).append(" ");
		}
		
		String concatedString = retVal.toString().trim();
		List<String> swFreeTerms = wordProcessor.removeStopwordsAndSpecialChars(concatedString);
		return WordProcessor.concatListMembers(swFreeTerms);
		
		
	}
}
