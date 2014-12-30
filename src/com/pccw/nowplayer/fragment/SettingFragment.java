package com.pccw.nowplayer.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pccw.common.notification.NotificationServiceSetting;
import com.pccw.nmal.appdata.B2BApiAppInfo;
import com.pccw.nowplayer.MainTabletActivity;
import com.pccw.nowplayer.app.BaseFragment;
import com.pccw.nowplayer.app.FragmentUtils.FragmentFeed;
import com.pccw.nowplayer.app.FragmentUtils.FragmentSwitcher;
import com.pccw.nowplayereyeapp.R;

public class SettingFragment extends BaseFragment implements OnClickListener{
	
	private boolean hasLogin=false;
	private ImageView setting_nowid_imageView;
	private ImageView setting_push_alert_imageView;
	private ImageView setting_language_imageView;
	private ImageView setting_service_notice_imageView;
	private Button setting_push_alert_button;
	private Button setting_nowid_button;
	private Button setting_language_button;
	private Button setting_service_notice_button;
	private TextView setting_title_tv;
	private Button setting_related_app_button;
	private ImageView setting_related_app_imgV;
	
	private FragmentSwitcher mFragmentSwitcher = new FragmentSwitcher(R.id.setting_right_container, new FragmentFeed() {
		@Override
		public Fragment newFragment(String tag) {
			if(tag.equals(NowIdFragment.class.getSimpleName())) {
				return new NowIdFragment();
			} else if(tag.equals(NowIdLoginFragment.class.getSimpleName())) {
				return new NowIdLoginFragment();
			} else if(tag.equals(PushAlertFragment.class.getSimpleName())) {
				return new PushAlertFragment();
			} else if(tag.equals(LangChangeFragment.class.getSimpleName())) {
				return new LangChangeFragment();
			} else if(tag.equals(ServiceNoticeFragment.class.getSimpleName())) {
				return new ServiceNoticeFragment();
			}else if(tag.equals(RelatedAPPFragment.class.getSimpleName())) {
				return new RelatedAPPFragment();
			}  
			
			return null;
		}
	});
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		// View view = inflater.inflate(R.layout.setting_tablet, container,false);
		View view = createOrRetrieveFragmentView(inflater, container, R.layout.setting_tablet);
		
		findView();
		
