package com.pccw.nowplayer.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Workflow:
 * 1. If the number of threads is less than the corePoolSize, create a new Thread to run a new task(same with normal ThreadPoolExecutor).
 * 2. If the number of threads is greater than the allWorkingThreads, put the task into the queue.
 * 3. If the number of threads is less than the allWorkingThreads:
 *    if pool size less than max size, create a new thread, 
 *    else reject the task(reject handler will put the task into the queue()).
 * 
 * @version 2012-10-15
 */
public class ScalingThreadPoolExecutor extends ThreadPoolExecutor {

	/**
	 * number of threads that are actively executing tasks
	 */
	private final AtomicInteger activeCount = new AtomicInteger();
	
    /**
     * The default rejected execution handler
     */
    private static final RejectedExecutionHandler sDefaultHandler = new ForcePutIntoQueuePolicy();

	public ScalingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new ScalingQueue<Runnable>(), sDefaultHandler);
		ScalingQueue<Runnable> queue = (ScalingQueue<Runnable>) getQueue();
		queue.setThreadPoolExecutor(this);
	}
    
	public ScalingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new ScalingQueue<Runnable>(), threadFactory, sDefaultHandler);
		ScalingQueue<Runnable> queue = (ScalingQueue<Runnable>) getQueue();
		queue.setThreadPoolExecutor(this);
	}

	@Override
	public int getActiveCount() {
		return activeCount.get();
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		activeCount.incrementAndGet();
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		activeCount.decrementAndGet();
	}
	
	public static class ScalingQueue<E> extends LinkedBlockingQueue<E> {
		
		private static final long serialVersionUID = -6747221060569417840L;
		
		/**
		 * The executor this Queue belongs to
		 */
		private ThreadPoolExecutor executor;

		/**
		 * Creates a TaskQueue with a capacity of {@link Integer#MAX_VALUE}.
		 */
		public ScalingQueue() {
			super();
		}

		/**
		 * Sets the executor this queue belongs to.
		 */
		public void setThreadPoolExecutor(ThreadPoolExecutor executor) {
			this.executor = executor;
		}

		/**
		 * Inserts the specified element at the tail of this queue if there is at
		 * least one available thread to run the current task. If all pool threads
		 * are actively busy, it rejects the offer.
		 * 
		 * @param o the element to add.
		 * @return true if it was possible to add the element to this queue, else false
		 * @see ThreadPoolExecutor#execute(Runnable)
		 */
		@Override
		public boolean offer(E o) {
			int allWorkingThreads = executor.getActiveCount() + super.size();
			// if allworingthead >= poolsize && poolsize < max then create thread else put to Queue
			return allWorkingThreads < executor.getPoolSize() && super.offer(o);
		}
		
	} // end of inner class.
	
	public static class ForcePutIntoQueuePolicy implements RejectedExecutionHandler {

		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			try {
				executor.getQueue().put(r);
			} catch (InterruptedException e) {
				throw new RejectedExecutionException(e);
			}
		}
		
	} // end of inner class.

}