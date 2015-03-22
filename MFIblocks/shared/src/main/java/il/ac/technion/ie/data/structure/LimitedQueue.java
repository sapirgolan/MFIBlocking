package il.ac.technion.ie.data.structure;

import java.util.concurrent.LinkedBlockingQueue;

public class LimitedQueue<E> extends LinkedBlockingQueue<E> {

	private int maxSize;
	 public LimitedQueue(int maxSize)
	 {
		 super(maxSize);
		 this.maxSize = maxSize;	     
	 }

    @Override
    public boolean offer(E e)
    {
        // turn offer() and add() into a blocking calls (unless interrupted)
        try {
            put(e);           
            return true;
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
    
    @Override
    public boolean add(E e)
    {
        // turn offer() and add() into a blocking calls (unless interrupted)
        try {
            put(e);         
            return true;
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

}