		return view;		
	
	}
	
	private void findView() {
		setting_title_tv= (TextView) ((MainTabletActivity)getActivity()).findViewById(R.id.setting_left_title);
		
		
		setting_nowid_imageView  =(ImageView) ((MainTabletActivity)getActivity()).findViewById(R.id.setting_nowid_imageView);
		setting_push_alert_imageView=(ImageView) ((MainTabletActivity)getActivity()).findViewById(R.id.setting_push_alert_imageView);
		setting_language_imageView=(ImageView) ((MainTabletActivity)getActivity()).findViewById(R.id.setting_language_imageView);
		setting_service_notice_imageView=(ImageView) ((MainTabletActivity)getActivity()).findViewById(R.id.setting_service_notice_imageView);
		
		setting_nowid_button= (Button) ((MainTabletActivity)getActivity()).findViewById(R.id.setting_nowid_button);
		setting_push_alert_button=(Button) ((MainTabletActivity)getActivity()).findViewById(R.id.setting_push_alert_button);
		setting_language_button=(Button) ((MainTabletActivity)getActivity()).findViewById(R.id.setting_language_button);
		setting_service_notice_button=(Button) ((MainTabletActivity)getActivity()).findViewById(R.id.setting_service_notice_button);
		
		
		setting_related_app_button= (Button) ((MainTabletActivity)getActivity()).findViewById(R.id.setting_related_app_button);
		setting_related_app_imgV= (ImageView) ((MainTabletActivity)getActivity()).findViewById(R.id.setting_related_app_imageView);
		
		
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		findView();
		initView();
		setOnClickListener();
		if(!super.isFragmentRecreatedWithHoldView())
			setting_language_button.performClick();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		Log.d(getTag(), "onConfigChange()");

		NotificationServiceSetting.setContentTitle(getString(R.string.app_name));
		NotificationServiceSetting.setCancelBtnString(getString(R.string.notification_cancel));
		NotificationServiceSetting.setViewBtnText(getString(R.string.notification_view));
	}
	@Override
	public void onLocaleChanged() {
		super.onLocaleChanged();
		
		initView();
	}
	
	private void initView() {
		
		setting_title_tv.setText(getResources().getString(R.string.setting_title));
		
		setting_nowid_button.setText(getResources().getString(R.string.setting_now_id));
		setting_push_alert_button.setText(getResources().getString(R.string.setting_push_alert));
		setting_language_button.setText(getResources().getString(R.string.setting_language));
		setting_service_notice_button.setText(getResources().getString(R.string.setting_service_notice));
		
		setting_related_app_button.setText(getResources().getString(R.string.setting_related_app));
		if(B2BApiAppInfo.getRelatedAppURL()!=null){
			((MainTabletActivity)getActivity()).findViewById(R.id.setting_related_app_lo).setVisibility(View.VISIBLE);
		}
	}


	private void setOnClickListener() {
		
		setting_nowid_button.setOnClickListener(this);
		setting_push_alert_button.setOnClickListener(this);
		setting_language_button.setOnClickListener(this);
		setting_service_notice_button.setOnClickListener(this);
		setting_related_app_button.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.setting_nowid_button:
			setSelectedView(R.id.setting_nowid_button);
			setting_nowid_imageView.setImageResource(R.drawable.setting_disclosurearrow_active);

			if(userLogined()){
				hasLogin=false;
				mFragmentSwitcher.switchFragment(getActivity(), NowIdFragment.class.getSimpleName());
			}else{
				hasLogin=true;
				mFragmentSwitcher.switchFragment(getActivity(), NowIdLoginFragment.class.getSimpleName());
			}
			break;
		case R.id.setting_push_alert_button:
			setSelectedView(R.id.setting_push_alert_button);
			setting_push_alert_imageView.setImageResource(R.drawable.setting_disclosurearrow_active);
			mFragmentSwitcher.switchFragment(getActivity(), PushAlertFragment.class.getSimpleName());
			setting_push_alert_button.setOnClickListener(null);
			setting_language_button.setOnClickListener(this);
			setting_service_notice_button.setOnClickListener(this);
			setting_related_app_button.setOnClickListener(this);
			break;
		case R.id.setting_language_button:
			setSelectedView(R.id.setting_language_button);
			setting_language_imageView.setImageResource(R.drawable.setting_disclosurearrow_active);
			
			mFragmentSwitcher.switchFragment(getActivity(), LangChangeFragment.class.getSimpleName());
			
			setting_push_alert_button.setOnClickListener(this);
			setting_language_button.setOnClickListener(null);
			setting_service_notice_button.setOnClickListener(this);
			setting_related_app_button.setOnClickListener(this);
			break;
		case R.id.setting_service_notice_button:
			setSelectedView(R.id.setting_service_notice_button);
			setting_service_notice_imageView.setImageResource(R.drawable.setting_disclosurearrow_active);
			
			mFragmentSwitcher.switchFragment(getActivity(), ServiceNoticeFragment.class.getSimpleName());
			
			setting_push_alert_button.setOnClickListener(this);
			setting_language_button.setOnClickListener(this);
			setting_service_notice_button.setOnClickListener(null);
			setting_related_app_button.setOnClickListener(this);
			break;
		case R.id.setting_related_app_button:
			setSelectedView(R.id.setting_related_app_button);
			setting_related_app_imgV.setImageResource(R.drawable.setting_disclosurearrow_active);
			
			mFragmentSwitcher.switchFragment(getActivity(), RelatedAPPFragment.class.getSimpleName());
			
			setting_push_alert_button.setOnClickListener(this);
			setting_language_button.setOnClickListener(this);
			setting_service_notice_button.setOnClickListener(this);
			setting_related_app_button.setOnClickListener(null);
			break;

		default:
			break;
		}
		
		
	}

	
	private boolean userLogined() {
		return hasLogin;
	}

	private void setSelectedView(int currentSelectID){
		
		setting_nowid_button.setBackgroundResource(0);
		setting_nowid_button.setTextColor(getResources().getColor(R.color.setting_leftpanel_btn_textcolor));
		setting_push_alert_button.setBackgroundResource(0);
		setting_push_alert_button.setTextColor(getResources().getColor(R.color.setting_leftpanel_btn_textcolor));
		setting_language_button.setBackgroundResource(0);
		setting_language_button.setTextColor(getResources().getColor(R.color.setting_leftpanel_btn_textcolor));
		setting_service_notice_button.setBackgroundResource(0);
		setting_service_notice_button.setTextColor(getResources().getColor(R.color.setting_leftpanel_btn_textcolor));
		setting_related_app_button.setBackgroundResource(0);
		setting_related_app_button.setTextColor(getResources().getColor(R.color.setting_leftpanel_btn_textcolor));
		
		setting_nowid_imageView.setImageResource(R.drawable.setting_disclosurearrow);
		setting_push_alert_imageView.setImageResource(R.drawable.setting_disclosurearrow);
		setting_language_imageView.setImageResource(R.drawable.setting_disclosurearrow);
		setting_service_notice_imageView.setImageResource(R.drawable.setting_disclosurearrow);
		setting_related_app_imgV.setImageResource(R.drawable.setting_disclosurearrow);
		
		findViewById(currentSelectID).setBackgroundResource(R.color.setting_leftpanel_btn_bg_active);
		((Button) findViewById(currentSelectID)).setTextColor(getResources().getColor(R.color.setting_leftpanel_btn_textcolor_active));
		
	}
	

}
