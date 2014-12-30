package com.pccw.nowplayer.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pccw.nmal.appdata.B2BApiAppInfo;
import com.pccw.nmal.checkout.CheckoutFlowController;
import com.pccw.nmal.checkout.CheckoutFlowController.CheckoutUIEvent;
import com.pccw.nmal.checkout.LiveChannelCheckout;
import com.pccw.nmal.model.LiveCatalog;
import com.pccw.nmal.model.LiveCatalog.LiveCatalogChannelData;
import com.pccw.nmal.model.LiveDetail;
import com.pccw.nmal.model.LiveDetail.LiveDetailChannel;
import com.pccw.nmal.model.LiveDetail.LiveDetailData;
import com.pccw.nmal.model.StreamInfo;
import com.pccw.nmal.service.LongPlayPromptService;
import com.pccw.nmal.util.LanguageHelper;
import com.pccw.nowplayer.AppDataLoader;
import com.pccw.nowplayer.Constants;
import com.pccw.nowplayer.adapter.CustomSingleChoiceDialogItemAdapter;
import com.pccw.nowplayer.app.AppLocaleAide;
import com.pccw.nowplayer.app.BaseFragment;
import com.pccw.nowplayer.app.HandlerTimer;
import com.pccw.nowplayer.model.LiveChannelRow;
import com.pccw.nowplayer.model.LiveFullEPGData;
import com.pccw.nowplayer.res.ImageCachePath;
import com.pccw.nowplayer.utils.DatetimeUtils;
import com.pccw.nowplayer.utils.DialogUtils;
import com.pccw.nowplayer.utils.ImageCacheParams;
import com.pccw.nowplayer.utils.LayoutUtils;
import com.pccw.nowplayer.utils.MediaPlayerWrapper;
import com.pccw.nowplayer.utils.MediaPlayerWrapper.OnSeekBarProgressUpdatedListener;
import com.pccw.nowplayer.utils.NowplayerDialogUtils;
import com.pccw.nowplayer.utils.RemoteGroupImageLoader;
import com.pccw.nowplayer.utils.RemoteSingleImageLoader;
import com.pccw.nowplayer.utils.TradImageCacheHelper;
import com.pccw.nowplayer.utils.VideoUtils;
import com.pccw.nowplayereyeapp.NowPlayerApplication;
import com.pccw.nowplayereyeapp.R;

import custom.widget.HorizontalScrollBar;
import custom.widget.ObservableHorizontalScrollView;
import custom.widget.ScrollableHelper;
import custom.widget.adapterview.ObservableListView;

public class ChannelFragment extends BaseFragment implements ObservableHorizontalScrollView.OnScrollChangedListener, 
		ObservableListView.OnScrollChangedListener {

	private boolean FOR_GZ_TEST = false;
	public static final String TAG = ChannelFragment.class.getSimpleName();
	private View mLastTouchedListView;
	private List<LiveChannelRow> mLeftChannelData;
	private List<LiveFullEPGData> mFullEPGData;
	
	/** channel list */
	private ObservableListView current_program_table_rightScrollView = null;
	/** current channel program */
	private ObservableListView current_program_table_leftScrollView = null;
    private ObservableListView fullepgScrollView = null;
    
    private ObservableHorizontalScrollView live_fullepg_timelineScrollView = null;
    private ObservableHorizontalScrollView fullepgScrollViewHorizontal = null;
    
    private ImageView live_epg_anchor_left= null;
    private ImageView live_epg_anchor_right = null;
    private ImageView live_epg_anchor_now = null;
    
    Boolean isEPGOpened = false;
    float previsousX = -1;
    private static final int ONE_MINETE_DP = 8;
    private static final int TIMER_REFRESH_INTERVAL = 1000 * 60 * 5;
    private HandlerTimer timer = new HandlerTimer(true);
    
    private ScrollableHelper.ListViewScrollingSynchronizer listViewScrollingSynchronizer;
    
    private LeftChannelListAdapter leftAdapter;
    private RightEPGListAdapter rightAdapter;
    private FullEPGListAdapter fullAdapter;
    
    private ImageCacheParams params;
    private TradImageCacheHelper imageHelper;
    private RemoteGroupImageLoader imageLoader;
    private RemoteSingleImageLoader imageLoaderSingle;

    private boolean isFullScreenFlag = false;
    private MediaPlayerWrapper player;
    private Animation slideup, slidedown;
    private Timer toolBarTimer = new Timer(); // count time to hide the toolbar
	private TimerTask hideToolbarTimerTask;
	private static final int POPUP_TOOL_AUTO_DISMISS_TIME = 5000;
    private boolean hasBeenPlay = false;
    private SeekBar videoBar;
    private TextView durationNow, durationToatl;
    private boolean seekBallOnPress = false;
    private String checkoutUrl;
    private Dialog longPlayPromptDialog = null;
    private LongPlayPromptService lppService;
    //private boolean isQualityDialogShowing = false;
    
    private static final int HANDLER_PROGRESS_CHANGED  = 1;
    private static final int HANDLER_SHOW_QUALITY_DIALOG = 2;
    private static final int HANDLER_SHOW_MESSAGE_DIALOG = 3;
    private static final int HANDLER_ANIMATION_TIMEOUT = 4;
    
    private int smallScreenSurfaceviewWidth = 0;
    private int smallScreenSurfaceviewHeight = 0;
    
    private String mChannelId;
    private String mAudioCode; 
    private boolean mCheckImage;
    
    private PowerManager.WakeLock mWakeLock;
    
	//private AccountManagerHelper mAccountManagerHelper;
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
        params = new ImageCacheParams(activity, ImageCachePath.CHANNEL_LOGO);
        imageHelper = new TradImageCacheHelper(params);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	final B2BApiAppInfo appInfo = new B2BApiAppInfo(getActivity(), Constants.APP_INFO_URL, Constants.APP_INFO_APP_ID);
		appInfo.setDownloadConfigCallback(new B2BApiAppInfo.DownloadInfoCallback() {
			@Override
			public void onRegionChanged(String oldRegion, String newRegion) {
			}
			@Override
			public void onDownloadInfoSuccess() {
				android.util.Log.d(TAG, "B2BApiAppInfo onDownloadInfoSuccess");
				// download JSON zip.
				AppDataLoader epgDataLoader = new AppDataLoader(getApplicationContext());
				epgDataLoader.downloadJsonZip(new AppDataLoader.AppDataListener() {
					@Override
					public void onDataLoaded() {
						android.util.Log.d(TAG, "AppData onDataLoaded");
						// refresh data and UI.
						refreshUI();
					}
					@Override
					public void onJsonZipFailed() {
					}
				});
			}
			@Override
			public void onDownloadInfoFailed(String reason) {
				// splash task failed.
				android.util.Log.d(TAG, "AppInfo onDownloadInfoFailed : " + reason);
			}
		});
		appInfo.downloadInfo();
		
		initPlayer();
		
		
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(checkoutUrl != null){
					playCheckOutResult(false);
				}
				if(isFullScreenFlag){
					findViewById(R.id.live_player_full_screen).setVisibility(View.GONE);
				} else {
//					findViewById(R.id.live_player_full_screen).setVisibility(View.VISIBLE);
				}
			}
		}, 200);
    }
    
	@Override
	public void onPause() {
		super.onPause();
		if(player != null){
			player.stop();
//			player.reset();
		}
		stopLongPlayPrompt();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(player != null){
			player.release();
		}
	}
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	timer.cancelAll();
    	timer.cancelRepeatExecution();
    }
    
    @Override
    public void onDetach() {
    	super.onDetach();
    	timer.quit();
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return createOrRetrieveFragmentView(inflater, container, R.layout.live);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		timer.scheduleRepeatExecution(new Runnable() {
			@Override
			public void run() {
				// now anchor not move, timeline and epg move.
				// scrollToCurrentTime();
				// now anchor move, timeline and epg not move.
				updateNowAnchor();
				updateCurrentProgram(getSelectedByPosition());
				rightAdapter.notifyDataSetChanged(); // update current program ListView
			}
		}, 5000, TIMER_REFRESH_INTERVAL);
		
		if(!isFragmentRecreatedWithHoldView()) {
			loadData();
	        leftAdapter = new LeftChannelListAdapter(mLeftChannelData);
	        // use left channel data to get right epg data
	        rightAdapter = new RightEPGListAdapter(mLeftChannelData);
	        fullAdapter = new FullEPGListAdapter(mFullEPGData);
		}
//		initPlayer();
		
		if(isFragmentRecreatedWithHoldView())
			return;
		current_program_table_rightScrollView = (ObservableListView) findViewById(R.id.current_program_table_rightScrollView);
		if (current_program_table_rightScrollView != null) {
			current_program_table_rightScrollView.setTag("right");
			current_program_table_rightScrollView.setOnScrollChangedListener(this);	
		}
		
		current_program_table_leftScrollView = (ObservableListView) findViewById(R.id.current_program_table_leftScrollView);
		if (current_program_table_leftScrollView!=null) {
			current_program_table_leftScrollView.setTag("left");
			current_program_table_leftScrollView.setOnScrollChangedListener(this);	
		}
		
		imageLoader = new RemoteGroupImageLoader(imageHelper, current_program_table_leftScrollView);
		imageLoader.setLoadingImage(getResources(), R.drawable._channel_logo);
		imageLoaderSingle = new RemoteSingleImageLoader(getActivity(), imageHelper);
		imageLoaderSingle.setLoadingImage(getResources(), R.drawable._channel_logo);
		
		fullepgScrollView = (ObservableListView) findViewById(R.id.fullepgScrollView);
		if (fullepgScrollView !=null) {
			fullepgScrollView.setTag("full");
			fullepgScrollView.setOnScrollChangedListener(this);	
		}
        
        live_fullepg_timelineScrollView = (ObservableHorizontalScrollView)findViewById(R.id.live_fullepg_timelineScrollView);
        if (live_fullepg_timelineScrollView != null) {
        	live_fullepg_timelineScrollView.setOnScrollChangedListener(this);	
		}
        
        fullepgScrollViewHorizontal = (ObservableHorizontalScrollView)findViewById(R.id.fullepgScrollView_horizontal);
        if (fullepgScrollViewHorizontal != null) {
        	fullepgScrollViewHorizontal.setOnScrollChangedListener(this);	
		}
        
        live_epg_anchor_left = (ImageView)findViewById(R.id.live_anchor_left);
        live_epg_anchor_right = (ImageView)findViewById(R.id.live_anchor_right);
        live_epg_anchor_now = (ImageView)findViewById(R.id.live_anchor_now);
        
        final View.OnTouchListener lastOnTouchListViewListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//System.out.println("view tag " + v.getTag());
				mLastTouchedListView = v;
				return false;
			}
		}; 
		current_program_table_leftScrollView.setOnTouchListener(lastOnTouchListViewListener);
		current_program_table_rightScrollView.setOnTouchListener(lastOnTouchListViewListener);
		fullepgScrollView.setOnTouchListener(lastOnTouchListViewListener);
        
        //loadData();
