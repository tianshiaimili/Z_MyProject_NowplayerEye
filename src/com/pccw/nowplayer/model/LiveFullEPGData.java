package com.pccw.nowplayer.model;

import java.util.ArrayList;

import com.pccw.nmal.model.LiveCatalog.LiveCatalogChannelData;
import com.pccw.nmal.model.LiveDetail.LiveDetailData;

public class LiveFullEPGData {

	private LiveCatalogChannelData channel;
	private ArrayList<LiveDetailData> fullEPG;

	public ArrayList<LiveDetailData> getFullEPG() {
		return fullEPG;
	}

	public void setFullEPG(ArrayList<LiveDetailData> fullEPG) {
		this.fullEPG = fullEPG;
	}
	
	public boolean isFullEPGSection() {
		// if have channel data don't have epg data, epgfull data will be null.
		return channel == null;
	}

	public LiveCatalogChannelData getChannel() {
		return channel;
	}

	public void setChannel(LiveCatalogChannelData channel) {
		this.channel = channel;
	}

}
