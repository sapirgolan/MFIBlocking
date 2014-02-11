package candidateMatches;

import java.util.Comparator;

public class LimitedMinHeap<T> extends MinHeap{

	private int maxSize;
	public LimitedMinHeap(Comparator<PositionUpdater> comparator, int maxSize) {
		super(comparator,maxSize);
		this.maxSize = maxSize;
	}
	
	public LimitedMinHeap(Comparator<PositionUpdater> comparator) {
		super(comparator);
		this.maxSize = Integer.MAX_VALUE;
	}
	@Override
	/**
	 * This function assumes that element is not currently in the heap
	 */
	public synchronized void insert(final PositionUpdater element) {
		if(super.size() < maxSize){
			super.insert(element);
			return;
		}
		Node node = new Node();
		node.element = element;
		element.setHeapPos(1);
		node.position = 1; //replaces the root - i.e, the minimum
		heap.set(1, node);
		minHeapify(node);
		
	}

}