//        leftAdapter = new LeftChannelListAdapter(mLeftChannelData);
//        // use left channel data to get right epg data
//        rightAdapter = new RightEPGListAdapter(mLeftChannelData);
//        fullAdapter = new FullEPGListAdapter(mFullEPGData);
        current_program_table_leftScrollView.setAdapter(leftAdapter);
        current_program_table_rightScrollView.setAdapter(rightAdapter);
        fullepgScrollView.setAdapter(fullAdapter);
        
        LayoutUtils.getViewLayoutInfoBeforeOnResume(getView(), new LayoutUtils.OnViewGlobalLayoutListener() {
			@Override
			public void onViewGlobalLayout(View view) {
				
				// if not post runnable, listViews will flash() "Live EPG is closing" effect
				view.post(new Runnable() {
					
					@Override
					public void run() {
						onViewGlobalLayoutReady();
						if(leftAdapter.getCount() >= 2) {
							setSelected(1);
							updateCurrentProgram(1); // default show the first channel current program
						}
					}
				});
				// delays creating the timeline bar
				view.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						int fullWidth = getView().getWidth();
						// HorizontalScrollBar hsb = 
						new HorizontalScrollBar.Builder(fullWidth, provider)
							.setCenterTwoEdges(false)
							.setDivider(null, 0)
							.setDataList(Arrays.asList(timeline))
							.setItemLayoutResId(R.layout.live_timeline_item)
							.create((HorizontalScrollView)findViewById(R.id.live_fullepg_timelineScrollView));
					
					}
				}, 300);
			}
		});
        
        listViewScrollingSynchronizer = new ScrollableHelper.ListViewScrollingSynchronizer(fullepgScrollView, 
        		current_program_table_leftScrollView, current_program_table_rightScrollView);
        
        final AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(view instanceof TextView) 
					return;
				
				ViewHolder holder = null;
				
				int childPos = 0;
				int childCount = parent.getChildCount();
				for (int i = 0; i < childCount; i++) {
					View v = parent.getChildAt(i);
					if(v instanceof TextView)
						continue;
					if(v == view) {
						childPos = i;
					} else {
						fullepgScrollView.getChildAt(i).setBackgroundColor(getResources().getColor(android.R.color.transparent));
						current_program_table_leftScrollView.getChildAt(i).setBackgroundColor(getResources().getColor(android.R.color.transparent));
						current_program_table_rightScrollView.getChildAt(i).setBackgroundColor(getResources().getColor(android.R.color.transparent));
						holder = (ViewHolder)(current_program_table_rightScrollView.getChildAt(i).getTag());
						ImageView img = holder.isSelected;
						img.setBackgroundResource(R.drawable.live_epg_playarrow);					
						TextView time = holder.timeLine;
						time.setTextColor(getResources().getColor(R.color.live_text_white));
					}
				}
				fullepgScrollView.getChildAt(childPos).setBackgroundColor(getResources().getColor(R.color.live_currentprogram_selected));
				current_program_table_leftScrollView.getChildAt(childPos).setBackgroundColor(getResources().getColor(R.color.live_currentprogram_selected));
				current_program_table_rightScrollView.getChildAt(childPos).setBackgroundColor(getResources().getColor(R.color.live_currentprogram_selected));
				holder = (ViewHolder)(current_program_table_rightScrollView.getChildAt(childPos).getTag());
				ImageView img = holder.isSelected;
				img.setBackgroundResource(R.drawable.live_epg_playarrow_selected);
				TextView time = holder.timeLine;
				time.setTextColor(getResources().getColor(R.color.live_text_highlighted));
				
				setSelected(position);
				updateCurrentProgram(position); // update current program info (bottom right)
				stopLongPlayPrompt();
				playVideo();
			}
		};
        
