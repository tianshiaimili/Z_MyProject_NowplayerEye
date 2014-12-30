package com.pccw.nowplayer.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pccw.nowplayer.utils.LogUtils2;
import com.pccw.nowplayer.utils.NetUtils;
import com.pccw.nowplayer.utils.ThreadPoolUtils;


/**
 * Loading time:
 * dynamic(has task) = [MIN_DYNAMIC_SPLASH_TIME, task_done or timeout] (if task_done larger than MIN_DYNAMIC_SPLASH_TIME)
 * static(no task) = [MIN_STATIC_SPLASH_TIME, MAX_STATIC_SPLASH_TIME]
 * 
 * @author AlfredZhong
 * @version 2013-06-04
 * @version 2013-08-28, introduce SplashSetting inner class.
 */
public abstract class BaseSplashActivity extends BaseActivity {

	private static final String TAG = BaseSplashActivity.class.getSimpleName();
	private static final int HANDLER_NETWORK_UNAVAILABLE = 0;
	private static final int HANDLER_SPLASH_TASK_ERROR = 1;
	private static final int HANDLER_CHECK_TASK_FINISHED = 2;
	private static final int HANDLER_CHECK_TASK_TIMEOUT = 3;
	public static final int ERROR_REASON_NETWORK_UNAVAILABLE = HANDLER_NETWORK_UNAVAILABLE;
	public static final int ERROR_REASON_SPLASH_TASK_ERROR = HANDLER_SPLASH_TASK_ERROR;
	private Handler mHandler = new InnerStaticHandler(this);
	private SplashSetting settings;
	private boolean mIsOKToEnterHomePage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewWillCreate();
		settings = getSplashSetting();
		if(settings == null) {
			throw new RuntimeException("SplashSetting should initialize in viewWillCreate().");
		}
		if(settings.isDynamicSplashTime) {
			final Runnable splashTask = new Runnable() {
				@Override
				public void run() {
					try {
						Log.d(TAG, "Begin splash task.");
						settings.splashTask.run();
						Log.d(TAG, "Finish splash task.");
					} catch (Exception e) {
						mHandler.obtainMessage(HANDLER_SPLASH_TASK_ERROR, e.toString()).sendToTarget();
					}
				}
			};
			//
			boolean connected = settings.isIgnoreNetworkConnectivity ? true : NetUtils.checkConnectivity(this);
			if(connected) {
				mHandler.sendEmptyMessageDelayed(HANDLER_CHECK_TASK_FINISHED, SplashSetting.MIN_DYNAMIC_SPLASH_TIME);
				if(settings.maxDynamicSplashTime != SplashSetting.MAX_DYNAMIC_SPLASH_TIME) {
					mHandler.sendEmptyMessageDelayed(HANDLER_CHECK_TASK_TIMEOUT, settings.maxDynamicSplashTime);
				}
				if(settings.isSplashTaskRunOnUiThread) {
					LogUtils2.i("***settings.isSplashTaskRunOnUiThread");
					splashTask.run();
				} else {
					ThreadPoolUtils.execute(splashTask);
				}
			} else {
				mHandler.sendEmptyMessage(HANDLER_NETWORK_UNAVAILABLE);
			}
		} else {
			Log.d(TAG, "No need to do splash task. Show splash page " + settings.maxStaticSplashTime + " ms.");
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Log.w(TAG, "Static splash enter home page.");
					startActivity(settings.homePageIntent);
					finish();
				}
			}, settings.maxStaticSplashTime);
		}
		Log.w(TAG, TAG + " onCreate() completed.");
	}
	
	/**
	 * Called before view will create.
	 */
	protected void viewWillCreate() {
		// empty implement.
	}
	
	/**
	 * Called when splash task occurs exception.
	 * 
	 * @param reason {@link #ERROR_REASON_NETWORK_UNAVAILABLE}, network disconnected;
	 * 		{@link #ERROR_REASON_SPLASH_TASK_ERROR}, splash task error.
	 * 
	 * @return true if sub class handled the exception, false will prompt dialog with specified reason message.
	 */
	protected boolean onSplashTaskFailed(int reason) {
		// default prompt up dialog.
		return false;
	}
	
	protected abstract SplashSetting getSplashSetting();
	
	/**
	 * set splash task successful and enter home page if it is OK to enter.
	 */
	public void setSplashTaskSuccessful() {
		mHandler.sendEmptyMessage(HANDLER_CHECK_TASK_FINISHED);
	}
	
	/**
	 * set splash task failed and prompt up dialog.
	 * You can set timeout if you don't want to set failed by yourself and the splash task has timeout limit.
	 */
	public void setSplashTaskFailded() {
		mHandler.obtainMessage(HANDLER_SPLASH_TASK_ERROR, "setSplashTaskFailded().").sendToTarget();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case ERROR_REASON_NETWORK_UNAVAILABLE:
			return new AlertDialog.Builder(this).setTitle(settings.networkUnavailableTitle).setMessage(settings.networkUnavailableMsg)
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).create();
		case ERROR_REASON_SPLASH_TASK_ERROR:
			return new AlertDialog.Builder(this).setTitle(settings.splashTaskErrorTitle).setMessage(settings.splashTaskErrorMsg)
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).create();
		}
		return null;
	}
	
	private void removeAllHandlerMessage() {
		mHandler.removeMessages(HANDLER_NETWORK_UNAVAILABLE);
		mHandler.removeMessages(HANDLER_SPLASH_TASK_ERROR);
		mHandler.removeMessages(HANDLER_CHECK_TASK_FINISHED);
		mHandler.removeMessages(HANDLER_CHECK_TASK_TIMEOUT);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		removeAllHandlerMessage();
		Log.w(TAG, "onDestroy.");
	}
	
	/**
	 * Should be static !!!!!!
	 */
	private static class InnerStaticHandler extends WeakHandler<BaseSplashActivity> {
		
		InnerStaticHandler(BaseSplashActivity act) {
			super(act);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void handleWeakHandlerMessage(BaseSplashActivity contextObject, Message msg) {
			switch (msg.what) {
			case HANDLER_NETWORK_UNAVAILABLE:
				Log.e(TAG, "Network unavailable.");
				contextObject.removeAllHandlerMessage();
				if(!contextObject.onSplashTaskFailed(HANDLER_NETWORK_UNAVAILABLE)) {
					if(!contextObject.isFinishing()) {
						contextObject.showDialog(HANDLER_NETWORK_UNAVAILABLE);
					}
				}
				break;
			case HANDLER_SPLASH_TASK_ERROR:
			case HANDLER_CHECK_TASK_TIMEOUT:
				if(msg.what == HANDLER_SPLASH_TASK_ERROR) {
					Log.e(TAG, "Execute splash task failed : " + msg.obj);
				} else {
					Log.e(TAG, "Execute splash task failed : timeout " + contextObject.settings.maxDynamicSplashTime + " ms.");
				}
				contextObject.removeAllHandlerMessage();
				if(!contextObject.onSplashTaskFailed(HANDLER_SPLASH_TASK_ERROR)) {
					if(!contextObject.isFinishing()) {
						contextObject.showDialog(HANDLER_SPLASH_TASK_ERROR);
					}
				}
				break;
			case HANDLER_CHECK_TASK_FINISHED:
				if(contextObject.mIsOKToEnterHomePage) {
					// isOKToEnterHomePage is true, enter home page here.
					Log.i(TAG, "Execute splash task success. About to close splash page and show home page.");
					contextObject.removeAllHandlerMessage();
					contextObject.startActivity(contextObject.settings.homePageIntent);
					contextObject.finish();
				} else {
					// isOKToEnterHomePage is false, ask the later one(task done or MIN_DYNAMIC_SPLASH_TIME check) to enter home page.
					contextObject.mIsOKToEnterHomePage = true;
				}
				break;
			}
		}
		
	}
	
	/*
	 * extends Handler directly. Should be static !!!!!!
	 * 
	private static class InnerStaticHandler extends Handler {
		
		private final WeakReference<BaseSplashActivity> mContextObject;
		
		InnerStaticHandler(BaseSplashActivity object) {
			mContextObject = new WeakReference<BaseSplashActivity>(object);
		}
		
	    @Override
	    public final void handleMessage(Message msg){
	    	BaseSplashActivity act = mContextObject.get();
	         if (act != null) {
	 			switch (msg.what) {
				case HANDLER_NETWORK_UNAVAILABLE:
					act.onLoadingConfigFailed(HANDLER_NETWORK_UNAVAILABLE);
					break;
				case HANDLER_REQUEST_API_ERROR:
					if(!act.isFinishing()) {
						act.onLoadingConfigFailed(HANDLER_REQUEST_API_ERROR);
					}
					break;
				}
	         }
	    }

	}
	*
	*/
	
	public static class SplashSetting {
		
		// splash time
		private static final int MIN_DYNAMIC_SPLASH_TIME = 1000;
		private static final int MAX_DYNAMIC_SPLASH_TIME = Integer.MAX_VALUE;
		private static final int MIN_STATIC_SPLASH_TIME = 1000;
		private static final int MAX_STATIC_SPLASH_TIME = 5000;
		// settings
		private int maxDynamicSplashTime = MAX_DYNAMIC_SPLASH_TIME;
		private int maxStaticSplashTime = MAX_STATIC_SPLASH_TIME;
		private boolean isDynamicSplashTime;
		private Runnable splashTask;
		private boolean isSplashTaskRunOnUiThread;
		private Intent homePageIntent;
		private String networkUnavailableTitle;
		private String networkUnavailableMsg;
		private String splashTaskErrorTitle;
		private String splashTaskErrorMsg;
		private boolean isIgnoreNetworkConnectivity;
		
		public SplashSetting(boolean isDynamicSplashTime) {
			this.isDynamicSplashTime = isDynamicSplashTime;
		}
		
		/**
		 * set max static splash time in milliseconds, default 5000ms.
		 * Note that max static splash time can only be range of [1000, 5000].
		 * 
		 * @param maxDynamicSplashTime
		 */
		public void setMaxStaticSplashTime(int maxStaticSplashTime) {
			if(maxStaticSplashTime > MAX_STATIC_SPLASH_TIME) {
				maxStaticSplashTime = MAX_STATIC_SPLASH_TIME;
			} else if(maxStaticSplashTime < MIN_STATIC_SPLASH_TIME) {
				maxStaticSplashTime = MIN_STATIC_SPLASH_TIME;
			} 
			this.maxStaticSplashTime = maxStaticSplashTime;
		}
		
		
		/**
		 * set max dynamic splash time in milliseconds, default infinite until splash task are done.
		 * 
		 * @param maxDynamicSplashTime
		 */
		public void setMaxDynamicSplashTime(int maxDynamicSplashTime) {
			if(maxDynamicSplashTime > MAX_DYNAMIC_SPLASH_TIME) {
				maxDynamicSplashTime = MAX_DYNAMIC_SPLASH_TIME;
			} else if(maxDynamicSplashTime < MIN_DYNAMIC_SPLASH_TIME) {
				maxDynamicSplashTime = MIN_DYNAMIC_SPLASH_TIME;
			} 
			this.maxDynamicSplashTime = maxDynamicSplashTime;
		}

		/**
		 * set splash task and set whether the splash run on UI thread.
		 * 
		 * @param splashTask
		 * @param runOnUiThread
		 */
		public void setSplashTask(Runnable splashTask, boolean runOnUiThread) {
			this.splashTask = splashTask;
			this.isSplashTaskRunOnUiThread = runOnUiThread;
		}
		
		/**
		 * set home page intent.
		 * 
		 * @param intent
		 */
		public void setHomePageIntent(Intent intent) {
			this.homePageIntent = intent;
		}

		public void setNetworkUnavailableTitle(String splashTaskErrorTitle) {
			this.networkUnavailableTitle = splashTaskErrorTitle;
		}
		
		/**
		 * set network unavailable prompt message string resource.
		 * 
		 * @param networkUnavailableMsg
		 */
		public void setNetworkUnavailableMsg(String networkUnavailableMsg) {
			this.networkUnavailableMsg = networkUnavailableMsg;
		}

		public void setSplashTaskErrorTitle(String splashTaskErrorTitle) {
			this.splashTaskErrorTitle = splashTaskErrorTitle;
		}
		
		/**
		 * set splash task prompt message string resource.
		 * 
		 * @param splashTaskErrorMsg
		 */
		public void setSplashTaskErrorMsg(String splashTaskErrorMsg) {
			this.splashTaskErrorMsg = splashTaskErrorMsg;
		}
		
		/**
		 * set Splash ignore network connectivity.
		 */
		public void setIgnoreNetworkConnectivity() {
			isIgnoreNetworkConnectivity = true;
		}
		
	} // end of inner class.
	
}
