package com.pccw.nowplayer.app;


import com.pccw.nowplayer.app.AppLocaleAide.AppLocaleAideSupport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;


/**
 * @author AlfredZhong, AaronLuo
 * @version 1.0, 2013-05-30
 */
public class BaseActivity extends Activity implements AppLocaleAideSupport {
	
	private static final String TAG = BaseActivity.class.getSimpleName();
	private AppLocaleAide mAppLocaleAide = new AppLocaleAide(this);
	private AppProgressDialogAide mAppDialogAide;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.w(TAG, getClass().getName() + " onCreate(), extras = " + getIntent().getExtras());
		if(savedInstanceState != null) {
			Log.e(TAG, getClass().getSimpleName() + " savedInstanceState = " + savedInstanceState);
		}
		mAppLocaleAide.maintainAppLocale(this);
		mAppDialogAide = new AppProgressDialogAide(this);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, getClass().getName() + " onRestart().");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, getClass().getName() + " onStart().");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, getClass().getName() + " onResume().");
		mAppLocaleAide.checkAppLocale(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.i(TAG, getClass().getName() + " onNewIntent().");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, getClass().getName() + " onPause().");
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.v(TAG, getClass().getName() + " onBackPressed().");
	}

	@Override
	public void finish() {
		super.finish();
		Log.v(TAG, getClass().getName() + " finish().");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, getClass().getName() + " onStop().");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, getClass().getName() + " onDestroy().");
		mAppDialogAide.destroyProgressDialog();
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.d(TAG, getClass().getName() + " dispatchKeyEvent().");
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, getClass().getName() + " onKeyDown().");
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.d(TAG, getClass().getName() + " onKeyUp().");
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.i(TAG, getClass().getName() + " onPrepareOptionsMenu().");
		return super.onPrepareOptionsMenu(menu);
	}
	
	public void setProgressDialogMessage(CharSequence message) {
		mAppDialogAide.setProgressDialogMessage(message);
	}
	
	public final void showProgressDialog(boolean cancelable) {
		mAppDialogAide.showProgressDialog(cancelable);
	}
	
	public final void showProgressDialog(Context context, boolean cancelable) {
		mAppDialogAide.showProgressDialog(context, cancelable);
	}
	
	public final void dismissProgressDialog() {
		mAppDialogAide.dismissProgressDialog();
	}
	
	public void setProgressDialogCancelTag(Object cancelTag) {
		mAppDialogAide.setProgressDialogCancelTag(cancelTag);
	}
	
	public final boolean progressDialogHasCanceled(Object cancelTag) {
		return mAppDialogAide.progressDialogHasCanceled(cancelTag);
	}

	@Override
	public void onLocaleChanged() {
	}

	@Override
	public AppLocaleAide getAppLocaleAide() {
		return mAppLocaleAide;
	}
    
}
