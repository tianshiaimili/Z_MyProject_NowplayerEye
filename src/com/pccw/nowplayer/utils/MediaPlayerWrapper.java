package com.pccw.nowplayer.utils;

import java.io.IOException;
import java.lang.reflect.Method;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.SeekBar;

import com.pccw.nowplayer.app.HandlerTimer;


/**
 * A wrapper class of android.media.MediaPlayer and support some useful features.
 * 
 * @author AlfredZhong
 * @version 2012-09-22
 * @version 2013-09-04, integrate video and SeekBar features.
 */
public class MediaPlayerWrapper {

	// extra 3rd properties
	private static final String TAG = MediaPlayerWrapper.class.getSimpleName();
	private MediaPlayer mMediaPlayer;
    private boolean mCanPause;
    private boolean mCanSeek;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    private MediaPlayerState mState;
    // MediaPlayer settings
    private int mStreamtype = AudioManager.STREAM_MUSIC;
    private boolean mLooping;
    private static final float MAX_VOLUME_SCALE = 1;
    private float mLeftVolume, mRightVolume;
    // video settings
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private SurfaceHolder.Callback mSurfaceHolderCallback;
	private int mVideoWidth, mVideoHeight;
	private int mLayoutWidth, mLayoutHeight;
	private VideoMode mMode = VideoMode.MODE_LAYOUT;
	// SeekBar settings
	private SeekBar mSeekBar; // secondary buffering, seek, playing progress.
	private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener;
	private OnSeekBarProgressUpdatedListener mOnSeekBarProgressUpdatedListener;
	private HandlerTimer mTimer;
	private Runnable mUpdateSeekBarProgressRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				if(mSeekBar != null && mMediaPlayer != null && isPlaying() && !mSeekBar.isPressed()) {
					int duration = getDuration();
					int currentPosition = getCurrentPosition() < duration ? getCurrentPosition() : duration;
					if (duration > 0) {
						int progress = (int) (mSeekBar.getMax() * currentPosition / duration);
						Log.v(TAG, "SeekBar :: duration=" + duration + ", currentPosition=" + currentPosition + ", progress=" + progress);
						mSeekBar.setProgress(progress);
						if(mOnSeekBarProgressUpdatedListener != null) {
							mOnSeekBarProgressUpdatedListener.onProgressUpdated(currentPosition, duration);
						}
					}
				}
			} catch(Exception e) {
				Log.w(TAG, "Update SeekBar progress failed : " + e);
			}
		}
	};
	
    /*
     * If I don't mention the states in method description, 
     * that means the method does not rely on MediaPlayer states.
     */
	public MediaPlayerWrapper() {
		mMediaPlayer = new MediaPlayer();
		mState = MediaPlayerState.STATE_IDLE;
		mMediaPlayer.setAudioStreamType(mStreamtype);
		setVolume(MAX_VOLUME_SCALE, MAX_VOLUME_SCALE);
		// set internal listeners.
		mMediaPlayer.setOnPreparedListener(mInternalOnPreparedListener);
		mMediaPlayer.setOnBufferingUpdateListener(mInternalOnBufferingUpdateListener);
		mMediaPlayer.setOnCompletionListener(mInternalOnCompletionListener);
		mMediaPlayer.setOnVideoSizeChangedListener(mInternalOnVideoSizeChangedListener);
		mMediaPlayer.setOnErrorListener(mInternalOnErrorListener);
		mMediaPlayer.setOnSeekCompleteListener(mInternalOnSeekCompleteListener);
		mMediaPlayer.setOnInfoListener(mInternalOnInfoListener);
		mTimer = new HandlerTimer(true);
	}
	
	public MediaPlayer getMediaPlayer() {
		return mMediaPlayer;
	}
    
    /**
     * <pre>
     * Valid states : {Idle, Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted, Error}
     * Invalid states : {}
     * </pre>
     */
    public void reset() throws IllegalStateException {
    	Log.d(TAG, "MediaPlayer reset.");
		/*
		 * NOTE: set MediaPlayer to be in the Idle state.
		 * If MediaPlayer has called release(), reset() will throw IllegalStateException.
		 * Besides, without calling reset(), OnErrorListener.onError() won't be called 
		 * by the internal player engine and the object state remains unchanged, 
		 * not transfered to the Error state.
		 */
		mMediaPlayer.reset();
		mState = MediaPlayerState.STATE_IDLE;
    }
    
    /**
     * <pre>
     * Valid states : {Idle} 
     * Invalid states : {Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted, Error} 
     * </pre>
     */
    public void setDataSource(String path) throws IllegalArgumentException, IllegalStateException, IOException {
    	Log.d(TAG, "MediaPlayer setDataSource : " + path);
		/*
		 * NOTE: setDataSource() will set MediaPlayer in the Initialized state 
		 * if it does not throws exception; otherwise, MediaPlayer keeps Idle.
		 * setDataSource() may throws the Exception below:
		 * IOException, usually status=0x80000000, means lack of INTERNET permission.
		 * IllegalArgumentException, data source is null
		 * IllegalStateException, MediaPlayer is in an invalid state
		 * 
		 * Set the path variable to a local media file path.
		 * Or
		 * Set path variable to progressive streamable mp4 or 3gpp format URL. Http protocol should be used.
		 * Mediaplayer can only play "progressive streamable contents" which basically means: 
		 * 1. the movie atom has to precede all the media data atoms. 
		 * 2. The clip has to be reasonably interleaved.
		 */
		mMediaPlayer.setDataSource(path);
		mState = MediaPlayerState.STATE_INITIALIZED;
    }
    
    /**
     * <pre>
     * Valid states : {Idle} 
     * Invalid states : {Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted, Error} 
     * </pre>
     */
    public void setDataSource(Context context, Uri uri) throws IllegalArgumentException, 
    		IllegalStateException, IOException, SecurityException {
    	Log.d(TAG, "MediaPlayer setDataSource : " + uri);
		mMediaPlayer.setDataSource(context, uri);
		mState = MediaPlayerState.STATE_INITIALIZED;
    }
    
    /**
     * <pre>
     * Valid states : {Initialized, Stopped} 
     * Invalid states : {Idle, Prepared, Started, Paused, PlaybackCompleted, Error} 
     * 
     * Note:
     * 1. prepare() or prepareAsync() can NOT be called before surfaceCreated() if you play videos.
     * 2. prepare() will blocks until MediaPlayer is ready for playback;
     *    prepareAsync() returns immediately, rather than blocking until enough data has been buffered.
     * </pre>
     * @param isFile set false if you don't know whether data source is streaming media or local file.
     * @throws IllegalStateException
     * @throws IOException
     */
    public void prepare(boolean isFile) throws IllegalStateException, IOException {
    	Log.d(TAG, "MediaPlayer prepare.");
    	mState = MediaPlayerState.STATE_PREPARING;
		if(isFile) {
			/*
			 * NOTE: 
			 * prepare() throws IOException, IllegalStateException.
			 * 
			 * prepare() blocks until MediaPlayer is ready for playback;
			 * So we'd better NOT to call prepare() from application's UI thread.
			 * 
			 * If MediaPlayer is in the Initialized or Stopped state, prepare() or|and prepareAsync() 
			 * may trigger OnErrorListener.onError() and OnCompletionListener.onCompletion() 
			 * when it occurs exception and we catch its exception. 
			 * This feature will be in effect or not depending on devices.
			 * Usually prepare() will have this feature in some devices.
			 */
			mMediaPlayer.prepare();
			mState = MediaPlayerState.STATE_PREPARED;
		} else {
			/*
			 * NOTE: 
			 * prepareAsync() throws IllegalStateException.
			 */
			mMediaPlayer.prepareAsync();
		}
    }
    
    /**
     * <pre>
     * Valid states : {Prepared, Started, Paused, PlaybackCompleted}
     * Invalid states : {Idle, Initialized, Stopped, Error}
     * </pre>
     */
    public void start() throws IllegalStateException {
    	Log.d(TAG, "MediaPlayer start.");
    	mMediaPlayer.start();
    	mState = MediaPlayerState.STATE_STARTED;
    	if(!mTimer.isRepeatExecutionRunning()) {
    		mTimer.scheduleRepeatExecution(mUpdateSeekBarProgressRunnable, 0, 800);
    	}
    }
    
    /**
     * <pre>
     * Valid states : {Started, Paused}
     * Invalid states : {Idle, Initialized, Stopped, Error}
     * </pre>
     */
    public void pause() throws IllegalStateException {
    	Log.d(TAG, "MediaPlayer pause.");
    	/*
    	 * Pauses playback. Call start() to resume.
    	 * throws IllegalStateException if the internal player engine has not been initialized.
    	 */
    	mMediaPlayer.pause();
    	mState = MediaPlayerState.STATE_PAUSED;
    	mTimer.cancelRepeatExecution();
    }
    
    /**
     * <pre>
     * Valid states : {Prepared, Started, Stopped, Paused, PlaybackCompleted}
     * Invalid states : {Idle, Initialized, Error}
     * </pre>
     */
    public void stop() throws IllegalStateException {
    	Log.w(TAG, "MediaPlayer stop.");
    	/*
    	 * Stops playback. Call prepare() or prepareAsync(), then start() to resume.
    	 * throws IllegalStateException if the internal player engine has not been initialized.
    	 */
    	mMediaPlayer.stop();
    	mState = MediaPlayerState.STATE_STOPPED;
    	mTimer.cancelRepeatExecution();
    }
    
    /**
     * <pre>
     * Valid states : {Prepared, Started, Paused, PlaybackCompleted} 
     * Invalid states : {Idle, Initialized, Stopped, Error}
     * </pre>
     */
    public void seekTo(int msec) throws IllegalStateException {
    	Log.d(TAG, "MediaPlayer seekTo " + msec);
    	// throws IllegalStateException if the internal player engine has not been initialized.
    	mMediaPlayer.seekTo(msec);
    }
    
    /**
     * <pre>
     * Valid states : any
     * Invalid states : {}  
     * </pre>
     */
    public void release() {
    	Log.e(TAG, "MediaPlayer release.");
		/*
		 * NOTE: After release(), the object is no longer available.
		 * You can not call reset() to reuse the MediaPlayer.
		 * You have to re create a new MediaPlayer object.
		 */
		mMediaPlayer.release();
		mState = MediaPlayerState.STATE_END;
		if(mSurfaceHolder != null && mSurfaceHolderCallback != null) {
			mSurfaceHolder.removeCallback(mSurfaceHolderCallback);
		}
		mTimer.cancelRepeatExecution();
		mTimer.quit();
    }
    
    /**
     * <pre>
     * Valid states : {Idle, Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted} 
     * Invalid states : {Error}  
     * </pre>
     */
    public int getCurrentPosition() {
		/*
		 * There is is a bug in Android MediaPlayer. 
		 * When playing network music, if MediaPlayer seek to a position which has not been buffered, 
		 * it continues playing from the position where it was before seeking.
		 * But the MediaPlayer.getCurrentPosition() returns the the position after seeking.
		 * Therefore, When playing reaches its end and OnCompletionListener.onCompletion 
		 * is called, the current media player position is much higher than real song duration.
		 * more information:
		 * Issue 4124:  MediaPlayer seekTo doesn't work for streams  
		 * http://code.google.com/p/android/issues/detail?id=4124
		 */
    	return mMediaPlayer.getCurrentPosition();
    }
    
    /**
     * <pre>
     * Valid states : {Prepared, Started, Paused, Stopped, PlaybackCompleted} 
     * Invalid states : {Idle, Initialized, Error}   
     * </pre>
     */
    public int getDuration() {
    	return mMediaPlayer.getDuration();
    }
    
    /**
     * <pre>
     * Valid states : {Idle, Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted} 
     * Invalid states : {Error}
     * </pre>
     */
    public int getVideoHeight() {
    	int height = mMediaPlayer.getVideoHeight();
    	Log.d(TAG, "getVideoHeight " + height);
    	return height;
    }
    
    /**
     * <pre>
     * Valid states : {Idle, Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted} 
     * Invalid states : {Error}
     * </pre>
     */
    public int getVideoWidth() {
    	int width = mMediaPlayer.getVideoWidth();
    	Log.d(TAG, "getVideoWidth " + width);
    	return width;
    }
    
    /**
     * <pre>
     * Valid states : {Idle, Initialized, Stopped, Prepared, Started, Paused, PlaybackCompleted} 
     * Invalid states : {Error}
     * </pre>
     */
    public void setAudioStreamType(int streamtype) {
    	mStreamtype = streamtype;
    	Log.d(TAG, "MediaPlayer setAudioStreamType " + mStreamtype);
    	/*
    	 * Must call this method before prepare() or prepareAsync() 
    	 * in order for the target stream type to become effective thereafter.
    	 * Usually use AudioManager.STREAM_MUSIC.
    	 */
    	mMediaPlayer.setAudioStreamType(mStreamtype);
    }
    
    /**
     * <pre>
     * Valid states : {Idle, Initialized, Stopped, Prepared, Started, Paused, PlaybackCompleted}
     * Invalid states : {Error}
     * </pre>
     */
    public void setLooping(boolean looping) {
    	mLooping = looping;
    	Log.d(TAG, "MediaPlayer setLooping " + mLooping);
    	/*
    	 * There is a bug in setLooping(), we should call this method 
    	 * after MediaPlayer onPrepared, otherwise some device won't loop.
    	 * 
    	 * Also, note that setLooping() is not valid on OGG files.
    	 * Because OGG files contain ANDROID_LOOP=true in their metadata will loop.
    	 * 
    	 * Note that setLooping(true); will disable OnCompletionListener.
    	 */
    	mMediaPlayer.setLooping(mLooping);
    }
    
    /**
     * <pre>
     * Valid states : {Idle, Initialized, Stopped, Prepared, Started, Paused, PlaybackCompleted}
     * Invalid states : {Error}
     * </pre>
     */
    public boolean isPlaying() {
    	return mMediaPlayer.isPlaying();
    }
    
    /**
     * <pre>
     * Valid states : {Idle, Initialized, Stopped, Prepared, Started, Paused, PlaybackCompleted}
     * Invalid states : {Error}
     * </pre>
     */
    public void setVolume(float leftVolume, float rightVolume) {
    	/*
    	 * Balancing the output of audio streams within an application with the current global stream volume,
    	 * which will not change the global stream volume.
    	 * 0 means muting, 1 means the volume of current global stream volume.
    	 * If any parameter is larger than 1, this call will be ignored.
    	 */
    	mLeftVolume = leftVolume;
    	mRightVolume = rightVolume;
    	mMediaPlayer.setVolume(leftVolume, rightVolume);
    }
    
    public void setDisplay(SurfaceHolder sh) {
    	Log.w(TAG, "MediaPlayer setDisplay.");
    	/*
    	 * setDisplay() can NOT be called before surfaceCreated() if you play videos.
    	 */
    	mMediaPlayer.setDisplay(sh);
    }
    
    public boolean isLooping() {
    	return mMediaPlayer.isLooping();
    }
    
    public void setScreenOnWhilePlaying(boolean screenOn) {
    	mMediaPlayer.setScreenOnWhilePlaying(screenOn);
    }
    
    public void setWakeMode(Context context, int mode) {
    	mMediaPlayer.setWakeMode(context, mode);
    }
    
	// MediaPlayer Listeners
	private OnPreparedListener mExternalOnPreparedListener;
	private OnBufferingUpdateListener mExternalOnBufferingUpdateListener;
	private OnCompletionListener mExternalOnCompletionListener;
	private OnVideoSizeChangedListener mExternalOnVideoSizeChangedListener;
	private OnErrorListener mExternalOnErrorListener;
	private OnSeekCompleteListener mExternalOnSeekCompleteListener;
	private OnInfoListener mExternalOnInfoListener;
	
	private OnPreparedListener mInternalOnPreparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mp) {
			Log.w(TAG, "MediaPlayer onPrepared.");
			mState = MediaPlayerState.STATE_PREPARED;
			if(mLooping) {
				mMediaPlayer.setLooping(mLooping);
			}
			MediaMetadata metadata = getMediaMetadata(mp);
			if(metadata != null) {
			    mCanPause = metadata.pauseAvailable;
			    mCanSeek = metadata.seekAvailable;
			    mCanSeekBack = metadata.seekBackwardAvailable;
			    mCanSeekForward = metadata.seekForwardAvailable;
			    Log.d(TAG, "MediaPlayer metadata: canPause " + mCanPause 
			    		+ ", canSeekBack " + mCanSeekBack + ", canSeekForward " + mCanSeekForward);
			} else {
				// Default true like android.widget.VideoView.
			    mCanPause = mCanSeek = mCanSeekBack = mCanSeekForward = true;
			}
			/*
			 * It is not a good idea to get video width and height here.
			 * Because at this point, maybe there is no display surface was set, 
			 * or the video width and height has not been determined yet.
			 * So it always returns zero video width and video height here.
			 * Therefore, we should better registered OnVideoSizeChangedListener
			 * to provide a notification when the width and height is available.
			 */
			if(mExternalOnPreparedListener != null) {
				mExternalOnPreparedListener.onPrepared(mp);
			}
		}
	};
	/*
	 * OnBufferingUpdateListener only valid when MediaPlayer's data source is network streams. 
	 */
	private OnBufferingUpdateListener mInternalOnBufferingUpdateListener = new OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			Log.v(TAG, "onBufferingUpdate " + percent);
			if(mSeekBar != null) {
				mSeekBar.setSecondaryProgress(percent);
			}
			if(mExternalOnBufferingUpdateListener != null) {
				mExternalOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
			}
		}
	};
	/*
	 * OnCompletionListener won't work if setLooping(true) has been called successfully.
	 */
	private OnCompletionListener mInternalOnCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			Log.w(TAG, "MediaPlayer onCompletion.");
			mState = MediaPlayerState.STATE_PLAYBACKCOMPLETED;
			if(mExternalOnCompletionListener != null) {
				mExternalOnCompletionListener.onCompletion(mp);
			}
		}
	};
	private OnVideoSizeChangedListener mInternalOnVideoSizeChangedListener = new OnVideoSizeChangedListener() {
		@Override
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			/*
			 * Will be called after MediaPlayer.start() and the width and height is available.
			 * And, we can and can only get video original size after this function has been called.
			 * 
			 * This method will be called only once.
			 */
			Log.w(TAG, "MediaPlayer onVideoSizeChanged : (" + width + ", " + height + ").");
			mVideoWidth = width;
			mVideoHeight = height;
			Log.d(TAG, "Current video mode is " + mMode);
			if(mExternalOnVideoSizeChangedListener != null) {
				mExternalOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height);
			}
		}
	};
	private OnErrorListener mInternalOnErrorListener = new OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			mState = MediaPlayerState.STATE_ERROR;
			// what, error type; extra, error code, depending on the device.
			switch(what) {	
			case MediaPlayer.MEDIA_ERROR_UNKNOWN:
				Log.e(TAG, "MEDIA_ERROR_UNKNOWN");
				break;
			case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
				Log.e(TAG, "MEDIA_ERROR_SERVER_DIED");
				break;
			}
			Log.e(TAG, "MediaPlayer onError:(" + what + ", " + extra + ")." );
			if(mExternalOnErrorListener != null) {
				return mExternalOnErrorListener.onError(mp, what, extra);
			}
			/*
			 * Returning false, or not having an OnErrorListener at all, 
			 * will cause the OnCompletionListener to be called.
			 */
			return false;
		}
	};
	private OnSeekCompleteListener mInternalOnSeekCompleteListener = new OnSeekCompleteListener() {
		@Override
		public void onSeekComplete(MediaPlayer mp) {
			if(mExternalOnSeekCompleteListener != null) {
				mExternalOnSeekCompleteListener.onSeekComplete(mp);
			}
		}
	};
	private OnInfoListener mInternalOnInfoListener = new OnInfoListener() {
		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			Log.v(TAG, "MediaPlayer onInfo:(" + what + ", " + extra + ")." );
			if(mExternalOnInfoListener != null) {
				return mExternalOnInfoListener.onInfo(mp, what, extra);
			}
			return false;
		}
	};
	
    /**
     * Register a callback to be invoked when the media source is ready for playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnPreparedListener(OnPreparedListener listener) {
        mExternalOnPreparedListener = listener;
    }
    
    /**
     * Register a callback to be invoked when the status of a network stream's buffer has changed.
     *
     * @param listener the callback that will be run.
     */
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        mExternalOnBufferingUpdateListener = listener;
    }
    
    /**
     * Register a callback to be invoked when the end of a media source has been reached during playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mExternalOnCompletionListener = listener;
    }

    /**
     * Register a callback to be invoked when the video size is known or updated.
     *
     * @param listener the callback that will be run
     */
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        mExternalOnVideoSizeChangedListener = listener;
    }
    
    /**
     * Register a callback to be invoked when an error has happened during an asynchronous operation.
     *
     * @param listener the callback that will be run
     */
    public void setOnErrorListener(OnErrorListener listener) {
        mExternalOnErrorListener = listener;
    }

    /**
     * Register a callback to be invoked when a seek operation has been completed.
     *
     * @param listener the callback that will be run
     */
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
    	mExternalOnSeekCompleteListener = listener;
    }
    
    /**
     * Register a callback to be invoked when an info/warning is available.
     *
     * @param listener the callback that will be run
     */
    public void setOnInfoListener(OnInfoListener listener) {
        mExternalOnInfoListener = listener;
    }
    
    /**********************************************************************
     *************************** Useful Features ***************************
     **********************************************************************/
    
    /*
     * All possible MediaPlayer states:
     * Idle, Initialized, Preparing, Prepared, Started, Paused, Stopped, PlaybackCompleted, Error, End
     */
	public enum MediaPlayerState {
		STATE_IDLE,
		STATE_INITIALIZED,
		STATE_PREPARING, 
		STATE_PREPARED, 
		STATE_STARTED, 
		STATE_PAUSED, 
		STATE_STOPPED, 
		STATE_PLAYBACKCOMPLETED, 
		STATE_ERROR, 
		STATE_END
	}
    
	public MediaPlayerState getMediaPlayerState() {
		return mState;
	}
	
	/**
	 * @author AlfredZhong
	 * @version 2012-09-22
	 */
	private static class MediaMetadata {
		
	    // Playback capabilities fields.
	    public static final String FIELD_PAUSE_AVAILABLE = "PAUSE_AVAILABLE";
	    public static final String FIELD_SEEK_BACKWARD_AVAILABLE = "SEEK_BACKWARD_AVAILABLE";
	    public static final String FIELD_SEEK_FORWARD_AVAILABLE = "SEEK_FORWARD_AVAILABLE";
	    public static final String FIELD_SEEK_AVAILABLE = "SEEK_AVAILABLE";
	    
	    public boolean pauseAvailable;
	    public boolean seekBackwardAvailable;
	    public boolean seekForwardAvailable;
	    public boolean seekAvailable;
	    
	    public MediaMetadata() {
	    }
	    
	} // end of inner class.
	
	/**
	 * Returns MediaMetadata or null if parse MediaMetadata fail or MediaMetadata is null.
	 * You should call this method after MediaPlayer onPrepared.
	 * 
	 * @param metadata
	 * @return
	 */
	private static MediaMetadata getMediaMetadata(MediaPlayer mp) {
		Object metadata = null;
		try {
			Class<MediaPlayer> clazz = MediaPlayer.class;
			Method method = ReflectionUtils.getMethod(clazz, true, "getMetadata", boolean.class, boolean.class);
			boolean sMETADATA_ALL = (Boolean) ReflectionUtils.getStaticFieldValue(
					ReflectionUtils.getField(clazz, true, "METADATA_ALL")); // false
			boolean sBYPASS_METADATA_FILTER = (Boolean) ReflectionUtils.getStaticFieldValue(
					ReflectionUtils.getField(clazz, true, "BYPASS_METADATA_FILTER")); // false
			metadata = ReflectionUtils.invokeInstanceMethod(mp, method, sMETADATA_ALL, sBYPASS_METADATA_FILTER);
		} catch (Exception e) {
			Log.e(TAG, "get media metadata from MediaPlayer.getMetadata() failed : " + e);
		}
		try {
			if (metadata != null) {
				MediaMetadata data = new MediaMetadata();
				Class<?> clazz = ReflectionUtils.getClass("android.media.Metadata");
				Method hasMethod = ReflectionUtils.getMethod(clazz, true, "has", int.class);
				Method getBooleanMethod = ReflectionUtils.getMethod(clazz, true, "getBoolean", int.class);
				int key = (Integer) ReflectionUtils.getStaticFieldValue(
						ReflectionUtils.getField(clazz, true, MediaMetadata.FIELD_PAUSE_AVAILABLE));
				if((Boolean) ReflectionUtils.invokeInstanceMethod(metadata, hasMethod, key)) {
					data.pauseAvailable = (Boolean) ReflectionUtils.invokeInstanceMethod(metadata, getBooleanMethod, key);
				}
				key = (Integer) ReflectionUtils.getStaticFieldValue(
						ReflectionUtils.getField(clazz, true, MediaMetadata.FIELD_SEEK_BACKWARD_AVAILABLE));
				if((Boolean) ReflectionUtils.invokeInstanceMethod(metadata, hasMethod, key)) {
					data.seekBackwardAvailable = (Boolean) ReflectionUtils.invokeInstanceMethod(metadata, getBooleanMethod, key);
				}
				key = (Integer) ReflectionUtils.getStaticFieldValue(
						ReflectionUtils.getField(clazz, true, MediaMetadata.FIELD_SEEK_FORWARD_AVAILABLE));
				if((Boolean) ReflectionUtils.invokeInstanceMethod(metadata, hasMethod, key)) {
					data.seekForwardAvailable = (Boolean) ReflectionUtils.invokeInstanceMethod(metadata, getBooleanMethod, key);
				}
				key = (Integer) ReflectionUtils.getStaticFieldValue(
						ReflectionUtils.getField(clazz, true, MediaMetadata.FIELD_SEEK_AVAILABLE));
				if((Boolean) ReflectionUtils.invokeInstanceMethod(metadata, hasMethod, key)) {
					data.seekAvailable = (Boolean) ReflectionUtils.invokeInstanceMethod(metadata, getBooleanMethod, key);
				}
				return data;
			} 
		} catch (Exception e) {
			Log.e(TAG, "parse Metadata object failed : " + e);
		}
		return null;
	}
	
    public boolean canPause() {
        return mCanPause;
    }

    public boolean canSeek() {
    	return mCanSeek;
    }
    
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    public boolean canSeekForward() {
        return mCanSeekForward;
    }
    
    /**
     * Mute the MediaPlayer.
     */
    public void mute() {
    	mMediaPlayer.setVolume(0, 0);
    }
    
    /**
     * Unmute the MediaPlayer to the volume before muting.
     */
    public void unmute() {
    	mMediaPlayer.setVolume(mLeftVolume, mRightVolume);
    }
    
    public float getLeftVolume() {
    	return mLeftVolume;
    }
    
    public float getRightVolume() {
    	return mRightVolume;
    }
    
    /**
     * reset() MediaPlayer and setDataSource() and then prepareAsync().
     * You can start() MediaPlayer OnPreparedListener.onPrepared().
     * 
     * @author AlfredZhong
     * @version 2012-10-08
     * 
     * @param context
     * @param uri
     * @throws Exception
     */
	public void play(Context context, Uri uri) throws Exception {
		Exception error = null;
		// reset
		try {
			/*
			 * reset() won't clear MediaPlayer settings like looping and so on.
			 */
			reset();
		} catch (IllegalStateException e) {
			Log.e(TAG, "reset failed", e);
		}
		// setDataSource
		try {
			setDataSource(context, uri);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "setDataSource failed", e);
			error = e;
		} catch (IllegalStateException e) {
			Log.e(TAG, "setDataSource failed", e);
			error = e;
		} catch (IOException e) {
			Log.e(TAG, "setDataSource failed", e);
			error = e;
		}
		if(error != null) {
			throw error;
		}
		// start
		try {
			prepare(false);
		} catch (IllegalStateException e) {
			Log.e(TAG, "prepare failed", e);
			error = e;
		} catch (IOException e) {
			Log.e(TAG, "prepare failed", e);
			error = e;
		}
		if(error != null) {
			throw error;
		}
	}
	
	/**
     * reset() MediaPlayer and setDataSource() and then prepareAsync().
     * You can start() MediaPlayer OnPreparedListener.onPrepared().
	 * 
     * @author AlfredZhong
     * @version 2012-10-08
     * 
	 * @param context
	 * @param path
	 * @throws Exception
	 */
	public void play(Context context, String path) throws Exception {
		Uri uri = Uri.parse(path);
		play(context, uri);
	}
	
	/**
	 * If you need to set video SurfaceView, you should call this method before activity onResume().
	 * 
	 * @param surface
	 */
	@SuppressWarnings("deprecation")
	public void setSurfaceView(SurfaceView surfaceView) {
		mSurfaceView = surfaceView;
		mLayoutWidth = mSurfaceView.getLayoutParams().width;
		mLayoutHeight = mSurfaceView.getLayoutParams().height;
		// Initiate SurfaceHolder.
		mSurfaceHolder = mSurfaceView.getHolder();
		if(mSurfaceHolderCallback == null) {
			mSurfaceHolderCallback = new SurfaceHolder.Callback() {
				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					Log.e(TAG, "surfaceCreated called.");
					// setDisplay after surfaceCreated(in ICS) and before MediaPlayer.prepare().
					// java.lang.IllegalArgumentException: The surface has been released
					// You can NOT play video before surfaceCreated() called.
					setDisplay(holder);
				}
				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
					// This is called immediately after any structural changes (format or size) have been made to the surface. 
					// This method is always called at least once, after surfaceCreated.
					Log.d(TAG, "surfaceChanged (format, width, height) = (" + format + ", " + width + ", " + height + ")");
				}
				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					Log.w(TAG, "surfaceDestroyed called.");
				}
			};
		}
		mSurfaceHolder.addCallback(mSurfaceHolderCallback);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
    /**
     * Returns the width and height that the aspect ratio equals to original ratio size.
     * 
     * @param ratioWidth
     * @param ratioHeight
     * @param displayAreaWidth
     * @param displayAreaHeight
     * @return array[0] is resize ratio width, array[1] is resize ratio height.
     */
	public static int[] getAspectRatioSize(int ratioWidth, int ratioHeight, int displayAreaWidth, int displayAreaHeight) {
		/*
		 * To get the width and height that the aspect ratio equals to original video.
		 * 
		 * Firstly, set video width and height to full screen.
		 * Secondly, calculate and find out whether video width too wide or video height too tall.
		 * Finally, if width or height is incorrect, fix it.  
		 */
		int[] risizeXY = new int[]{displayAreaWidth, displayAreaHeight};
		// whether width too wide or height too tall. If width or height is incorrect, fix it.  
		if (ratioWidth > 0 && ratioHeight > 0) {
            if ( ratioWidth * displayAreaHeight  > displayAreaWidth * ratioHeight ) {
            	// ratioWidth / ratioHeight > width / height
                // height too tall, shrink it.
            	risizeXY[1] = displayAreaWidth * ratioHeight / ratioWidth;
            } else if ( ratioWidth * displayAreaHeight  < displayAreaWidth * ratioHeight ) {
            	// ratioWidth / ratioHeight < width / height
                // width too wide, shrink it.
            	risizeXY[0] = displayAreaHeight * ratioWidth / ratioHeight;
            } else {
                // aspect ratio is correct
            }
        }
        return risizeXY;
	}
    
	/**
	 * NOTE:
	 * Video size can not larger than the larger of screenWidth and screenHeight.
	 * For example, in 800*480 device, video height and width can not larger than 800.
	 */
	public static void resizeVideo(SurfaceView surfaceView, int width, int height) {
		/*
		 * If not exact value set in XML, SurfaceView will be as large as screen.
		 * And, if SurfaceView work with MediaPlayer, it default Video size to screen.
		 * So we have to resize video when the width and height is available if we don't want screen size.
		 */
		Log.w(TAG, "resizeVideo : (" + width + ", " + height + ").");
		android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
		lp.width = width;
		lp.height = height;
		/*
		 * SurfaceView.setLayoutParams() will invoke requestLayout();
		 * and cause SurfaceView to onMeasure().
		 * so need call surfaceView.setLayoutParams() to refresh UI.
		 */
		surfaceView.setLayoutParams(lp);
	}
	
	public enum VideoMode {
		MODE_LAYOUT, // SurfaceView layout size
		MODE_VIDEO, // video size.
		MODE_RATIO, // ratio size with the specific video size and layout size.
		MODE_FIXED // fixed size.
	}
	
	public VideoMode getVideoMode() {
		return mMode;
	}
	
	public void setVideoModeLayout() {
		// mLayoutWidth and mLayoutHeight are initialized on setSurfaceView(). 
		resizeVideo(mSurfaceView, mLayoutWidth, mLayoutHeight);
		mMode = VideoMode.MODE_LAYOUT;
		Log.d(TAG, "Current video mode is " + mMode);
	}
	
	public void setVideoModeVideo() {
		resizeVideo(mSurfaceView, mVideoWidth, mVideoHeight);
		mMode = VideoMode.MODE_VIDEO;
		Log.d(TAG, "Current video mode is " + mMode);
	}
	
	public void setVideoModeRatio(int layoutWidth, int layoutHeight) {
		int wh[] = getAspectRatioSize(mVideoWidth, mVideoHeight, layoutWidth, layoutHeight);
		resizeVideo(mSurfaceView, wh[0], wh[1]);
		mMode = VideoMode.MODE_RATIO;
		Log.d(TAG, "Current video mode is " + mMode);
	}
	
	public void setVideoModeFixed(int width, int heigh) {
		resizeVideo(mSurfaceView, width, heigh);
		mMode = VideoMode.MODE_FIXED;
		Log.d(TAG, "Current video mode is " + mMode);
	}
	
	public void setSeekBar(SeekBar seekBar) {
		mSeekBar = seekBar;
		// for user to seek.
		if(mOnSeekBarChangeListener == null) {
			mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
				int msec;
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					if(mMediaPlayer != null) {
						seekTo(msec);
					}
				}
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					// Log.v(TAG, "SeekBar progress = " + progress);
					if(fromUser && mMediaPlayer != null) {
						msec = progress * getDuration() / seekBar.getMax();
					}
				}
			};
		}
		mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
	}
	
	// for caller to update UI, like play time TextView, duration TextView.
	public static interface OnSeekBarProgressUpdatedListener {
		public void onProgressUpdated(int currentPosition, int duration);
	}
	
	public void setOnSeekBarProgressUpdatedListener(OnSeekBarProgressUpdatedListener l) {
		mOnSeekBarProgressUpdatedListener = l;
	}
	
	/**
	 * Transfer millisecond to mm:ss.
	 * 
	 * @param milliseconds
	 * @return
	 */
	public static String transMillionToTime(long milliseconds) {
		int seconds = (int)milliseconds / 1000;
		int minute = seconds / 60;
		int second = seconds % 60;
		String minStr = minute < 10 ? "0" + minute : String.valueOf(minute);
		String secStr = second < 10 ? "0" + second : String.valueOf(second);
		String time = minStr + ":" + secStr;
		return time;
	}
	
}
