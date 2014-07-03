package preprocessing;

public class DBSize {
	int[] sizes;
	private int limit = Integer.MAX_VALUE;
	
	public DBSize(int size) {
		sizes=new int[size];
	}
	
	public void setSize (int index, int value){
		sizes[index]=value;
	}
	
	public int getSize(int index){
		return sizes[index];
	}
	
	public int getTotalSize(){
		int total=0;
		for (int size : sizes){
			total+=size;
		}
		return total;
	}
	public void setLimit(int num){
		limit=num;
	}
	
	public int getLimit(){
		return limit;
	}
	
	public int getRelativeSize(int index){
		int totalSize=getTotalSize();
		double ratio = (double)sizes[index]/totalSize;
		int difference = totalSize - limit;
		return sizes[index] - (int)( ratio * difference);
	}
	
	
}
