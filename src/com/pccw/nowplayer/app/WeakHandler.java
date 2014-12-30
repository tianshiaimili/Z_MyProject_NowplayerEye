package com.pccw.nowplayer.app;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

/**
 * If Handler class is not static, it will have a reference to your Activity/Service/... object.
 * Handler objects for the same thread all share a common Looper object, which they post messages to and read from.
 * As messages contain target Handler, as long as there are messages with target handler in the message queue, the handler cannot be garbage collected. 
 * If handler is not static, your Service or Activity cannot be garbage collected, even after being destroyed.
 * This may lead to memory leaks, for some time at least - as long as the messages stay int the queue. 
 * This is not much of an issue unless you post long delayed messages.
 * 
 * If you want to use a nested class, it has to be static. 
 * Otherwise, WeakReference doesn't change anything. 
 * Inner (nested but not static) class always holds strong reference to outer class. 
 * There is no need for any static variables though.
 * 
 * T can be Activity, Service, Fragment and so on.
 * 
 * @author AlfredZhong
 * @version 2013-06-04
 */
public abstract class WeakHandler<T> extends Handler {
	
	private final WeakReference<T> mContextObject;
	
	public WeakHandler(T contextObject) {
		mContextObject = new WeakReference<T>(contextObject);
	}
	
    @Override
    public final void handleMessage(Message msg){
         T obj = mContextObject.get();
         if (obj != null) {
        	 // If you use a nested class, you can directly call obj.xxx() here.
        	 handleWeakHandlerMessage(obj, msg);
         }
    }
    
    public abstract void handleWeakHandlerMessage(T contextObject, Message msg);
	
}
