package com.pccw.nowplayereyeapp;

import com.pccw.nowplayer.app.BaseApplication;
import com.pccw.nowplayer.utils.BuildConstants;
import com.pccw.nowplayereyeapp.notification.MyNotificationStartupReceiver;

public class NowPlayerApplication extends BaseApplication {
	public static String QUALITY_ALERY_DIALOG_TAG = null;

	@Override
	public void onCreate() {
		super.onCreate();
//		initAdConstants();
		MyNotificationStartupReceiver.startService(this);
	}
	
	private void initAdConstants(){
		BuildConstants.init();
	}
	
}
