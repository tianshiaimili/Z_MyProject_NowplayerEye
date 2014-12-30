package com.pccw.nowplayereyeapp.notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.pccw.common.notification.log.PLog;

public class MyNotificationStartupReceiver extends BroadcastReceiver {

	private static final String TAG = MyNotificationStartupReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
//		PLog.d(context, TAG, "NotificationStartupReceiver onReceive");
		startService(context);
	}
	
	public static void startService(final Context context){
//		new AsyncTask<Object, Integer, Boolean>(){
//	    	@Override
//	    	protected Boolean doInBackground(Object... params) {
//	    		PLog.d(context, TAG, "NotificationStartupReceiver startService doInBackground begin");	    		
	    		//MyNotificationService.startWithAlarmManager(context, MyNotificationService.class);
	    		context.startService(new Intent(context, MyNotificationService.class));
				//Start Service    
//				PLog.d(context, TAG, "NotificationStartupReceiver startService doInBackground end");
//	            return true;
//	        }
//	    }.execute();
//
	}
}