//		fullepgScrollView.setOnItemClickListener(onItemClick); // full epg do NOT need onItemClick
		current_program_table_leftScrollView.setOnItemClickListener(onItemClick);
		current_program_table_rightScrollView.setOnItemClickListener(onItemClick);
		
		View.OnClickListener nowAnchorClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scrollToCurrentTime();
			}
		};
	    live_epg_anchor_left.setOnClickListener(nowAnchorClickListener);
	    live_epg_anchor_right.setOnClickListener(nowAnchorClickListener);
	    
		/*mAccountManagerHelper = new AccountManagerHelper(getActivity(), 
				Constants.EYEAPP_ACCOUNT_TYPE, Constants.EYEAPP_TOKEN_TYPE, this) {
					@Override
					public void onAccountEmpty() {
						//loginAccount(getActivity());
					}
					@Override
					public void onLoginSuccess(String type, String name) {
						dismissProgressDialog(); // remove loading after user successful login
//						showProgressDialog(true);
//						setProgressDialogCancelTag(getSelectedByPosition());
//						checkEyeIDAndCheckoutPlaylist(mChannelId, mChannelId, mCheckImage);
//						findViewById(R.id.live_video_surfaceview).setVisibility(View.INVISIBLE);
//						findViewById(R.id.live_video_surfaceview).setVisibility(View.VISIBLE);
					}
					@Override
					public void onAuthToken(String token) {
						if(token == null || token.equals("")) {
							checkEyeIDFailed(mChannelId, mAudioCode, mCheckImage, getString(R.string.error_general_error));
						} else {
							// get token and checkout playlist.
							continueCheckout(mChannelId, mAudioCode, token, mCheckImage);
						}
					}
					@Override
					public void onException(Exception e) {
						dismissProgressDialog(); // remove loading after user cancel login
					}
		};*/
	    
	    // load video preview image
	    String channelId = mLeftChannelData.get(getSelectedByPosition()).getData().getId();
	    Log.d(TAG, "VideoUtils channelId = " + channelId);
		//checkEyeIDAndCheckoutPlaylist(channelId, null, true);

	}
	
	private void refreshUI() {
		// reload data.
		loadData();
		Log.w(TAG, "genre 1 = " + mLeftChannelData.get(0).getChannelSectionName());
		// refresh ListView
		leftAdapter.setData(mLeftChannelData);
		rightAdapter.setData(mLeftChannelData);
		fullAdapter.setData(mFullEPGData);
		// refresh current program.
		updateCurrentProgram(getSelectedByPosition());
	}
	
	@Override
	public void onLocaleChanged() {
		super.onLocaleChanged();
		Log.i(TAG, "onLocaleChanged() " + LanguageHelper.getCurrentLanguage());
		refreshUI();
		// refresh text and image
		todayText.setText(getDateStr(DatetimeUtils.formatDate(new Date(), "yyyyMMdd"), AppLocaleAide.isAppLocaleZh(getActivity())));
		tomorrowText.setText(getDateStr(DatetimeUtils.formatDate(DatetimeUtils.moveToDate(Calendar.DATE, 1), "yyyyMMdd"), AppLocaleAide.isAppLocaleZh(getActivity())));
		((ImageView)findViewById(R.id.epg_slider)).setImageDrawable(getResources().getDrawable(R.drawable.live_epg_slider));
	}

	private String[] timeline = new String[]{
			"00:00 AM", "00:30 AM", "01:00 AM", "01:30 AM", "02:00 AM", "02:30 AM",
			"03:00 AM", "03:30 AM", "04:00 AM", "04:30 AM", "05:00 AM", "05:30 AM",
			"06:00 AM", "06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
			"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
			
			"12:00 PM", "12:30 PM", "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM",
			"03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM",
			"06:00 PM", "06:30 PM", "07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM",
			"09:00 PM", "09:30 PM", "10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM",
			
			"00:00 AM", "00:30 AM", "01:00 AM", "01:30 AM", "02:00 AM", "02:30 AM",
			"03:00 AM", "03:30 AM", "04:00 AM", "04:30 AM", "05:00 AM", "05:30 AM",
			"06:00 AM", "06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
			"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
			
			"12:00 PM", "12:30 PM", "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM",
			"03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM",
			"06:00 PM", "06:30 PM", "07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM",
			"09:00 PM", "09:30 PM", "10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM",
	}; 
	
	private TextView todayText, tomorrowText;
	
	HorizontalScrollBar.ScrollBarItemInfoProvider provider = new HorizontalScrollBar.ScrollBarItemInfoProvider() {

		private String today = DatetimeUtils.formatDate(new Date(), "yyyyMMdd");
		private String tomorrow = DatetimeUtils.formatDate(DatetimeUtils.moveToDate(Calendar.DATE, 1), "yyyyMMdd");
		
		@Override
		public int getItemWidth(View convertView) {
			return (int) getResources().getDimension(R.dimen.live_fullepg_timeline_30min_width);
		}

		@Override
		public void setItemContent(HorizontalScrollBar builder,
				View convertView, Object value, int position) {
			TextView date = (TextView) convertView.findViewById(R.id.live_timeline_date);
			TextView time = (TextView) convertView.findViewById(R.id.live_timeline_time);
			time.setText((String) value);
			if(position == 0) {
				date.setText(getDateStr(today, AppLocaleAide.isAppLocaleZh(getActivity())));
				todayText = date;
			} else if(position == 48) {
				date.setText(getDateStr(tomorrow, AppLocaleAide.isAppLocaleZh(getActivity())));
				tomorrowText = date;
			}
		}
		
	};
	
    /**
     * Note: Cost tens of ms.
     * @param yyyyMMdd
     * @param isChinese
     * @return 8月7日(週二) or 7 Aug(Fri)
     */
    public String getDateStr(String yyyyMMdd, boolean isChinese) {
        Date date = DatetimeUtils.parseDateString(yyyyMMdd, "yyyyMMdd");
        String retval = null;
        if(isChinese) {
        	retval = DatetimeUtils.formatDate(date, "M月d日 (週几)", Locale.TRADITIONAL_CHINESE);
            return retval.replace('几', DatetimeUtils.getChineseDayInWeek(date));
        }
        return DatetimeUtils.formatDate(date, "d MMM(E)", Locale.US);
    }
	
	private void loadData() {
		//added
		Log.w(TAG, "loadData()");
		LinkedHashMap<String, LiveDetailChannel> liveDetailChannelList = LiveDetail.getInstance().getLiveDetailChannelList();
		LinkedHashMap<String, LiveCatalogChannelData> liveChannelGenreList = LiveCatalog.getInstance().getLiveChannelGenreList();
		
		if (LiveCatalog.getInstance().isLiveChannelListLoaded()) {
			LiveChannelRow row;
			ArrayList<LiveChannelRow> rows = new ArrayList<LiveChannelRow>(); 
			
			LiveFullEPGData epgRow;
			ArrayList<LiveFullEPGData> epgRows = new ArrayList<LiveFullEPGData>(); 
			
			for(LiveCatalogChannelData liveChannelGenre : liveChannelGenreList.values()){
				Log.d(TAG,"my test liveChannelGenre.getName() : " + liveChannelGenre.getName());
				
				if (liveChannelGenre.childNodes != null){
					row = new LiveChannelRow();
					row.setChannelSectionName(liveChannelGenre.getName());
					rows.add(row);
					
					epgRow = new LiveFullEPGData();
					epgRows.add(epgRow);
					
					for (LiveCatalogChannelData liveCatalogChannelData : liveChannelGenre.childNodes.values()){
						if(!liveCatalogChannelData.getIsLive()){
							continue;
						}
						Log.d(TAG,"my test data.getName() : " + liveCatalogChannelData.getName() + "--id=" +liveCatalogChannelData.getId());
						Log.d(TAG,"my test data.getChannelLogoLink() : " + liveCatalogChannelData.getChannelLogoLink());
						Log.d(TAG,"my test data.getAudioList().size() : " + liveCatalogChannelData.getAudioList().size());
						
						row = new LiveChannelRow();
						row.setData(liveCatalogChannelData);
						rows.add(row);
						
						epgRow = new LiveFullEPGData();
						if (liveDetailChannelList.get(liveCatalogChannelData.getNodeId())!=null){
							epgRow.setFullEPG(liveDetailChannelList.get(liveCatalogChannelData.getNodeId()).programs);
						}
						epgRow.setChannel(liveCatalogChannelData);
						epgRows.add(epgRow);
						
						Log.d(TAG,"my test  liveDetailChannelList.get(id): " + liveDetailChannelList.get(liveCatalogChannelData.getId()));
						
					}
				}
				
			}
			mLeftChannelData = rows;
			mFullEPGData = epgRows;
			row = null;
			epgRows = null;
		}
		//end
	}
	
	@SuppressWarnings("deprecation")
	private int getCurrentTimeMinutes() {
		//240dp = 30mins
		Date date = new Date();
		return Math.round(ONE_MINETE_DP * (date.getHours() * 60 + date.getMinutes()));
	}
	
	private int scrollToCurrentTime() {
		int current_time = getCurrentTimeMinutes();
		// why 60, not half of anchor???
		//final int current_time_margin = Math.round(getResources().getDimension(R.dimen.live_fullepg_timeline_now_anchor_left_margin)/getResources().getDisplayMetrics().density);
		final int current_time_margin =  live_epg_anchor_now.getMeasuredWidth()/2;
		//Set scroll to current time - current_time_margin
		live_fullepg_timelineScrollView.scrollTo( Math.round(getResources().getDisplayMetrics().density*(current_time - current_time_margin)), 0);
		return current_time;
	}
	
	 public void onViewGlobalLayoutReady() {
	  
	  //Adjust the current program table left, right and fullepg scrollview size
	  RelativeLayout current_program_table_right = (RelativeLayout)findViewById(R.id.current_program_table_right);
	  RelativeLayout current_program_table_left = (RelativeLayout)findViewById(R.id.current_program_table_left);
	  RelativeLayout fullepgcanvas = (RelativeLayout)findViewById(R.id.full_epg_canvas);
	  LinearLayout live_player_panel = (LinearLayout)findViewById(R.id.live_player_panel);
	  ImageView epg_slider = (ImageView)findViewById(R.id.epg_slider);
	  
	  if (current_program_table_right != null && live_player_panel != null && current_program_table_left != null && fullepgcanvas != null && epg_slider != null) {
		  
		  //Get the parent content canvas size
		  RelativeLayout parent = (RelativeLayout)current_program_table_right.getParent();
		  
		  //Calculate the current program table left, right size
		  int total_width = parent.getMeasuredWidth();
		  int playerpanel_width = live_player_panel.getMeasuredWidth();
		  int current_program_table_right_width = (total_width - playerpanel_width)/2;
		  int current_program_table_left_width = total_width - playerpanel_width - current_program_table_right_width;
		  
		  Log.d("nowplayereye3","total_width = "+ total_width);
		  Log.d("nowplayereye3","playerpanel_width = "+ playerpanel_width);
		  Log.d("nowplayereye3","current_program_table_right_width = "+ current_program_table_right_width);
			
		  //Adjust corresponding layout params
		  RelativeLayout.LayoutParams lp_right = (RelativeLayout.LayoutParams) current_program_table_right.getLayoutParams();
		  lp_right.width = current_program_table_right_width;
		  current_program_table_right.setLayoutParams(lp_right);
		  current_program_table_right.setVisibility(View.VISIBLE);
			
		  RelativeLayout.LayoutParams lp_left = (RelativeLayout.LayoutParams) current_program_table_left.getLayoutParams();
		  lp_left.width = current_program_table_left_width;
		  current_program_table_left.setLayoutParams(lp_left);
		  current_program_table_left.setVisibility(View.VISIBLE);
		  
		  
		  RelativeLayout.LayoutParams lp_fullepg =  (RelativeLayout.LayoutParams) fullepgcanvas.getLayoutParams();
		  lp_fullepg.leftMargin = current_program_table_left_width;
		  fullepgcanvas.setVisibility(View.VISIBLE);
		  
		  if (this.live_fullepg_timelineScrollView != null) {
			  this.live_fullepg_timelineScrollView.setPadding(current_program_table_left_width, 0, 0, 0);
		  }
		  
		  
		  //Setup the epg slider touch handling
		  epg_slider.setOnTouchListener(new OnTouchListener() {
			
			@SuppressWarnings("unused")
			@Override
			public boolean onTouch(View v, MotionEvent motionEvent) {
				
				int init_slider_margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
				int break_slider_margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()));
				
				ImageView tmp_epg_slider = (ImageView)findViewById(R.id.epg_slider);
				int slider_width = tmp_epg_slider.getMeasuredWidth();
				android.widget.RelativeLayout.LayoutParams epg_slider_lp = (android.widget.RelativeLayout.LayoutParams) tmp_epg_slider.getLayoutParams();
		    	
				
				RelativeLayout current_program_table_right = (RelativeLayout)findViewById(R.id.current_program_table_right);
				RelativeLayout.LayoutParams lp_right = (RelativeLayout.LayoutParams) current_program_table_right.getLayoutParams();
				
				
				RelativeLayout epg_slider_group = (RelativeLayout)findViewById(R.id.epg_slider_group);
		    	RelativeLayout.LayoutParams epg_slider_group_lp = (RelativeLayout.LayoutParams) epg_slider_group.getLayoutParams();

				
				 switch (motionEvent.getAction()) {
			        case MotionEvent.ACTION_DOWN:

			        	//If not yet open full EPG, reset the current epg time
			        	if (!isEPGOpened) {
			        		scrollToCurrentTime();
						}
			        	
			        	//Reset the previous X
			        	previsousX = -1;
			        	//Log.d("nowplayereye3","onTouch ACTION_DOWN");
			            break;

			        case MotionEvent.ACTION_MOVE:

			        	if (!isEPGOpened) {
							
							//Calculate the dx
							float deltaX = 0;
							float currentX = motionEvent.getRawX();
							if (previsousX >= 0) {
								deltaX = currentX - previsousX;
							}
							//Log.d("nowplayereye3", "currentX ="+ currentX+ " previsousX = " +previsousX+ " deltaX = "+ deltaX);
							previsousX = currentX;
							

							//Calculate the new slider margin
							int new_slider_margin = epg_slider_lp.rightMargin - Math.round(deltaX);
							
							//Moving to left of the slider
							if (new_slider_margin > 0-init_slider_margin) {
								
								//Limit slider to init margin position
								new_slider_margin = 0-init_slider_margin;
								
								//Adjust the slider margin
						    	epg_slider_lp.rightMargin = new_slider_margin;
						    	tmp_epg_slider.setLayoutParams(epg_slider_lp);	
						    	
						    	//Adjust the Right Table Margin, if it has opened
						    	if (lp_right.rightMargin<0) {
						    		lp_right.rightMargin = lp_right.rightMargin - Math.round(deltaX);
						    		if (lp_right.rightMargin>0) {
										lp_right.rightMargin = 0;
									}
							    	current_program_table_right.setLayoutParams(lp_right);
								}
						    	
						    	//Adjust the epg_slider_group Margin, if it has opened
						    	//????
						    	if (epg_slider_group_lp.rightMargin<0) {
						    		epg_slider_group_lp.rightMargin = epg_slider_group_lp.rightMargin - Math.round(deltaX);
						    		if (epg_slider_group_lp.rightMargin>0) {
						    			epg_slider_group_lp.rightMargin = 0;
									}
							    	epg_slider_group.setLayoutParams(epg_slider_group_lp);
								}
						    	
							}
							
							//Moving to the right of the slider
							if (new_slider_margin < 0-break_slider_margin) {

								//Moving outside the breaking
								
								//Max new slider margin is 
								new_slider_margin = 0-break_slider_margin;

								//Adjust the slider margin
						    	epg_slider_lp.rightMargin = new_slider_margin;
						    	tmp_epg_slider.setLayoutParams(epg_slider_lp);
						    	
						    	
						    	//Adjust the Right Table Margin 
						    	lp_right.rightMargin = lp_right.rightMargin - Math.round(deltaX);
						    	current_program_table_right.setLayoutParams(lp_right);
						    	
						    	//Adjust the epg_slider_group Margin
						    	//????
						    	epg_slider_group_lp.rightMargin = epg_slider_group_lp.rightMargin - Math.round(deltaX);
						    	epg_slider_group.setLayoutParams(epg_slider_group_lp);
						    	
							}else{
								
								//Moving within the breaking limit
								
								//Adjust the slider margin
						    	epg_slider_lp.rightMargin = new_slider_margin;
						    	tmp_epg_slider.setLayoutParams(epg_slider_lp);	
							}
							
					    	//Redraw View
//					    	View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
							View rootView = getView();
							rootView.invalidate();
							
						}
			        	
			        	
			            
			            
			            break;
			        case MotionEvent.ACTION_UP:
			        	
			        	//Determine the total touched time
			        	long downtime = motionEvent.getDownTime();
			        	long eventtime = motionEvent.getEventTime();
			        	
			        	//Total touched duration within 180ms, will recongized as single tap
			        	if (eventtime - downtime <= 180) {
							if (!isEPGOpened) {
								//Open EPG
								OpenEPG();
							}else{
								//Close EPG
								CloseEPG();
							}
						}else{
							if (!isEPGOpened) {
				        		
				        		//Determine open or close EPG
				        		int openorclose = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.live_fullepg_timeline_openorclose_determine), getResources().getDisplayMetrics()));
				        		
				        		if (epg_slider_group_lp.rightMargin < openorclose) {
									//open EPG
				        			OpenEPG();
								}else{
									//close EPG
									CloseEPG();
								}
				        		
							}else{
								//close EPG
								CloseEPG();
							}
						}
			        	
			        	//Reset the previous X
			        	previsousX = -1;
			        	
			        	break;
			        
			    }
				return false;
			}
		});
		  
	  }
	  
	  FrameLayout playerCanvas = (FrameLayout) findViewById(R.id.player_canvas);
	  playerCanvas.getLayoutParams().height = (int) (getResources().getDisplayMetrics().density*350);
	  
	  if(smallScreenSurfaceviewWidth == 0 && smallScreenSurfaceviewHeight == 0){
		  smallScreenSurfaceviewWidth = findViewById(R.id.live_video_surfaceview).getWidth();
		  smallScreenSurfaceviewHeight = findViewById(R.id.live_video_surfaceview).getHeight();
	  }
	  
	}
	
	
	 
	@Override
	public void onScrollChanged(ObservableListView lv, int x, int y, int oldx,
			int oldy) {
		listViewScrollingSynchronizer.syncListViewScrolling(mLastTouchedListView, lv);
	}
	
	public void onScrollChanged(ObservableHorizontalScrollView scrollView, int x, int y, int oldx, int oldy) {
		ScrollableHelper.syncHorizontalScrollViewScrolling(scrollView, live_fullepg_timelineScrollView, fullepgScrollViewHorizontal);
		updateNowAnchor();
    }
	
	private void updateNowAnchor() {
		//Get require size
		RelativeLayout current_program_table_left = (RelativeLayout)findViewById(R.id.current_program_table_left);
		RelativeLayout live_canvas = (RelativeLayout)findViewById(R.id.live_canvas);
		
		int live_canvas_width = live_canvas.getMeasuredWidth();
		int TableLeftWidth = current_program_table_left.getMeasuredWidth();
		int now_anchor_width = live_epg_anchor_now.getMeasuredWidth();
		int current_x_offset = Math.round(live_fullepg_timelineScrollView.getScrollX()/getResources().getDisplayMetrics().density);
		
		final int current_time = getCurrentTimeMinutes();
		
		//float transX = (TableLeftWidth -current_x_offset + current_time - (now_anchor_width/2))*getResources().getDisplayMetrics().density;
		
		float transX = TableLeftWidth - (now_anchor_width/2) + (-current_x_offset + current_time )*getResources().getDisplayMetrics().density;
		
		//Set the translate X
		live_epg_anchor_now.setTranslationX(transX);
		
		
		//Set the Left Anchor
		if (transX < -now_anchor_width && this.isEPGOpened) {
			this.live_epg_anchor_left.setVisibility(View.VISIBLE);
		}else{
			this.live_epg_anchor_left.setVisibility(View.INVISIBLE);
		}
		
		//Set the Right Anchor
		if (transX>live_canvas_width  && this.isEPGOpened) {
			this.live_epg_anchor_right.setVisibility(View.VISIBLE);
		} else {
			this.live_epg_anchor_right.setVisibility(View.INVISIBLE);
		}
		
	}
	
	
	public void TappedAnchor(View v){
		//Hide all anchors
		this.live_epg_anchor_left.setVisibility(View.INVISIBLE);
		this.live_epg_anchor_right.setVisibility(View.INVISIBLE);
		
		scrollToCurrentTime();
	}
	

	public void OpenEPG(){
		
		/*******************************************************/
		//Open the EPG from current point to final point
		/*******************************************************/
		
		long AnimationDuration = 500;
		
		final RelativeLayout current_program_table_right = (RelativeLayout)findViewById(R.id.current_program_table_right);
		final RelativeLayout.LayoutParams lp_right = (RelativeLayout.LayoutParams) current_program_table_right.getLayoutParams();
		final int right_panel_width = current_program_table_right.getMeasuredWidth();

		
		final RelativeLayout current_program_table_left = (RelativeLayout)findViewById(R.id.current_program_table_left);
		
		
		final LinearLayout live_player_panel = (LinearLayout)findViewById(R.id.live_player_panel);
		final int liveplayerwidth = live_player_panel.getMeasuredWidth();

		final RelativeLayout epg_slider_group = (RelativeLayout)findViewById(R.id.epg_slider_group);
		final RelativeLayout.LayoutParams epg_slider_group_lp = (RelativeLayout.LayoutParams) epg_slider_group.getLayoutParams();

		
		
    	final ImageView epg_slider = (ImageView)findViewById(R.id.epg_slider);    	
    	final android.widget.RelativeLayout.LayoutParams epg_slider_lp = (android.widget.RelativeLayout.LayoutParams) epg_slider.getLayoutParams();

		
		
		
		/*******************************************************/
    	final int current_time = scrollToCurrentTime();
	
		/*******************************************************/
		
		final int table_right_newMargin = 0-liveplayerwidth-right_panel_width;
		final int table_right_currentMargin = lp_right.rightMargin;
		final int table_right_DeltaMargin = table_right_newMargin-table_right_currentMargin;
		
		Animation openanim = new Animation() {
			
		    @Override
		    protected void applyTransformation(float interpolatedTime, Transformation t) {
		    	
				lp_right.rightMargin = (int)(table_right_DeltaMargin * interpolatedTime) + table_right_currentMargin;
				current_program_table_right.setLayoutParams(lp_right);
		    }
		};
		
		openanim.setAnimationListener(new Animation.AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				if (isEPGOpened) {
					//Update the now anchor position
					/*
					//Get require size
					int TableLeftWidth = current_program_table_left.getMeasuredWidth();
					int now_anchor_width = live_epg_anchor_now.getMeasuredWidth();
					int current_x_offset = live_fullepg_timelineScrollView.getScrollX();
					
					//Set the translate X
					live_epg_anchor_now.setTranslationX(TableLeftWidth -current_x_offset + current_time - (now_anchor_width/2));
					*/
					//Unhide the anchor
					live_epg_anchor_now.setVisibility(View.VISIBLE);
					
					updateNowAnchor();
				}
				
				
			}
		});
		
		//Calculate the animation duration
		long openanimdeltaduration  =  Math.round(Math.abs((float)AnimationDuration * ((float)table_right_DeltaMargin / (float)table_right_newMargin)));
		
		openanim.setDuration(openanimdeltaduration);
		openanim.setRepeatMode(0);

		current_program_table_right.clearAnimation();
		current_program_table_right.startAnimation(openanim);
		
		/*******************************************************/
		
		
		// ??? padding
		//final int epg_slider_group_paddingright = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.live_fullepg_timeline_slider_group_epg_open_rightmargin), getResources().getDisplayMetrics()));
		//final int epg_slider_group_paddingright = Math.round(getResources().getDimension(R.dimen.live_fullepg_timeline_slider_group_epg_open_rightmargin)/getResources().getDisplayMetrics().density);
		final int epg_slider_group_paddingright = epg_slider.getMeasuredWidth()/5;
		final int epg_slider_group_newMargin = 0-liveplayerwidth-epg_slider_group_paddingright;
		final int epg_slider_group_currentMargin = epg_slider_group_lp.rightMargin;
		final int epg_slider_group_deltaMargin = epg_slider_group_newMargin - epg_slider_group_currentMargin;

		Animation epg_slider_group_openanim = new Animation() {
			
		    @Override
		    protected void applyTransformation(float interpolatedTime, Transformation t) {
		    	
		    	epg_slider_group_lp.rightMargin = (int)(epg_slider_group_deltaMargin * interpolatedTime) + epg_slider_group_currentMargin;
		    	epg_slider_group.setLayoutParams(epg_slider_group_lp);
		    }
		};
		
		//Calculate the animation duration
		long epg_slider_group_deltaduration  =  Math.round(Math.abs((float)AnimationDuration * ((float)epg_slider_group_deltaMargin / (float)epg_slider_group_newMargin)));
		
		epg_slider_group_openanim.setDuration(epg_slider_group_deltaduration);
		epg_slider_group_openanim.setRepeatMode(0);
		
		epg_slider_group.clearAnimation();
		epg_slider_group.startAnimation(epg_slider_group_openanim);
		
		/*******************************************************/
		
		//final int epg_slider_newMargin =  Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.live_fullepg_timeline_slider_rightMargin_open), getResources().getDisplayMetrics()));  
		//final int epg_slider_initMargin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.live_fullepg_timeline_slider_rightMargin), getResources().getDisplayMetrics()));
		//final int epg_slider_newMargin =  Math.round(getResources().getDimension(R.dimen.live_fullepg_timeline_slider_rightMargin_open)/getResources().getDisplayMetrics().density);  
		final int epg_slider_newMargin =  Math.round(epg_slider.getMeasuredWidth()*0.58f);
		//final int epg_slider_initMargin = Math.round(getResources().getDimension(R.dimen.live_fullepg_timeline_slider_rightMargin)/getResources().getDisplayMetrics().density);
		final int epg_slider_currentMargin = epg_slider_lp.rightMargin;
		final int epg_slider_deltaMargin = - epg_slider_newMargin - epg_slider_currentMargin;

		Animation epg_slider_openanim = new Animation() {
			
		    @Override
		    protected void applyTransformation(float interpolatedTime, Transformation t) {
		    	epg_slider_lp.rightMargin = (int)((epg_slider_deltaMargin) * interpolatedTime) + epg_slider_currentMargin;
		    	epg_slider.setLayoutParams(epg_slider_lp);
		    }
		};
		
		//Calculate the animation duration
		long epg_slider_deltaduration  =  Math.round(Math.abs((float)AnimationDuration * ((float)epg_slider_deltaMargin / (float)(epg_slider_newMargin))));

		
		epg_slider_openanim.setDuration(epg_slider_deltaduration);
		epg_slider_openanim.setRepeatMode(0);
		
		epg_slider.clearAnimation();
		epg_slider.startAnimation(epg_slider_openanim);
		
		
		/*******************************************************/

		
		if(this.live_fullepg_timelineScrollView!=null){
			AlphaAnimation fadeintimeline = new AlphaAnimation(0.0f, 1.0f);
			
			fadeintimeline.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					live_fullepg_timelineScrollView.setAlpha(1);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					live_fullepg_timelineScrollView.setAlpha(1);
				}
			});
			
			fadeintimeline.setDuration(AnimationDuration);
			fadeintimeline.setRepeatMode(0);
			
			this.live_fullepg_timelineScrollView.clearAnimation();
			this.live_fullepg_timelineScrollView.startAnimation(fadeintimeline);
		}

		
		/*******************************************************/

		this.isEPGOpened = true;
	}
	
	public void CloseEPG(){
		
		/*******************************************************/
		//Close the EPG from current point to final point
		/*******************************************************/
		
		long AnimationDuration = 500;
		
		final RelativeLayout current_program_table_right = (RelativeLayout)findViewById(R.id.current_program_table_right);
		final RelativeLayout.LayoutParams lp_right = (RelativeLayout.LayoutParams) current_program_table_right.getLayoutParams();
		final int right_panel_width = current_program_table_right.getMeasuredWidth();		
		
		final LinearLayout live_player_panel = (LinearLayout)findViewById(R.id.live_player_panel);
		final int liveplayerwidth = live_player_panel.getMeasuredWidth();

		final RelativeLayout epg_slider_group = (RelativeLayout)findViewById(R.id.epg_slider_group);
		final RelativeLayout.LayoutParams epg_slider_group_lp = (RelativeLayout.LayoutParams) epg_slider_group.getLayoutParams();

		
		
    	final ImageView epg_slider = (ImageView)findViewById(R.id.epg_slider);    	
    	final android.widget.RelativeLayout.LayoutParams epg_slider_lp = (android.widget.RelativeLayout.LayoutParams) epg_slider.getLayoutParams();

		/*******************************************************/
    	//Hide all anchors
		this.live_epg_anchor_left.setVisibility(View.INVISIBLE);
		this.live_epg_anchor_right.setVisibility(View.INVISIBLE);
		this.live_epg_anchor_now.setVisibility(View.INVISIBLE);
	
		/*******************************************************/
		
		final int table_right_newMargin = 0-liveplayerwidth-right_panel_width;
		final int table_right_currentMargin = lp_right.rightMargin;
		final int table_right_DeltaMargin = 0-table_right_currentMargin;
		
		Animation openanim = new Animation() {
			
		    @Override
		    protected void applyTransformation(float interpolatedTime, Transformation t) {
		    	
				lp_right.rightMargin = (int)(table_right_DeltaMargin * interpolatedTime) + table_right_currentMargin;
				current_program_table_right.setLayoutParams(lp_right);
		    }
		};

		
		//Calculate the animation duration
		long openanimdeltaduration  = Math.round(Math.abs((float)AnimationDuration * ((float)table_right_DeltaMargin / (float)table_right_newMargin)));
		
		if (openanimdeltaduration < 100) {
			openanimdeltaduration = 100;
		}
		
		openanim.setDuration(openanimdeltaduration);
		openanim.setRepeatMode(0);

		current_program_table_right.clearAnimation();
		current_program_table_right.startAnimation(openanim);
		
		/*******************************************************/
		
		/*before fix epg X position
		final int epg_slider_group_paddingright = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.live_fullepg_timeline_slider_group_epg_open_rightmargin), getResources().getDisplayMetrics()));
		final int epg_slider_group_newMargin = 0-liveplayerwidth-epg_slider_group_paddingright;
		final int epg_slider_group_currentMargin = epg_slider_group_lp.rightMargin;
		final int epg_slider_group_deltaMargin = 0 - epg_slider_group_currentMargin;
		 */
		
		//fix epg x position
		final int epg_slider_group_paddingright = epg_slider.getMeasuredWidth()/5;
		final int epg_slider_group_newMargin = 0-liveplayerwidth-epg_slider_group_paddingright;
		final int epg_slider_group_currentMargin = epg_slider_group_lp.rightMargin;
		final int epg_slider_group_deltaMargin = 0 - epg_slider_group_currentMargin;
		//end of fix epg x position
		Animation epg_slider_group_openanim = new Animation() {
			
		    @Override
		    protected void applyTransformation(float interpolatedTime, Transformation t) {
		    	
		    	epg_slider_group_lp.rightMargin = (int)(epg_slider_group_deltaMargin * interpolatedTime) + epg_slider_group_currentMargin;
		    	epg_slider_group.setLayoutParams(epg_slider_group_lp);
		    }
		};
		
		//Calculate the animation duration
		long epg_slider_group_deltaduration  =  Math.round(Math.abs((float)AnimationDuration * ((float)epg_slider_group_deltaMargin /(float)epg_slider_group_newMargin)));
		
		if (epg_slider_group_deltaduration < 100) {
			epg_slider_group_deltaduration = 100;
		}
		
		epg_slider_group_openanim.setDuration(epg_slider_group_deltaduration);
		epg_slider_group_openanim.setRepeatMode(0);
		
		epg_slider_group.clearAnimation();
		epg_slider_group.startAnimation(epg_slider_group_openanim);
		
		/*******************************************************/
		
		
		/* before fix epg x position
		final int epg_slider_newMargin =  Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.live_fullepg_timeline_slider_rightMargin_open), getResources().getDisplayMetrics()));  
		final int epg_slider_initMargin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.live_fullepg_timeline_slider_rightMargin), getResources().getDisplayMetrics()));
		final int epg_slider_currentMargin = epg_slider_lp.rightMargin;
		final int epg_slider_deltaMargin = epg_slider_initMargin - epg_slider_currentMargin;
		*/
		
		//fix epg x position
		final int epg_slider_newMargin = -epg_slider.getMeasuredWidth()/5 ;  
		final int epg_slider_initMargin = -epg_slider.getMeasuredWidth()/5;
		final int epg_slider_currentMargin = epg_slider_lp.rightMargin;
		final int epg_slider_deltaMargin = epg_slider_initMargin - epg_slider_currentMargin;
		//end fix epg x position

		Log.d("","testing epg_slider.getMeasuredWidth = " + epg_slider.getMeasuredWidth());
		
		Animation epg_slider_openanim = new Animation() {
			
		    @Override
		    protected void applyTransformation(float interpolatedTime, Transformation t) {
		    	
		    	epg_slider_lp.rightMargin = (int)((epg_slider_deltaMargin) * interpolatedTime) + epg_slider_currentMargin;
		    	epg_slider.setLayoutParams(epg_slider_lp);
		    }
		};
		
		//Calculate the animation duration
		long epg_slider_deltaduration  =  Math.round(Math.abs((float)AnimationDuration * ((float)epg_slider_deltaMargin / (float)(epg_slider_newMargin))));

		
		epg_slider_openanim.setDuration(epg_slider_deltaduration);
		epg_slider_openanim.setRepeatMode(0);
		
		epg_slider.clearAnimation();
		epg_slider.startAnimation(epg_slider_openanim);
		
		
		/*******************************************************/

		
		if(this.live_fullepg_timelineScrollView!=null){
			AlphaAnimation fadeouttimeline = new AlphaAnimation(1.0f, 0.0f);
			
			fadeouttimeline.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					live_fullepg_timelineScrollView.setAlpha(0);
					
				}
			});
			
			fadeouttimeline.setDuration(AnimationDuration);
			fadeouttimeline.setRepeatMode(0);
			
			this.live_fullepg_timelineScrollView.clearAnimation();
			this.live_fullepg_timelineScrollView.startAnimation(fadeouttimeline);
		}

		
		/*******************************************************/

		this.isEPGOpened = false;
		
	}
	
	//////做item选中高亮,3个adapter用同一个selectedMap//////////////////////////////////////////
	private HashMap<Integer, Boolean> selectedMap = new HashMap<Integer, Boolean>();
	public void setSelected(int position) {
		if(leftAdapter == null) return;
		for(int i = 0; i < leftAdapter.getCount();i++){
			if(i == position){
				selectedMap.put(position, true);
			} else{
				selectedMap.put(i, false);
			}
		}
	}
	
	private int getSelectedByPosition(){
		if(leftAdapter == null) return -1;
		int pos = 1;
		for(int i = 0; i < leftAdapter.getCount();i++){
			if(selectedMap != null && selectedMap.get(i) != null && selectedMap.get(i).booleanValue() == true){
				pos = i;
				break;
			}
		}
		return pos;
	}
	
	private boolean isSelected(int position){
		if(selectedMap != null && selectedMap.get(position) != null && selectedMap.get(position).booleanValue() == true){
			return true;
		} else {
			return false;
		}
	}
	////////////////////////////////////////////////
	
	private class LeftChannelListAdapter extends BaseAdapter {

		private LayoutInflater mLayoutInflater;
		private List<LiveChannelRow> mData;
		public static final int VIEW_TYPE_COUNT = 2;
		public static final int VIEW_TYPE_SECTION = 0;
		public static final int VIEW_TYPE_CHANNEL = 1;
		
		LeftChannelListAdapter(List<LiveChannelRow> datas) {
			mData = datas;
			mLayoutInflater = LayoutInflater.from(getActivity());
		}
		
		public void setData(List<LiveChannelRow> datas) {
			this.mData = datas;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (mData != null)
				return mData.size();
			return 0;
		}

		@Override
		public LiveChannelRow getItem(int position) {
			if (mData != null)
				return mData.get(position);
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public int getViewTypeCount() {
			return VIEW_TYPE_COUNT;
		}
		
		@Override
		public int getItemViewType(int position) {
			if(getItem(position) == null)
				return super.getItemViewType(position);
			if(getItem(position).isChannelSection())
				return VIEW_TYPE_SECTION;
			return VIEW_TYPE_CHANNEL;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LiveChannelRow item = getItem(position);
			switch (getItemViewType(position)) {
			case VIEW_TYPE_SECTION:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.live_left_channel_list_section, parent, false);
				} 
				TextView sectionName = (TextView) convertView.findViewById(R.id.live_left_channel_list_section_text);
				sectionName.setText(item.getChannelSectionName());
				break;
			case VIEW_TYPE_CHANNEL:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.live_left_channel_list_item, parent, false);
				} else {
					convertView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
				}
				ImageView channelLogo = (ImageView) convertView.findViewById(R.id.live_left_channel_list_image);
				//String url =  AppInfo.getImageDomain() + item.getData().getLargeLivePageLogoLink(); // getTabletPromoPosterLink();
				String url = item.getData().getChannelLogoLink(); // getTabletPromoPosterLink();
				Log.w(TAG, "Channel Logo URL = " + url);
				imageLoader.setRemoteImage(channelLogo, url, position);
				//channelLogo.setImageResource(R.drawable._channel_logo);
				
				if(isSelected(position)){
					convertView.setBackgroundColor(getResources().getColor(R.color.live_currentprogram_selected));
				} else {
					convertView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
				}
				break;
			}
			return convertView;
		} 

	}
	
	
	private class RightEPGListAdapter extends BaseAdapter {

		private LayoutInflater mLayoutInflater;
		private List<LiveChannelRow> mData;
		public static final int VIEW_TYPE_COUNT = 2;
		public static final int VIEW_TYPE_SECTION = 0;
		public static final int VIEW_TYPE_CHANNEL = 1;
		
		RightEPGListAdapter(List<LiveChannelRow> datas) {
			mData = datas;
			mLayoutInflater = LayoutInflater.from(getActivity());
		}
		
		public void setData(List<LiveChannelRow> datas) {
			this.mData = datas;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (mData != null)
				return mData.size();
			return 0;
		}

		@Override
		public LiveChannelRow getItem(int position) {
			if (mData != null)
				return mData.get(position);
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public int getViewTypeCount() {
			return VIEW_TYPE_COUNT;
		}
		
		@Override
		public int getItemViewType(int position) {
			if(getItem(position) == null)
				return super.getItemViewType(position);
			if(getItem(position).isChannelSection())
				return VIEW_TYPE_SECTION;
			return VIEW_TYPE_CHANNEL;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LiveChannelRow item = getItem(position);
			switch (getItemViewType(position)) {
			case VIEW_TYPE_SECTION:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.live_left_epg_list_section, parent, false);
				} 
				break;
			case VIEW_TYPE_CHANNEL:
				ViewHolder holder = null;
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.live_left_epg_list_item, parent, false);
					holder = new ViewHolder();
					holder.title = (TextView) convertView.findViewById(R.id.live_epg_list_item_title);
					holder.timeLine = (TextView) convertView.findViewById(R.id.live_epg_list_item_timeline);
					holder.isSelected = (ImageView) convertView.findViewById(R.id.live_epg_list_item_selected_img);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}
				LiveDetailData program = LiveDetail.getInstance().getProgramByChannelAndTime(
						item.getData().getId(), System.currentTimeMillis());
				
				
				
				
				if(program == null){
					holder.title.setText("");
					holder.timeLine.setText("");
				} else {
					holder.title.setText(program.getName());
					holder.timeLine.setText(program.getStartTime() + " - " + program.getEndTime());
				}
				
				if(isSelected(position)){
					convertView.setBackgroundColor(getResources().getColor(R.color.live_currentprogram_selected));
					holder.isSelected.setBackgroundResource(R.drawable.live_epg_playarrow_selected);	
					TextView time = holder.timeLine;
					time.setTextColor(getResources().getColor(R.color.live_text_highlighted));
				} else {
					convertView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
					holder.isSelected.setBackgroundResource(R.drawable.live_epg_playarrow);	
					TextView time = holder.timeLine;
					time.setTextColor(getResources().getColor(R.color.live_text_white));
				}
				
				break;
			}
			return convertView;
		}

	}

	private static class ViewHolder {
		TextView title;
		TextView timeLine;
		ImageView isSelected;
	}
	
	private static class ViewHolderFull {
		TextView title;
		TextView timeLine;
	}
	
	private class FullEPGListAdapter extends BaseAdapter {

		private LayoutInflater mLayoutInflater;
		private List<LiveFullEPGData> mData;
		public static final int VIEW_TYPE_COUNT = 2;
		public static final int VIEW_TYPE_SECTION = 0;
		public static final int VIEW_TYPE_CHANNEL = 1;
		private float density;
		private int height;
		private int marginTop;
		
		FullEPGListAdapter(List<LiveFullEPGData> datas) {
			mData = datas;
			mLayoutInflater = LayoutInflater.from(getActivity());
			density = getActivity().getResources().getDisplayMetrics().density;
			height = (int) (density * 86);
			marginTop = (int) (density * 4);
		}

		public void setData(List<LiveFullEPGData> datas) {
			this.mData = datas;
			this.notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			if (mData != null)
				return mData.size();
			return 0;
		}

		@Override
		public LiveFullEPGData getItem(int position) {
			if (mData != null)
				return mData.get(position);
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public int getViewTypeCount() {
			return VIEW_TYPE_COUNT;
		}
		
		@Override
		public int getItemViewType(int position) {
			if(getItem(position) == null)
				return super.getItemViewType(position);
			if(getItem(position).isFullEPGSection())
				return VIEW_TYPE_SECTION;
			return VIEW_TYPE_CHANNEL;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LiveFullEPGData item = getItem(position);
			switch (getItemViewType(position)) {
			case VIEW_TYPE_SECTION:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.live_full_epg_list_section, parent, false);
				} 
				break;
			case VIEW_TYPE_CHANNEL:
				
				if (convertView == null) {
					//Log.w(TAG, "111111 getview " + position);
					convertView = mLayoutInflater.inflate(R.layout.live_full_epg_list_item, parent, false);
				} else {
					//Log.w(TAG, "111112 getview " + position);
				}
				
				LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.live_full_epg_list_item_container);
				//layout.removeAllViews();
				
				int size = 0;
				// check no fullEPG data
				if(item != null && item.getFullEPG() != null){
					size = item.getFullEPG().size();
				}

				View programeItem = null;
				LinearLayout.LayoutParams ll = null;
				int addSize = size - layout.getChildCount();
				if(addSize > 0) {
					for(int i=0; i<addSize; i++) {
						ViewHolderFull holder = new ViewHolderFull();
						programeItem = mLayoutInflater.inflate(R.layout.live_full_epg_list_item_segment, null);
						ll = new LinearLayout.LayoutParams(0, height);
						ll.setMargins(0, marginTop, 0, 0);
						layout.addView(programeItem, ll);
						holder.title = (TextView) programeItem.findViewById(R.id.live_full_epg_item_segment_title);
						holder.timeLine = (TextView) programeItem.findViewById(R.id.live_full_epg_item_segment_time);
						programeItem.setTag(holder);
					}
				} 
				final LiveCatalogChannelData channel = item.getChannel();
				int newChildCount = layout.getChildCount();
				ViewHolderFull holder;
				for(int i=0; i<newChildCount; i++) {
					//programeItem = mLayoutInflater.inflate(R.layout.live_full_epg_list_item_segment, null);
					programeItem = layout.getChildAt(i);
					if(i < size) {
						final LiveDetailData pro = item.getFullEPG().get(i);
						holder = (ViewHolderFull) programeItem.getTag();
						//TextView title = (TextView) programeItem.findViewById(R.id.live_full_epg_item_segment_title);
						//TextView time = (TextView) programeItem.findViewById(R.id.live_full_epg_item_segment_time);
						holder.title.setText(pro.getName());
						holder.timeLine.setText(pro.getStartTime());
						programeItem.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								NowplayerDialogUtils.createLiveDetailDialog(getActivity(), channel, pro, imageLoaderSingle).show();
							}
						});
						//ll = new LinearLayout.LayoutParams((int) (pro.getDuration() * ONE_MINETE_DP * density), height);
						//ll.setMargins(0, marginTop, 0, 0);
						//layout.addView(programeItem, ll);
						if (i==0){
							Calendar c = Calendar.getInstance();
							c.set(Calendar.HOUR_OF_DAY, 0);
							c.set(Calendar.MINUTE, 0);
							c.set(Calendar.SECOND, 0);
							c.set(Calendar.MILLISECOND, 0);
							int adjustDuration = (int) ((c.getTimeInMillis() - pro.getStart())/60000l);
							if (adjustDuration>0){
								programeItem.getLayoutParams().width = (int) ((pro.getDuration()-adjustDuration) * ONE_MINETE_DP * density);
							} else{
								programeItem.getLayoutParams().width = (int) (pro.getDuration() * ONE_MINETE_DP * density);	
							}
						} else{
							programeItem.getLayoutParams().width = (int) (pro.getDuration() * ONE_MINETE_DP * density);
						}
						
					} else {
						programeItem.getLayoutParams().width = 0;
					}
				}
				if(isSelected(position)){
					convertView.setBackgroundColor(getResources().getColor(R.color.live_currentprogram_selected));
				} else {
					convertView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
				}
				break;
			}
			return convertView;
		}

	}
	
	/**
	 * Update current program info (bottom right panel, full screen video tool bar) 
	 * @param clickPosition onItemClick position
	 */
	private void updateCurrentProgram(int clickPosition){
		if(mLeftChannelData != null && clickPosition < mLeftChannelData.size()){
			LiveCatalogChannelData channel = mLeftChannelData.get(clickPosition).getData();
			LiveDetailData p = LiveDetail.getInstance().getProgramByChannelAndTime(channel.getId(), System.currentTimeMillis());
			updateCurrentProgram(channel, p);
		}
	}
	
	/**
	 * Update current program info (bottom right panel, full screen video tool bar) 
	 * @param channel
	 * @param program
	 */
	private void updateCurrentProgram(LiveCatalogChannelData channel, LiveDetailData program){
		if(channel == null)
			return;
		if(getActivity() == null || getView() == null){
			return;
		}
		//String logoUrl = AppInfo.getImageDomain() + channel.getLargeLivePageLogoLink();
		String logoUrl =  channel.getChannelLogoLink();
		Log.d(TAG, "Current program - channel logo url = " + logoUrl);
		
		View view = getView();
		//////////////////////////////////////////////////////
		// set current program info at the right bottom panel 
		//////////////////////////////////////////////////////
		ImageView logo = (ImageView) view.findViewById(R.id.current_ch_logo);
		TextView channelTitle = (TextView) view.findViewById(R.id.current_ch_title);
		TextView programTitle = (TextView) view.findViewById(R.id.current_program_title);
		TextView programTime = (TextView) view.findViewById(R.id.current_program_time);
		
		// reset image and texts
		logo.setImageResource(R.drawable._channel_logo);
		channelTitle.setText("");
		programTitle.setText("");
		programTime.setText("");
		
		imageLoaderSingle.setRemoteImage(logo, logoUrl);  // channel logo
		channelTitle.setText(channel.getName());
		if(program != null){
			programTitle.setText(program.getName());
			programTime.setText(program.getStartTime() + " - " + program.getEndTime());
		}
		
		////////////////////////////////////
		// update full screen video tool bar
		////////////////////////////////////
		ImageView videoLogo = (ImageView) view.findViewById(R.id.popupTool_logo);
		TextView videoTitle = (TextView) view.findViewById(R.id.popupTool_title);
		TextView videoTime = (TextView) view.findViewById(R.id.popupTool_time);
		
		// reset image and texts
		videoLogo.setImageResource(R.drawable._channel_logo);
		videoTitle.setText("");
		videoTime.setText("");
		
		
		imageLoaderSingle.setRemoteImage(videoLogo, logoUrl);   // channel logo
		if(program != null){
			videoTitle.setText(program.getName());
			videoTime.setText(program.getStartTime() + " - " + program.getEndTime());
		}
	}

	
	/**
	 * author Aaron8
	 */
	private void initPlayer() {
		hasBeenPlay = false;
		findViewById(R.id.live_player_btn_play).setVisibility(View.VISIBLE);
		findViewById(R.id.live_player_bg).setVisibility(View.VISIBLE);
		findViewById(R.id.live_player_full_screen).setVisibility(View.GONE);
		
		videoBar = (SeekBar) findViewById(R.id.popupTool_seekbar);
		durationNow = (TextView) findViewById(R.id.popup_duration_now);
		durationToatl = (TextView) findViewById(R.id.popup_duration_total);
		
		findViewById(R.id.live_player_full_screen).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FrameLayout playerCanvas = (FrameLayout) findViewById(R.id.player_canvas);
				LinearLayout.LayoutParams lp = (LayoutParams) playerCanvas.getLayoutParams();
				lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
				lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
				playerCanvas.setLayoutParams(lp);
				
				FrameLayout.LayoutParams flp = (android.widget.FrameLayout.LayoutParams) playerCanvas.findViewById(R.id.live_video_surfaceview).getLayoutParams();
				flp.height = FrameLayout.LayoutParams.MATCH_PARENT;
				flp.width = FrameLayout.LayoutParams.MATCH_PARENT;
				playerCanvas.findViewById(R.id.live_video_surfaceview).setLayoutParams(flp);
				
				findViewById(R.id.current_program_table_left).setVisibility(View.GONE);
				findViewById(R.id.current_program_table_right).setVisibility(View.GONE);
				findViewById(R.id.epg_slider_group).setVisibility(View.GONE);
				findViewById(R.id.full_epg_canvas).setVisibility(View.GONE);
				findViewById(R.id.helperview).setVisibility(View.GONE);
				findViewById(R.id.live_player_full_screen).setVisibility(View.GONE);
				findActivityViewById(R.id.sidebar).setVisibility(View.GONE);
				findViewById(R.id.live_player_btn_play).setVisibility(View.GONE);
				isFullScreenFlag = true;
				
				DisplayMetrics dm = getResources().getDisplayMetrics();
				player.setVideoModeRatio(dm.widthPixels, dm.heightPixels);
				
				findViewById(R.id.popupTool).setVisibility(View.GONE);
			}
		});
		
		findViewById(R.id.popupTool_exit_fullscreen).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FrameLayout playerCanvas = (FrameLayout) findViewById(R.id.player_canvas);
				LinearLayout.LayoutParams lp = (LayoutParams) playerCanvas.getLayoutParams();
				lp.height = (int) (getResources().getDisplayMetrics().density*350);
				lp.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
				playerCanvas.setLayoutParams(lp);
				
				findViewById(R.id.current_program_table_left).setVisibility(View.VISIBLE);
				findViewById(R.id.current_program_table_right).setVisibility(View.VISIBLE);
				findViewById(R.id.epg_slider_group).setVisibility(View.VISIBLE);
				findViewById(R.id.full_epg_canvas).setVisibility(View.VISIBLE);
				findViewById(R.id.helperview).setVisibility(View.VISIBLE);
				findViewById(R.id.live_player_full_screen).setVisibility(View.VISIBLE);
				findActivityViewById(R.id.sidebar).setVisibility(View.VISIBLE);
				findViewById(R.id.popupTool).setVisibility(View.GONE);
				isFullScreenFlag = false;
				
				if(!player.isPlaying() || !hasBeenPlay){
					findViewById(R.id.live_player_btn_play).setVisibility(View.VISIBLE);
				}
				
