package com.pccw.nowplayer.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * @author AlfredZhong
 * @version 2012-07-30
 */
class ImageLoader {

	private static final String TAG = ImageLoader.class.getName();
	private Bitmap mLoadingBitmap;
	
    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap) {
    	mLoadingBitmap = bitmap;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(Resources res, int resId) {
    	mLoadingBitmap = null;
    	try {
    		mLoadingBitmap = BitmapFactory.decodeResource(res, resId);
    	} catch (Exception e) {
			Log.w(TAG, "setLoadingImage failed " + e);
		}
    }
    
    public Bitmap getLoadingImage() {
    	return mLoadingBitmap;
    } 

}
