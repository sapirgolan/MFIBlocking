package il.ac.technion.ie.pools;

import il.ac.technion.ie.utils.GDS_NG;

import java.util.LinkedList;

public class GDSPool {

	private LinkedList<GDS_NG> matriceDBs = new LinkedList<GDS_NG>();
	private int created = 0;
	private static GDSPool self = null;
	
	private GDSPool() {
	};

	public static GDSPool getInstance() {
		if (self == null) {
			self = new GDSPool();
		}
		return self;
	}

	public int getNumCreated() {
		return created;
	}

	public GDS_NG getGDS(double NGLimit) {
		synchronized (this) {
			if (matriceDBs.size() > 0) {
				GDS_NG toReturn = matriceDBs.remove();
				toReturn.clearDb();
				toReturn.setNGLimit(NGLimit);
				return toReturn;
			}
			created++;
			System.out.println("num of gds created is " + created);
		}
		return new GDS_NG(NGLimit);
	}

	public void returnGDS(GDS_NG gds) {
		gds.clearDb();
		synchronized (this) {
			matriceDBs.add(gds);
		}
	}
	
	public void restart(){
		System.out.println(" Clearing " + matriceDBs.size() + " GDS");
		matriceDBs = null;
		matriceDBs =  new LinkedList<GDS_NG>();
	}

}
