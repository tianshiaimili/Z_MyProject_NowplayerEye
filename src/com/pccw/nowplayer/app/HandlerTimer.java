package com.pccw.nowplayer.app;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

/**
 * A helper class that do the similar job of java.util.Timer, but lightweight than Timer.
 * Currently support only one repeat timer task at a time, but multiple one shot timer.
 * 
 * @author AlfredZhong
 * @version 1.0, 2012-03-20
 * @version 2.0, 2012-05-08
 * @version 2012-08-29
 * @version 2012-08-30, added HandlerTimer(boolean runOnUiThread), quit(), isQuit().
 */
public class HandlerTimer {

	private static final String TAG = HandlerTimer.class.getName();
	private HandlerThread mHandlerThread;
	private Handler mHandler;
	private Runnable mRepeatTask;
	private long mInterval;
	private boolean mRunning = false;
	private boolean mQuit = false;
	
	/**
	 * @param runOnUiThread whether execute tasks on UI thread.
	 */
	public HandlerTimer(boolean runOnUiThread) {
		if(runOnUiThread) {
			mHandler = new Handler(Looper.getMainLooper());
		} else {
			mHandlerThread = new HandlerThread(TAG + "$HandlerThread");
			mHandlerThread.start();
			// Thread has been started, HandlerThread.getLooper() will block until the looper has been initialized.  
			mHandler = new Handler(mHandlerThread.getLooper());
		}
		Log.d(TAG, "Handler associate with thread " + mHandler.getLooper().getThread().getName());
		Log.d(TAG, "HandlerTimer is ready.");
	}
	
	/**
	 * @param handler run timer task in the thread which handler is associated with.
	 */
	public HandlerTimer(Handler handler) {
		mHandler = handler;
		Log.d(TAG, "Handler associate with thread " + mHandler.getLooper().getThread().getName());
		Log.d(TAG, "HandlerTimer is ready.");
	}
	
	private void checkTimer(Runnable task, long delay, long interval) {
		if (mQuit) {
			// Once quit, Handler can not send message to a Handler on a dead thread.
			throw new IllegalStateException("Timer has been quit. Can not run task in a quit timer.");
		}
		if(task == null) {
			 throw new IllegalArgumentException("Timer task can not be null.");
		}
        if (delay < 0) {
            throw new IllegalArgumentException("delay must be >= 0.");
        }
        if (interval <= 0) {
        	throw new IllegalArgumentException("interval must be > 0.");
        }
	}
	
	private Runnable mOnTick = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "Execute task in thread " + Thread.currentThread().getName());
			// Only the original thread that created a view hierarchy can touch its views.
			mRepeatTask.run();
			if (mRunning && !mQuit) {
				mHandler.postDelayed(mOnTick, mInterval);
			}
		}
	};
	
	/**
	 * Schedule a task for repeated interval execution after a specific delay.
	 * 
	 * @param task the task to schedule.
	 * @param delay amount of time in milliseconds before first execution.
	 * @param interval amount of time in milliseconds between subsequent executions.
	 * @throws IllegalStateException if repeat execution is running or timer has been quit.
	 * @throws IllegalArgumentException if {@code delay < 0} or {@code interval <= 0}.
	 */
	public synchronized void scheduleRepeatExecution(Runnable task, long delay, long interval) 
			throws IllegalStateException, IllegalArgumentException {
		Log.d(TAG, "scheduleRepeatExecution(), delay = " + delay + ", interval = " + interval + "ms");
		checkTimer(task, delay, interval);
		if (mRunning) {
			throw new IllegalStateException("Timer is running a repeating execution. You should stop it before schedule a new one.");
		}
        mRepeatTask = task;
		mInterval = interval;
		// Set true before post() or postDelayed().
		mRunning = true;
		if(delay == 0) {
			// NOTE: not task here.
			mHandler.post(mOnTick);
		} else {
			// NOTE: not task here.
			// To be run after the specified amount of time elapses. No matter system time changed by user or not.
			mHandler.postDelayed(mOnTick, delay);
		}
	}
	
	/**
	 * Stop the current running repeat execution; if no execution, nothing will happen.
	 * NOTE: Remember to call cancelRepeatExecution(), otherwise repeat execution will continue running.
	 */
	public synchronized void cancelRepeatExecution() {
		Log.d(TAG, "cancelRepeatExecution()");
		mHandler.removeCallbacks(mOnTick);
		mRunning = false;
	}
	
	/**
	 * Whether the repeat execution is running.
	 * @return
	 */
	public synchronized boolean isRepeatExecutionRunning() {
		return mRunning;
	}
	
	/**
	 * Schedule a task for single execution after a specified delay.
	 * 
	 * @param task the task to schedule.
	 * @param delay amount of time in milliseconds before execution.
	 * @throws IllegalStateException if timer has been quit.
	 * @throws IllegalArgumentException if {@code delay < 0}.
	 */
	public void scheduleSingleExecution(Runnable task, long delay) throws IllegalStateException, IllegalArgumentException {
		Log.d(TAG, "scheduleSingleExecution(), delay = " + delay);
		checkTimer(task, delay, Integer.MAX_VALUE); // Integer.MAX_VALUE just make sure interval is larger than 0.
		if(delay == 0) {
			mHandler.post(task);
		} else {
			mHandler.postDelayed(task, delay);
		}
	}
	
	/**
	 * Cancel any pending posts of Runnable task that are in the execution queue.
	 * 
	 * @param task
	 */
	public void cancelSingleExecution(Runnable task) {
		Log.d(TAG, "cancelSingleExecution()");
		mHandler.removeCallbacks(task);
	}
	
	/**
	 * Cancel all pending posts of Runnable task that are in the execution queue,
	 * no matter it is repeat or one shot execution task.
	 */
	public void cancelAll() {
		Log.d(TAG, "cancelAll()");
		// all callbacks and messages will be removed.
		mHandler.removeCallbacksAndMessages(null);
	}
	
	/**
	 * Quit timer. 
	 * Note: once quit(), all the tasks will be removed, and you can not use this timer any more.
	 */
	public void quit() {
		cancelAll();
		if(mHandlerThread != null) {
			// only thread created by us need quit.
			mHandlerThread.quit();
			mHandlerThread.interrupt();
			mHandlerThread = null;
		} 
		mQuit = true;
		Log.w(TAG, "Quit timer.");
	}
	
	public boolean isQuit() {
		return mQuit;
	}

}
