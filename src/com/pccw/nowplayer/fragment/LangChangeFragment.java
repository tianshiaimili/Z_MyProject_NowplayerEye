package com.pccw.nowplayer.fragment;

import java.util.Locale;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pccw.android.ad.common.UserSettings;
import com.pccw.nmal.util.LanguageHelper;
import com.pccw.nowplayer.AppDataLoader;
import com.pccw.nowplayer.MainTabletActivity;
import com.pccw.nowplayer.app.AppLocaleAide;
import com.pccw.nowplayer.app.BaseFragment;
import com.pccw.nowplayer.app.BaseFragmentActivity;
import com.pccw.nowplayer.utils.ThreadPoolUtils;
import com.pccw.nowplayereyeapp.R;
import com.pccw.nowplayereyeapp.notification.UserSetting;

/**
 * Chnage the user Language Fragment
 * @author zero
 *
 */
public class LangChangeFragment extends BaseFragment implements OnClickListener {

	protected static final String TAG = LangChangeFragment.class.getSimpleName();
	private static final int NOTIFICATION_ON = 1;
	private static final int NOTIFICATION_OFF = 0;
	private static final String NOTIFICATION_KEY = "notification";
	public static final String LANGUAGE_KEY = "language";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 super.onCreateView(inflater, container, savedInstanceState);
		 View view = inflater.inflate(R.layout.setting_lang_tablet, container,false);
			
			return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
//		findViewById(R.id.setting_language_zh_iv).setOnClickListener(this);
//		findViewById(R.id.setting_language_en_iv).setOnClickListener(this);
		findViewById(R.id.setting_language_zh_tv).setOnClickListener(this);
		findViewById(R.id.setting_language_en_tv).setOnClickListener(this);
		findViewById(R.id.setting_language_zh_lo).setOnClickListener(this);
		findViewById(R.id.setting_language_en_lo).setOnClickListener(this);
		
		initView();
	}
	
	

	private void initView() {
		boolean isEng  = AppLocaleAide.isAppLocaleEn(getActivity());
		if(!isEng){
			initLangView("zh");
		}else{
			initLangView("en");
		}
		
	}

	@Override
	public void onClick(View v) {
		boolean isEng  = AppLocaleAide.isAppLocaleEn(getActivity());
		switch (v.getId()) {
		case R.id.setting_language_en_tv:
		case R.id.setting_language_zh_iv:
		case R.id.setting_language_en_lo:
			if(!isEng){
				changeLanguage("en");
				initLangView("en");
				changeOtherFragmentLang();
				reloadLiveEPGData();
				savePushToService();
			}
			break;
		case R.id.setting_language_zh_tv:
		case R.id.setting_language_en_iv:
		case R.id.setting_language_zh_lo:
			if(isEng){
				changeLanguage("zh");
				initLangView("zh");
				changeOtherFragmentLang();
				reloadLiveEPGData();
				savePushToService();
			}
			
			break;
		default:
			break;
		}
		
	}

	private void changeOtherFragmentLang() {
		MainTabletActivity activity = ((MainTabletActivity)getActivity());
		BaseFragment settingFrag = ((BaseFragment)activity.getSupportFragmentManager()
				.findFragmentByTag(SettingFragment.class.getSimpleName()));
		if(settingFrag!=null){
			settingFrag.onLocaleChanged();
		}
		
		((BaseFragmentActivity) getActivity()).onLocaleChanged();
	}

	private void changeLanguage(String languageCode) {
		
		if("zh".equalsIgnoreCase(languageCode)){
			AppLocaleAide.setAppLocale(getActivity(), AppLocaleAide.TRADITIONAL_CHINESE_HK, this);
		}else{
			AppLocaleAide.setAppLocale(getActivity(), AppLocaleAide.ENGLISH_US, this);
		}
		// Sync with nmal library's LanguageHelper
		LanguageHelper.setCurrentLanguage(languageCode);

        // Sync with AdEngine Lib
		syncLanguageWithAdLibrary();
	}
	
	public static String getLanguage() {
		Locale loc = Locale.getDefault();
		return loc.getLanguage();
	}
	
	/**
	 * initial the User set language
	 */
	public static void syncLanguageWithAdLibrary() {
		if (getLanguage() != null) {
			UserSettings.setLanguage(getLanguage().equalsIgnoreCase("en")? UserSettings.LANGUAGE_ENGLISH : UserSettings.LANGUAGE_CHINESE);
		}
	}

	private void initLangView(String languageCode) {
		
		if("zh".equalsIgnoreCase(languageCode)){
			findViewById(R.id.setting_language_zh_iv).setVisibility(View.VISIBLE);
			findViewById(R.id.setting_language_en_iv).setVisibility(View.GONE);
			
			((TextView)findViewById(R.id.setting_language_zh_tv)).setTextColor
			(getResources().getColor(R.color.setting_rightpanel_detail_color));
			((TextView)findViewById(R.id.setting_language_en_tv)).setTextColor
			(getResources().getColor(R.color.setting_rightpanel_content_color));
			
		}else{
			
			findViewById(R.id.setting_language_en_iv).setVisibility(View.VISIBLE);
			findViewById(R.id.setting_language_zh_iv).setVisibility(View.GONE);
			
			((TextView)findViewById(R.id.setting_language_en_tv)).setTextColor
			(getResources().getColor(R.color.setting_rightpanel_detail_color));
			((TextView)findViewById(R.id.setting_language_zh_tv)).setTextColor
			(getResources().getColor(R.color.setting_rightpanel_content_color));
			
		}
		((TextView)findViewById(R.id.setting_language_title)).setText(R.string.setting_language_title);
	}
	
	private void reloadLiveEPGData(){
		setProgressDialogMessage(getString(R.string.loading));
		showProgressDialog(false);
		ThreadPoolUtils.execute(new Runnable() {
			
			@Override
			public void run() {
				AppDataLoader epgDataLoader = new AppDataLoader(getApplicationContext());
				epgDataLoader.downloadJsonZip(new AppDataLoader.AppDataListener() {
					@Override
					public void onDataLoaded() {
						Log.w(TAG, "reloadLiveEPGData Finished!!");
						dismissLoadingInUiThread();
					}
					@Override
					public void onJsonZipFailed() {
						Log.w(TAG, "reloadLiveEPGData Failed!!");
						dismissLoadingInUiThread();
					}
				});				
			}
		});
	}

	private void dismissLoadingInUiThread(){
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				dismissProgressDialog();
			}
		});
	}
	
	private void savePushToService() {
		UserSetting us= new com.pccw.nowplayereyeapp.notification.UserSetting(getActivity()) ;
//		String deviceId=us.getDeviceId();
//		String pushToken=us.getPushToken();
//		String deviceType=us.getDeviceType();
		int notificationState = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(NOTIFICATION_KEY, NOTIFICATION_ON);
		if (notificationState==NOTIFICATION_ON) {
			us.setPushAlertOn(true);
		}else{
			us.setPushAlertOn(false);
		}
		if(AppLocaleAide.isAppLocaleZh(getActivity())){
			us.setLanguage("zh_tw");
		} else {
			us.setLanguage("en");
		}
		us.save();
	}
}
