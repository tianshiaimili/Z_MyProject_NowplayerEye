package com.pccw.nowplayer.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pccw.nmal.checkout.B2BCheckoutFlowController;
import com.pccw.nmal.checkout.B2BCheckoutFlowController.CheckoutUIEvent;
import com.pccw.nmal.checkout.B2BVODCheckout;
import com.pccw.nmal.model.StreamInfo;
import com.pccw.nmal.model.VOD;
import com.pccw.nmal.model.VOD.VODCategoryNodeData;
import com.pccw.nmal.model.VOD.VODData;
import com.pccw.nowplayer.Constants;
import com.pccw.nowplayer.adapter.CustomSingleChoiceDialogItemAdapter;
import com.pccw.nowplayer.adapter.MulticastAllAdapter;
import com.pccw.nowplayer.app.BaseFragment;
import com.pccw.nowplayer.app.VODPlayerActivity;
import com.pccw.nowplayer.res.ImageCachePath;
import com.pccw.nowplayer.utils.AccountManagerHelper;
import com.pccw.nowplayer.utils.DialogUtils;
import com.pccw.nowplayer.utils.ImageCacheParams;
import com.pccw.nowplayer.utils.LogUtils2;
import com.pccw.nowplayer.utils.MediaPlayerWrapper;
import com.pccw.nowplayer.utils.NowplayerDialogUtils;
import com.pccw.nowplayer.utils.RemoteSingleImageLoader;
import com.pccw.nowplayer.utils.TradImageCacheHelper;
import com.pccw.nowplayereyeapp.NowPlayerApplication;
import com.pccw.nowplayereyeapp.R;

import custom.widget.HorizontalScrollBar;
import custom.widget.HorizontalScrollBar.OnScrollBarItemClickListener;
import custom.widget.HorizontalScrollBar.ScrollBarItemInfoProvider;

public class Multicast extends BaseFragment{
	private static String TAG=Multicast.class.getSimpleName();
	
	private Menu1ButtonClickListener menuButtonListener1;
	private Menu2ButtonClickListener menuButtonListener2;
	
//	private ProgramAllAdapter programAllAdapter;
	private MulticastAllAdapter program2AllAdapter;
	private List<String> categoryDatas;
	private List<String> channelDatas;
	
	private int selectedGenrePosition = 0; // 0 = ALL
	private int selectedSubGenrePosition = 0; // 0 = ALL
	
	private List<VOD.VODCategoryNodeData> vodGenreList;
	
    private static final int HANDLER_PROGRESS_CHANGED  = 1;
    private static final int HANDLER_SHOW_QUALITY_DIALOG = 2;
    private static final int HANDLER_SHOW_MESSAGE_DIALOG = 3;
    private static final int HANDLER_ANIMATION_TIMEOUT = 4;
    private  AccountManagerHelper mAccountManagerHelper;

    //video playback variable
    private String checkoutDisplayTitle;
    private String checkoutUrl;
    private ArrayList<String> slateUrl;
	MediaPlayerWrapper player ;
	SeekBar videoBar;
	TextView durationNow;
	TextView durationToatl;
	VOD.VODCategoryNodeData selectedCatalog = null;
	
    RemoteSingleImageLoader loader;
    
