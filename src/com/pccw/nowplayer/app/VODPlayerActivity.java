package com.pccw.nowplayer.app;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pccw.nowplayer.res.ImageCachePath;
import com.pccw.nowplayer.utils.DatetimeUtils;
import com.pccw.nowplayer.utils.DialogUtils;
import com.pccw.nowplayer.utils.ImageCacheParams;
import com.pccw.nowplayer.utils.MediaPlayerWrapper;
import com.pccw.nowplayer.utils.MediaPlayerWrapper.MediaPlayerState;
import com.pccw.nowplayer.utils.MediaPlayerWrapper.OnSeekBarProgressUpdatedListener;
import com.pccw.nowplayer.utils.MediaPlayerWrapper.VideoMode;
import com.pccw.nowplayer.utils.RemoteSingleImageLoader;
import com.pccw.nowplayer.utils.TradImageCacheHelper;
import com.pccw.nowplayereyeapp.R;

/**
 * @author AlfredZhong
 * @version 1.0, 2011-6-9
 * @version 1.1, 2011-10-14, add resize function.
 * @version 1.2, 2011-10-20, add volume control and media button control function.
 */
public class VODPlayerActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "VODPlayerActivity";
	private SurfaceView surfaceView;
	private MediaPlayerWrapper player;
	// about bottom bar
	private boolean showTool = true;
	private FrameLayout layout;
	private int maxVolume, curVolume;
    private Timer toolBarTimer = new Timer(); // count time to hide the toolbar
	private TimerTask hideToolbarTimerTask;
	private Animation slideup, slidedown;
	private static final int HANDLER_ANIMATION_TIMEOUT = 4;
	private static final int POPUP_TOOL_AUTO_DISMISS_TIME = 5000;
	private int lastPos = 0; 
	private String checkoutUrl;
	private ArrayList<String> slateUrl;
	private String checkoutDisplayTitle = "";
	private ArrayList<String> combinePlaylist = new ArrayList<String>();
	private int playIndex = 0;
	private boolean isLive = false;
	private String channelLogoPath = null;
	
	private PowerManager.WakeLock mWakeLock;
	RemoteSingleImageLoader loader;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		checkoutUrl = getIntent().getExtras().getString("checkoutUrl");
		slateUrl = (ArrayList<String>) getIntent().getExtras().getSerializable("slateUrl");
		checkoutDisplayTitle = getIntent().getExtras().getString("checkoutDisplayTitle");
		channelLogoPath = getIntent().getExtras().getString("channelLogoPath");
		if (slateUrl!=null){
			for (String slate : slateUrl){
				Log.d("testing", "my testing slate = " + slate);
				combinePlaylist.add(slate);
			}
		}
		
		if (checkoutUrl !=null){
			Log.d("testing", "my testing v = " + checkoutUrl);
			combinePlaylist.add(checkoutUrl);
		}
		
		if (combinePlaylist.size()==1){
			isLive = true;
		}
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.vodplayer);
		initPlayer();
		
		
		ImageCacheParams params = new ImageCacheParams(this, ImageCachePath.PROGRAM_ALL);
		TradImageCacheHelper cacheHelper = new TradImageCacheHelper(params);
		loader = new RemoteSingleImageLoader(this, cacheHelper);
		loader.setLoadingImage(getResources(), R.drawable.empty_program_c);
		
		if (channelLogoPath!=null && !"".equals(channelLogoPath)){
			loader.setRemoteImage((ImageView) findViewById(R.id.vod_popupTool_logo), channelLogoPath);
		}
	}

	private void initPlayer(){
		surfaceView = (SurfaceView) findViewById(R.id.vod_video_surfaceview);
		player = new MediaPlayerWrapper();
		player.setScreenOnWhilePlaying(true);
		player.setSeekBar((SeekBar) findViewById(R.id.vod_popupTool_seekbar));
		player.setSurfaceView(surfaceView);
		
		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.d(TAG,"my testing onCompletion");
				Log.d(TAG,"onCompletion mp.getCurrentPosition :" + mp.getCurrentPosition());
				try {
					if (playIndex==combinePlaylist.size()-1){
						isLive = true;
					}
					
					if (playIndex<combinePlaylist.size()){
						player.reset();
						lastPos = 0;
						player.play(VODPlayerActivity.this, combinePlaylist.get(playIndex++));
					} else {
						DialogUtils.createMessageAlertDialog(VODPlayerActivity.this, null, 
							getString(R.string.alert_end_of_program_title), 
							getString(R.string.alert_end_of_program_message), 
							getString(R.string.ok), 
							null, null, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									finish();
									
								}
							}, true).show();
					}
					//player.play(VODPlayerActivity.this, "https://dl.dropboxusercontent.com/s/4rnlizd7rfoech0/jingxun.m3u8");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		player.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				player.start();
				findViewById(R.id.vod_player_btn_play).setVisibility(View.GONE);
				findViewById(R.id.vod_popupTool_play).setBackgroundResource(R.drawable.selector_playerbar_pause);
				DisplayMetrics dm = getResources().getDisplayMetrics();
				player.setVideoModeRatio(dm.widthPixels, dm.heightPixels);
				keepAwake();
				
			}
		});
		player.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
			@Override
			public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
				DisplayMetrics dm = getResources().getDisplayMetrics();
				player.setVideoModeRatio(dm.widthPixels, dm.heightPixels);
			}
		});
		
		
		player.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			@Override
			public void onSeekComplete(MediaPlayer mp) {
				player.start();
				keepAwake();
			}
		});
		
		player.setOnSeekBarProgressUpdatedListener(new OnSeekBarProgressUpdatedListener() {
			@Override
			public void onProgressUpdated(int currentPosition, int duration) {
				((TextView)findViewById(R.id.vod_popup_duration_now)).setText(DatetimeUtils.transMillionToTime(currentPosition));
				((TextView)findViewById(R.id.vod_popup_duration_total)).setText(" / " + DatetimeUtils.transMillionToTime(duration));
			}
		});
		
		try {
			player.play(this, combinePlaylist.get(playIndex++));
			//player.play(this, "https://dl.dropboxusercontent.com/s/lbyqx2zwumwgh7w/test_slate.m3u8");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		findViewById(R.id.vod_popupTool_play).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (player!=null){
					if (player.isPlaying()){
						if(player.canPause()){
							player.pause();
							findViewById(R.id.vod_player_btn_play).setVisibility(View.VISIBLE);
							findViewById(R.id.vod_popupTool_play).setBackgroundResource(R.drawable.selector_playerbar_play);
						}
					} else if (player.getMediaPlayerState() == MediaPlayerState.STATE_PAUSED) {
						player.start();
						findViewById(R.id.vod_player_btn_play).setVisibility(View.GONE);
						findViewById(R.id.vod_popupTool_play).setBackgroundResource(R.drawable.selector_playerbar_pause);
						keepAwake();
					}
				}
				
			}
		});

		findViewById(R.id.vod_player_btn_play).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.vod_player_btn_play).setVisibility(View.GONE);
				findViewById(R.id.vod_popupTool_play).setBackgroundResource(R.drawable.selector_playerbar_pause);
				player.start();
			}
		});
		
		
		findViewById(R.id.vod_popupTool_exit_fullscreen).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(player.getVideoMode() == VideoMode.MODE_LAYOUT) {
					DisplayMetrics dm = getResources().getDisplayMetrics();
					player.setVideoModeRatio(dm.widthPixels, dm.heightPixels);
					((Button) findViewById(R.id.vod_popupTool_exit_fullscreen)).setBackgroundResource(R.drawable.selector_playerbar_enterfullscreen);
					
					
				} else if(player.getVideoMode() == VideoMode.MODE_RATIO) {
					player.setVideoModeLayout();
					((Button) findViewById(R.id.vod_popupTool_exit_fullscreen)).setBackgroundResource(R.drawable.selector_playerbar_exitfullscreen);
				} 
			}
		});
		
		if (checkoutDisplayTitle!=null){
			((TextView) (findViewById(R.id.vod_popupTool_title))).setText(checkoutDisplayTitle);
		}
		
		
		addAnimationToSurfaceView();
	}
	
	private void addAnimationToSurfaceView() {
		findViewById(R.id.vod_video_surfaceview).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if (!isLive){
					return false;
				}
				
				if (findViewById(R.id.vod_popupTool).getVisibility() == View.VISIBLE) {
					dismissToolBar();
				}
				else {
					addToolbarAnim();
				}
				return false;
			}
		});
		
		findViewById(R.id.vod_popupTool).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
	}
	
	
	private void dismissToolBar(){
		if(findViewById(R.id.vod_popupTool).getVisibility() == View.VISIBLE){
			findViewById(R.id.vod_popupTool).setAnimation(slidedown);
			findViewById(R.id.vod_popupTool).setVisibility(View.GONE);
		}
	}
	
	private void addToolbarAnim() {
		slidedown = AnimationUtils.loadAnimation(this,
				R.anim.slide_down);
		if (findViewById(R.id.vod_popupTool).getVisibility() != View.VISIBLE) {
			slideup = AnimationUtils.loadAnimation(this,
					R.anim.slide_up);
			findViewById(R.id.vod_popupTool).setAnimation(slideup);
			findViewById(R.id.vod_popupTool).setVisibility(View.VISIBLE);
			delayToHideToolbar();
		}
	}
	
	private void delayToHideToolbar() {
		toolBarTimer.cancel();
		if (hideToolbarTimerTask != null)
			hideToolbarTimerTask.cancel();
		toolBarTimer = new Timer();
		hideToolbarTimerTask = new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(HANDLER_ANIMATION_TIMEOUT);
			}
		};
		toolBarTimer.schedule(hideToolbarTimerTask, POPUP_TOOL_AUTO_DISMISS_TIME);
	}
	
	final Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HANDLER_ANIMATION_TIMEOUT:
					View view = findViewById(R.id.vod_popupTool);
					if(view != null && view.getVisibility() == View.VISIBLE) {
						dismissToolBar();
					}
				break;
			}
		}
	};
	
	
	@Override
	protected void onResume() {
		Log.d("tseting", "my testing VODPLayerActivity onResume");
		if (player!=null){
			if (MediaPlayerWrapper.MediaPlayerState.STATE_PAUSED == player.getMediaPlayerState()){
				//player.start();
				player.seekTo(lastPos);
				Log.d(TAG, "my testing resume at " + lastPos);
				findViewById(R.id.vod_player_btn_play).setVisibility(View.GONE);
				findViewById(R.id.vod_popupTool_play).setBackgroundResource(R.drawable.selector_playerbar_pause);
			}
		}
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		if (player!=null && player.isPlaying()){
			lastPos = player.getCurrentPosition();
			player.pause();
			Log.d(TAG, "my testing paused at " + lastPos);
			findViewById(R.id.vod_player_btn_play).setVisibility(View.VISIBLE);
			findViewById(R.id.vod_popupTool_play).setBackgroundResource(R.drawable.selector_playerbar_play);
			releaseAwake();
		}
		super.onPause();
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d("tseting", "my testing VODPLayerActivity onDestroy");
		releaseAwake();
		player.reset();
		player.release();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	private void keepAwake(){
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag"); 
		mWakeLock.acquire();
	}
	
	private void releaseAwake(){
		try {
			mWakeLock.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
