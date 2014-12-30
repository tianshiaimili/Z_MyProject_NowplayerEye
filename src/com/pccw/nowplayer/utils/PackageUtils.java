package com.pccw.nowplayer.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class PackageUtils {
	
	private static PackageManager mPackageManager;

	private PackageUtils() {
		// Cannot instantiate.
	}
	
	public static String getSelfPackageName(Context context) {
		return context.getPackageName();
	}
	
	public static PackageManager getPackageManager(Context context) {
		// It is OK not to keep singleton, so we don't need to synchronized here.
		if(mPackageManager == null) {
			mPackageManager = context.getPackageManager();
		}
		return mPackageManager;
	}
	
	public static PackageInfo getSelfPackage(Context context) throws NameNotFoundException {
		return getPackage(context, context.getPackageName());
	}
	
	public static PackageInfo getPackage(Context context, Class<?> cls) throws NameNotFoundException {
		ComponentName comp = new ComponentName(context, cls);
		return getPackageManager(context).getPackageInfo(comp.getPackageName(), PackageManager.PERMISSION_GRANTED);
	}
	
	public static PackageInfo getPackage(Context context, String packageName) throws NameNotFoundException {
		// getPackageInfo() since API Level 1.
		return getPackageManager(context).getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
	}
	
	/**
	 * Check the package installed or not. Costs several milliseconds.
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean checkPackageInstalled(Context context, String packageName) {
		boolean installed = false;
		try {
			getPackage(context, packageName);
			installed = true;
		} catch (PackageManager.NameNotFoundException e) {
		}
		return installed;
	}
	
}
