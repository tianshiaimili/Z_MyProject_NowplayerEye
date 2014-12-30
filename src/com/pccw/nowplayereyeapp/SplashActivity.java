package com.pccw.nowplayereyeapp;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.pccw.nmal.Nmal;
import com.pccw.nmal.appdata.B2BApiAppInfo;
import com.pccw.nmal.service.B2BApiPixelLogService;
import com.pccw.nowplayer.AppDataLoader;
import com.pccw.nowplayer.Constants;
import com.pccw.nowplayer.MainTabletActivity;
import com.pccw.nowplayer.app.BaseSplashActivity;
import com.pccw.nowplayer.fragment.LangChangeFragment;
import com.pccw.nowplayer.utils.DeviceUtil;
import com.pccw.nowplayer.utils.DialogUtils;
import com.pccw.nowplayer.utils.LogUtils2;
import com.pccw.nowplayereyeapp.notification.UserSetting;

public class SplashActivity extends BaseSplashActivity {

	protected static final String TAG = SplashActivity.class.getSimpleName();
	private SplashSetting mSplashSetting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.splash);
		TextView version = (TextView) findViewById(R.id.splash_app_version);
		version.setText(getString(R.string.version) + " : " + DeviceUtil.getAppVersion(this));
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				//start notification service
//				UserSetting userSetting = new UserSetting(SplashActivity.this);
//				userSetting.save();
			}
		});
		
		// moved into function onDownloadInfoSuccess() in this class: new PixelLogService(Constants.PIXEL_LOG_URL, Constants.PIXEL_LOG_APP_NAME, new UserSetting(this).getDeviceToken()).pixelLogInOpenApp(DeviceUtil.getAppVersion(this));

		LangChangeFragment.syncLanguageWithAdLibrary();
	}
	
	@Override
	protected void viewWillCreate() {
		super.viewWillCreate();
		LogUtils2.i("viewWillCreate****");
		Nmal.init(getApplicationContext());
		mSplashSetting = new SplashSetting(false);
		mSplashSetting.setNetworkUnavailableMsg(getString(R.string.load_config_network_unavailable));
		mSplashSetting.setSplashTaskErrorMsg(getString(R.string.load_config_server_error));
		mSplashSetting.setHomePageIntent(new Intent(SplashActivity.this, MainTabletActivity.class));
		mSplashSetting.setSplashTaskErrorTitle(getString(R.string.error_title));
		mSplashSetting.setSplashTaskErrorMsg(getString(R.string.error_general_error));
		//mSplashSetting.setIgnoreNetworkConnectivity();
		//mSplashSetting.setMaxDynamicSplashTime(10000);
		mSplashSetting.setSplashTask(new Runnable() {
			@Override
			public void run() {
				doSplashTask();
			}
		}, true);
	}
	
	@Override
	protected SplashSetting getSplashSetting() {
		return mSplashSetting;
	}
	
	private void doSplashTask() {
		LogUtils2.i("doSplashTask****");
		final B2BApiAppInfo appInfo = new B2BApiAppInfo(SplashActivity.this, Constants.APP_INFO_URL, Constants.APP_INFO_APP_ID);
		appInfo.setDownloadConfigCallback(new B2BApiAppInfo.DownloadInfoCallback() {
			@Override
			public void onRegionChanged(String oldRegion, String newRegion) {
			}
			@Override
			public void onDownloadInfoSuccess() {
				LogUtils2.i("doSplashTask. onDownloadInfoSuccess****");
				//new B2BApiPixelLogService(Constants.PIXEL_LOG_URL, Constants.PIXEL_LOG_APP_NAME, new UserSetting(getApplicationContext()).getDeviceToken()).pixelLogInOpenApp(DeviceUtil.getAppVersion(getApplicationContext()));
				/*
				 * Force update: forceUpdateVersion >= current installed version name (not version code) <= allowedVersion
				 * -- If in latest version, direct go into app
				 * -- If in between, ask for update or later.
				 * -- If lower, force update
				 * 
				 * Note: version may not only an integer or a float, a version could be 1.2.3.4, 
				 * assume format is mulitple numbers and with dots between the numbers.
				 */
				boolean noUpdates = appInfo.isAllowedVersion(), forceUpdate = B2BApiAppInfo.isForceUpdate();

				// noUpdates = false; forceUpdate = false;
//				if(noUpdates) {
				if(true) {
					// no updates available, direct go into app.
					LogUtils2.i("doSplashTask. noUpdates****");
					downloadData();
				} else if(forceUpdate) {
					// forced update needed.
					DialogUtils.createMessageAlertDialog(SplashActivity.this, null, getString(R.string.force_update_dialog_title), getString(R.string.force_update_dialog_message), 
							getString(R.string.force_update_dialog_negative), null, getString(R.string.force_update_dialog_positive), 
							new DialogInterface.OnClickListener() {
								@Override	
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case DialogInterface.BUTTON_NEGATIVE:
										finish();
										break;
									case DialogInterface.BUTTON_POSITIVE:
										upgradeApp();
										break;
									}
								}
							}, false).show();
				} else {
					// ask for update or later.
					DialogUtils.createMessageAlertDialog(SplashActivity.this, null, getString(R.string.remind_update_dialog_title), getString(R.string.remind_update_dialog_message), 
							getString(R.string.remind_update_dialog_negative), null, getString(R.string.remind_update_dialog_positive), 
							new DialogInterface.OnClickListener() {
								@Override	
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case DialogInterface.BUTTON_NEGATIVE:
										downloadData();
										break;
									case DialogInterface.BUTTON_POSITIVE:
										upgradeApp();
										break;
									}
								}
							}, false).show();
				}

			}
			@Override
			public void onDownloadInfoFailed(String reason) {
				// splash task failed.
				LogUtils2.i("doSplashTask. onDownloadInfoFailed****reason = "+reason);
//				setSplashTaskFailded();
				downloadData();
			}
		});
		appInfo.downloadInfo();
	}
	
	private void downloadData() {
		// download JSON zip.
		AppDataLoader epgDataLoader = new AppDataLoader(getApplicationContext());
		epgDataLoader.downloadJsonZip(new AppDataLoader.AppDataListener() {
			@Override
			public void onDataLoaded() {
				// enter home page. 
				setSplashTaskSuccessful();
			}

			@Override
			public void onJsonZipFailed() {
				setSplashTaskFailded(); 
			}
		});
	}
	
	private void upgradeApp() {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("eye://product/" + getApplicationContext().getPackageName()));
		Bundle b = new Bundle();
		b.putBoolean("AutoDownload", true);
		b.putBoolean("Autoinstall", true);
		i.putExtras(b);
		try {
			startActivityForResult(i, 1001);
		} catch (ActivityNotFoundException anfe) {
			Log.e(TAG, "ActivityNotFoundException");
		}
	}
	
}
