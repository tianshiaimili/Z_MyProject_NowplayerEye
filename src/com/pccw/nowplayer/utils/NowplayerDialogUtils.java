package com.pccw.nowplayer.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pccw.nmal.model.LiveCatalog.LiveCatalogChannelData;
import com.pccw.nmal.model.LiveDetail.LiveDetailData;
import com.pccw.nmal.model.VOD;
import com.pccw.nmal.model.VOD.VODData;
import com.pccw.nmal.util.LanguageHelper;
import com.pccw.nowplayer.fragment.ProgramFragment;
import com.pccw.nowplayer.fragment.Multicast;
import com.pccw.nowplayereyeapp.R;

public class NowplayerDialogUtils {

	
	public static Dialog createProgramDetailDialog(final Context context, VOD.VODCategoryNodeData catalog, RemoteSingleImageLoader imageLoader, final ProgramFragment fragment) {
		final Dialog dialog=new Dialog(context, R.style.dialog_noTitle);	
		final LinearLayout mainContentView=(LinearLayout)LinearLayout.inflate(context, R.layout.programdetail_dialog, null);
		if (catalog!=null){
			ImageView img = (ImageView) mainContentView.findViewById(R.id.thumbnail);
			imageLoader.setRemoteImage(img, catalog.getHdImg1Path());
			
			if (catalog.getCategoryImagePath()!=null && !"null".equals(catalog.getCategoryImagePath())){
				ImageView logoImageView = (ImageView) mainContentView.findViewById(R.id.channel_logo);
				imageLoader.setRemoteImage(logoImageView, catalog.getCategoryImagePath());
			}
			
			((TextView) mainContentView.findViewById(R.id.current_ch_title)).setText(catalog.getName());
			if (catalog.getSynopsis()!=null && !"null".equals(catalog.getSynopsis())){
				((TextView) mainContentView.findViewById(R.id.program_detail_synopsis)).setText(catalog.getSynopsis());
			} else{
				((TextView) mainContentView.findViewById(R.id.program_detail_synopsis)).setText("");
			}
			
			
			ArrayList<VODData> productList = new ArrayList<VODData> (VOD.getInstance().getVODDataByNodeId(catalog.getNodeId()).values());
			
			VODData firstProductWithLang = null;
			for (VODData product : productList){
				if (product.getLanguages()!=null && !"".equals(product.getLanguages().trim())){
					firstProductWithLang = product;
					break;
				}
			}
			
			if (firstProductWithLang!=null && firstProductWithLang.getLanguages()!=null && !"".equals(firstProductWithLang.getLanguages())){
				((TextView) mainContentView.findViewById(R.id.programdetail_language)).setText(firstProductWithLang.getLanguages());
			} else{
				mainContentView.findViewById(R.id.programdetail_language_view).setVisibility(View.GONE);
			}
		}
		
		dialog.setContentView(mainContentView);
		fragment.setupProgramDialogDetail(dialog, catalog);
		return dialog;
	}

	public static Dialog createProgram2DetailDialog(final Context context, VOD.VODCategoryNodeData catalog, RemoteSingleImageLoader imageLoader, final Multicast fragment) {
		final Dialog dialog=new Dialog(context, R.style.dialog_noTitle);	
		final LinearLayout mainContentView=(LinearLayout)LinearLayout.inflate(context, R.layout.programdetail_dialog, null);
		if (catalog!=null){
			ImageView img = (ImageView) mainContentView.findViewById(R.id.thumbnail);
			imageLoader.setRemoteImage(img, catalog.getHdImg1Path());
			
			if (catalog.getCategoryImagePath()!=null && !"null".equals(catalog.getCategoryImagePath())){
				ImageView logoImageView = (ImageView) mainContentView.findViewById(R.id.channel_logo);
				imageLoader.setRemoteImage(logoImageView, catalog.getCategoryImagePath());
			}
			
			((TextView) mainContentView.findViewById(R.id.current_ch_title)).setText(catalog.getName());
			if (catalog.getSynopsis()!=null && !"null".equals(catalog.getSynopsis())){
				((TextView) mainContentView.findViewById(R.id.program_detail_synopsis)).setText(catalog.getSynopsis());
			} else{
				((TextView) mainContentView.findViewById(R.id.program_detail_synopsis)).setText("");
			}
			
			
			ArrayList<VODData> productList = new ArrayList<VODData> (VOD.getInstance().getVODDataByNodeId(catalog.getNodeId()).values());
			
			VODData firstProductWithLang = null;
			for (VODData product : productList){
				if (product.getLanguages()!=null && !"".equals(product.getLanguages().trim())){
					firstProductWithLang = product;
					break;
				}
			}
			
			if (firstProductWithLang!=null && firstProductWithLang.getLanguages()!=null && !"".equals(firstProductWithLang.getLanguages())){
				((TextView) mainContentView.findViewById(R.id.programdetail_language)).setText(firstProductWithLang.getLanguages());
			} else{
				mainContentView.findViewById(R.id.programdetail_language_view).setVisibility(View.GONE);
			}
		}
		
		dialog.setContentView(mainContentView);
		fragment.setupProgramDialogDetail(dialog, catalog);
		return dialog;
	}
	
	
	public static Dialog createLiveDetailDialog(final Context context, LiveCatalogChannelData channelData, LiveDetailData program, RemoteSingleImageLoader imageLoader) {
		final Dialog dialog=new Dialog(context, R.style.dialog_noTitle);	
		final LinearLayout mainContentView=(LinearLayout)LinearLayout.inflate(context, R.layout.livedetail_dialog, null);
		TextView title = (TextView) mainContentView.findViewById(R.id.livedetail_title);
		TextView channel = (TextView) mainContentView.findViewById(R.id.livedetail_channel);
		TextView time = (TextView) mainContentView.findViewById(R.id.livedetail_time);
		ImageView logo = (ImageView)mainContentView.findViewById(R.id.livedetail_logo);
		imageLoader.setRemoteImage(logo, channelData.getChannelLogoLink());
		title.setText(program.getName());
		channel.setText(channelData.getName());
		boolean isChinese = LanguageHelper.getCurrentLanguage().equals("zh");
		time.setText(getDateStr(program.getDate(), isChinese) + " " + program.getStartTime() + " - " + program.getEndTime());
		
		//mainContentView.getLayoutParams().height = (int)(274 * context.getResources().getDisplayMetrics().density);
		dialog.setContentView(mainContentView);
		return dialog;
	}
	
	
    /**
     * Note: Cost tens of ms.
     * @param yyyyMMdd
     * @param isChinese
     * @return 8月7日(週二) or 7 Aug(Fri)
     */
    public static String getDateStr(String yyyyMMdd, boolean isChinese) {
        Date date = DatetimeUtils.parseDateString(yyyyMMdd, "yyyyMMdd");
        String retval = null;
        if(isChinese) {
        	retval = DatetimeUtils.formatDate(date, "M月d日 (週几)", Locale.TRADITIONAL_CHINESE);
            return retval.replace('几', DatetimeUtils.getChineseDayInWeek(date));
        }
        return DatetimeUtils.formatDate(date, "d MMM (E)", Locale.US);
    }

    
    
    
}
