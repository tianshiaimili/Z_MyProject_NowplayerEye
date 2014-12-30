package com.pccw.nowplayer.adapter;

import java.util.ArrayList;
import java.util.Random;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.pccw.nmal.model.VOD;
import com.pccw.nmal.model.VOD.VODCategoryNodeData;
import com.pccw.nowplayer.fragment.ProgramFragment;
import com.pccw.nowplayer.res.ImageCachePath;
import com.pccw.nowplayer.utils.ImageCacheParams;
import com.pccw.nowplayer.utils.NowplayerDialogUtils;
import com.pccw.nowplayer.utils.RemoteSingleImageLoader;
import com.pccw.nowplayer.utils.TradImageCacheHelper;
import com.pccw.nowplayereyeapp.R;

public class ProgramAllAdapter extends BaseAdapter {
	private Context mContext;
	private ProgramFragment mFragment;
	private ArrayList<String> datalist;
	private LayoutInflater inflater;
	private GridView gridView;
	private RemoteSingleImageLoader loader;
	
	
	public ProgramAllAdapter(Context context, ArrayList<String> datalist, GridView gridView, ProgramFragment fragment) {
		mContext = context;
		mFragment = fragment;
		this.datalist = datalist;
		inflater = LayoutInflater.from(context);
		this.gridView = gridView;
		
		ImageCacheParams params = new ImageCacheParams(context, ImageCachePath.PROGRAM_ALL);
		TradImageCacheHelper cacheHelper = new TradImageCacheHelper(params);
		loader = new RemoteSingleImageLoader(mContext, cacheHelper);
		loader.setLoadingImage(mContext.getResources(), R.drawable.empty_program_c);
	}

	@Override
	public int getCount() {
		if(datalist == null){
			return 0;
		}
		return datalist.size();
	}

	@Override
	public String getItem(int position) {
		if(datalist != null){
			return datalist.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d("testing", "my testing : posistion = " + position);
		ViewHolder holder = null;
		if(convertView != null){
			holder = (ViewHolder) convertView.getTag();
		} else {
			holder = new ViewHolder();
			
			convertView = inflater.inflate(R.layout.program_item, null);
			holder.emptyImageView = (ImageView) convertView.findViewById(R.id.program_item_empty);
			holder.icon = (ImageView) convertView.findViewById(R.id.program_item_image);
			holder.title = (TextView) convertView.findViewById(R.id.program_item_title);
			convertView.setTag(holder);
		}

		final int click_position = position;
		final VODCategoryNodeData catagoryData = VOD.getInstance().getVODCategoryByNodeId(datalist.get(position));
		holder.title.setText(catagoryData.getName());
		holder.icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog dialog = NowplayerDialogUtils.createProgramDetailDialog(mContext, catagoryData, loader, mFragment);
				dialog.show();
				Log.d("testing", "my testing : clicked position = " + click_position);
			}
		});
		
		if (catagoryData.getHdImg1Path()!=null && !"".equals(catagoryData.getHdImg1Path())){
			loader.setRemoteImage(holder.icon, catagoryData.getHdImg1Path());
		} else {
			holder.icon.setImageResource(R.drawable._program_thumb_nail);
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
