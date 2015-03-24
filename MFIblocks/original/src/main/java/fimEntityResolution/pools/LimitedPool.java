package fimEntityResolution.pools;

import il.ac.technion.ie.utils.ClearerFactory;
import il.ac.technion.ie.data.structure.Clearer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class LimitedPool {

	private static final int LIMIT = 120;
	private final Semaphore available = new Semaphore(LIMIT, true);
	private LinkedBlockingQueue<Clearer> freeItems = new LinkedBlockingQueue<Clearer>();
	private AtomicInteger created = new AtomicInteger(0);
	private ClearerFactory factory;
	private static LimitedPool self;

	private LimitedPool(ClearerFactory factory) {
		this.factory = factory;
	}

	public static LimitedPool getInstance(ClearerFactory factory) {
		if (self == null) {
			self = new LimitedPool(factory);
		}
		return self;
	}

	public synchronized Clearer getItem() throws InterruptedException {
		available.acquireUninterruptibly();		
		if (freeItems.size() > 0) {
			return freeItems.take();
		}
		created.addAndGet(1);
		return factory.createInstance();

	}

	public synchronized void returnItem(Clearer returnedItem)
			throws InterruptedException {
		returnedItem.clearAll();
		freeItems.add(returnedItem);
		available.release();
	}

}
