package com.pccw.nowplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pccw.nowplayereyeapp.R;

public class CustomSingleChoiceDialogItemAdapter extends BaseAdapter {
	private String[] qualities;
	private Context mContext;
	private LayoutInflater inflater;

	public CustomSingleChoiceDialogItemAdapter(String[] qualities, Context mContext) {
		this.qualities = qualities;
		this.mContext = mContext;
		
		inflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		if(qualities != null){
			return qualities.length;
		}
		return 0;
	}

	@Override
	public String getItem(int position) {
		if(qualities != null){
			return qualities[position];
		}
		return "";
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = inflater.inflate(R.layout.custom_single_choice_dialog_item, null);
		TextView tv = (TextView) (convertView.findViewById(R.id.custom_single_choice_item));
		tv.setText(getItem(position));
		return convertView;
	}

}
