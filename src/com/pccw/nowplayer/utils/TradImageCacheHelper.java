package com.pccw.nowplayer.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * A helper class to manage images cache in memory and disk.
 * <p>Note: you should remove the bitmap from memory if you don't need it.
 * 
 * @author AlfredZhong
 * @version 1.0, 2011-12-28
 * @version 2012-06-27, changed super class to AbsImageCacheHelper.
 * @version 2012-07-27, added ImageCacheParams.
 */
public class TradImageCacheHelper extends AbsImageCacheHelper {

	private static final String TAG = TradImageCacheHelper.class.getSimpleName();
	private static final HashMap<String, SoftReference<Bitmap>> bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
	private ImageCacheParams mCacheParams;
	
	public TradImageCacheHelper(ImageCacheParams params) {
		if(params == null) {
			throw new NullPointerException("ImageCacheParams can not be null.");
		}
		mCacheParams = params;
		if(mCacheParams.isDiskCacheEnabled()) {
			// call mkdirs() every time in case user delete the cache folder.
			new File(mCacheParams.getDiskCachePath()).mkdirs();
		}
		Log.w(TAG, mCacheParams.toString());
		Log.w(TAG, TAG + " memory cache map size is " + size());
	}
	
	@Override
	public void putImage(String key, Bitmap bitmap) {
		if(bitmap == null) {
			Log.w(TAG, TAG + " putImage failed, bitmap is null.");
			return;
		}
		// Put into cache (memory)
		bitmapCache.put(key, new SoftReference<Bitmap>(bitmap));
		if(mCacheParams.isDiskCacheEnabled()){
			// save image file to disk.
			String filename = getFilename(key);
			if(filename != null)
				BitmapHelper.writeBitmapToFile(filename, bitmap, mCacheParams.getCompressFormat(), mCacheParams.getCompressQuality());
		}
	}
	
	@Override
	public boolean containsKey(String key) {
		return bitmapCache.containsKey(key);
	}

	@Override
	public Bitmap getImage(String key) {
		Bitmap bm = null;
		SoftReference<Bitmap> sr = null;
		// Whether the bitmap reference is in the map.
		if((sr = bitmapCache.get(key)) != null) {
			// Find image in memory, hold a strong reference to the referent to use it.
			bm = sr.get();
		}
		// If bitmap is null, try to find image in disk.
		if(bm == null) {
			String filename = getFilename(key);
			if(filename != null && new File(filename).exists()) {
				bm = BitmapFactory.decodeFile(filename);
			}
			if(bm != null) {
				// The image file exists in the cache path, put bitmap into map.
				bitmapCache.put(key, new SoftReference<Bitmap>(bm));
			} 
		} 
		return bm;
	}
	
	@Override
	public void removeImage(String key) {
		Bitmap bm = null;
		SoftReference<Bitmap> sr = null;
		if((sr = bitmapCache.get(key)) != null) {
			// hold a strong reference to the referent to use it
			bm = sr.get();
		}
		if(bm != null){
			bm.recycle();
			bm = null;
		}
		// remove reference in cache map
		bitmapCache.remove(key); 
	}
	
	@Override
	public void clear() {
		if(bitmapCache.size() > 0) {
			ArrayList<String> keys = new ArrayList<String>(bitmapCache.keySet());
			for(String key : keys) {
				removeImage(key);
			}
		}
		bitmapCache.clear();
	}
	
	@Override
	public String getFilename(String key) {
		if(key == null)
			return null;
		try {
			return mCacheParams.getDiskCachePath() + URLEncoder.encode(key.replace("*", ""), "UTF-8") + "." + mCacheParams.getCompressFormat();
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
	
	@Override
	public int size() {
		return bitmapCache.size();
	}
	
}
