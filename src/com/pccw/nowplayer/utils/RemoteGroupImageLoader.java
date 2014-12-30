package com.pccw.nowplayer.utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * A helper class to load image from Internet and refresh the ImageView in GroupView.
 * <p>Need android.permission.INTERNET, android.permission.WRITE_EXTERNAL_STORAGE
 * 
 * @author AlfredZhong
 * @version 2012-04-03
 * @version 2012-06-27, changed the strategy of finding ImageView with tag.
 * @version 2012-07-30, added ImageLoader.
 * @version 2012-07-31, changed AbsListView to ViewGroup.
 */
public class RemoteGroupImageLoader extends ImageLoader {

	private static final String TAG = RemoteGroupImageLoader.class.getName();
	private AbsImageCacheHelper mImageCacheHelper;
	private ViewGroup mViewGroup;
	private HashMap<String, DownloadTaskStatus> mTaskStatus;
	private boolean mIsTagUnique;

	/**
	 * @param helper the helper to cache images
	 * @param view such as ListView and GridView
	 */
	public RemoteGroupImageLoader(AbsImageCacheHelper helper, ViewGroup view) throws IllegalArgumentException {
		mImageCacheHelper = helper;
		mViewGroup = view;
		if(mImageCacheHelper == null || mViewGroup == null) {
			throw new IllegalArgumentException(TAG + " parameters can not be null.");
		}
		mTaskStatus = new HashMap<String, DownloadTaskStatus>();
	}
	
	/**
	 * Set true if the view tag is unique to every item. Default is false.
	 * @param unique
	 */
	public void setImageViewTagUnique(boolean unique) {
		mIsTagUnique = unique;
	}
	
	/**
	 * Get the bitmap from the Internet with the specified URL and set the bitmap for the ImageView asynchronously.
	 * Note that don't set tag for the ImageView outside and not to call this function in a thread which not has view root.
	 * 
	 * @param imageView
	 * @param url
	 * @param position just to use position to log currently
	 */
	public void setRemoteImage(ImageView imageView, String url, int position) {
		if(imageView == null || url == null || url.equals("")) {
			Log.w(TAG, "setRemoteImage() imageView == null or url == null or url is empty.");
			return;
		}
		imageView.setTag(url);
		Bitmap bitmap = mImageCacheHelper.getImage(url);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			return;
		}
		imageView.setImageBitmap(getLoadingImage());
		// bitmap is null, that means never download or downloading or bitmap has been GC or download failed.
		DownloadTaskStatus status = mTaskStatus.get(url);
		boolean needNewTask = false;
		if(status != null) {
			if(status == DownloadTaskStatus.PENDING || status == DownloadTaskStatus.RUNNING) {
			} else if(status == DownloadTaskStatus.SUCCESS) {
				Log.w(TAG, "position " + position + " has been GC.");
				needNewTask = true;
			} else if(status == DownloadTaskStatus.FAILED) {
				Log.w(TAG, "position " + position + " download failed.");
				mTaskStatus.remove(url);
				needNewTask = true;
			}
		} else {
			Log.w(TAG, "position " + position + " never download.");
			needNewTask = true;
		}
		if(needNewTask) {
			new ImageDownloadTask(mViewGroup, url).execute();
		}
	}
	
    /**
     * Indicates the current status of the ImageView download task. 
     * Each status will be set only once during the lifetime of a task.
     */
    private enum DownloadTaskStatus {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,
        /**
         * Indicates that the task is running.
         */
        RUNNING,
        /**
         * Indicates that the task has finished.
         */
        SUCCESS,
        /**
         * Indicates that the task has finished.
         */
        FAILED,
    }
    
    /**
     * A helper class to load image from the Internet and cached the bitmap data with its URL as key and refresh the ImageView.
     * 
     * @author AlfredZhong
     * @version 2012-04-03
     * @version 2012-06-27, changed the strategy of finding ImageView with tag, added BitmapProcessor.
     * @version 2012-07-25, used ViewGroup to refer to the parent view.
     * @version 2012-07-28, used WeakReference to ViewGroup to ensure the ViewGroup can be garbage collected.
     */
    private class ImageDownloadTask extends ScalingAsyncTask<Void, Void, Bitmap> {

    	private String url;
    	private WeakReference<ViewGroup> viewGroupReference;
    	
    	public ImageDownloadTask(ViewGroup view, String url) {
    		this.url = url;
    		this.viewGroupReference = new WeakReference<ViewGroup>(view);
    		mTaskStatus.put(url, DownloadTaskStatus.PENDING);
    	}

    	@Override
    	protected Bitmap doInBackground(Void... params) {
    		mTaskStatus.put(this.url, DownloadTaskStatus.RUNNING);
    		Bitmap bitmap = null;
         	try {
    			byte[] data = NetUtils.getByteArray(this.url);
    			if(data != null) {
    				bitmap = BitmapFactory.decodeByteArray(data, 0, data.length); 
    			}
    		} catch (Exception e) {
    			Log.w(TAG, TAG + " download failed : " + e + "\nurl : " + this.url);
    		}
         	if(bitmap != null && mImageCacheHelper != null) {
    			mImageCacheHelper.putImage(url, bitmap);
         	}
    		return bitmap;
    	}
    	
    	@Override
    	protected void onPostExecute(Bitmap result) {
    		if(result != null) {
    			// update status.
    			mTaskStatus.put(this.url, DownloadTaskStatus.SUCCESS);
    			ViewGroup viewGroup = this.viewGroupReference.get();
    			if(viewGroup != null && result != null){
    				if(mIsTagUnique) {
    					setImageView(viewGroup, this.url, result);
    				} else {
    					int childCount = viewGroup.getChildCount();
    					for (int i = 0; i < childCount; i++) {
    						setImageView(viewGroup.getChildAt(i), this.url, result);
    					}
    				}
    			} else {
    				Log.w(TAG, "ViewGroup has been garbage collected or bitmap is null.");
    			}
    			viewGroup = null;
    		} else {
    			// update status.
    			mTaskStatus.put(this.url, DownloadTaskStatus.FAILED);
    		}
    		this.url = null;
    		this.viewGroupReference = null;
    	}
    	
    	private void setImageView(View parent, String tag, Bitmap bmp) {
    		View viewWithUrlTag = parent.findViewWithTag(tag);
    		if (viewWithUrlTag != null && viewWithUrlTag instanceof ImageView) {
    			ImageView iv = (ImageView) viewWithUrlTag;
    			if(iv != null) {
    				iv.setImageBitmap(bmp);
    			}
    		} 
    	}

    } // end of inner class

} // end of public class
