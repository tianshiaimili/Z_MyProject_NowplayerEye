package com.pccw.nowplayer.adapter;

import java.util.List;
import java.util.Random;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pccw.nowplayer.res.ImageCachePath;
import com.pccw.nowplayer.utils.ImageCacheParams;
import com.pccw.nowplayer.utils.NowplayerDialogUtils;
import com.pccw.nowplayer.utils.RemoteGroupImageLoader;
import com.pccw.nowplayer.utils.TradImageCacheHelper;
import com.pccw.nowplayereyeapp.R;

import custom.widget.adapterview.HorizontalListView;

public class programHorizontalAdapter extends BaseAdapter{
	private Context mContext;
	private List<String> mData;
	private LayoutInflater inflater;
	private HorizontalListView listview;
	private RemoteGroupImageLoader loader;
	
	public programHorizontalAdapter(Context context, List<String> data, HorizontalListView mListview) {
		this.mData = data;
		this.listview = mListview;
		this.mContext = context;
		inflater = LayoutInflater.from(context);
		
		ImageCacheParams params = new ImageCacheParams(context, ImageCachePath.PROGRAM_SESSION);
		TradImageCacheHelper cacheHelper = new TradImageCacheHelper(params);
		loader = new RemoteGroupImageLoader(cacheHelper, listview);
	}

	@Override
	public int getCount() {
		if(mData != null){
			return mData.size();
		}
		return 0;
	}

	@Override
	public String getItem(int position) {
		if(mData != null){
			return mData.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView != null){
			holder = (ViewHolder) convertView.getTag();
		} else {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.program_item, parent, false);
			holder.emptyImageView = (ImageView) convertView.findViewById(R.id.program_item_empty);
			holder.icon = (ImageView) convertView.findViewById(R.id.program_item_image);
			holder.title = (TextView) convertView.findViewById(R.id.program_item_title);
			
			holder.title.setText("白領公寓");
//			holder.icon.setImageResource(R.drawable._program_thumb_nail);
			
			holder.icon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//NowplayerDialogUtils.createProgramDetailDialog(mContext).show();
				}
			});
			
			loader.setLoadingImage(mContext.getResources(), R.drawable.empty_program_c);
			loader.setRemoteImage(holder.icon, getImg(position), position);
			
			convertView.setTag(holder);
		}
		
		return convertView;
	}
	
	class ViewHolder{
		ImageView emptyImageView;
		ImageView icon;
		TextView title;
	}
	
	private String getImg(int position) {
		String url = "http://10.37.131.68:8080/logo170/";
		Random ran = new Random();
		int ranInt = ran.nextInt(395);
		ranInt = position;
		url += ranInt + ".jpg";
		return url;
	}
	
}
