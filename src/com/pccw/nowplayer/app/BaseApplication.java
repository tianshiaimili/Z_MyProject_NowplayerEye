package com.pccw.nowplayer.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.pccw.nowplayer.utils.LogUtils2;

import android.app.Application;
import android.util.Log;

/**
 * @author AlfredZhong
 * @version 2012-07-18
 */
public class BaseApplication extends Application {

	private static final String TAG = BaseApplication.class.getSimpleName();
	private static String filesDir;
	
	/**
	 * Note:
	 * <ol>
	 * <li>Called when the application is starting, before any other application objects have been created.</li>
	 * <li>Implementations should be as quick as possible.</li>
	 * <li>If you override this method, be sure to call super.onCreate().</li>
	 * <li>There is no way to know for sure when an application is killed. 
	 * You can just find out that individual Activities have been destroyed. 
	 * However, if Application.onCreate() is called on your application then you know that 
	 * it was killed at some point in the past, so you can do whatever reloading you need to do then.</li>
	 * <li>After application was killed by system, it can recreate itself only if it have one or more services.</li>
	 * <li>When application is onCreate() after it was killed by system in the past, its activity stack and
	 * activity intents will restore by system.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG,  BaseApplication.class.getName() + " onCreate().");
		initFilesPath();
	}
	
	private void initFilesPath() {
		filesDir = getFilesDir().getAbsolutePath(); // usually "/data/data/xxxx(一般是程序的包名 例如 com.pccw.noewplayerapp)/files".
		if(filesDir != null && filesDir.length() > 0) {
			if(!filesDir.substring(filesDir.length() - 1).equals("/")) {
				filesDir += "/";
			}
		}
//		Log.w(TAG,  "Init files dir " + filesDir);
		LogUtils2.w("Init files dir " + filesDir);
	}
	
	/**
	 * Returns the absolute path to the directory on the filesystem 
	 * where files created with {@link #openFileOutput(String, int)} are stored.
	 * 
	 * @return Returns the path of the directory holding application files, e.g. "/data/data/com.android.app/files/".
	 */
	public static String getFilesPath() {
		return filesDir;
	}
	
	/**
	 * Open a private file associated with this application package for reading.
	 * 
	 * @param name The name of the file to open; can not contain path separators.
	 * @return FileInputStream Resulting input stream.
	 * @throws FileNotFoundException
	 */
	public static InputStream openFilesInputStream(String name) throws FileNotFoundException {
		return new FileInputStream(getFilesPath() + name);
	}
	
}
