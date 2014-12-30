package com.pccw.nowplayer.utils;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap.CompressFormat;

/**
 * A holder class that contains cache parameters.
 * 
 * @author AlfredZhong
 * @version 2012-07-27
 */
public class ImageCacheParams {

	// Default settings
	private static final boolean DEFAULT_EXTERNAL_DISK_CACHE_ENABLED = true;
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.PNG;
    private static final int DEFAULT_COMPRESS_QUALITY = 100;
	// cache path
    private String diskCachePath;
    // Settings
	private boolean diskCacheEnabled = DEFAULT_EXTERNAL_DISK_CACHE_ENABLED;
	private CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
	private int compressQuality = DEFAULT_COMPRESS_QUALITY;
	
	/**
	 * @param context
	 * @param uniqueFolderName unique folder name under internal or external cache dir.
	 */
	public ImageCacheParams(Context context, String uniqueFolderName) {
		File cacheFile = StorageHelper.getExternalCacheDir(context);
		if(uniqueFolderName == null || uniqueFolderName.equals("")) {
			uniqueFolderName = "Temp";
		}
		diskCachePath = cacheFile.getPath() + File.separator + uniqueFolderName + File.separator;
	}
	
	public String getDiskCachePath() {
		return diskCachePath;
	}
	
	/**
	 * Whether disk cache enabled. Default true.
	 * @return
	 */
	public boolean isDiskCacheEnabled() {
		return diskCacheEnabled;
	}
	
	public void setDiskCacheEnabled(boolean diskCacheEnabled) {
		this.diskCacheEnabled = diskCacheEnabled;
	}

	public CompressFormat getCompressFormat() {
		return compressFormat;
	}

	public void setCompressFormat(CompressFormat compressFormat) {
		this.compressFormat = compressFormat;
	}

	public int getCompressQuality() {
		return compressQuality;
	}

	public void setCompressQuality(int compressQuality) {
		this.compressQuality = compressQuality;
	}

	@Override
    public String toString() {
    	StringBuilder sb = new StringBuilder("ImageCacheParams:\n");
    	sb.append("diskCachePath = ").append(diskCachePath).append("\n");
    	sb.append("diskCacheEnabled = ").append(diskCacheEnabled).append("\n");
    	sb.append("compressFormat = ").append(compressFormat).append("\n");
    	sb.append("compressQuality = ").append(compressQuality).append("\n");
    	return sb.toString();
    }
	
}
