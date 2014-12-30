package com.pccw.nowplayer;

import java.util.LinkedHashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.pccw.nmal.appdata.B2BApiAppInfo;
import com.pccw.nmal.appdata.JsonZip;
import com.pccw.nmal.appdata.JsonZip.ZipType;
import com.pccw.nmal.model.LiveCatalog;
import com.pccw.nmal.model.LiveChannel;
import com.pccw.nmal.model.LiveDetail;
import com.pccw.nmal.model.LiveDetail.LiveDetailChannel;
import com.pccw.nmal.model.VOD;
import com.pccw.nmal.util.LanguageHelper;
import com.pccw.nowplayer.utils.DeviceUtil;


public class AppDataLoader {
	private static final String TAG = AppDataLoader.class.getSimpleName();
	AppDataListener mAppDataListener;
	Context mContext; 
	
	public AppDataLoader(Context context) {
		mContext = context;
	}
	
	// channel
	public static final String GET_LIVE_CATALOG = "liveCatalog.json";
	public static final String GET_LIVE_DETAIL = "liveDetail.json";
	// vod
	public static final String GET_VOD_DETAIL = "vodDetail.json";
	public static final String GET_VOD_CATALOG = "vodCatalog.json";
	
    public interface AppDataListener {
    	void onDataLoaded();
    	void onJsonZipFailed();
    }
    
    public void setOnDataLoadedListener(AppDataListener l) {
    	mAppDataListener = l;
    }
    
	boolean hasPKG = false;
	boolean hasEPG = false;
	
	public void downloadJsonZip(AppDataListener l){
		//LanguageHelper.setCurrentLanguage("en");
		Log.w(TAG, "Begin downloadJsonZip.");
		mAppDataListener = l;
		getJsonZip(ZipType.PKG);
	}
	
	private void getJsonZip(ZipType zipType) {
		if(mContext == null) {
			return;
		}
		
		//check if version updated
		String currentVersion = DeviceUtil.getAppVersion(mContext);
		final SharedPreferences settings;
		settings = mContext.getSharedPreferences("currentVersion", Context.MODE_PRIVATE);
		String previousVersion = settings.getString("currentVersion", "");
		if (!previousVersion.equals(currentVersion)){
			Editor editor = settings.edit();
			editor.putString("currentVersion", currentVersion);
			editor.commit();
		}
		Log.d("testing", "testing currentVersion = " + currentVersion);
		Log.d("testing", "testing previousVersion = " + previousVersion);
		
		//end of checking

		
		JsonZip jsonZip = new JsonZip(mContext, B2BApiAppInfo.getJsonVersionPath(),Constants.JSON_ZIP_VERSION_PREFIX,
				LanguageHelper.getCurrentLanguage());
		// Log.d(TAG, "getJsonZip " + jsonZip + ", " + mContext + ", " + zipType);

		if (!previousVersion.equals(currentVersion)){
			jsonZip.clearAppCache(mContext);
		}
		
		if (jsonZip.shouldUpdateJSONZipVersion(zipType)) {
			jsonZip.startDownload(zipType, new JsonZip.Callback() {
				@Override
				public void updateProgress(int precent) {
				}
				@Override
				public void onDownloadCompleted(ZipType zipType, boolean isOK) {
					if(isOK) {
						parseData(zipType);
					} else {
						Log.e(TAG, "Download Zip " + zipType.toString() + " FAILED !!");
						mAppDataListener.onJsonZipFailed();
					}
				}
			});
		} else {
			parseData(zipType);
			Log.d(TAG, "Jsonzip up to date, download skipped");
		}
	}
	
	private void parseData(ZipType zipType) {
			parseAllChannel();
			parseEpg();
			parseChannelGenre();
			hasEPG = true;
			Log.w("EPGData", "channel list loaded = " + LiveChannel.getInstance().isLiveChannelListLoaded());
			parseVodcatalog();
			parseVodDetail();
			hasPKG = true;
			checkDownloadFinish();
	}
	
	private void checkDownloadFinish(){
		if(hasEPG && hasPKG){
			Log.i(TAG, "Json DownloadFinish !!");
			if(mAppDataListener != null)
				mAppDataListener.onDataLoaded();
		}
	}
	
/*	
	private void parseAllChannel() {
		Log.e(TAG, "parseAllChannel" + LanguageHelper.getCurrentLanguage());
		JsonZip jsonZip = new JsonZip(mContext, AppInfo.getCatalogueDomain(), Constants.JSON_ZIP_VERSION_PREFIX, LanguageHelper.getCurrentLanguage());
		LiveChannel.getInstance().clearChannelList();
		String getAllChannelJson = jsonZip.getJSONData(ZipType.PKG, LanguageHelper.getCurrentLanguage() + "/getAllChannels.json");
		LiveChannel.getInstance().parseLiveChannelJSON(getAllChannelJson);  // Parse
		
		// Print all channels.
//		Log.d(TAG, "getAllChannel: " + getAllChannelJson);
//		String channelList = "";
//		for (LiveChannelData lcd : LiveChannel.getInstance().getLiveChannelList(true)) {
//			channelList += lcd.getId() + lcd.getName() + "\n";
//		}
	}
*/
	
	
	
