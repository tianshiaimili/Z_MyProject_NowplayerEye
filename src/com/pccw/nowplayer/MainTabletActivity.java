package com.pccw.nowplayer;

import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pccw.android.ad.AdSplashDialog;
import com.pccw.android.ad.common.AppConfigInfo;
import com.pccw.nmal.appdata.B2BApiAppInfo;
import com.pccw.nmal.service.B2BApiPixelLogService;
import com.pccw.nowplayer.app.BaseFragment;
import com.pccw.nowplayer.app.BaseFragmentActivity;
import com.pccw.nowplayer.app.FragmentUtils.FragmentFeed;
import com.pccw.nowplayer.app.FragmentUtils.FragmentSwitcher;
import com.pccw.nowplayer.fragment.ChannelFragment;
import com.pccw.nowplayer.fragment.EmptyFragment;
import com.pccw.nowplayer.fragment.ProgramFragment;
import com.pccw.nowplayer.fragment.Multicast;
import com.pccw.nowplayer.fragment.SettingFragment;
import com.pccw.nowplayer.utils.AnimateUtils;
import com.pccw.nowplayer.utils.DeviceUtil;
import com.pccw.nowplayer.utils.LogUtils2;
import com.pccw.nowplayer.utils.ThreadPoolUtils;
import com.pccw.nowplayereyeapp.R;

public class MainTabletActivity extends BaseFragmentActivity implements OnClickListener {
	
	private String[] tabArray;
	public static final int LOAD_SIDEBAR_CATEGORY_OK=0;
	public static final String ITEM_TAG_IMG_BG = "BG_";
	public static final String ITEM_TAG_IMG_BT = "IMG_BUTTON_";
	public static final String ITEM_TAG_TITLE = "TITLE_";
	private static final String TAG = MainTabletActivity.class.getSimpleName();
	private static final int DIALOG_QUIP_APP_CONFIRM = 1;
	private LinearLayout itemViewLO;
	private LinearLayout itemViewBottom;
	private View sidebar;
	private FragmentSwitcher mFragmentSwitcher = new FragmentSwitcher(R.id.container, new FragmentFeed() {
		@Override
		public Fragment newFragment(String tag) {
			if(tag.equals(ChannelFragment.class.getSimpleName())) {
				return new ChannelFragment();
			} else if(tag.equals(ProgramFragment.class.getSimpleName())) {
				return new ProgramFragment();
			}else if(tag.equals(Multicast.class.getSimpleName())){
				//TODO
				return new Multicast();
			}else if(tag.equals(SettingFragment.class.getSimpleName())) {
				return new SettingFragment();
			} else if(tag.equals(EmptyFragment.class.getSimpleName())) {
				return new EmptyFragment();
			} 
			return null;
		}
	});
	//private B2BApiPixelLogService pixelLogService;

	@Override
	protected void onCreate(Bundle bun) {
		super.onCreate(bun);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main_layout_tablet);
		sidebar = findViewById(R.id.sidebar);
		//setOnClickListener();
		
	//	findViewById(R.id.Sidebar_btn_live_ImageButton).performClick();
		if(!isFinishing()){
			showtAd();
		}
		//MyNotificationStartupReceiver.startService(this);
		//pixelLogService = new B2BApiPixelLogService(Constants.PIXEL_LOG_URL, Constants.PIXEL_LOG_APP_NAME, new UserSetting(this).getDeviceToken());
		
		//START EMERGENCY FIX show alert box
		final SharedPreferences settings;
		getApplicationContext();
		settings = getSharedPreferences("TNCALERTBOX", Context.MODE_PRIVATE);
		int id = settings.getInt("ID", 0);
		if (id != 1){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.tnc_title);

