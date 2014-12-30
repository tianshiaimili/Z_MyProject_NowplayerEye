package com.pccw.nowplayer.app;


import com.pccw.nowplayer.app.AppLocaleAide.AppLocaleAideSupport;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class BaseFragmentActivity extends FragmentActivity implements AppLocaleAideSupport {
	private AppLocaleAide mAppLocaleAide = new AppLocaleAide(this);
	private AppProgressDialogAide mAppDialogAide;
	
	public void setContentViewFragment(int containerId, Fragment fragment, String tag) {
		// Do NOT use getFragmentManager().
		getSupportFragmentManager().beginTransaction().add(containerId, fragment, tag).commit();
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		mAppDialogAide = new AppProgressDialogAide(this);
		mAppLocaleAide.maintainAppLocale(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mAppLocaleAide.checkAppLocale(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAppDialogAide.destroyProgressDialog();
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
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
