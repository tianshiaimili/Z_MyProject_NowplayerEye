package com.pccw.nowplayer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.pccw.nowplayer.utils.MediaPlayerWrapper;
import com.pccw.nowplayer.utils.MediaPlayerWrapper.MediaPlayerState;
import com.pccw.nowplayer.utils.MediaPlayerWrapper.VideoMode;
import com.pccw.nowplayereyeapp.R;

/**
 * @author AlfredZhong
 * @version 1.0, 2011-6-9
 * @version 1.1, 2011-10-14, add resize function.
 * @version 1.2, 2011-10-20, add volume control and media button control function.
 */
public class VideoActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "BackgroundVideoActivity";
	private SurfaceView surfaceView;
	private MediaPlayerWrapper player;
	// about bottom bar
	private boolean showTool = true;
	private FrameLayout layout;
	private int maxVolume, curVolume;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.background_video);
		surfaceView = (SurfaceView) findViewById(R.id.surface_view);
		player = new MediaPlayerWrapper();
		player.setSurfaceView(surfaceView);
		player.setSeekBar((SeekBar) findViewById(R.id.seekbar));
		player.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				//mSurfaceView.setVisibility(View.INVISIBLE);
				player.start();
			}
		});
		player.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
			@Override
			public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
				//mSurfaceView.setVisibility(View.VISIBLE);
			}
		});
		// volume
		maxVolume = 5;
		curVolume = maxVolume;
		initUI();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		player.unmute();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		player.mute();
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		player.stop();
		player.release();
	}
	
	private void initUI() {
		layout = (FrameLayout) findViewById(R.id.bottom_bar_layout);
		findViewById(R.id.main_layout).setOnClickListener(this);
		findViewById(R.id.voice_btn_down).setOnClickListener(this);
		findViewById(R.id.voice_btn_up).setOnClickListener(this);
		findViewById(R.id.mode_btn).setOnClickListener(this);
		findViewById(R.id.play).setOnClickListener(this);
		findViewById(R.id.pause).setOnClickListener(this);
		findViewById(R.id.stop).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_layout:
			if(showTool) {
				layout.setVisibility(View.GONE);
				showTool = false;
			} else {
				layout.setVisibility(View.VISIBLE);
				showTool = true;
			}
			break;
		case R.id.voice_btn_up:

			break;
		case R.id.voice_btn_down:

			break;
		case R.id.mode_btn:
			System.out.println("Current position " + player.getCurrentPosition() + ", duration " + player.getDuration());
			if(player.getVideoMode() == VideoMode.MODE_VIDEO) {
				DisplayMetrics dm = getResources().getDisplayMetrics();
				player.setVideoModeRatio(dm.widthPixels, dm.heightPixels);
			} else if(player.getVideoMode() == VideoMode.MODE_RATIO) {
				player.setVideoModeLayout();
			} else if(player.getVideoMode() == VideoMode.MODE_LAYOUT) {
				player.setVideoModeFixed(player.getVideoWidth() / 2, 
						player.getVideoHeight() / 2);
			} else if(player.getVideoMode() == VideoMode.MODE_FIXED) {
				player.setVideoModeVideo();
			}
			break;
		case R.id.play:
			if(player.getMediaPlayerState() == MediaPlayerState.STATE_PAUSED) {
				player.start();
			} else {
				try {
					// after surfaceCreated() called.
					boolean local = true;
					if(local) {
						player.play(VideoActivity.this, Environment.getExternalStorageDirectory().getPath() + "/123.mp4"); 
						player.setLooping(true);
					} else {
						//player.play(VideoActivity.this, "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
						player.play(VideoActivity.this, "https://dl.dropboxusercontent.com/s/4rnlizd7rfoech0/jingxun.m3u8");
					}
				} catch (Exception e) {
					android.widget.Toast.makeText(VideoActivity.this, "Play video failed.", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.pause:
			player.pause();
			break;
		case R.id.stop:
			player.stop();
			break;
		}
	}
	
}