			Resources res = getResources();
			InputStream in_s = res.openRawResource(R.raw.tnc);	 
			byte[] b;
			String Msg = null;
			try {
				b = new byte[in_s.available()];
				in_s.read(b);
				Msg = new String(b);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			builder.setMessage(Msg)
			.setCancelable(false)
			.setPositiveButton(R.string.tnc_agree, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Editor editor = settings.edit();
					editor.putInt("ID", 1);
					editor.commit();
					dialog.cancel();
				}
			})
			.setNegativeButton(R.string.tnc_deny, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					MainTabletActivity.this.finish();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			
			
			//Intent i = new Intent(MainTabletActivity.this, TutorialActivity.class);
			//startActivity(i);
		}
		// END of emergency fix
		
	}

	//simulate add a other Programs2
	public static String[] testAddPrograms2(){
		 String [] str = {"Live","Programs","Setting","Multicast"}; 
		return str;
	}

	private void createTabCategory() {
		
		//tabArray = new String[]{"Live","Programs","VideoExpress","Movie","Service Notice","Setting"};//----test--
		//tab
		 itemViewLO   = (LinearLayout) findViewById(R.id.sidebar_category_lo);
		 itemViewBottom   = (LinearLayout) findViewById(R.id.sidebar_bottom_btns);
		LayoutInflater inflater = LayoutInflater.from(this);
		
		
		
		if(tabArray==null||tabArray.length==0)return;
		if(tabArray!=null){
			for(int i=0;i<tabArray.length;i++){
				Log.d(TAG, "sider bar category:"+tabArray[i]);
				//if(tabArray[i].equals("Live"))continue;
			//	if(tabArray[i].equals("Programs"))continue;
				if(tabArray[i].equals("Service Notice"))continue;
				//if(tabArray[i].equals("Setting"))continue;
				
				View  view = 	inflater.inflate(R.layout.tab_cat_list_item, null);
				RelativeLayout list_item_layout = (RelativeLayout) view.findViewById(R.id.sidebar_list_item);
				ImageView bgImg = (ImageView) view.findViewById(R.id.sidebar_bg_item);
				ImageButton item_imag_bt  =(ImageButton)	view.findViewById(R.id.sidebar_imageButton_item);
				TextView item_tv  =(TextView)	view.findViewById(R.id.sidebar_tv_item);
				bgImg.setTag(ITEM_TAG_IMG_BG+tabArray[i]);
				item_imag_bt.setTag(ITEM_TAG_IMG_BT+tabArray[i]);
				item_tv.setTag(ITEM_TAG_TITLE+tabArray[i]);
				
				
				if(tabArray[i].contains("Setting")){
					float scale = this.getResources().getDisplayMetrics().density;
					int layoutHeight = (int) ((int) 45*scale+0.5f);//------------45 is 45dp
					LayoutParams itemLayoutParams =  (LayoutParams) list_item_layout.getLayoutParams();
					itemLayoutParams.height=layoutHeight;
					list_item_layout.setLayoutParams(itemLayoutParams);
					bgImg.setScaleType(ScaleType.FIT_XY);
					item_imag_bt.setImageResource(R.drawable.sidebar_icon_setting);
					item_tv.setVisibility(View.GONE);
					itemViewBottom.addView(view);
				}
				else if(tabArray[i].contains("Live")){
					item_tv.setText(R.string.sidebar_channel);
					item_imag_bt.setImageResource(R.drawable.sidebar_icon_live);
					itemViewLO.addView(view);
				}
				else if(tabArray[i].contains("Programs")){
					item_tv.setText(R.string.sidebar_program);
					item_imag_bt.setImageResource(R.drawable.sidebar_icon_channel);
					itemViewLO.addView(view);
				}
				//TODO 
				else if(tabArray[i].equals("Multicast")) {
					item_tv.setText(R.string.sidebar_multicast);
					item_imag_bt.setImageResource(R.drawable.sidebar_icon_channel);
					itemViewLO.addView(view);
				}
				else{
					item_tv.setText(tabArray[i]);
					itemViewLO.addView(view);	
				}
				
				item_imag_bt.setOnClickListener(new SideBarOnClickLis()) ;
			
			}
		
			Log.i("tabArray", tabArray[0]);
			LogUtils2.e("tabArray   == "+tabArray[0]);
			itemViewLO.findViewWithTag(ITEM_TAG_IMG_BT+tabArray[0]).performClick();
			
		}
		
		
		

		
	}
	
	class SideBarOnClickLis implements OnClickListener{

		

		@Override
		public void onClick(View v) {
			changeSidebarView(v);
			setTabFragment(v);
		}

		
		
	}
	private int getCurrentCatId(String viewTag) {
		if(viewTag ==null)  return 0;
		if(tabArray==null||tabArray.length==0)return 0;
		if(tabArray!=null){
			for(int i=0;i<tabArray.length;i++){
				
				if(viewTag.equalsIgnoreCase(ITEM_TAG_IMG_BT+tabArray[i])){
					return i;
				}
				
			}
		}else{
			return 0;
		}
		return 0;
	}
	@Override
	public void onStart() {
		super.onStart();
		getSideBarMenu();
		
	}
	@Override
	protected void onPause() {
		super.onPause();
		AdSplashDialog.dismissSplashDialog();
	}
	private void getSideBarMenu() {
		if(tabArray == null){
		//	showProgressDialog(true);
			ThreadPoolUtils.execute(new Runnable() {

				@Override
				public void run() {
					tabArray  = B2BApiAppInfo.getTabConfig();
//					LogUtils2.e("tabArray== "+tabArray[0]+"  "+tabArray.length);
//					for(String string : tabArray){
//						LogUtils2.i("tabArray== "+string);
//					}
					//TODO
					tabArray = testAddPrograms2();
					Log.i("tabArray", tabArray[0]);
					handler.obtainMessage(LOAD_SIDEBAR_CATEGORY_OK).sendToTarget();
				}
				
			});
		
		}
	}
	final Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOAD_SIDEBAR_CATEGORY_OK:
				//dismissProgressDialog();
				 createTabCategory();
				break;

			default:
				break;
			}
			
			
		};
		
	};
	private int currentCatId=0;
	private int oldSelectedCatId=0;
	@Override
	public void onLocaleChanged() {
		super.onLocaleChanged();
		
		//initView();
		changeSidebarText();
	}
	
	private void changeSidebarText() {
		if(tabArray==null||tabArray.length==0)return;
		for(int i = 0 ;i<tabArray.length;i++){
		TextView item_tv = (TextView)sidebar.findViewWithTag(ITEM_TAG_TITLE+tabArray[i]);
		if (item_tv!=null){
			if(tabArray[i].contains("Setting")){
			}
			else if(tabArray[i].contains("Live")){
				item_tv.setText(R.string.sidebar_channel);
			}
			else if(tabArray[i].contains("Programs")){
				item_tv.setText(R.string.sidebar_program);
			}else if(tabArray[i].contains("Multicast")){
				item_tv.setText(R.string.sidebar_multicast);
			}
			else{
				item_tv.setText(tabArray[i]);
			}
		}
		
		}
		
	}


	private void initView() {
		((TextView)findViewById(R.id.sidebar_channel_tv)).setText(R.string.sidebar_channel);
		((TextView)findViewById(R.id.sidebar_program_tv)).setText(R.string.sidebar_program);
		((TextView)findViewById(R.id.sidebar_program_tv2)).setText(R.string.sidebar_multicast);
		
	}
	
	

	private void setOnClickListener() {
		
		findViewById(R.id.Sidebar_btn_live_ImageButton).setOnClickListener(this);
		findViewById(R.id.Sidebar_btn_program_imageButton).setOnClickListener(this);
		//TODO
		findViewById(R.id.Sidebar_btn_program_imageButton2).setOnClickListener(this);
		findViewById(R.id.Sidebar_btn_setting_imageButton).setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.Sidebar_btn_live_ImageButton:
				mFragmentSwitcher.switchFragment(this, ChannelFragment.class.getSimpleName());
				sidebarTapped(v);
				break;
			case R.id.Sidebar_btn_program_imageButton:
				mFragmentSwitcher.switchFragment(this, ProgramFragment.class.getSimpleName());
				sidebarTapped(v);
				break;
				//TODO
			case R.id.Sidebar_btn_program_imageButton2:
				mFragmentSwitcher.switchFragment(this, Multicast.class.getSimpleName());
				sidebarTapped(v);
				break;
			case R.id.Sidebar_btn_setting_imageButton:
				mFragmentSwitcher.switchFragment(this, SettingFragment.class.getSimpleName());
			    sidebarTapped(v);
				break;
		}
		//tabViewOnclick( v);
	}
	
	//the v must be the ImageButton android:id="@+id/sidebar_imageButton_item"
	private void setTabFragment(View v) {
		//["Live","Programs","VideoExpress","Movie","Service Notice","Setting"],
		if(tabArray==null||tabArray.length==0)return;
		String viewTag = v.getTag().toString();
		if((ITEM_TAG_IMG_BT+"Live").equalsIgnoreCase(viewTag)){
			mFragmentSwitcher.switchFragment(this, ChannelFragment.class.getSimpleName());
			//pixelLogService.pixelLogOnTabPress(B2BApiPixelLogService.PIXELLOG_LIVE, DeviceUtil.getAppVersion(this));
		}
		if((ITEM_TAG_IMG_BT+"Programs").equalsIgnoreCase(viewTag)){
			mFragmentSwitcher.switchFragment(this, ProgramFragment.class.getSimpleName());
			//pixelLogService.pixelLogOnTabPress(B2BApiPixelLogService.PIXELLOG_PROG, DeviceUtil.getAppVersion(this));
		}
		
		if((ITEM_TAG_IMG_BT+"Multicast").equalsIgnoreCase(viewTag)){
			mFragmentSwitcher.switchFragment(this, Multicast.class.getSimpleName());
			//pixelLogService.pixelLogOnTabPress(B2BApiPixelLogService.PIXELLOG_PROG, DeviceUtil.getAppVersion(this));
		}
		
		if((ITEM_TAG_IMG_BT+"Setting").equalsIgnoreCase(viewTag)){
			mFragmentSwitcher.switchFragment(this, SettingFragment.class.getSimpleName());
		}
		if((ITEM_TAG_IMG_BT+"VideoExpress").equalsIgnoreCase(viewTag)){
			mFragmentSwitcher.switchFragment(this, EmptyFragment.class.getSimpleName());
			//pixelLogService.pixelLogOnTabPress(B2BApiPixelLogService.PIXELLOG_VE, DeviceUtil.getAppVersion(this));
		}
		if((ITEM_TAG_IMG_BT+"Movie").equalsIgnoreCase(viewTag)){
			mFragmentSwitcher.switchFragment(this, EmptyFragment.class.getSimpleName());
			//pixelLogService.pixelLogOnTabPress(B2BApiPixelLogService.PIXELLOG_MOVIE, DeviceUtil.getAppVersion(this));
		}
		
	}

	private void changeSidebarView(View v) {
		
		String viewTag  = v.getTag().toString();
		currentCatId = getCurrentCatId(viewTag);
		
		switchSideBar(oldSelectedCatId,currentCatId);
		//remember the old selected category id
		oldSelectedCatId = currentCatId;
	}


	private void switchSideBar( int oldSelectedCatId, int currentCatId) {
		
		String oldViewTag  = ITEM_TAG_IMG_BT+tabArray[oldSelectedCatId];
		String newViewTag  = ITEM_TAG_IMG_BT+tabArray[currentCatId];;
		
		final TextView  oldTv = (TextView) sidebar.findViewWithTag(ITEM_TAG_TITLE+tabArray[oldSelectedCatId]);
		final TextView  newTv = (TextView) sidebar.findViewWithTag(ITEM_TAG_TITLE+tabArray[currentCatId]);
		final ImageButton oldImgBT = (ImageButton) sidebar.findViewWithTag(ITEM_TAG_IMG_BT+tabArray[oldSelectedCatId]);
		final ImageButton newImgBT = (ImageButton) sidebar.findViewWithTag(ITEM_TAG_IMG_BT+tabArray[currentCatId]);
		final ImageView oldBG = (ImageView) sidebar.findViewWithTag(ITEM_TAG_IMG_BG+tabArray[oldSelectedCatId]);
		final ImageView newBG = (ImageView) sidebar.findViewWithTag(ITEM_TAG_IMG_BG+tabArray[currentCatId]);
		
		
		
		//change text color
		oldTv.setTextColor(getResources().getColor(
				R.color.sidebar_icon_title_color));
		newTv.setTextColor(getResources().getColor(
				R.color.sidebar_icon_title_color_active));
		
		//change icon image
		if((ITEM_TAG_IMG_BT+"Live").equalsIgnoreCase(oldViewTag)){
			oldImgBT.setImageResource(R.drawable.sidebar_icon_live);
			
		}
		if((ITEM_TAG_IMG_BT+"Live").equalsIgnoreCase(newViewTag)){
			newImgBT.setImageResource(R.drawable.sidebar_icon_live_active);
			
		}
		if((ITEM_TAG_IMG_BT+"Programs").equalsIgnoreCase(oldViewTag)){
			oldImgBT.setImageResource(R.drawable.sidebar_icon_channel);
		}
		if((ITEM_TAG_IMG_BT+"Programs").equalsIgnoreCase(newViewTag)){
			newImgBT.setImageResource(R.drawable.sidebar_icon_channel_active);
		}
		
		//TODO
		if((ITEM_TAG_IMG_BT+"Multicast").equalsIgnoreCase(oldViewTag)){
			oldImgBT.setImageResource(R.drawable.sidebar_icon_channel);
		}
		if((ITEM_TAG_IMG_BT+"Multicast").equalsIgnoreCase(newViewTag)){
			newImgBT.setImageResource(R.drawable.sidebar_icon_channel_active);
		}
		
		if((ITEM_TAG_IMG_BT+"VideoExpress").equalsIgnoreCase(oldViewTag)){
			oldImgBT.setImageResource(R.drawable.sidebar_icon_live);
			
		}
		if((ITEM_TAG_IMG_BT+"VideoExpress").equalsIgnoreCase(newViewTag)){
			newImgBT.setImageResource(R.drawable.sidebar_icon_live_active);
			
		}
		if((ITEM_TAG_IMG_BT+"Movie").equalsIgnoreCase(oldViewTag)){
			oldImgBT.setImageResource(R.drawable.sidebar_icon_channel);
		}
		if((ITEM_TAG_IMG_BT+"Movie").equalsIgnoreCase(newViewTag)){
			newImgBT.setImageResource(R.drawable.sidebar_icon_channel_active);
		}
		if((ITEM_TAG_IMG_BT+"Setting").equalsIgnoreCase(oldViewTag)){
			oldImgBT.setImageResource(R.drawable.sidebar_icon_setting);
			
		}
		if((ITEM_TAG_IMG_BT+"Setting").equalsIgnoreCase(newViewTag)){
			newImgBT.setImageResource(R.drawable.sidebar_icon_setting_active);
		}
		
		
		//change icon background image //start animation;
		oldBG.setBackgroundResource(0);
		oldBG.clearAnimation();
		oldBG.startAnimation(AnimateUtils.fadeInOutAnimation(oldBG, 1, 0, 300));
		
		if((ITEM_TAG_IMG_BT+"Setting").equalsIgnoreCase(newViewTag)){
			newBG.setImageResource(R.drawable.sidebar_icon_pointer_white);
		}else{
			newBG.setBackgroundResource(R.drawable.sidebar_icon_pointer);
		}
		newBG.clearAnimation();
		newBG.startAnimation(AnimateUtils.fadeInOutAnimation(newBG, 0, 1, 300));
		
	}


	public void sidebarTapped(View v) {

		RelativeLayout btn_live = (RelativeLayout) findViewById(R.id.Sidebar_btn_live);
		RelativeLayout btn_program = (RelativeLayout) findViewById(R.id.Sidebar_btn_program);
		RelativeLayout btn_program2 = (RelativeLayout) findViewById(R.id.Sidebar_btn_program2);
		RelativeLayout btn_setting = (RelativeLayout) findViewById(R.id.Sidebar_btn_setting);

		
		RelativeLayout parent = (RelativeLayout) v.getParent();

		if (parent == btn_live) {
			this.selectSideBarItem(btn_live);
		} else {
			this.deselectSideBarItem(btn_live);
		}

		if (parent == btn_program) {
			this.selectSideBarItem(btn_program);
		} else {
			this.deselectSideBarItem(btn_program);
		}

		/*add a new btn_program*/
		//TODO
		if (parent == btn_program2) {
			this.selectSideBarItem(btn_program2);
		} else {
			this.deselectSideBarItem(btn_program2);
		}
		
		if (parent == btn_setting) {
			this.selectSideBarItem(btn_setting);
		} else {
			this.deselectSideBarItem(btn_setting);
		}
		

	}

	public void selectSideBarItem(RelativeLayout itemLayout) {

		RelativeLayout btn_live = (RelativeLayout) findViewById(R.id.Sidebar_btn_live);
		RelativeLayout btn_program = (RelativeLayout) findViewById(R.id.Sidebar_btn_program);
		RelativeLayout btn_program2 = (RelativeLayout) findViewById(R.id.Sidebar_btn_program2);
		RelativeLayout btn_setting = (RelativeLayout) findViewById(R.id.Sidebar_btn_setting);

		
		
		final ImageView bg = (ImageView) itemLayout.findViewWithTag("bg");
		final TextView title = (TextView) itemLayout.findViewWithTag("title");
		final ImageButton btn = (ImageButton) itemLayout.findViewWithTag("btn");

		if (itemLayout.getTag().equals("big")) {
			title.setTextColor(getResources().getColor(
					R.color.sidebar_icon_title_color_active));
		}

		if (itemLayout == btn_live) {
			btn.setImageResource(R.drawable.sidebar_icon_live_active);
		}

		if (itemLayout == btn_program) {
			btn.setImageResource(R.drawable.sidebar_icon_channel_active);
		}
		
		//TODO
		if (itemLayout == btn_program2) {
			btn.setImageResource(R.drawable.sidebar_icon_channel_active);
		}
		

		if (itemLayout == btn_setting) {
			btn.setImageResource(R.drawable.sidebar_icon_setting_active);
		}

		if (itemLayout.getTag().equals("big")) {
			bg.setBackgroundResource(R.drawable.sidebar_icon_pointer);
		} else {
			bg.setBackgroundResource(R.drawable.sidebar_icon_pointer_white);
		}

		bg.clearAnimation();
		bg.startAnimation(AnimateUtils.fadeInOutAnimation(bg, 0, 1, 300));
	}

	public void deselectSideBarItem(RelativeLayout itemLayout) {

		RelativeLayout btn_live = (RelativeLayout) findViewById(R.id.Sidebar_btn_live);
		RelativeLayout btn_program = (RelativeLayout) findViewById(R.id.Sidebar_btn_program);
		RelativeLayout btn_program2 = (RelativeLayout) findViewById(R.id.Sidebar_btn_program2);
		RelativeLayout btn_setting = (RelativeLayout) findViewById(R.id.Sidebar_btn_setting);

		final ImageView bg = (ImageView) itemLayout.findViewWithTag("bg");
		final TextView title = (TextView) itemLayout.findViewWithTag("title");
		final ImageButton btn = (ImageButton) itemLayout.findViewWithTag("btn");

		if (itemLayout.getTag().equals("big")) {
			title.setTextColor(getResources().getColor(
					R.color.sidebar_icon_title_color));
		}

		if (itemLayout == btn_live) {
			btn.setImageResource(R.drawable.sidebar_icon_live);
		}

		if (itemLayout == btn_program) {
			btn.setImageResource(R.drawable.sidebar_icon_channel);
		}

		//TODO
		if (itemLayout == btn_program2) {
			btn.setImageResource(R.drawable.sidebar_icon_channel);
		}
		
		
		if (itemLayout == btn_setting) {
			btn.setImageResource(R.drawable.sidebar_icon_setting);
		}

		if (itemLayout.getTag().equals("big")) {
			bg.setBackgroundResource(R.drawable.sidebar_icon_pointer);
		} else {
			bg.setBackgroundResource(R.drawable.sidebar_icon_pointer_white);
		}

		if (bg.getAlpha() != 0.0f) {
			bg.clearAnimation();
			bg.startAnimation(AnimateUtils.fadeInOutAnimation(bg, 1, 0, 300));
		}

	}

	private void showtAd(){
//		Intent intents = new Intent();
//		Bundle bundle = new Bundle();
		//AppConstants.setSlotAppName("com.pccw.nowplayereyeapp");
	//	AppConstants.initUrls();
		Log.d(TAG, "ad url:"+ AppConfigInfo.getAdSplashURL());
//		bundle.putString("url", AppConstants.URL_SPLASH_AD);
//		intents.putExtras(bundle);
//		intents.setClass(this, AdWebViewActivity.class);
//		startActivity(intents);
		AdSplashDialog.dismissSplashDialog();//for the dialog leak windows exception
		AdSplashDialog.showSplashDialog(MainTabletActivity.this, AppConfigInfo.getAdSplashURL());
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		Fragment selectedFragment = mFragmentSwitcher.getCurrentFragment();
		if(selectedFragment == null) {
			// no fragment
			super.onBackPressed();
		} else {
			if(selectedFragment instanceof BaseFragment) {
				if (((BaseFragment) selectedFragment).onHostActivityBackPressed() == false) {
//					showDialog(DIALOG_QUIP_APP_CONFIRM);
					super.onBackPressed();
				} else {
					// do nothing. fragment handled onBackPressed().
					System.out.println("Fragment handled onBackPressed().");
					((BaseFragment)selectedFragment).onHostActivityBackPressed();
				}
			} else {
				super.onBackPressed();
			}
		}
	}
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id){
		case DIALOG_QUIP_APP_CONFIRM:
			AlertDialog ad = (AlertDialog)dialog;
			ad = (AlertDialog)dialog;
			ad.setMessage(getString(R.string.quit_app_confirm));
			// 1, BUTTON_POSITIVE; 2, BUTTON_NEGATIVE; 3, BUTTON_NEUTRAL
			ad.getButton(DialogInterface.BUTTON_POSITIVE).setText(getString(R.string.close));
			ad.getButton(DialogInterface.BUTTON_NEGATIVE).setText(getString(R.string.cancel));
			break;
		}
		super.onPrepareDialog(id, dialog);
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id){
		case DIALOG_QUIP_APP_CONFIRM:
			return new AlertDialog.Builder(this)
			.setMessage(getString(R.string.quit_app_confirm))
			.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.setNegativeButton(getString(R.string.cancel), null)
			.setCancelable(false)
			.create();
		}
		return null;
	}

}
