package com.pccw.nowplayer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * @author Marcus Situ
 * @version 20130909
 */
public class VideoUtils {
	
	private static final String TAG = VideoUtils.class.getSimpleName();
	
	/**
	 * M3U8 EXTINF tag
	 */
	private static final String EXTINF = "#EXTINF:";
	
	/**
	 * Use for {@link VideoUtils#createVideoFrameFromM3U8(Activity, String, VideoImageCallback)}
	 */
	public interface VideoImageCallback {
		/**
		 * Should call on UI thread
		 * @param bitmap video preview image
		 */
		void onComplete(Bitmap bitmap);
	}
	
	/**
	 * Create video preview image of m3u8 URL and callback with the bitmap
	 * @param activity
	 * @param m3u8URL
	 * @param callback Should on UI thread
	 */
	public static void createVideoFrameFromM3U8(final Activity activity, final String m3u8URL, final VideoImageCallback callback){
		Log.e(TAG, "createVideoFrameFromM3U8 !!!!!!!!!!!!!!!!!");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Bitmap bm = null;
				android.media.MediaMetadataRetriever retriever = null;
				try {
					String firstSegmentName = getFirstSegmentNameFromM3U8(m3u8URL);
					/* 
					 * 20130909 by Situ 
					 * 
					 * 	android.media.MediaMetadataRetriever -- API level 10
					 * 
					 * setDataSource (String uri, Map<String, String> headers) 
					 * -- API level 14 (4.0)
					 * "Sets the data source (URI) to use. Call this method before the rest of the methods in this class.
					 * This method may be time-consuming."
					 * -- use this for Internet URL video file.
					 * -- Must be video file URL, e.g. http://example.com/video.ts
					 * -- ".m3u8" URL does not work, throws "IllegalArgumentException"
					 * -- parameter "headers" CANNOT be null, throws "NullPointerException"
					 * 
					 * setDataSource (String path) 
					 * "Sets the data source (file pathname) to use. Call this method before the rest of the methods in this class. 
					 * This method may be time-consuming."
					 * -- does not work for Internet URL, only for local file path.
					 * 
					 * setDataSource (Context context, Uri uri)
					 * -- does not work for Internet URL.
					 * -- MAYBE is used for content provider.
					 */
					retriever = new android.media.MediaMetadataRetriever();
					retriever.setDataSource(firstSegmentName, new HashMap<String, String>());
					bm = retriever.getFrameAtTime();
				} catch (Exception e) {
					Log.e(TAG, "ERROR - VideoUtils capture frame. " + e.toString());
				}finally{
					if(retriever != null)
						retriever.release();
				}
				final Bitmap bitmap = bm;
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						callback.onComplete(bitmap);
					}
				});
			}
		}).start();
	}
	
	private static String getFirstSegmentNameFromM3U8(String m3u8URL) throws IOException{
		Log.d(TAG, "getFirstSegmentNameFromM3U8");
		String firstSegmentName = null;
		InputStream is = NetUtils.getInputStream(m3u8URL);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));   
        String line = null;   
        boolean nextLineIsFinename = false;
        try {   
            //while()
            // parse m3u8
            while ((line = reader.readLine()) != null) {   
        		// line of duration
				if(line.startsWith(EXTINF))
				{
					nextLineIsFinename = true;
                } 
				else if(nextLineIsFinename == true)
				{
            		//line of filename
            		nextLineIsFinename = false;
            		firstSegmentName = line;
            		Log.i(TAG, "first Segment Name == "+ firstSegmentName);
            		break;
            	}
            } //while  
        } catch (IOException e) {   
            Log.w(TAG, "ERROR - getFirstSegmentNameFromM3U8: " + e.toString());
        } finally {   
            try {   
                is.close();   
            } catch (IOException e) {   
            	Log.w(TAG, "ERROR - getFirstSegmentNameFromM3U8 clean up InputStream: " + e.toString());
            }   
        }
		return firstSegmentName;   
	}	

}