//				System.out.println("Width():"+ smallScreenSurfaceviewWidth + "  Height() :"+ smallScreenSurfaceviewHeight);
				player.setVideoModeRatio(smallScreenSurfaceviewWidth, smallScreenSurfaceviewHeight);
			}
		});
		
		findViewById(R.id.popupTool_play).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(player != null){
					if(hasBeenPlay){
						if(player.isPlaying()){
							player.pause();
							((Button)findViewById(R.id.popupTool_play)).setBackgroundResource(R.drawable.selector_playerbar_play);
							if(!isFullScreenFlag)
								findViewById(R.id.live_player_btn_play).setVisibility(View.VISIBLE);
							
							stopLongPlayPrompt();
						} else {
							player.start();
							((Button)findViewById(R.id.popupTool_play)).setBackgroundResource(R.drawable.selector_playerbar_pause);
							findViewById(R.id.live_player_btn_play).setVisibility(View.GONE);
							
							startLongPlayPrompt();
						}
					} else {
						Log.w(TAG, "------play----");
//						setProgressDialogMessage(getString(R.string.loading));
//						showProgressDialog(false);
//						checkEyeIDAndCheckoutPlaylist(mLeftChannelData.get(getSelectedByPosition()).getData().getId(), mAudioCode);
						playVideo();
						((Button)findViewById(R.id.popupTool_play)).setBackgroundResource(R.drawable.selector_playerbar_pause);
					}

				}
			}
		});
		
		findViewById(R.id.live_player_btn_play).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				findViewById(R.id.live_player_btn_play).setVisibility(View.GONE);
