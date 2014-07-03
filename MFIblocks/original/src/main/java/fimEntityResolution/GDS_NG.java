package fimEntityResolution;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicLong;

import org.enerj.core.SparseBitSet.Iterator;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.util.FileUtils;

import fimEntityResolution.interfaces.SetPairIF;

public class GDS_NG implements SetPairIF{

	private static int runningNum = 1;
	private static final String RECORD_DB_DIR = "target/";	
	private final static double FREE_MEM_THRESH = 20;
	private double NGLimit = Double.MAX_VALUE;
	
	private EmbeddedGraphDatabase GDS;
	private String storeDir;
	
	private int maxNG = 0;
	private BitSet coveredRows = new BitSet();	
	private BitMatrix tempStorageMatrix;
	boolean wroteToDB = false;
	
	


	private static Sigar sigar = new Sigar();
	public static Mem getMem(){
		Mem retVal = null;
		try {
			retVal = sigar.getMem();
		} catch (SigarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}
	
	private static enum RelTypes implements RelationshipType {
		CONNECTED
	}

	public GDS_NG(double NGLimit) {		
		tempStorageMatrix = new BitMatrix(RecordSet.DB_SIZE);
		maxNG = 0;
		this.NGLimit = NGLimit;
	}
	
	public GDS_NG(){
		tempStorageMatrix = new BitMatrix(RecordSet.DB_SIZE);
		maxNG = 0;
	}
			
	public void setNGLimit(double NGLimit){
		this.NGLimit = NGLimit;
	}

	private void createEmbeddedDB(){
		storeDir = RECORD_DB_DIR + "gds" + runningNum;		
		try {
			FileUtils.deleteRecursively(new File(storeDir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		runningNum++;
		GDS = new EmbeddedGraphDatabase(storeDir);
		registerShutdownHook(GDS);
	}
	
	public static AtomicLong timeSpentClearingDB = new AtomicLong(0);

	public void clearDb() {
		long start = System.currentTimeMillis();
		maxNG = 0;
		try {
			if(wroteToDB){
				FileUtils.deleteRecursively(new File(storeDir));
				wroteToDB = false;
			}
			if (tempStorageMatrix != null) {
				tempStorageMatrix.clearAll();
			}
			this.coveredRows.clear();			
			NGLimit = Double.MAX_VALUE;
			timeSpentClearingDB.addAndGet(System.currentTimeMillis() - start);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	public GraphDatabaseService getGDS() {
		return GDS;
	}

	public int getMaxNG() {
		return maxNG;		
	}

	public BitSet getCoveredRows() {
		return coveredRows;		
	}

	public void setPair(int i, int j, double score) {
		if (i < j) {
			setIndex(i, j);
		} else {
			setIndex(j, i);
		}
	}
	
	

	public final static String IDS_INDEX_NAME = "ids";
	private final static String CONNECT_INDEX_NAME = "connected";
	private final static String REL_INDEX_KEY = "type";
	private final static String REL_INDEX_VAL = "conn";
	private final static String NG_PROP_NAME = "NG_PROP_NAME";

	private Node getNodeFromIdx(int id) {
		IndexManager IM = GDS.index();
		Index<Node> index = IM.forNodes(IDS_INDEX_NAME);
		IndexHits<Node> hits = index.get(DBRecord.ID_PROP_NAME, id);
		Node retVal = hits.getSingle();
		if (retVal != null) {
			return retVal;
		}

		retVal = GDS.createNode();
		retVal.setProperty(DBRecord.ID_PROP_NAME, id);
		retVal.setProperty(NG_PROP_NAME, 0);
		// add to node index
		index.add(retVal, DBRecord.ID_PROP_NAME, id);
		return retVal;

	}

	private boolean doesRelationshipExist(Node inode, Node jnode) {
		IndexManager IM = GDS.index();
		RelationshipIndex index = IM.forRelationships(CONNECT_INDEX_NAME);
		boolean exists = (index.get(REL_INDEX_KEY, REL_INDEX_VAL, inode, jnode)
				.getSingle() != null);
		return exists;
	}

	private void addRelationshipToIndex(Relationship rel) {
		IndexManager IM = GDS.index();
		RelationshipIndex index = IM.forRelationships(CONNECT_INDEX_NAME);
		index.add(rel, REL_INDEX_KEY, REL_INDEX_VAL);

	}

	public static double percentegeOfFreeMem(){
		return getMem().getFreePercent();		
	}
	private int incrememntNeighbors(Node node, int num) {
		Integer nbrs = (Integer) node.getProperty(NG_PROP_NAME);
		node.setProperty(NG_PROP_NAME, nbrs + num);
		return (nbrs + num);
	}
	
	public void writeToDB(BitMatrix BM){
		if(!wroteToDB){
			createEmbeddedDB();
			wroteToDB = true;
		}			
		Transaction tx = GDS.beginTx();
		try {
			int[] pair = new int[2];
			Iterator It = BM.getSBS().getIterator();

			while (It.hasNext()) {
				long nextSetBit = It.next();
				BM.getSetPairFromIndex(nextSetBit, pair);
				int r = pair[0];
				int c = pair[1];
				Node rnode = getNodeFromIdx(r);
				Node cnode = getNodeFromIdx(c);
				if (!doesRelationshipExist(rnode, cnode)) {
					Relationship rel = rnode.createRelationshipTo(cnode,
							RelTypes.CONNECTED);
					addRelationshipToIndex(rel);
					int newrnodeNbrs = incrememntNeighbors(rnode, 1);
					int newcnodeNbrs = incrememntNeighbors(cnode, 1);
					maxNG = Math.max(maxNG, Math.max(newrnodeNbrs,
							newcnodeNbrs));
				}
			}
			tx.success();				
		} finally {
			tx.finish();
		}		
	}

	

	private final static int NUM_SET_THRESH = 10000;
	private void setIndex(int i, int j) {
		tempStorageMatrix.setPair(i, j);
		maxNG = Math.max(maxNG, tempStorageMatrix.getMaxNG());
		updateCoverage(i);
		updateCoverage(j);	
		
		/**
		 * We want to write to the DB on the following terms:
		 * 1. not enough free mem
		 * 2. we will still need to write to the BM, meaning  maxNG < NGLimit 
		 * 3. The matrix has at least 10000 set bits (otherwise there is no point in writing it)
		 */
		if (tempStorageMatrix.numOfSet() > NUM_SET_THRESH &&  maxNG < NGLimit && percentegeOfFreeMem() < FREE_MEM_THRESH) {
			//System.out.println("DEBUG: getActualFree(): " + getMem().getActualFree() + " MB");			
			//System.out.println("DEBUG: memAn.getActualUsed(): " + getMem().getActualUsed() + " MB");
			//System.out.println("DEBUG: perfreeMem: " + percentegeOfFreeMem());
			//first time writing to DB
			writeToDB(tempStorageMatrix);
			tempStorageMatrix= null;
			Runtime.getRuntime().gc();
			tempStorageMatrix = new BitMatrix(RecordSet.DB_SIZE);	
		}

	}

	public void endUse() {
		if(GDS != null){
			GDS.shutdown();
		}
	}

	private void updateCoverage(int index) {
		coveredRows.set(index);
	}

	public BitMatrix exportToBM() {
		BitMatrix retval = tempStorageMatrix;		
		if(wroteToDB){
			IndexManager IM = GDS.index();
			RelationshipIndex index = IM.forRelationships(CONNECT_INDEX_NAME);
			for (Relationship connected : index.get(REL_INDEX_KEY, REL_INDEX_VAL)) {
				Integer i = (Integer) connected.getStartNode().getProperty(
						DBRecord.ID_PROP_NAME);
				Integer j = (Integer) connected.getEndNode().getProperty(
						DBRecord.ID_PROP_NAME);
				// System.out.println("i:" + i + " j:" + j);
				retval.setPair(i, j);
			}
		}
		System.out.println("exportToBM: returning a matrix with "
				+ retval.numOfSet() + " set ");
		return retval;

	}

	public static void main(String[] args) {
		GDS_NG gds_ng = new GDS_NG(123);
		gds_ng.setPair(1, 3,0);
		gds_ng.setPair(2, 3,0);
		gds_ng.setPair(4, 1000,0);
		gds_ng.setPair(1000, 4,0);
		gds_ng.setPair(2, 5,0);
		BitSet bs = gds_ng.getCoveredRows();
		System.out.println("bs: " + bs.toString());
		System.out.println("gds_ng.getMaxNG(): " + gds_ng.getMaxNG());		
		BitMatrix bm = gds_ng.exportToBM();
		System.out.println("end test");

	}

	// TP+ FP - 1 in both the Ground Truth and in the result
	public static double[] TrueAndFalsePositives(BitMatrix GTMatrix,
			GDS_NG ActualGDS) {
		long TP = 0;
		long FP = 0;
		IndexManager IM = ActualGDS.getGDS().index();
		RelationshipIndex index = IM.forRelationships(CONNECT_INDEX_NAME);
		for (Relationship connected : index.get(REL_INDEX_KEY, REL_INDEX_VAL)) {
			Integer i = (Integer) connected.getStartNode().getProperty(
					DBRecord.ID_PROP_NAME);
			Integer j = (Integer) connected.getEndNode().getProperty(
					DBRecord.ID_PROP_NAME);
			if (GTMatrix.getPair(i, j)) {
				TP++;
			} else {
				FP++;
			}
		}
		return new double[] { TP, FP };
	}

	public static double FalseNegatives(BitMatrix GTMatrix, GDS_NG ActualGDS) {
		long FN = 0;
		Iterator It = GTMatrix.getSBS().getIterator();
		while (It.hasNext()) {
			long nextSetBit = It.next();
			int j = (int) (nextSetBit % (long) RecordSet.DB_SIZE);
			int i = (int) ((nextSetBit - j) / (long) RecordSet.DB_SIZE);
			Node nodei = ActualGDS.getNodeFromIdx(i);
			Node nodej = ActualGDS.getNodeFromIdx(j);
			boolean exists = ActualGDS.doesRelationshipExist(nodei, nodej);
			if (!exists) {
				FN++;
			}
		}
		return FN;
	}



}
