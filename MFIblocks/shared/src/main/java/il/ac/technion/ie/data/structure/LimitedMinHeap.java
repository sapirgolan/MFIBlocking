package il.ac.technion.ie.data.structure;

import il.ac.technion.ie.model.PositionUpdater;

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
        //element will replace the root - i.e, the minimum
		Node node = new Node(element, 1);
		element.setHeapPos(1);
		heap.set(1, node);
		minHeapify(node);
		
	}

}