//				findViewById(R.id.live_player_bg).setVisibility(View.GONE);
				((Button)findViewById(R.id.popupTool_play)).performClick();
			}
		});
		
		if(player == null) player = new MediaPlayerWrapper();
		player.setScreenOnWhilePlaying(true);
		player.setSurfaceView((SurfaceView)findViewById(R.id.live_video_surfaceview));
		player.setSeekBar((SeekBar) findViewById(R.id.popupTool_seekbar));
		player.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				//mSurfaceView.setVisibility(View.INVISIBLE);
				
				player.start();
				findViewById(R.id.live_player_btn_play).setVisibility(View.GONE);
//				findViewById(R.id.live_player_bg).setVisibility(View.GONE);
				
				startLongPlayPrompt();
				
				if(player.isPlaying()){
					((Button)findViewById(R.id.popupTool_play)).setBackgroundResource(R.drawable.selector_playerbar_pause);
				} else {
					((Button)findViewById(R.id.popupTool_play)).setBackgroundResource(R.drawable.selector_playerbar_play);
				}
			}
		});
		player.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
			@Override
			public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
				if(isFullScreenFlag){
					DisplayMetrics dm = getResources().getDisplayMetrics();
					player.setVideoModeRatio(dm.widthPixels, dm.heightPixels);
				} else {
					if(smallScreenSurfaceviewWidth > 0 && smallScreenSurfaceviewWidth > 0){
						player.setVideoModeRatio(smallScreenSurfaceviewWidth, smallScreenSurfaceviewHeight);
					}
				}
				findViewById(R.id.live_player_bg).setVisibility(View.GONE);
				dismissProgressDialog();//dismiss when begin to play
			}
		});
		player.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				hasBeenPlay = false;
				findViewById(R.id.live_player_btn_play).setVisibility(View.VISIBLE);
				stopLongPlayPrompt();
				
				//Small video player, the full screen button only show after checkout success.
				findViewById(R.id.live_player_full_screen).setVisibility(View.GONE);
				
				checkoutUrl = null;
				handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_general_error)).sendToTarget();
			}
		});
		
		player.setOnSeekBarProgressUpdatedListener(new OnSeekBarProgressUpdatedListener() {

			@Override
			public void onProgressUpdated(int playerCurrentPosition,
					int duration) {
//				durationNow.setText(DatetimeUtils.transMillionToTime(playerCurrentPosition));
//				durationToatl.setText(DatetimeUtils.transMillionToTime(duration));
				
				durationNow.setText(getString(R.string.live));
				durationToatl.setVisibility(View.GONE);
			}
			
		});
		
		player.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// in case the OnCompletionListener to be called.
				return true;
			}
		});
		
		addAnimationToSurfaceView();
		
	}
	
	private void checkEyeIDFailed(String channelId, String audioCode, boolean checkImage, String alertMsg) {
		if(checkImage) {
			// If checkImage true, that means we check to get first frame, no need to handle failed case.
			return;
		}
		if(FOR_GZ_TEST) {
			// for GZ test only. Act as checkout success.
			Log.e(TAG, "checkEyeIDFailed, GZ test, call a fake continueCheckout");
			continueCheckout(channelId, audioCode, checkImage);
		} else {
			// normal action for checkout eyeID
			if(alertMsg == null) {
				dismissProgressDialog();
			} else {
				handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, alertMsg).sendToTarget();
			}
		}
	}
	
	private void checkEyeIDAndCheckoutPlaylist(final String channelId, final String audioCode, final boolean checkImage) {
	    mChannelId = channelId;
	    mAudioCode = audioCode; 
	    mCheckImage = checkImage;
	    continueCheckout(channelId, audioCode, checkImage);
	    //mAccountManagerHelper.requestAuthToken();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.e(TAG, "ChannelFragment :: onActivityResult " + requestCode + ", " + resultCode);
		checkEyeIDAndCheckoutPlaylist(mChannelId, mChannelId, mCheckImage);
		/*if(mAccountManagerHelper.checkActivityResult(requestCode, resultCode, data)) {
			dismissProgressDialog();
		} else {
			dismissProgressDialog();
			// checkEyeIDFailed(mChannelId, mChannelId, mCheckImage, getString(R.string.error_general_error));
		}*/
	}
	
	private CheckoutUIEvent userCheckoutUIEvent = new CheckoutUIEvent() {

		@Override
		public void onNotLoggedIn() {
			Log.e(TAG, "onNotLoggedIn");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_general_error)).sendToTarget();
		}

		@Override
		public void onNotBinded() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onConnectivityWarning() {
			Log.e(TAG, "onConnectivityWarning");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_general_error)).sendToTarget();
		}

		@Override
		public void onCheckoutFailed(String errorCode) {
			Log.e(TAG, "Checkout Failed! " + errorCode);
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_general_error)+" ("+errorCode+") ").sendToTarget();

		}

		@Override
		public void onReceivedPlaylist(List<List<StreamInfo>> playlist,
				String serviceId, int bookmark) {
			Log.e(TAG, "onReceivedPlaylist");
			// playlist.get(quality).get(0)
			Log.w(TAG, "checkout and play");
			handler.obtainMessage(HANDLER_SHOW_QUALITY_DIALOG, playlist).sendToTarget();
		}

		@Override
		public void onReceivedPlaylistWithConcurrent(
				List<List<StreamInfo>> playlist, String serviceId,
				int bookmark, String ccCustId, String ccDomain,
				String ccPoolType) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onNeedSubscription() {
			Log.e(TAG, "onNeedSubscription");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_need_subscribed)).sendToTarget();
		}
		
		@Override
		public void onSystemMaintenance() {
			Log.e(TAG, "onSystemMaintenance");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_system_maintenance)).sendToTarget();
		}

		@Override
		public void onSuccess() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onParentalLock(String responseCode) {
			Log.e(TAG, "onParentalLock");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_general_error)).sendToTarget();
		}

		@Override
		public void onDeviceRegistration(String responseCode) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAccountNotFound_BPL() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAvailableDeviceSlot_BPL() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onNotRegisteredDevice_BPL() {
			// TODO Auto-generated method stub
			
		}
		
		/*@Override
		public void onHomeWifiOnly() {
			Log.e(TAG, "onHomeWifiOnly");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_home_wifi_only)).sendToTarget();
		}

		@Override
		public void onInvalidToken() {
			Log.e(TAG, "onInvalidToken");
			//handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_invalid_token)).sendToTarget();
			//mAccountManagerHelper.clearAuthToken();
			//mAccountManagerHelper.requestAuthToken();
		}

		@Override
		public void onServiceForbidden() {
			Log.e(TAG, "onServiceForbidden");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_service_forbidden)).sendToTarget();
		}*/
		
	};
	
	private CheckoutUIEvent autoCheckoutUIEvent = new CheckoutUIEvent() {

		@Override
		public void onNotLoggedIn() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onNotBinded() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onConnectivityWarning() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCheckoutFailed(String errorCode) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReceivedPlaylist(List<List<StreamInfo>> playlist,
				String serviceId, int bookmark) {
			Log.e(TAG, "onReceivedPlaylist");
			Log.w(TAG, "checkout and checkImage");
			try {
				// Just get first frame here, no need to ask user to select quality.
				String url = playlist.get(0).get(0).getUrl();
//				url = "https://dl.dropboxusercontent.com/s/4rnlizd7rfoech0/jingxun.m3u8"; // test url
				VideoUtils.createVideoFrameFromM3U8(getActivity(), url, 
					new VideoUtils.VideoImageCallback() {
					@Override
					public void onComplete(Bitmap bitmap) {
						Log.d(TAG, "Video Image Complete - bitmap = " + bitmap);
						ImageView bg = (ImageView)findViewById(R.id.live_player_bg);
						bg.setImageBitmap(bitmap);
//						bg.setImageResource(R.drawable._program_thumb_nail);
					}
				});
			} catch (Exception e) {
				Log.w(TAG, "ERROR - checkout checkImage :", e);
			}
		}

		@Override
		public void onReceivedPlaylistWithConcurrent(
				List<List<StreamInfo>> playlist, String serviceId,
				int bookmark, String ccCustId, String ccDomain,
				String ccPoolType) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onNeedSubscription() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSystemMaintenance() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSuccess() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onParentalLock(String responseCode) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDeviceRegistration(String responseCode) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAccountNotFound_BPL() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAvailableDeviceSlot_BPL() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onNotRegisteredDevice_BPL() {
			// TODO Auto-generated method stub
			
		}
		/*
		@Override
		public void onCheckoutFailed(String arg0) {
		}
		@Override
		public void onConnectivityWarning() {
		}
		@Override
		public void onHomeWifiOnly() {
		}
		@Override
		public void onInvalidToken() {
		}
		@Override
		public void onNeedSubscription() {
		}
		@Override
		public void onNotLoggedIn() {
		}
		@Override
		public void onParentalLock(boolean arg0) {
		}
		@Override
		public void onServiceForbidden() {
		}
		@Override
		public void onSystemMaintenance() {
			
		}*/
	};
	
	private void continueCheckout(String channelId, final String audioCode, final boolean checkImage) {
		// Find the channel to checkout
		LiveCatalogChannelData lcd = LiveCatalog.getInstance().getLiveChannelDataById(channelId);
		// If the channel has an audioTemplate, should ask user for which audio to play
		String deviceId = ""; // Can be omitted for EyeApp
		String appId = "01"; // Constant for EyeApp
		CheckoutFlowController cfc = new CheckoutFlowController(getActivity());
		cfc.setCheckoutStepHandler(new LiveChannelCheckout(lcd, audioCode, deviceId, appId));
		cfc.setCheckoutEventHandler(checkImage ? autoCheckoutUIEvent : userCheckoutUIEvent);
		cfc.startCheckout();
	}
	
	private List<List<LiveChannelRow>> newTempPlaylist2() {
		List<List<LiveChannelRow>> playlist = new ArrayList<List<LiveChannelRow>>();
		for(int i=0; i<3; i++) {
			ArrayList<LiveChannelRow> fix = new ArrayList<LiveChannelRow>();
//			String url = Environment.getExternalStorageDirectory().getPath() + "/456.mp4";
			String url = "https://dl.dropboxusercontent.com/s/4rnlizd7rfoech0/jingxun.m3u8";
			for(int n=0; n<3; n++) {
				LiveChannelRow s = new LiveChannelRow();
				s.setChannelSectionName(url);
				fix.add(s);
			}
			playlist.add(fix);
		}

		return playlist;
	}
	
	private void addAnimationToSurfaceView() {
		findViewById(R.id.live_video_surfaceview).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (findViewById(R.id.popupTool).getVisibility() == View.VISIBLE) {
					dismissToolBar();
				}
				else {
					if(isFullScreenFlag){
						addToolbarAnim();
					} else {
						findViewById(R.id.popupTool_play).performClick();
					}
				}
				return false;
			}
		});
		
		findViewById(R.id.popupTool).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
	}
	
	private void dismissToolBar(){
		if(findViewById(R.id.popupTool).getVisibility() == View.VISIBLE){
			findViewById(R.id.popupTool).setAnimation(slidedown);
			findViewById(R.id.popupTool).setVisibility(View.GONE);
		}
	}
	
	private void addToolbarAnim() {
		slidedown = AnimationUtils.loadAnimation(getActivity(),
				R.anim.slide_down);
		if (findViewById(R.id.popupTool).getVisibility() != View.VISIBLE) {
			slideup = AnimationUtils.loadAnimation(getActivity(),
					R.anim.slide_up);
			findViewById(R.id.popupTool).setAnimation(slideup);
			findViewById(R.id.popupTool).setVisibility(View.VISIBLE);
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
	
	//点击item时，重新播放使用
	private void playVideo() {
		if(player != null){
//			player.pause();
			player.stop();
			player.reset();
		}
		findViewById(R.id.live_player_bg).setVisibility(View.VISIBLE);
		
		setProgressDialogMessage(getString(R.string.loading));
		
		if(mLeftChannelData == null || mLeftChannelData.get(getSelectedByPosition()) == null){
			return;
		}
		LiveCatalogChannelData channel = mLeftChannelData.get(getSelectedByPosition()).getData();
		//channel = LiveChannel.getInstance().getLiveChannelDataById("630");
		Log.d(TAG, "my testing channel " + channel.getName() + 
				" : audio list size = " + (channel.getAudioList() == null ? "null" : channel.getAudioList().size()));
		
		//TODO: Disable audio selection for testing only, please enable it when production 
		if(channel.getAudioList() != null && channel.getAudioList().size() > 1){
			// show choose audio dialog;
			showChooseAudioDialog(channel);
		} else {
			checkoutAndRefreshSurfaceView(channel, null);
		}
	}
	
	private void checkoutAndRefreshSurfaceView(LiveCatalogChannelData channel, String audioCode){
		showProgressDialog(true);
		setProgressDialogCancelTag(getSelectedByPosition());
		checkEyeIDAndCheckoutPlaylist(channel.getId(), audioCode, false);
//		initPlayer();
		findViewById(R.id.live_video_surfaceview).setVisibility(View.INVISIBLE); //重绘，调surfaceCreated
		findViewById(R.id.live_video_surfaceview).setVisibility(View.VISIBLE);
	}
	
	private void playCheckOutResult(boolean showDialog){
		try {
			//checkoutUrl = "https://dl.dropboxusercontent.com/s/4rnlizd7rfoech0/jingxun.m3u8";
			player.play(getActivity(), checkoutUrl);
			hasBeenPlay = true;
			if(showDialog) showProgressDialog(true);
			//Small video player, the full screen button only show after checkout success.
			findViewById(R.id.live_player_full_screen).setVisibility(View.VISIBLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	final Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_PROGRESS_CHANGED:
				durationNow.setText(DatetimeUtils.transMillionToTime(msg.arg1));
				durationToatl.setText(DatetimeUtils.transMillionToTime(msg.arg2));
				
				if(!seekBallOnPress){
					videoBar.setMax((int)msg.arg2);
					videoBar.setProgress((int)msg.arg1);
				}

				break;
				
			case HANDLER_SHOW_QUALITY_DIALOG:
				
				@SuppressWarnings("unchecked")
				final List<List<?>> playlist = (List<List<?>>) msg.obj;
				
				Log.d(TAG, "HANDLER_SHOW_QUALITY_DIALOG");
				if(playlist != null){
					if(playlist.size() > 0){
						List<?> fixedQualityList = playlist.get(0);
						if(fixedQualityList != null)
							Log.i(TAG, "fixedQualityList size = " + fixedQualityList.size());
					}
					if(playlist.size() > 1){
						List<?> varQualityList = playlist.get(1);
						if(varQualityList != null)
							Log.i(TAG, "varQualityList size = " + varQualityList.size());
					}
				}
				try {
					int size = playlist.size();
					String[] qualities = new String[size];
//					for(int i=0; i<size; i++) {
//						if(FOR_GZ_TEST){
//							qualities[i] = ((LiveChannelRow)playlist.get(0).get(i)).getChannelSectionName() + "";
//						} else {
//							qualities[i] = ((StreamInfo)playlist.get(0).get(i)).getQuality() + "";
//						}
//					}
					if(size >= 3){
						qualities[0] = getString(R.string.quality_high);
						qualities[1] = getString(R.string.quality_medium);
						qualities[2] = getString(R.string.quality_low);
					} else if(size == 2){
						qualities[0] = getString(R.string.quality_high);
						qualities[1] = getString(R.string.quality_low);
					} else if(size == 1){
						qualities[0] = getString(R.string.quality_high);
					}
					if(qualities.length == 1){
						if(FOR_GZ_TEST){
							checkoutUrl = ((LiveChannelRow)playlist.get(0).get(0)).getChannelSectionName();
						} else {
							checkoutUrl = ((StreamInfo)playlist.get(0).get(0)).getUrl();
						}
						playCheckOutResult(false);
					// } else if(!isQualityDialogShowing && qualities.length > 0){
					} else if(qualities.length > 0) {
						if(!progressDialogHasCanceled(getSelectedByPosition())){
							dismissProgressDialog();
						
							Dialog d = DialogUtils.createCustomSingleChoiceItemsAlertDialog(getActivity(), null, getString(R.string.dialog_select_quality), new CustomSingleChoiceDialogItemAdapter(qualities, getActivity()), -3, null, null, getString(R.string.cancel), 
									new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, final int which) {
									if(which > -1) {
										if(((NowPlayerApplication)getApplication()).QUALITY_ALERY_DIALOG_TAG == null){
											((NowPlayerApplication)getApplication()).QUALITY_ALERY_DIALOG_TAG = "have";
											DialogUtils.createMessageAlertDialog(getActivity(), null, null, getString(R.string.quality_alert_msg), getString(R.string.ok), null, null, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int w) {
													if(FOR_GZ_TEST){
														checkoutUrl = ((LiveChannelRow)playlist.get(which).get(0)).getChannelSectionName();
													} else {
														checkoutUrl = ((StreamInfo)playlist.get(which).get(0)).getUrl();
													}
													playCheckOutResult(true);
												}
											}, false).show();
										} else{
											if(FOR_GZ_TEST){
												checkoutUrl = ((LiveChannelRow)playlist.get(which).get(0)).getChannelSectionName();
											} else {
												checkoutUrl = ((StreamInfo)playlist.get(which).get(0)).getUrl();
											}
											playCheckOutResult(true);
										}
									} 
									dialog.dismiss();
								}
							}, true);
							d.setOnDismissListener(new OnDismissListener(){
								@Override
								public void onDismiss(DialogInterface dialog) {
									//isQualityDialogShowing = false;
								}
							});
							d.show();
							//isQualityDialogShowing = true;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case HANDLER_SHOW_MESSAGE_DIALOG:
				dismissProgressDialog();
				DialogUtils.createMessageAlertDialog(getActivity(), null, 
						getString(R.string.error_title), (String)msg.obj, getString(R.string.ok), null, null, null, true).show();
				break;
			case HANDLER_ANIMATION_TIMEOUT:
				View view = findViewById(R.id.popupTool);
				if(view != null)
					if (view.getVisibility() == View.VISIBLE) {
						dismissToolBar();
					}
				break;
			}
		}
	};
	
	@Override
	public boolean onHostActivityBackPressed() {
		if(isFullScreenFlag){
			findViewById(R.id.popupTool_exit_fullscreen).performClick();
			return true;
		}
		return super.onHostActivityBackPressed();
	}
	
	private void startLongPlayPrompt() {
		if(lppService == null){
			lppService = new LongPlayPromptService(B2BApiAppInfo.getLongPlayPromptLong(), 60000);//AppInfo.getLongPlayPromptLong()
			lppService.setCallback(new LongPlayPromptService.Callback() {
				
				@Override
				public void onShowLongPlayPrompt() {
					longPlayPromptDialog = DialogUtils.createMessageAlertDialog(getActivity(), null, getString(R.string.warning), getString(R.string.long_play_prompt_hint), getString(R.string.long_play_prompt_continue), null, null, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							longPlayPromptDialog = null;
							
							lppService.stop();
							lppService.start();
						}
					}, false);
					longPlayPromptDialog.show();
				}
				
				@Override
				public void onLongPlayStopPlayback() {
					if (longPlayPromptDialog != null) {
						longPlayPromptDialog.dismiss();
						
						if(player.isPlaying()){
							((Button)findViewById(R.id.popupTool_play)).performClick();
						}
					}
				}
			});
		}
		lppService.start();
		
		keepAwake();
	}
	private void stopLongPlayPrompt(){
		if(lppService != null){
			lppService.stop();
		}
		
		releaseAwake();
	}
	
	private void showChooseAudioDialog(final LiveCatalogChannelData channel){
		final List<LiveCatalog.LiveCatalogChannelData.Audio> audioList = channel.getAudioList();
		
		if(audioList != null && audioList.size() > 1){
			String[] audios = new String[audioList.size()];
			for(int i = 0; i < audioList.size(); i++){
				LiveCatalogChannelData.Audio au = audioList.get(i);
				audios[i] = au.getDisplayName();
			}
			Log.d(TAG, "AudioList = " + audios.toString());
			Dialog d = DialogUtils.createCustomSingleChoiceItemsAlertDialog(getActivity(), null, getString(R.string.dialog_select_audio), new CustomSingleChoiceDialogItemAdapter(audios, getActivity()), -3, null, null, getString(R.string.cancel), 
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "Choose Audio - onClick " + which);
					if(which > -1) {
						String audioCode = audioList.get(which).getAudioCode();
						checkoutAndRefreshSurfaceView(channel, audioCode);
					} 
					dialog.dismiss();
				}
			}, true);
			d.setOnDismissListener(new OnDismissListener(){
				@Override
				public void onDismiss(DialogInterface dialog) {
				}
			});
			d.show();
			Log.d(TAG, "showChooseAudioDialog");
		} 
		
	}
	
	private void keepAwake(){
		PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
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
