package com.pccw.nowplayer.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * @author AlfredZhong
 * @version 2013-06-26
 */
public class LayoutUtils {

	private LayoutUtils() {
	}
	
	public static interface OnViewGlobalLayoutListener {
		
		public void onViewGlobalLayout(View view);
		
	}
	
	/**
	 * Try to get the view layout info before onResume().
	 * 
	 * @param view the view you want to get its layout info before onResume(), ViewGroup is also OK.
	 * @param listener the callback when you can get the view layout info.
	 */
	public static void getViewLayoutInfoBeforeOnResume(final View view, final LayoutUtils.OnViewGlobalLayoutListener listener) {
		// The ViewTreeObserver observer is not guaranteed to remain valid for the lifetime of this View.
		// So we have to check isAlive().
		if(view.getViewTreeObserver().isAlive()) {
			view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				// We use the new method when supported
				@SuppressWarnings("deprecation")
				// We check which build version we are using.
				@SuppressLint("NewApi")
				@Override
				public void onGlobalLayout() {
					// remove listener
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
						view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					} else {
						view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
					if (listener != null) {
						listener.onViewGlobalLayout(view);
					}
				}
			});
		} else {
			System.err.println("== The ViewTreeObserver is NOT alive. ==");
		}
	}
	
}
