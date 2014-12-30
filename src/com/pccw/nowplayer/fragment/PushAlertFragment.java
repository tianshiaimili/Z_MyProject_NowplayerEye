package com.pccw.nowplayer.fragment;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.pccw.nowplayer.app.AppLocaleAide;
import com.pccw.nowplayer.app.BaseFragment;
import com.pccw.nowplayereyeapp.R;
import com.pccw.nowplayereyeapp.notification.UserSetting;

public class PushAlertFragment extends BaseFragment implements OnCheckedChangeListener {

	private Switch push_alert_cbox;
	private SharedPreferences prefs;
	private int notificationState;
	private static final int NOTIFICATION_ON = 1;
	private static final int NOTIFICATION_OFF = 0;
	private static final String NOTIFICATION_KEY = "notification";
	public static final String LANGUAGE_KEY = "language";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 super.onCreateView(inflater, container, savedInstanceState);
		 View view = inflater.inflate(R.layout.setting_push_tablet, container,false);
			
			return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		push_alert_cbox = (Switch)findViewById(R.id.setting_push_alert_checkbox);
		initPushAlert();
		push_alert_cbox.setOnCheckedChangeListener(this);
		
	}
	
	private void initPushAlert() {
		notificationState = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(NOTIFICATION_KEY, NOTIFICATION_ON);
		switch (notificationState) {
		case NOTIFICATION_ON:
			push_alert_cbox.setChecked(true);
			break;
		case NOTIFICATION_OFF:
			push_alert_cbox.setChecked(false);
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	
		
		switch (buttonView.getId()) {
		case R.id.setting_push_alert_checkbox:
			if(!isChecked){
				((Switch)findViewById(R.id.setting_push_alert_checkbox)).setChecked(false);
				Editor editor = prefs.edit();
				editor.putInt(NOTIFICATION_KEY, NOTIFICATION_OFF);
				editor.commit();
				//Toast.makeText(getActivity(), "Push Alert Off", Toast.LENGTH_SHORT).show();
			}else{
				((Switch)findViewById(R.id.setting_push_alert_checkbox)).setChecked(true);
				Editor editor = prefs.edit();
				editor.putInt(NOTIFICATION_KEY, NOTIFICATION_ON);
				editor.commit();
				//Toast.makeText(getActivity(), "Push Alert On", Toast.LENGTH_SHORT).show();
			}
		    
			break;

		default:
			break;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	    saveToService();
	}
	private void saveToService() {
		UserSetting us= new com.pccw.nowplayereyeapp.notification.UserSetting(getActivity()) ;
//		String deviceId=us.getDeviceId();
//		String pushToken=us.getPushToken();
//		String deviceType=us.getDeviceType();
		
		boolean notificationChecked = ((Switch)findViewById(R.id.setting_push_alert_checkbox)).isChecked();
		if(notificationChecked){
			us.setPushAlertOn(true);
		} else {
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
