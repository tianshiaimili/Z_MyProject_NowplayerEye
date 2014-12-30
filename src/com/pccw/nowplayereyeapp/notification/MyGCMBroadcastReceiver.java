package com.pccw.nowplayereyeapp.notification;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class MyGCMBroadcastReceiver extends GCMBroadcastReceiver {
	
//		public MyGCMBroadcastReceiver(){
//			System.out.println("@default new MyGCMBroadcastReceiver");
//			
//		}
	  @Override
	    protected String getGCMIntentServiceClassName(Context context) {
		  //context.get
		  System.out.println("@default getGCMIntentServiceClassName="+super.getGCMIntentServiceClassName(context));
	        return MyNotificationService.class.getName();
	    }
	  
}