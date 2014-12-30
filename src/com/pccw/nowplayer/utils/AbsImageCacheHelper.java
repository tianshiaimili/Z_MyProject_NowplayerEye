package com.pccw.nowplayer.utils;

import java.util.List;

import android.graphics.Bitmap;

/**
 * A helper class to manage images cache in memory and SDcard.
 * 
 * @author AlfredZhong
 * @version 2012-06-27
 */
public abstract class AbsImageCacheHelper {
	
	/**
	 * Put bitmap into cache map and put image file in sdcard if saveToSdcard is set true.
	 * @param key
	 * @param bitmap
	 */
	public abstract void putImage(String key, Bitmap bitmap);
	
	/**
	 * Returns whether this cache map contains the specified key.
	 * @param key
	 * @return true if this cache map contains the specified key, false otherwise.
	 */
	public abstract boolean containsKey(String key);
	
	/**
	 * Get bitmap with the specified key from memory or sdcard.
	 * @param key
	 * @return bitmap with the specified key if bitmap in memory or sdcard, otherwise null.
	 */
	public abstract Bitmap getImage(String key);
	
	/**
	 * Remove the bitmap in memory cache.
	 * @param key
	 */
	public abstract void removeImage(String key);
	
	/**
	 * Remove the bitmaps with the giving keys in memory cache.
	 * @param key
	 */
	public void removeImages(List<String> keys) {
		if(keys != null) {
			for(String key : keys)
				removeImage(key);
		}
	}
	
	/**
	 * Remove all bitmaps in memory cache.
	 */
	public abstract void clear();
	
	/**
	 * @param key
	 * @return the filename in images cache.
	 */
	public abstract String getFilename(String key);
	
	public abstract int size();
	
}