	private void parseEpg() {
		Log.e(TAG, "parseEpg " + LanguageHelper.getCurrentLanguage());
		JsonZip jsonZip = new JsonZip(mContext, B2BApiAppInfo.getJsonVersionPath(), Constants.JSON_ZIP_VERSION_PREFIX, LanguageHelper.getCurrentLanguage());
		LiveDetail.getInstance().clearLiveDetailChannelData();
		String getLiveDetailJson = jsonZip.getJSONData(ZipType.PKG, GET_LIVE_DETAIL);
		LiveDetail.getInstance().parseLiveDetailJSON(getLiveDetailJson);  // Parse
		
		//print epg
		LinkedHashMap<String, LiveDetailChannel> channelList = LiveDetail.getInstance().getLiveDetailChannelList();
		for (LiveDetailChannel liveDetail : channelList.values()){
			Log.d(TAG, "liveDetail.getChannelId()=" + liveDetail.getChannelId());
		}
		
		
	}
	
	private void parseAllChannel() {
		Log.e(TAG, "parseAllChannel" + LanguageHelper.getCurrentLanguage());
		JsonZip jsonZip = new JsonZip(mContext, B2BApiAppInfo.getJsonVersionPath(), Constants.JSON_ZIP_VERSION_PREFIX, LanguageHelper.getCurrentLanguage());
		LiveCatalog.getInstance().clearLiveChannelGenreList();
		String getAllChannelJson = jsonZip.getJSONData(ZipType.PKG, GET_LIVE_CATALOG);
		boolean success = LiveCatalog.getInstance().parseLiveCatalogJSON(getAllChannelJson);  // Parse
		Log.d(TAG, "parseAllChannel success == " + success);
		
		// Print all channels.
//		Log.d(TAG, "getAllChannel: " + getAllChannelJson);
//		String channelList = "";
//		for (LiveChannelData lcd : LiveChannel.getInstance().getLiveChannelList(true)) {
//			channelList += lcd.getId() + lcd.getName() + "\n";
//		}
	}
	
	private void parseVodcatalog() {
		Log.e(TAG, "parseVodcatalog" + LanguageHelper.getCurrentLanguage());
		JsonZip jsonZip = new JsonZip(mContext, B2BApiAppInfo.getJsonVersionPath(), Constants.JSON_ZIP_VERSION_PREFIX, LanguageHelper.getCurrentLanguage());
		String json = jsonZip.getJSONData(ZipType.PKG, GET_VOD_CATALOG);
		Log.d(TAG, json);
		boolean success = VOD.getInstance().parseVODCatergories(json);  // Parse
		Log.d(TAG, "parseVodcatalog success == " + success);
		
	}
	
	private void parseVodDetail(){
		Log.e(TAG, "parseVodDetail" + LanguageHelper.getCurrentLanguage());
		JsonZip jsonZip = new JsonZip(mContext, B2BApiAppInfo.getJsonVersionPath(), Constants.JSON_ZIP_VERSION_PREFIX, LanguageHelper.getCurrentLanguage());
		String json = jsonZip.getJSONData(ZipType.PKG, GET_VOD_DETAIL);
		Log.d(TAG, json);
		boolean success = VOD.getInstance().parseVODDetails(json);  // Parse
		Log.d(TAG, "parseVodDetail success == " + success);

	}
	
	
	/**
	 * Parse the Live Channel Genre JSON. Note: this should be call after the Live Channel list is parsed, 
	 * otherwise, the list returned by LiveChannel.LiveChannelGenre.getChannelList() will not be populated.
	 */
	private void parseChannelGenre() {
		Log.e(TAG, "parseChannelGenre " + LanguageHelper.getCurrentLanguage());
		if (LiveChannel.getInstance().isLiveChannelListLoaded()) {
			JsonZip jsonZip = new JsonZip(mContext, B2BApiAppInfo.getJsonVersionPath(), Constants.JSON_ZIP_VERSION_PREFIX, LanguageHelper.getCurrentLanguage());
			String liveDetailJson = jsonZip.getJSONData(ZipType.PKG, "/" + GET_LIVE_DETAIL);
			boolean success = LiveDetail.getInstance().parseLiveDetailJSON(liveDetailJson );   // Parse
			Log.d(TAG, "parseChannelGenre success == " + success);
		} 
	}

}
