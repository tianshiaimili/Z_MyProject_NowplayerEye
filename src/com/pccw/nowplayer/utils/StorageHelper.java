package com.pccw.nowplayer.utils;

import java.io.File;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

/**
 * See here for more info:
 * http://developer.android.com/guide/topics/data/data-storage.html
 * 
 * Environment.getRootDirectory().getPath(); // "/system"
 * Environment.getDataDirectory().getPath(); // "/data"
 * Environment.getDownloadCacheDirectory().getPath(); // "/cache"
 * Environment.getExternalStorageDirectory().getPath(); // "/mnt/sdcard"
 * 
 * @author AlfredZhong
 * @version 2012-07-27
 */
public class StorageHelper {
	
	private static final String TAG = StorageHelper.class.getSimpleName();
	
	private StorageHelper() {};
	
	public static boolean isExternalStorageMounted() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			Log.w(TAG, "External storage is present and mounted at its mount point with read/write access.");
			return true;
		}
		return false;
	}
	
    /**
     * Check if OS version has built-in external cache dir method.
     *
     * @return
     */
    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }
	
    public static File getExternalCacheDir(Context context) {
    	File cacheFile = null;
        if (hasExternalCacheDir()) {
        	// If calling this before Froyo(Android2.2.x) will cause java.lang.NoSuchMethodError: android.content.Context.getExternalCacheDir.
        	// Returns null if ExternalStorageState illegal or you don't have the permission(ApplicationContext : Unable to create external cache directory).
        	cacheFile = context.getExternalCacheDir();
        }
        if(cacheFile == null) {
            // Before Froyo we need to construct the external cache dir ourselves
            // internal cache dir is "/data/data/com.example.android.bitmapfun/cache"
            final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache";
        	cacheFile = new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
        }
        return cacheFile;
    }
	
}
