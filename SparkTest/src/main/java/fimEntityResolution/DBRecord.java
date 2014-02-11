package fimEntityResolution;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import fimEntityResolution.interfaces.IFRecord;


/**
 * This implementation will read/write information to the DB
 * @author Tal
 *
 */
public class DBRecord implements IFRecord {
	private final static String NODE_INDEX_NAME = "ids";
	public final static String ITEM_INDEX_NAME = "items";
	public final static String ID_PROP_NAME = "id";
	private final static String REC_STR_PROP_NAME = "recordStr";
	private final static String SRC_PROP_NAME = "src";	
	public final static String ITEM_ID_PREFIX = "I_";
	
	private int id;
	private Node recordNode = null;
	private int size = -1;
	private Map<Integer, Integer> itemIdToFreq = null;
	
	public DBRecord(int id){
		this.id = id;
	}
	
	public DBRecord(Node recordNode){
		this.recordNode = recordNode;
		this.id = (Integer)recordNode.getProperty(ID_PROP_NAME);
		this.getItemsToFrequency();
	}
	
	public DBRecord(int id, String recordStr){
		this.id = id;
		//check id this record already exists in the DB
		Node node = getNodeFromIndex(id);
		if(node != null){
			return; //record already exists in the DB
		}
		node = Utilities.recordDB.createNode();
		node.setProperty(ID_PROP_NAME, id);
		node.setProperty(REC_STR_PROP_NAME, recordStr);
		//add to node index
		IndexManager IM = Utilities.recordDB.index();
		Index<Node> nodeIdx = IM.forNodes(NODE_INDEX_NAME);
		nodeIdx.add(node, ID_PROP_NAME, id);
	}
	
	private Node getNodeFromIndex(int id){
		if(recordNode != null)
			return recordNode;
		IndexManager IM = Utilities.recordDB.index();
		Index<Node> nodeIdx = IM.forNodes(NODE_INDEX_NAME);
		IndexHits<Node> hits = nodeIdx.get( "id", id );
		recordNode = hits.getSingle();
		return recordNode;		
	}
	
	
	public void addItem(int itemId) {
		Node node = getNodeFromIndex(id);
		int freq = 1;
		//first check if the item has already been added
		String itemAsStr = ITEM_ID_PREFIX + Integer.toString(itemId);		
		if(node.hasProperty(itemAsStr)){
			Integer prevFreq = (Integer) node.getProperty(itemAsStr);
			freq = prevFreq +1;					
		}
		else{ //add to index only once
			//add to lucene index
			IndexManager IM = Utilities.recordDB.index();
			Index<Node> itemIdx = IM.forNodes(ITEM_INDEX_NAME);
			itemIdx.add(node, itemAsStr, "1");
		}
		node.setProperty(itemAsStr, freq);
	}

	
	public int getId() {
		return id;
	}
	
	public int getSize(){
		if(size > 0){
			return size;
		}
		size = this.getItemsToFrequency().size();
		return size;
	}

	
	public Map<Integer, Integer> getItemsToFrequency() {
		if(itemIdToFreq != null){
			return itemIdToFreq;
		}
		Node node = getNodeFromIndex(id);
		Iterable<String> propKeysIt = node.getPropertyKeys();
		itemIdToFreq = new HashMap<Integer, Integer>();
		for (String propKey : propKeysIt) {
			if(propKey.startsWith(ITEM_ID_PREFIX)){
				String itemId = propKey.substring(2);
				itemIdToFreq.put(Integer.parseInt(itemId),
						((Integer) node.getProperty(propKey)));
			}
		}
		return itemIdToFreq;
	}

	private final static String WORD_SEP = " ";
	
	
	public String getNumericline(Set<Integer> appropriateItems) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer,Integer> itemIdFreqPair : getItemsToFrequency().entrySet()) {
			if(appropriateItems.contains(itemIdFreqPair.getKey())){
				for(int i=0 ; i < itemIdFreqPair.getValue() ; i++){
					sb.append(itemIdFreqPair.getKey()).append(WORD_SEP);
				}
			}
		}
		return sb.toString();	
	}

	
	public String getRecordStr() {
		Node node = getNodeFromIndex(id);
		if(node == null){
			System.out.println("failed to find record with id " + id + " in the index");
			return null;
		}
		return (String) node.getProperty(REC_STR_PROP_NAME);
	}

	
	public String getSrc() {
		Node node = getNodeFromIndex(id);
		if(node == null){
			System.out.println("failed to find record with id " + id + " in the index");
			return null;
		}
		if(node.hasProperty(SRC_PROP_NAME)){
			return (String) node.getProperty(SRC_PROP_NAME);
		}
		else{
			return null;
		}
	}

	
	public void setSrc(String src) {
		Node node = getNodeFromIndex(id);
		if(node == null){
			System.out.println("failed to find record with id " + id + " in the index");
			return;
		}
		if(src != null){
			node.setProperty(SRC_PROP_NAME,src);
		}
		
	}
	private String NEW_LINE = System.getProperty("line.separator");
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.id).append(":");
		String recordStr = getRecordStr();
		if(recordStr != null){
			sb.append(recordStr).append(NEW_LINE);
		}
		for (Map.Entry<Integer,Integer> itemIdFreqPair : getItemsToFrequency().entrySet()) {
			for(int i=0 ; i < itemIdFreqPair.getValue() ; i++){
				sb.append(itemIdFreqPair.getKey()).append(WORD_SEP);
			}
		}	
	
		return sb.toString();
	}
	
}
