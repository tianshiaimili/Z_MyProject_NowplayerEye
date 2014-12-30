package com.pccw.nowplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * @author AlfredZhong
 * @version 2013-05-02
 */
public class KeyboardUtils {

	/*
	 * Android emulator keyboard height:
	 * 240x320, 165 pixels
	 * 320x480, 222 pixels
	 * 480x640, 332 pixels
	 * 480x800, 332 pixels
	 * 720x1280, 443 pixels
	 */
	public static final int MINIMUN_KEYBOARD_HEIGHT = 165;
	
	private KeyboardUtils() {
	}
	
	public static void hideKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
	/**
	 * Set after setContentView().
	 * 
	 * @param activity
	 * @param alwaysHidden set true if you don't want keyboard shown when window receives focus.
	 */
	public static void setSoftInputAdjustResize(Activity activity, boolean alwaysHidden) {
		// you can also set this feature on AndroidManifest.xml activity tag.
		int mode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
		if(alwaysHidden) {
			mode += WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
		}
		activity.getWindow().setSoftInputMode(mode);
	}
	
	/**
	 * Set after setContentView().
	 * 
	 * @param activity
	 * @param alwaysHidden set true if you don't want keyboard shown when window receives focus.
	 */
	public static void setSoftInputAdjustPan(Activity activity, boolean alwaysHidden) {
		int mode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
		if(alwaysHidden) {
			mode += WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
		}
		activity.getWindow().setSoftInputMode(mode);
	}
	
	/**
	 * Set EditText not auto focus.
	 * 
	 * @param edittextParentLayout the parent layout of EditText.
	 */
	public static void setEditTextNotAutoFocus(View edittextParentLayout) {
		/*
		 * android:focusable="true" 
		 * android:focusableInTouchMode="true" 
		 */
		edittextParentLayout.setFocusable(true);
		edittextParentLayout.setFocusableInTouchMode(true);
	}
	
	/**
	 * Should call requestVisibilityUpdate() before keyboard is shown.
	 * Should call removeVisibilityUpdate() if you don't need it.
	 */
	public static abstract class KeyboardVisibilityChangedListener {
		
		private ViewGroup mLayout;
		private OnGlobalLayoutListener listener;
		private boolean isKeyboardVisible;
		
		public KeyboardVisibilityChangedListener(ViewGroup layout) {
			isKeyboardVisible = false;
			mLayout = layout;
			listener = new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					// total height, not including TabletStatusBar.
					int layoutHeight = mLayout.getRootView().getHeight();
					// visible display area(not including status bar, but including title bar).
					Rect visibleDisplay = new Rect();
					// r will be populated with the coordinates of your view that area still visible.
					mLayout.getWindowVisibleDisplayFrame(visibleDisplay);
					int statusBarHeight = visibleDisplay.top;
					int visibleDisplayHeight = visibleDisplay.bottom - visibleDisplay.top;
					int keyboardHeight = layoutHeight - visibleDisplayHeight - statusBarHeight;
					/*
					 * when keyboard is visible:
					 * 720x1280, 1280, 50, 787, 443
					 * 480x800, 800, 38, 430, 332
					 * 480x640, 640, 38, 270, 332
					 * 320x480, 480, 25, 233, 222
					 * 240x320, 320, 19, 136, 165
					 */
					System.out.println(layoutHeight + ", " + statusBarHeight + ", " + visibleDisplayHeight + ", " + keyboardHeight);
					if (keyboardHeight >= MINIMUN_KEYBOARD_HEIGHT) {
						// if more than 165 pixels, it's probably a keyboard.
						System.out.println("Keyboard appears.");
						isKeyboardVisible = true;
						onKeyboardVisibilityChanged(isKeyboardVisible, layoutHeight, visibleDisplayHeight, keyboardHeight);
					} else {
						if(isKeyboardVisible) {
							System.out.println("Keyboard disappears.");
							isKeyboardVisible = false;
							onKeyboardVisibilityChanged(isKeyboardVisible, layoutHeight, visibleDisplayHeight, keyboardHeight);
						}
					}
				}
			};
			/*
			 * You can also define a custom layout and detect keyboard visibility onMeasure():
			 * protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			 *     final int currentHeight = getHeight();
			 *     final int tobeHeight = MeasureSpec.getSize(heightMeasureSpec);
			 *     System.out.println("currentHeight = " + currentHeight + " tobeHeight = " + tobeHeight);
			 *     if(currentHeight != 0) {
			 *         if (tobeHeight < currentHeight) {
			 *             System.out.println("Keyboard appears.");
			 *         } else if(tobeHeight > currentHeight) {
			 *             System.out.println("Keyboard disappears.");
			 *         }
			 *     }
			 *     super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			 * }
			 */
		}
		
		public void requestVisibilityUpdate() {
			mLayout.getViewTreeObserver().addOnGlobalLayoutListener(listener);
		}
		
		public void removeVisibilityUpdate() {
			mLayout.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
		}
		
		public abstract void onKeyboardVisibilityChanged(boolean visible, int layoutHeight, int visibleDisplayHeight, int keyboardHeight);
		
	}
	
}
