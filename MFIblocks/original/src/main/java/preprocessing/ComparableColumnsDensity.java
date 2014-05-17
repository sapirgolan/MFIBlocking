package preprocessing;

public class ComparableColumnsDensity implements Comparable<ComparableColumnsDensity> {

	public int columnID;
	//public int docID;
	public int density;
	
	public ComparableColumnsDensity(int columnID, int distance) {
		this.columnID = columnID;
		//this.docID = docID;
		this.density = distance;
	}

	public int compareTo(ComparableColumnsDensity other) {
		//if (other.columnID == this.columnID){
			if (this.density > other.density)
				return -1;
			if (this.density < other.density)
				return 1;
			return 1;
		//}
		//return this.columnID - other.columnID;
	}
	
}
