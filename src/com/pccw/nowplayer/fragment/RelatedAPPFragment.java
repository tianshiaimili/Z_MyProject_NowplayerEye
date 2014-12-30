package com.pccw.nowplayer.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pccw.nmal.appdata.B2BApiAppInfo;
import com.pccw.nowplayer.app.AppLocaleAide;
import com.pccw.nowplayer.app.BaseFragment;
import com.pccw.nowplayer.utils.ExternalIntentUtils;
import com.pccw.nowplayer.utils.WebViewUtils;
import com.pccw.nowplayereyeapp.R;

public class RelatedAPPFragment extends BaseFragment {
	
	private static final String TAG = RelatedAPPFragment.class.getSimpleName();
	private Button backBt;
	private String url;
	private WebView webView;
	private String url_en, url_zh;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = createOrRetrieveFragmentView(inflater, container, R.layout.setting_webview_tablet);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(super.isFragmentRecreatedWithHoldView()){
			return;
		}
		((TextView)findViewById(R.id.setting_webview_title)).setText(R.string.setting_related_app);
		backBt = (Button)findViewById(R.id.setting_right_back_bt);
		backBt.setVisibility(View.INVISIBLE);		
		webView = (WebView)findViewById(R.id.setting__webview);
		backBt.setOnClickListener(new MyBackClickListener(webView));
		WebViewUtils.initInternalBrowser(getActivity(), webView);
		WebViewUtils.limitWebView(webView, true, true, true, true);
		
		webView.setWebViewClient(new MyWebViewClient());
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.webview_progress);
				if (progressBar != null) {
					if (view.getVisibility() == View.VISIBLE) {
						if (progress == 100) {
							// web view finished loading, hide progress bar
							progressBar.setVisibility(View.GONE);
						} else {
							// web view is loading, show and update progress bar
							progressBar.setProgress(progress);
							progressBar.setVisibility(View.VISIBLE);
						}
					} else {
						progressBar.setVisibility(View.GONE);
					}
				}
			}
		});
		
		url = B2BApiAppInfo.getRelatedAppURL();
		if (url != null) {
			url_en = url.replace("[lang]", "en");
			url_zh = url.replace("[lang]", "zh");
//			//----------------test---------------------
//			url_en = "http://10.37.131.139:8080/relatedApp/en/relatedapp.html";
//			url_zh = "http://10.37.131.139:8080/relatedApp/zh/relatedapp.html";
			if(AppLocaleAide.isAppLocaleEn(getActivity())) {
				webView.loadUrl(url_en);
			} else {
				webView.loadUrl(url_zh);
			}
		}
	}
	
	@Override
	public void onLocaleChanged() {
		super.onLocaleChanged();
		Log.i(TAG, "onLocaleChanged()");
		backBt.setText(R.string.setting_back_button);
		((TextView)findViewById(R.id.setting_webview_title)).setText(R.string.setting_related_app);
		webView.clearView(); // clear view, so that cannot see the previous language
		if(AppLocaleAide.isAppLocaleEn(getActivity())){
			webView.loadUrl(url_en);
			Log.d(TAG, "load url_en = " + url_en);
		} else {
			webView.loadUrl(url_zh);
			Log.d(TAG, "load url_zh = " + url_zh);
		}
	}
	
	class MyWebViewClient extends WebViewClient{
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if(url.startsWith("https://play.google.com/store/apps/details?id=")){
				Log.d(TAG, "jump out for url:"+url);
		        ExternalIntentUtils.goToMarket(getActivity(), url);
		        //Tell the WebView you took care of it.
				return true;
			}else if (url.startsWith("eye://product/")){
				ExternalIntentUtils.goToMarket(getActivity(), url);
				return true;
			}else{
				Log.d(TAG, "not jump out for url:"+url);
				view.loadUrl(url);
				return false;
			}
			
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			if(url.equalsIgnoreCase(url_en) || url.equalsIgnoreCase(url_zh)){
				view.clearHistory(); // clear history, so that cannot go back to previous language
			}
			backBt.setVisibility( view.canGoBack() ? View.VISIBLE : View.INVISIBLE);
		}
	}
	
	class MyBackClickListener implements OnClickListener{

		private WebView webview ;
		public MyBackClickListener(WebView webView) {
			this.webview = webView;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.setting_right_back_bt:
				
				if(webview != null && webview.canGoBack()){
					webview.goBack();
				}
				break;

			default:
				break;
			}
		}
		
		
	}
}
