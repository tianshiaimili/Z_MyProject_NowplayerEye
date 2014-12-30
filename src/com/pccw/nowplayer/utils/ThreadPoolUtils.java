package com.pccw.nowplayer.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolUtils {
	
	private static final String TAG = ThreadPoolUtils.class.getSimpleName();
	
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        public Thread newThread(Runnable r) {
            return new Thread(r, TAG + "#" + mCount.getAndIncrement());
        }
    };
    
    private static final ExecutorService sExecutor = new ScalingThreadPoolExecutor(1, 25, 30L, TimeUnit.SECONDS, sThreadFactory);
    
    private ThreadPoolUtils() {
    	// Can not instantiate.
    }
    
    public static void execute(Runnable command) {
    	sExecutor.execute(command);
    }

    public static long getThreadId() {
    	return Thread.currentThread().getId();
    }
    
    public static String getThreadName() {
    	return Thread.currentThread().getName();
    }
    
}
