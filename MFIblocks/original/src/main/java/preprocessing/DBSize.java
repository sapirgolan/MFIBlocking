package preprocessing;

public class DBSize {
	int[] sizes;

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
	
	
}
