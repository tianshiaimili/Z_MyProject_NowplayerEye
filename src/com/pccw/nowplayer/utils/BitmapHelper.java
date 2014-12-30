package com.pccw.nowplayer.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

/**
 * @author AlfredZhong
 * @version 2012-07-27
 */
public class BitmapHelper {
	
	private static final int IO_BUFFER_SIZE = 1024 * 8;
	
	private BitmapHelper() {}
    
	public static void writeBitmapToFile(String filename, Bitmap bitmap, CompressFormat format, int quality) {
		if(!StorageHelper.isExternalStorageMounted()) {
			return;
		}
		BufferedOutputStream out = null;
		File file = new File(filename);
		try {
			out = new BufferedOutputStream(new FileOutputStream(file), IO_BUFFER_SIZE);
			bitmap.compress(format, quality, out);
			out.flush();
		} catch (Exception e) {
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
