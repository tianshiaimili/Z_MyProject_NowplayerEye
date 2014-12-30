package com.pccw.nowplayer.utils;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.pccw.nmal.util.ImageCache;

/**
 * This class wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 * 
 * @version 2012-07-30
 */
public class RemoteSingleImageLoader extends ImageLoader {
	
	private static final String TAG = RemoteSingleImageLoader.class.getName();
    private AbsImageCacheHelper mImageCacheHelper;
    protected Context mContext;

    public RemoteSingleImageLoader(Context context, AbsImageCacheHelper cacheHelper) {
        mContext = context;
        mImageCacheHelper = cacheHelper;
    }

    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link RemoteSingleImageLoader#processBitmap(Object)} to define the processing logic). A memory and disk
     * cache will be used if an {@link ImageCache} has been set using
     * {@link RemoteSingleImageLoader#setImageCache(ImageCache)}. If the image is found in the memory cache, it
     * is set immediately, otherwise an {@link AsyncTask} will be created to asynchronously load the
     * bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void setRemoteImage(ImageView imageView, String url) {
    	setRemoteImage(imageView, url, getLoadingImage());
    }
    
    public void setRemoteImage(ImageView imageView, String url, Bitmap defaultBitmap) {
		if(imageView == null || url == null || url.equals("")) {
			Log.w(TAG, "setRemoteImage() imageView == null or url == null or url is empty.");
			return;
		}
        Bitmap bitmap = null;
        if (mImageCacheHelper != null) {
            bitmap = mImageCacheHelper.getImage(url);
        }
        if (bitmap != null) {
            // Bitmap found in memory cache
            imageView.setImageBitmap(bitmap);
        } else if (cancelPotentialWork(url, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), defaultBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(url);
        }
    }

    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
        }
    }

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.url;
            if (bitmapData == null || !bitmapData.equals(data)) {
            	// Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }
    
    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    private static class AsyncDrawable extends BitmapDrawable {
    	
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
        
    } // end of inner class
    
    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends ScalingAsyncTask<String, Void, Bitmap> {
    	
		private String url;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        /**
         * Background processing.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
        	url = params[0];
            Bitmap bitmap = null;
            // If the image cache is available and this task has not been cancelled by another thread 
            // and the ImageView that was originally bound to this task is still bound back to this task
            if (mImageCacheHelper != null && !isCancelled() && getAttachedImageView() != null) {
                bitmap = mImageCacheHelper.getImage(url);
            }

            // If the bitmap was not found in the cache and this task has not been cancelled by another thread 
            // and the ImageView that was originally bound to this task is still bound back to this task
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null) {
             	try {
        			byte[] data = NetUtils.getByteArray(url);
        			if(data != null) {
        				bitmap = BitmapFactory.decodeByteArray(data, 0, data.length); 
        			}
        		} catch (Exception e) {
        			Log.w(TAG, TAG + " download failed : " + e + "\nurl : " + this.url);
        		}
            }
            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null && mImageCacheHelper != null) {
            	mImageCacheHelper.putImage(url, bitmap);
            }
            return bitmap;
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // if cancel was called on this task
            if (isCancelled()) {
                bitmap = null;
            }
            final ImageView imageView = getAttachedImageView();
            if (bitmap != null && imageView != null) {
            	imageView.setImageBitmap(bitmap);
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask) {
                return imageView;
            }
            return null;
        }
        
    } // end of inner class
    
}
