package com.pccw.nowplayer.utils;

import com.pccw.nowplayer.Constants;

public class BuildConstants {
	// usually to define different URL, e.g. true for QA, false for production
 	public static final boolean DEBUG = false;
 	
 	// should show family of app in setting fragment? for samsung store
 	public static final boolean SHOW_FAMILY_APP = true;

	public static void init() {
		// Ad Engine
		com.pccw.android.ad.common.GlobalInfo.setDebug(DEBUG);
		com.pccw.android.ad.common.AppConstants.setSlotAppName(Constants.AD_APP_ID);
		com.pccw.android.ad.common.AppConstants.setSplashAdSlotId(Constants.AD_SPLASH_SLOT_ID);
		com.pccw.android.ad.common.AppConstants.initUrls();
	}
}
