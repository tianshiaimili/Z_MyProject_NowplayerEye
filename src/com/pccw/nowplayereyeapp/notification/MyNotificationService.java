package com.pccw.nowplayereyeapp.notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.pccw.common.notification.NotificationService;
import com.pccw.common.notification.NotificationServiceSetting;
import com.pccw.nowplayer.MainTabletActivity;
import com.pccw.nowplayereyeapp.R;
import com.pccw.nowplayereyeapp.SplashActivity;


public class MyNotificationService extends NotificationService {
	private static final String TAG = MyNotificationService.class.getName();
	
	public static String getSelfAppName(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			ApplicationInfo ai;
			try {
			    ai = pm.getApplicationInfo(context.getPackageName(), 0);
			} catch (final NameNotFoundException e) {
			    ai = null;
			}
			String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
			return applicationName;
		} catch (Exception e) {
			Log.e(TAG, "Get Self App Name error");
			return null;
		}
	}
	
	@Override
	protected void registerServiceSetting() {
	Log.d(TAG,"registerServiceSetting()");
		NotificationServiceSetting.setContentTitle(getString(R.string.app_name));
		NotificationServiceSetting.setCancelBtnString(getString(R.string.notification_cancel));
		NotificationServiceSetting.setViewBtnText(getString(R.string.notification_view));
		NotificationServiceSetting.registerStartingClass(SplashActivity.class);		// Set the Starting Class
		NotificationServiceSetting.registerAppId("11");		// Set AppId
		NotificationServiceSetting.setIcon(R.drawable.ic_launcher);
		NotificationServiceSetting.setNumOfNotice(5);
		NotificationServiceSetting.setShowDialogNum(3);
		NotificationServiceSetting.setDisplayNum(false);		// Set Notification Receiver Time Interval
		NotificationServiceSetting.setTimeInterval(1 * 60 * 1000);
		NotificationServiceSetting.setFlagAutoClear(true);	
	}	
//	private static AlarmManager alarmMgr;
//	private static PendingIntent pendingIntent;
//
//	@Override
//	public void onCreate() {
//		PLog.d(this, TAG, "onCreate alarmManager begin");
//		
////		super.onCreate();
//
//		PLog.d(this, TAG, "onCreate alarmManager cancel");
//		if (alarmMgr != null && pendingIntent != null) {
//			alarmMgr.cancel(pendingIntent);
//			alarmMgr = null;
//			pendingIntent = null;
//		}
//
//		PLog.d(this, TAG, "onCreate alarmManager create");
//		// use this class as the receiver
//		Intent intent = new Intent(this, com.pccw.common.notification.NotificationService.class);
//		// Intent intent = new Intent(context,
//		// com.pccw.test.notification.NotificationStartupReceiver.class);
//		// create a PendingIntent that can be passed to the AlarmManager
//		// pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
//		// PendingIntent.FLAG_UPDATE_CURRENT);
//		pendingIntent = PendingIntent.getService(this, 0, intent,
//				PendingIntent.FLAG_CANCEL_CURRENT);
//
//		// create a repeating alarm, that goes of every x seconds
//		// AlarmManager.ELAPSED_REALTIME_WAKEUP = wakes up the cpu only
//	PLog.d(TAG,
//				"NotificationServiceSetting:"
//						+ NotificationServiceSetting.getTimeInterval());
//		alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//		alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//				SystemClock.elapsedRealtime(),
//				NotificationServiceSetting.getTimeInterval(), pendingIntent);
//		
//		PLog.d(this, TAG, "onCreate alarmManager end");
//	}
//	
//	@Override
//	public void onDestroy() {
//		PLog.d(this, TAG, "onDestroy alarmManager begin");
//
//		if (alarmMgr != null && pendingIntent != null) {
//			alarmMgr.cancel(pendingIntent);
//		}
//		PLog.d(this, TAG, "onDestroy alarmManager end");
//		super.onDestroy();
//	}
}
