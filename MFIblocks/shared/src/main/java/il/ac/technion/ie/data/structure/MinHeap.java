package il.ac.technion.ie.data.structure;

/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
//package jinngine.util;
import il.ac.technion.ie.model.PositionUpdater;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Minimum heap implementation. See [Cormen et al 1999] for formal theory.
 * Maintains all elements in a min-heap, such that the minimum element will be
 * the top-most node in the heap at all times. Among many other uses, heaps are
 * ideal for representing priority queues.
 */
public class MinHeap{
	private int size;
	final protected List<Node> heap;
	final private Comparator<PositionUpdater> comparator;

	protected class Node {

        public Node(PositionUpdater element, int position) {
            this.element = element;
            this.position = position;
        }

        public PositionUpdater element;
		public int position;
	}

	/**
	 * Create a new heap
	 * 
	 * @param comparator
	 *            A comparator that handles elements of type T
	 */
	public MinHeap(Comparator<PositionUpdater> comparator, int maxSize) {
		size = 0;
		// Allocate space
		heap = new ArrayList<Node>(maxSize+1); //1-based
		heap.add(null); //dum,my var for 1-based indexing

		// Comparator
		this.comparator = comparator;
	}

	public MinHeap(Comparator<PositionUpdater> comparator) {
		size = 0;
		// Allocate space
		heap = new ArrayList<Node>(); //1-based
		heap.add(null); //dum,my var for 1-based indexing

		// Comparator
		this.comparator = comparator;

	}
	/**
	 * Insert element into the heap. O(lg n) where n is the number of
	 * elements/nodes in the heap
	 * 
	 * @param element new element to be inserted
	 */
	public void insert(final PositionUpdater element) {
		size++;
		Node node = new Node(element, size);
		node.element.setHeapPos(size);
		heap.add(node.position, node);		
		decreaseKey(node);
		// return node;
	}

	public final void clear() {
		heap.clear();
		size = 0;
	}

	/**
	 * Return a reference to the top-most element on the heap. The method does
	 * not change the state of the heap in any way. O(k).
	 * 
	 * @return Reference to top-most element of heap
	 */
	public final PositionUpdater top() {
		return heap.get(1).element;
	}

	// bound check missing

	/**
	 * Pop an element of the heap. O(lg n) where n is the number of elements in
	 * heap.
	 */
	public PositionUpdater pop() {
		PositionUpdater returnNode = top();
		exchange(1, size);
		heap.remove(size);
		size--;

		// if any elements left in heap, do minHeapify
		if (size > 0) {
			minHeapify(heap.get(1));
		}

		return returnNode;
	}

	public final int size() {
		return size;
	}

	public final void decreaseKey(final Node node) {
		int index = node.position;
		
		while (index > 1
				&& comparator.compare(heap.get(parent(index)).element, heap
						.get(index).element) >= 0) {
			exchange(index, parent(index));
			index = parent(index);
		}		
	}

	// called after the actual decrease
	public void increaseKey(int nodePos) {
		minHeapify(heap.get(nodePos));
	}

	protected final void minHeapify(final Node node) {
		int smallest;
		int index = node.position;
		int left = left(index);
		int right = right(index);

		if (left <= size
				&& comparator.compare(heap.get(left).element,
						heap.get(index).element) <= 0)
			smallest = left;
		else
			smallest = index;

		if (right <= size
				&& comparator.compare(heap.get(right).element, heap
						.get(smallest).element) <= 0)
			smallest = right;
		if (smallest != index) {
			exchange(index, smallest);
			minHeapify(heap.get(smallest));
		}	
	}

	private final void exchange(final int index, final int index2) {
		Node temp = heap.get(index);
		temp.position = index2;
		temp.element.setHeapPos(index2);
		
		Node temp2 = heap.get(index2);
		temp2.position = index;
		temp2.element.setHeapPos(index);

		heap.set(index, temp2);
		heap.set(index2, temp);
	}

	private final int parent(final int i) {
		return i / 2;
	}

	private final int left(final int i) {
		return 2 * i;
	}

	private final int right(final int i) {
		return 2 * i + 1;
	}

	/**
	 * Returns an iterator that iterates over all elements of the heap, in no
	 * particular order
	 * 
	 * @return
	 */
	public final Iterator<PositionUpdater> iterator() {
		return new Iterator<PositionUpdater>() {
			private Iterator<Node> iterator = heap.iterator();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public PositionUpdater next() {
				return iterator.next().element;
			}

			@Override
			public void remove() {
			}
		};
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=1;i<=this.size;i++){
			sb.append(Integer.toString(i)).append(":").append(heap.get(i).element.toString()).append("   ");
		}
		return sb.toString();
	}
}