    private WebView webView ;
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		ImageCacheParams params = new ImageCacheParams(activity, ImageCachePath.PROGRAM_ALL);
		TradImageCacheHelper cacheHelper = new TradImageCacheHelper(params);
		loader = new RemoteSingleImageLoader(activity, cacheHelper);
		loader.setLoadingImage(getResources(), R.drawable.channel_logo);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		// View view = inflater.inflate(R.layout.program_twobar, container,false);
		View view = createOrRetrieveFragmentView(inflater, container, R.layout.multicast_program_threebar);
		webView = new WebView(getActivity());
		return view;		
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		menuButtonListener1 = new Menu1ButtonClickListener();
		menuButtonListener2 = new Menu2ButtonClickListener();
		
//		if(!isFragmentRecreatedWithHoldView())
//			genData();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if(categoryDatas == null){
			showProgressDialog(true);
		}
		
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				genData();
				dismissProgressDialog();
			}
		}, 20);
	}
	
	private void genData() {
		//Test VOD data loading
		String rootNodeID  = VOD.getInstance().getVODRootNodeID();
		vodGenreList = new ArrayList(VOD.getInstance().getVODCategoryByNodeId(rootNodeID).childNodes.values());
		for (VODCategoryNodeData vodGenreData : vodGenreList){
			Log.d("testing" , "my testing genre = " + vodGenreData.getName());
		}
		refreshUI();
		

	}

	public void refreshUI(){
		refreshGenreBar();
		refreshProgramLayout();
	}
	
	private void refreshGenreBar(){
		//////gen menu//////////////////////////////////////////////////////////////////////
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		
		//////for all :horizontal scroll views//////////////////////////////////////////////////////////////////////
		categoryDatas = new ArrayList<String>();
		categoryDatas.add(getString(R.string.vod_tabbar_all));
		
		//uncomment this for showing genre next to all
//		for (VODCategoryNodeData vodGenreData : vodGenreList){
//			categoryDatas.add(vodGenreData.getName());
//		}
		//end of uncomment
		LinearLayout categorybar = (LinearLayout) findViewById(R.id.program_menubar_lv1);
		categorybar.removeAllViews();
		for(int k = 0; k < categoryDatas.size(); k++){
			Button menuBtn = (Button) inflater.inflate(R.layout.program_menu_button_lv1, null);
			menuBtn.setText(categoryDatas.get(k));
			menuBtn.setOnClickListener(menuButtonListener1);
			categorybar.addView(menuBtn);
			if(k == selectedGenrePosition){
				//change active button
				menuBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.topbar_lv1_active));
				menuBtn.setTextColor(getResources().getColor(R.color.topbar_lv1_title_active));
			} else{
				//change inactive button
				menuBtn.setBackgroundDrawable(null);
				menuBtn.setTextColor(getResources().getColor(R.color.topbar_lv1_title));
			}
		}

		if(selectedGenrePosition!=0){
			channelDatas = new ArrayList<String>();
			List<VODCategoryNodeData> subGenreList = getSubGenreList(vodGenreList.get(selectedGenrePosition-1));
			if (subGenreList.size()>0){
				channelDatas.add(getString(R.string.vod_tabbar_all));
				for (VODCategoryNodeData subGenre : subGenreList){
					channelDatas.add(subGenre.getName());
				}
				enableSecondLevelGenreBar();
			
				LinearLayout channelbar = (LinearLayout) findViewById(R.id.program_menubar_lv2);
				channelbar.removeAllViews();
				for(int k = 0; k < channelDatas.size(); k++){
					Button menuBtn = (Button) inflater.inflate(R.layout.program_menu_button_lv2, null);
					menuBtn.setText(channelDatas.get(k));
					menuBtn.setOnClickListener(menuButtonListener2);
					channelbar.addView(menuBtn);
					
					if(k == selectedSubGenrePosition){
						//change active button
						menuBtn.setTextColor(getResources().getColor(R.color.topbar_lv2_title_active));	
					} else{
						//change inactive button
						menuBtn.setTextColor(getResources().getColor(R.color.topbar_lv1_title));
						
					}
				}
				
				//加一个隐形的背景撑高度
				Button menuBtn = (Button) inflater.inflate(R.layout.program_menu_button_lv2, null);
				menuBtn.setBackgroundResource(R.drawable.topbar_lv1_active);
				menuBtn.setVisibility(View.INVISIBLE);
				channelbar.addView(menuBtn);
				
				////////////////////////////////////////////////////////////////////////////
			} else {
				disableSecondLevelGenreBar();				
			}
		} else{
			disableSecondLevelGenreBar();
		}
	}
	
	private void refreshProgramLayout(){
		LogUtils2.i("on ProgramFragment2---refreshProgramLayout---");
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		
		//All program without genere splited
		if(selectedGenrePosition > 0){
			ArrayList<String> data = new ArrayList<String>();
			//////for session :gridview//////////////////////////////////////////////////////////////////////
			GridView gridView = (GridView) findViewById(R.id.program_gridview_session);
			
			VODCategoryNodeData selectedGenre = vodGenreList.get(selectedGenrePosition-1);
			List<VODCategoryNodeData> allSubGenre = getSubGenreList(selectedGenre);
			
			if(selectedSubGenrePosition==0){
				List<VODCategoryNodeData> allCategory = getAllCategoryByGenre(selectedGenre);
				for(int i=0;i<allCategory.size();i++){
					data.add(allCategory.get(i).getNodeId());
				}
			} else {
				VOD.VODCategoryNodeData subGenreData = allSubGenre.get(selectedSubGenrePosition-1);
				List<VODCategoryNodeData> allCategory = getAllCategoryByGenre(subGenreData);
				for(int i=0;i<allCategory.size();i++){
					data.add(allCategory.get(i).getNodeId());
				}
			}
			
//			programAllAdapter = new ProgramAllAdapter(getActivity(), data, gridView, ProgramFragment2.this);
//			gridView.setAdapter(programAllAdapter);
			LogUtils2.i("date  == "+data.size());
			program2AllAdapter = new MulticastAllAdapter(getActivity(), data, gridView, Multicast.this); 
			gridView.setAdapter(program2AllAdapter);
			
		}
		
		
		//All program splited with genere
		if(selectedGenrePosition == 0){
			LinearLayout programContainer = (LinearLayout) findViewById(R.id.program_container);
			programContainer.removeAllViews();
//			for(int n=0; n < vodGenreList.size(); n++){
//				List<String> itemDatas = new ArrayList<String>();
//				
//				List<VODCategoryNodeData> allCategory = getAllCategoryByGenre(vodGenreList.get(n));
//				
////				for(int i=0;i<allCategory.size();i++){
////					itemDatas.add(allCategory.get(i).getNodeId());
////				}
//				
//				for(int i=0;i<allCategory.size() - 2;i++){
//					itemDatas.add(allCategory.get(i).getNodeId());
//				}
//				
//				LogUtils2.i("on program2---itemDatas = "+itemDatas.size());
//				LinearLayout l = (LinearLayout) inflater.inflate(R.layout.program_session, null);
//				((TextView)l.findViewById(R.id.program_session_title)).setText(vodGenreList.get(n).getName());
//				HorizontalScrollView sessionBar = (HorizontalScrollView)l.findViewById(R.id.program_session_horizontalscrollview);
//				HorizontalScrollBar sessionContentBar = new HorizontalScrollBar.Builder(getResources().getDisplayMetrics().widthPixels, sProvider)
//		 			.setCenterTwoEdges(false)
//		 			.setDataList(itemDatas)
//		 			.setItemLayoutResId(R.layout.program_item)
//		 			.setOnScrollBarItemClickListener(new ScrollBarItemClickListener())
//		 			.create(sessionBar);
//				programContainer.addView(l);
//			}
			
			////////////////////////////////test 
			LinearLayout multicastLayout = (LinearLayout) inflater.inflate(R.layout.program_session, null);
			((TextView)multicastLayout.findViewById(R.id.program_session_title)).setText(R.string.sidebar_multicast_bar_title);
			HorizontalScrollView sessionBar = (HorizontalScrollView)multicastLayout.findViewById(R.id.program_session_horizontalscrollview);
//    		sessionBar.removeAllViews();

//			multicastLayout.seton
            final LinearLayout layout = new LinearLayout(getActivity());
            layout.removeAllViews();
            // Child of HorizontalScrollView layout_width is "wrap_content" hard code inside, use android:fillViewport="true". 
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            		LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(lp);
    		layout.setOrientation(LinearLayout.HORIZONTAL);
    		// set LinearLayout gravity="center"
    		layout.setGravity(android.view.Gravity.CENTER);
    		
			
			for(int i=0;i<3;i++){
				View subView = (RelativeLayout) inflater.inflate(R.layout.program_item, null);
				((ImageView)subView.findViewById(R.id.program_item_image)).setScaleType(ScaleType.FIT_XY);
				((ImageView)subView.findViewById(R.id.program_item_image)).setImageResource(R.drawable.multicast);
				((TextView)subView.findViewById(R.id.program_item_title)).setText("Channel"+(i+1));
				subView.setOnClickListener(new MultiSubViewOnClickListener(i));
//				layout.addView(subView);
				layout.addView(subView, i);
			}
			sessionBar.removeAllViews();
			sessionBar.addView(layout);
			sessionBar.setHorizontalScrollBarEnabled(false);
			sessionBar.scrollTo(0, 0);
			programContainer.addView(multicastLayout);
			
			
			
		}
		
		if(selectedGenrePosition == 0){
			findViewById(R.id.program_gridview_session).setVisibility(View.GONE);
			findViewById(R.id.program_scrollView_all).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.program_gridview_session).setVisibility(View.VISIBLE);
			findViewById(R.id.program_scrollView_all).setVisibility(View.GONE);
		}
	}
	
	/////test multicast HorizontalScrollView
	class MultiSubViewOnClickListener implements OnClickListener{

		int index;
		
		public MultiSubViewOnClickListener(int position){
			index = position;
		}
		
		@Override
		public void onClick(View v) {
			if(index == 0){
				View view = LayoutInflater.from(getActivity()).inflate(R.layout.multicast_alertdialog, null);
				final EditText editText = (EditText) view.findViewById(R.id.edittxt_diolog);
				new AlertDialog.Builder(getActivity()).setTitle("请输入URL")
				.setView(view)
									.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
//										LogUtils2.i("***testOpenMXP******== "+editText.getText().toString());
										if(editText.getText().toString() != null && !editText.getText().toString().equals("")){
											testOpenMXP(getActivity(), editText.getText().toString().trim(),index);
										}
										
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
			}else {
				
				testOpenMXP(getActivity(), "rtsp://192.168.1.125:8554/1",index);
				
			}
		}
		
	}
	
	
	
	private ScrollBarItemInfoProvider sProvider = new ScrollBarItemInfoProvider() {
		@Override
		public void setItemContent(HorizontalScrollBar builder, View convertView, Object value, int position) {

			TextView textView = (TextView) convertView.findViewById(R.id.program_item_title);
			ImageView img = (ImageView) convertView.findViewById(R.id.program_item_image);
			//value is nodeID
			if (value instanceof String){
				textView.setText(VOD.getInstance().getVODCategoryByNodeId(value.toString()).getName());
			} else{
				textView.setText("");
			}
			img.setImageResource(R.drawable.main_frame_bg);
			loader.setRemoteImage(img, VOD.getInstance().getVODCategoryByNodeId(value.toString()).getHdImg1Path());
		}

		@Override
		public int getItemWidth(View convertView) {
			return 0;
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		initPlayer();
	}
	
	class Menu1ButtonClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			TopBarItemTapped(v);
		}
		
	}
	
	class Menu2ButtonClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			SecondTapBarItemTapped(v);
		}
		
	}
	
	public void TopBarItemTapped(View v){
		//Get parent linear layout
		LinearLayout topbarLayout = (LinearLayout)v.getParent();
		
		//Deselect all
		for (int i = 0; i < topbarLayout.getChildCount(); i++) {
			Button b = (Button)topbarLayout.getChildAt(i);
			
			//Select tapped
			if (b == v && selectedGenrePosition != i) {
					selectedGenrePosition = i;
					selectedSubGenrePosition = 0;
					refreshUI();
			}
		}
	}
	
	public void SecondTapBarItemTapped(View v){
		//Get parent linear layout
		LinearLayout topbarLayout = (LinearLayout)v.getParent();
		
		//Deselect all
		for (int i = 0; i < topbarLayout.getChildCount(); i++) {
			Button b = (Button)topbarLayout.getChildAt(i);
			if (b == v && selectedSubGenrePosition != i) {
				selectedSubGenrePosition = i;
				refreshUI();
			}
		}
	}

	//show program details when the catalog item is clicked
	class ScrollBarItemClickListener implements OnScrollBarItemClickListener{
		@Override
		public void onClick(HorizontalScrollBar bar, View convertView,
				final int position) {
			VOD.VODCategoryNodeData catalog = VOD.getInstance().getVODCategoryByNodeId(bar.getDataList().get(position).toString());
//			Dialog dialog = NowplayerDialogUtils.createProgramDetailDialog(getActivity(), catalog, loader, ProgramFragment2.this);
			
//			Dialog dialog = NowplayerDialogUtils.createProgram2DetailDialog(getActivity(), catalog, loader, ProgramFragment2.this);
			
			selectedCatalog = catalog;
			//setupProgramDialogDetail(dialog, catalog);
//			dialog.show();
			//TODO
//			Toast.makeText(getActivity(), "lalalla", 300).show();
//			Dialog multicastDialog = new Dialog(getActivity());
//			final String url ;
			if(position == 0){
				View view = LayoutInflater.from(getActivity()).inflate(R.layout.multicast_alertdialog, null);
				final EditText editText = (EditText) view.findViewById(R.id.edittxt_diolog);
				new AlertDialog.Builder(getActivity()).setTitle("请输入URL")
				.setView(view)
									.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										LogUtils2.i("***testOpenMXP******== "+editText.getText().toString());
										if(editText.getText().toString() != null && !editText.getText().toString().equals("")){
											testOpenMXP(getActivity(), editText.getText().toString().trim(),position);
										}
										
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
			}else {
				
				testOpenMXP(getActivity(),"http://www.baidu.com/test.mp4",position);
			}
			
		}
	}

	public void setupProgramDialogDetail(Dialog dialog,VODCategoryNodeData catalog){
		LinearLayout episodeContainer = ((LinearLayout) dialog.findViewById(R.id.programdetail_episode_container));

		ArrayList<VODData> productList = new ArrayList<VODData> (VOD.getInstance().getVODDataByNodeId(catalog.getNodeId()).values());
		String categoryType = catalog.getType().toUpperCase();
		for (final VODData product : productList){
			LinearLayout row = (LinearLayout) LinearLayout.inflate(getActivity(), R.layout.programdetail_row, null);
			if (categoryType.contains("SERIES")){
				((TextView) row.findViewById(R.id.program_row_title)).setText(product.getEpisodeName());
			} else {
				((TextView) row.findViewById(R.id.program_row_title)).setText(product.getEpisodeTitle());
			}
			episodeContainer.addView(row);
			
			ImageView classificationView = (ImageView) row.findViewById(R.id.program_row_classification);
			
    		if (product.getClassification().equalsIgnoreCase("G")) {
    			classificationView.setImageResource(R.drawable.classification_g);
    		} else if (product.getClassification().equalsIgnoreCase("PG")) {
    			classificationView.setImageResource(R.drawable.classification_pg);
    		} else if (product.getClassification().equalsIgnoreCase("M")) {
    			classificationView.setImageResource(R.drawable.classification_m);
    		} else if (product.getClassification().equalsIgnoreCase("I")) {
    			classificationView.setImageResource(R.drawable.classification_i);
			} else if (product.getClassification().equalsIgnoreCase("IIA")) {
				classificationView.setImageResource(R.drawable.classification_iia);
			} else if (product.getClassification().equalsIgnoreCase("IIB")) {
				classificationView.setImageResource(R.drawable.classification_iib);
			} else if (product.getClassification().equalsIgnoreCase("III") ||
					product.getClassification().equalsIgnoreCase("M+")  ||
					product.getClassification().equalsIgnoreCase("R18") ||
					product.getClassification().equalsIgnoreCase("R/18")) {
				classificationView.setImageResource(R.drawable.classification_iii);
			} else {
				classificationView.setVisibility(View.INVISIBLE);
    		}
    		ImageButton  imagebutton = (ImageButton) row.findViewById(R.id.program_row_play_btn);
    		imagebutton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d("testing", "my testing product.getClassification() = " + product.getClassification());
					
					if (product.getClassification().equalsIgnoreCase("IIB") ||
							product.getClassification().equalsIgnoreCase("III") ||
						product.getClassification().equalsIgnoreCase("M+")  ||
							product.getClassification().equalsIgnoreCase("R18") ||
							product.getClassification().equalsIgnoreCase("R/18")
					){
						handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_general_error)).sendToTarget();
						return;
					}
					
					
					Log.d("testing", "my testing imagebutton.OnClick");
					mAccountManagerHelper = new AccountManagerHelper(getActivity(), 
							Constants.EYEAPP_ACCOUNT_TYPE, Constants.EYEAPP_TOKEN_TYPE, Multicast.this) {
								@Override
								public void onAccountEmpty() {
									Log.d("testing", "my testing onAccountEmpty");
									loginAccount(getActivity());
								}
								@Override
								public void onLoginSuccess(String type, String name) {
									Log.d("testing", "my testing login success");
//									showProgressDialog(true);
//									setProgressDialogCancelTag(getSelectedByPosition());
//									checkEyeIDAndCheckoutPlaylist(mChannelId, mChannelId, mCheckImage);
//									findViewById(R.id.live_video_surfaceview).setVisibility(View.INVISIBLE);
//									findViewById(R.id.live_video_surfaceview).setVisibility(View.VISIBLE);
								}
								@Override
								public void onAuthToken(String token) {
									if(token == null || token.equals("")) {
										Log.d("testing", "my testing onAuthToken, token is null");
										checkEyeIDFailed(getString(R.string.error_general_error));
									} else {
										// get token and checkout playlist.
										Log.d("testing", "my testing onAuthToken, token is " + token);
										continueCheckout(product, token, true);
									}
								}
								@Override
								public void onException(Exception e) {
									Log.d("testing", "my testing onException");
								}
					};
					mAccountManagerHelper.requestAuthToken();
				}
			});
		}
	}

	private void continueCheckout(VODData product, String eyeToken, final boolean autoCheckout) {
		//store the title of the product
		checkoutDisplayTitle = product.getEpisodeTitle();
		
		// Find the channel to checkout
		
		// If the channel has an audioTemplate, should ask user for which audio to play
		String deviceId = ""; // Can be omitted for EyeApp
		String appId = "04"; // Constant for EyeApp
		B2BCheckoutFlowController cfc = new B2BCheckoutFlowController(getActivity());
		cfc.setApiHost(Constants.CHECKOUT_URL);
		cfc.setCheckoutStepHandler(new B2BVODCheckout(product, eyeToken, deviceId, appId));
		//cfc.setCheckoutEventHandler(autoCheckout? autoCheckoutUIEvent : userCheckoutUIEvent);
		cfc.setCheckoutEventHandler(userCheckoutUIEvent);
		cfc.startCheckout();
		showProgressDialog(true);
	}
	
	private void checkEyeIDFailed(String alertMsg) {
		if(alertMsg == null) {
			dismissProgressDialog();
		} else {
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, alertMsg).sendToTarget();
		}

	}
	
	final Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HANDLER_SHOW_MESSAGE_DIALOG:
					dismissProgressDialog();
					DialogUtils.createMessageAlertDialog(getActivity(), null, 
							getString(R.string.error_title), (String)msg.obj, getString(R.string.ok), null, null, null, true).show();
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
							checkoutUrl = ((StreamInfo)playlist.get(0).get(0)).getUrl();
							slateUrl = new ArrayList<String>();
							for (StreamInfo info : (List<StreamInfo>) playlist.get(0)){
								if (!checkoutUrl.equals(info.getUrl())){
									slateUrl.add(info.getUrl());
								}
							}
							playCheckOutResult(false);
						// } else if(!isQualityDialogShowing && qualities.length > 0){
						} else if(qualities.length > 0) {
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
														checkoutUrl = ((StreamInfo)playlist.get(which).get(0)).getUrl();
														slateUrl = new ArrayList<String>();
														for (StreamInfo info : (List<StreamInfo>) playlist.get(which)){
															if (!checkoutUrl.equals(info.getUrl())){
																slateUrl.add(info.getUrl());
															}
														}
														playCheckOutResult(true);
													}
												}, false).show();
											} else{
												checkoutUrl = ((StreamInfo)playlist.get(which).get(0)).getUrl();
												Log.d("testing", "my testing heckouturl = " + checkoutUrl);
												slateUrl = new ArrayList<String>();
												for (StreamInfo info : (List<StreamInfo>) playlist.get(which)){
													if (!checkoutUrl.equals(info.getUrl())){
														slateUrl.add(info.getUrl());
														Log.d("testing", "my testing slateUrl = " + info.getUrl());
													}
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
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;

				
				
			}
		}
	};
	
	private CheckoutUIEvent userCheckoutUIEvent = new CheckoutUIEvent() {
		
		@Override
		public void onReceivedPlaylist(List<List<StreamInfo>> playlist,
				String serviceId, int bookmark) {
			Log.e(TAG, "onReceivedPlaylist");
			// playlist.get(quality).get(0)
			Log.w(TAG, "checkout and play");
			handler.obtainMessage(HANDLER_SHOW_QUALITY_DIALOG, playlist).sendToTarget();
		}
		
		@Override
		public void onParentalLock(boolean firstTry) {
			Log.e(TAG, "onParentalLock");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_general_error)).sendToTarget();
		}
		
		@Override
		public void onNotLoggedIn() {
			Log.e(TAG, "onNotLoggedIn");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_general_error)).sendToTarget();
		}
		
		@Override
		public void onNeedSubscription() {
			Log.e(TAG, "onNeedSubscription");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_need_subscribed)).sendToTarget();
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
		public void onHomeWifiOnly() {
			Log.e(TAG, "onHomeWifiOnly");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_home_wifi_only)).sendToTarget();
		}

		@Override
		public void onInvalidToken() {
			Log.e(TAG, "onInvalidToken");
			//handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_invalid_token)).sendToTarget();
			mAccountManagerHelper.clearAuthToken();
			mAccountManagerHelper.requestAuthToken();
		}

		@Override
		public void onServiceForbidden() {
			Log.e(TAG, "onServiceForbidden");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_service_forbidden)).sendToTarget();
		}

		@Override
		public void onSystemMaintenance() {
			Log.e(TAG, "onSystemMaintenance");
			handler.obtainMessage(HANDLER_SHOW_MESSAGE_DIALOG, getString(R.string.error_system_maintenance)).sendToTarget();
			
		}
		
	};
	
	
	private void enableSecondLevelGenreBar(){
		//enable the second level scrollbar
		findViewById(R.id.program_subcategory_bar).setVisibility(View.VISIBLE);
	}
	
	private void disableSecondLevelGenreBar(){
		//enable the second level scrollbar
		findViewById(R.id.program_subcategory_bar).setVisibility(View.GONE);
	}
	
	private List<VOD.VODCategoryNodeData> getSubGenreList(VOD.VODCategoryNodeData genreNode){
		List<VOD.VODCategoryNodeData> subGenreList = new ArrayList<VOD.VODCategoryNodeData>();
		for (VOD.VODCategoryNodeData data : genreNode.childNodes.values()){
			if ("genre".equalsIgnoreCase(data.getType())){
				subGenreList.add(data);
			}
		}
		return subGenreList;
	}
	
	private List<VOD.VODCategoryNodeData> getAllCategoryByGenre(VOD.VODCategoryNodeData genreNode){
		List<VOD.VODCategoryNodeData> allCategory = new ArrayList<VOD.VODCategoryNodeData>();
		
		for (VOD.VODCategoryNodeData childNode : genreNode.childNodes.values()){
			if ("genre".equalsIgnoreCase(childNode.getType())){
				allCategory.addAll(getAllCategoryByGenre(childNode));
			} else{
				allCategory.add(childNode);
			}
		}
		return allCategory;
	}
	
	private void playCheckOutResult(boolean showDialog){
		try {
			dismissProgressDialog();

			Intent intent = new Intent();
			intent.setClass(getActivity(), VODPlayerActivity.class);
			//checkoutUrl = "https://dl.dropboxusercontent.com/s/4rnlizd7rfoech0/jingxun.m3u8";
			//slateUrl.clear();
			//slateUrl.add("https://dl.dropboxusercontent.com/s/lbyqx2zwumwgh7w/test_slate.m3u8");
			intent.putExtra("checkoutUrl", checkoutUrl);
			intent.putExtra("slateUrl", slateUrl);
			intent.putExtra("checkoutDisplayTitle", checkoutDisplayTitle);
			intent.putExtra("channelLogoPath", selectedCatalog.getCategoryImagePath());
			startActivity(intent);
//			checkoutUrl = "https://dl.dropboxusercontent.com/s/4rnlizd7rfoech0/jingxun.m3u8";
//			player.play(getActivity(), checkoutUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private void initPlayer() {
		/*
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
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) playerCanvas.getLayoutParams();
				lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
				lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
				playerCanvas.setLayoutParams(lp);
				
				FrameLayout.LayoutParams flp = (android.widget.FrameLayout.LayoutParams) playerCanvas.findViewById(R.id.live_video_surfaceview).getLayoutParams();
				flp.height = FrameLayout.LayoutParams.MATCH_PARENT;
				flp.width = FrameLayout.LayoutParams.MATCH_PARENT;
				playerCanvas.findViewById(R.id.live_video_surfaceview).setLayoutParams(flp);
				findViewById(R.id.live_player_full_screen).setVisibility(View.GONE);
				findActivityViewById(R.id.sidebar).setVisibility(View.GONE);
				findViewById(R.id.live_player_btn_play).setVisibility(View.GONE);
				DisplayMetrics dm = getResources().getDisplayMetrics();
				player.setVideoModeRatio(dm.widthPixels, dm.heightPixels);
				findViewById(R.id.popupTool).setVisibility(View.GONE);
			}
		});
		
		if(player == null) player = new MediaPlayerWrapper();
		player.setSurfaceView((SurfaceView)findViewById(R.id.live_video_surfaceview));
		player.setSeekBar((SeekBar) findViewById(R.id.popupTool_seekbar));
		player.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				//mSurfaceView.setVisibility(View.INVISIBLE);
				findViewById(R.id.player_canvas2).setVisibility(View.VISIBLE);
				player.setSurfaceView((SurfaceView)findViewById(R.id.live_video_surfaceview));
				player.start();
				findViewById(R.id.live_player_btn_play).setVisibility(View.GONE);
				findViewById(R.id.live_player_full_screen).performClick();
			}
		});
		player.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
			@Override
			public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
					DisplayMetrics dm = getResources().getDisplayMetrics();
					player.setVideoModeRatio(dm.widthPixels, dm.heightPixels);
					findViewById(R.id.live_player_bg).setVisibility(View.GONE);
			}
		});
		*/
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}
	
	
	/**
	 * play MxPlayer
	 * @param context
	 */
	public void testOpenMXP(Context context,String url,int position){
		
		List<PackageInfo> packageInfos = context.getPackageManager().getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for(PackageInfo info : packageInfos)
		{
			//take the app package name
			String packageName = info.packageName;
//			LogUtils2.e("packageName=="+packageName);
			//com.mxtech.videoplayer  com.tencent.mtt
			String purposePackage1 = "com.mxtech.videoplayer";
			String purposePackage2 = "mxtech.videoplayer";
			
			if(packageName.contains(purposePackage1) || packageName.contains(purposePackage2)){
				
				//take the app message
				ApplicationInfo appInfo = info.applicationInfo;
				try
				{
					PackageInfo packageInfo = context.getPackageManager().getPackageInfo
							(packageName, 
									PackageManager.GET_UNINSTALLED_PACKAGES |
									PackageManager.GET_ACTIVITIES
									);
//					LogUtils2.d("appInfo.getPackageName()=="+packageName);
					
					ActivityInfo[] activityInfos = packageInfo.activities;
					if(activityInfos != null && activityInfos.length > 0)
					{
						
						try {
							//Select scanning the second ActivityInfo
							//because the first one is into the MediaList not play the Link
							ActivityInfo startActivity = activityInfos[2];
							//configuration Intent，start activity
							Intent intent = new Intent(Intent.ACTION_VIEW);
							 ComponentName componentName = 
									 new ComponentName(packageName, startActivity.name);
							 LogUtils2.e("startActivity.name== "+startActivity.name);
//							intent.addCategory(Intent.ACTION_DEFAULT); 
							 intent.setComponent(componentName);
						     String type = "video/*";  
						     String path=null;// = for test "rtsp://192.168.1.125:8554/1";
//						     if(position == 0){
//						    	 path = Environment.getExternalStorageDirectory()
//							    		 .getAbsolutePath()+"/Qiyivideo/Test.mp4";
//						     }else if(position == 1){
//						    	 path ="http://www.baidu.com";
//							}else if(position == 2){
								path ="rtsp://192.168.1.125:8554/1";
//							}
						     Uri name = Uri.parse(path);  
						     intent.setDataAndType(name, type); 
						     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							 context.startActivity(intent);
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
//							LogUtils2.d("error== "+e.getMessage());
							AlertDialog.Builder builder = new AlertDialog.Builder(context);
							builder.setMessage("error");
							builder.setPositiveButton("cancle", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
							builder.create().show();
						}
					}
					else
					{
						Toast.makeText(context, "no exist", Toast.LENGTH_SHORT).show();
					}
				}
				catch (NameNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
}
