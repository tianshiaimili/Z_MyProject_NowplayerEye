package com.pccw.nowplayer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pccw.nowplayer.app.BaseFragment;
import com.pccw.nowplayereyeapp.R;

public class MovieFragment extends BaseFragment {


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		// View view = inflater.inflate(R.layout.setting_tablet, container,false);
		View view = createOrRetrieveFragmentView(inflater, container, R.layout.empty);
		
		return view;		
	
	}
	
}
