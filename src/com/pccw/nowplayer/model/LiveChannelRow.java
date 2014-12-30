package com.pccw.nowplayer.model;

import com.pccw.nmal.model.LiveCatalog.LiveCatalogChannelData;

public class LiveChannelRow {
	private String channelSectionName;
	private LiveCatalogChannelData data;

	public String getChannelSectionName() {
		return channelSectionName;
	}
	
	public void setChannelSectionName(String channelSectionName) {
		this.channelSectionName = channelSectionName;
	}
	
	public LiveCatalogChannelData getData() {
		return data;
	}

	public void setData(LiveCatalogChannelData data) {
		this.data = data;
	}

	public boolean isChannelSection() {
		return data == null;
	}
}
