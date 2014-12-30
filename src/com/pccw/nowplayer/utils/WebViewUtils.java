package com.pccw.nowplayer.utils;

import android.app.Activity;
import android.util.Log;
import android.view.Display;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * @author AlfredZhong
 * @version 2013-02-25
 */
public class WebViewUtils {

	private static final String TAG = WebViewUtils.class.getSimpleName();
	
	private WebViewUtils() {
	}
	
	/**
	 * Tell the WebView to use the wide viewport and loads a page with overview mode.
	 * @param webview
	 */
	public static final void fitScreenWidth(WebView webview) {
		WebSettings ws = webview.getSettings();
		ws.setUseWideViewPort(true);
		ws.setLoadWithOverviewMode(true);
	}
	
	/**
	 * Sets the WebView supports zoom and sets whether the zoom mechanism built into WebView is used.
	 * @param webview
	 * @param showControler
	 */
	public static final void supportZoom(WebView webview, boolean showControler) {
		WebSettings ws = webview.getSettings();
		ws.setSupportZoom(true); // double-tap zoom and keep pinch 
	    ws.setBuiltInZoomControls(showControler);
	}
	
	/**
	 * Load the given image data into the WebView to show the image.
	 * @param webview
	 * @param imageData
	 */
	public static void loadImage(WebView webview, byte[] imageData) {
		final String image64 = android.util.Base64.encodeToString(imageData, android.util.Base64.DEFAULT);
		String html = "<html><body style='margin:0;padding:0;'><img src=\"data:image/jpeg;base64," + image64 + "\" /></body></html>";
		webview.loadDataWithBaseURL("", html, "text/html", "utf-8", "");
	}
	
	/**
	 * Load the given image file into the WebView to show the image.
	 * @param webview
	 * @param imageData
	 */
	public static void loadImage(WebView webview, String imagePath) {
		webview.getSettings().setAllowFileAccess(true);
		imagePath = "file://"+ imagePath;
		Log.d(TAG, "loadDiskImage " + imagePath);
		String html = "<html><body style='margin:0;padding:0;'><img src=\""+ imagePath + "\"></body></html>";
		webview.loadDataWithBaseURL("", html, "text/html", "utf-8", "");
	}
	
	public static int getWebViewScale(Activity act) {
        Display display = act.getWindowManager().getDefaultDisplay(); 
        int width = display.getWidth(); 
        Double val = Double.valueOf(width) / Double.valueOf(1000);
        val = val * 100d;
        return val.intValue();
	}
	
	public static void initInternalBrowser(Activity act, WebView webView) {
		webView.setHorizontalScrollBarEnabled(true);
		webView.setHorizontalScrollbarOverlay(true);
		webView.setVerticalScrollBarEnabled(true);
		webView.setVerticalScrollbarOverlay(true);
        webView.setPadding(0, 0, 0, 0);
        webView.setInitialScale(getWebViewScale(act));
	}
	
	/**
	 * Because WebView consumes web content that can include HTML and JavaScript, 
	 * improper use can introduce common web security issues such as cross-site-scripting (JavaScript injection). 
	 * Android includes a number of mechanisms to reduce the scope of these potential issues 
	 * by limiting the capability of WebView to the minimum functionality required by application.
	 * 
	 * 1 Javascript – Please disable and do not call setJavaScriptEnabled().
	 * 2 Use of addJavascriptInterface API - exposing addJavaScriptInterface() only to JavaScript that is contained within application APK. Disable it if no needed.
	 * 3 Plugin support for WebViews – if our application doest not use plugins such as flash, disable them explicitly.
	 * 4 Disabling the local file system access - By default the local file system access is enabled for WebViews. 
	 *   Resources and assets can be accessed as file:///resource_id or file:///asset_name respectively. 
	 *   It is a good idea to turn this off in order to reduce the impact of a possible compromise.
	 * 5 ClearCache - Use the clearCache() method to delete any files stored locally. 
	 *   Server-side headers like no-cache can also be used to indicate that an application should not cache particular content.
	 *   
	 * @param webView
	 */
	public static void limitWebView(WebView webView, boolean disableJS, boolean disablePlugin, boolean disableFileAccess, boolean noCache) {
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(disableJS);
		settings.setPluginsEnabled(disablePlugin);
		settings.setAllowFileAccess(disableFileAccess);
		settings.setAppCacheEnabled(noCache);
	}
	
}
