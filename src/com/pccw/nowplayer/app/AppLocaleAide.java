package com.pccw.nowplayer.app;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A helper class to change app locale and callback onResume() if app locale changed.
 * Note that app locale will save with PreferenceManager.getDefaultSharedPreferences(context)
 * which it is application level SharedPreferences, only one file of this kind, file name is packagename_preferences.xml.
 * 
 * User guide:
 * 1 Activity or Fragment implements AppLocaleAideSupport.
 * 2 private AppLocaleAide mAppLocaleAide = new AppLocaleAide(this);
 * 3 onCreate() call mAppLocaleAide.maintainAppLocale(context);
 * 4 onResume() call mAppLocaleAide.checkAppLocale(context);
 * 5 Call AppLocaleAide.setLocale() when you need.
 * 
 * @author AlfredZhong
 * @version 2013-07-22
 */
public final class AppLocaleAide {
	
	////////////////////// Application Locale Configuration //////////////////////
	
    private static final String TAG = AppLocaleAide.class.getSimpleName();
    private AppLocaleAideSupport mAppLocaleAideSupport; // for app locale changed callback.
    private Locale mLocale; // for app locale changed callback.
    public static final Locale SIMPLIFIED_CHINESE = new Locale("zh", "CN");
	public static final Locale TRADITIONAL_CHINESE_HK = new Locale("zh", "HK");
	public static final Locale TRADITIONAL_CHINESE_TW = new Locale("zh", "TW");
	public static final Locale ENGLISH_US = new Locale("en", "US");
	// Locale.toString() examples: "en", "en_US", "_US", "en__POSIX", "en_US_POSIX"
	private static final String APP_LOCALE_SHARED_PREFERENCES_LANGUAGE_KEY = "app_locale_language";
	private static final String APP_LOCALE_SHARED_PREFERENCES_COUNTRY_KEY = "app_locale_country";
	private static final String APP_LOCALE_SHARED_PREFERENCES_VARIANT_KEY = "app_locale_variant";
	private static final String APP_LOCALE_SHARED_PREFERENCES_NO_LANGUAGE = "no_language";
	private static final String APP_LOCALE_SHARED_PREFERENCES_NO_COUNTRY = "NO_COUNTRY";
	private static final String APP_LOCALE_SHARED_PREFERENCES_NO_VARIANT = "NO_VARIANT";
	
    public interface AppLocaleAideSupport {
    	
    	/**
    	 * Returns AppLocaleAide.
    	 */
    	public AppLocaleAide getAppLocaleAide();
    	
        /**
         * This method will be called if language changed when activity or fragment onResume() or resumed;
         * Note that if you don't recreate activities after locale changed,
         * you should call setContentView() to update whole views or setBackgroundDrawable() to update Buttons 
         * or setImageDrawable(), setImageBitmap() to update ImageViews.
         * Do NOT use setXXXResource() to update UI, it may not work on some devices sometimes.
         */
        public void onLocaleChanged();
        
    }
	
    public AppLocaleAide(AppLocaleAideSupport support) {
    	mAppLocaleAideSupport = support;
    }
    
    /**
     * Set application default locale.
     * Very useful to call this if you don't want the app default locale same as system locale.
     */
    public static void setDefaultAppLocale(Locale defaultLocale) {
    	Locale.setDefault(defaultLocale);
    }
    
    /**
     * Set application locale.
     * 
     * @param context
     * @param newLocale
     * @param aide set null if you don't use it.
     */
    public static void setAppLocale(Context context, Locale newLocale, AppLocaleAideSupport support) {
    	Resources res = context.getResources();
    	Configuration cfg = res.getConfiguration();
    	Log.d(TAG, "setAppLocale :: Configuration locale is " + cfg.locale + ", new locale is " + newLocale);
    	if(cfg.locale.equals(newLocale)) {
    		return;
    	} 
    	Log.w(TAG, "setAppLocale :: Update app locale from " + cfg.locale + " to " + newLocale);
    	cfg.locale = newLocale;
    	res.updateConfiguration(cfg, null);
    	// setDefault locale just to synchronize default locale and app configuration locale. 
    	Locale.setDefault(cfg.locale);
    	// save app locale to SharedPreferences
    	SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	editor.putString(APP_LOCALE_SHARED_PREFERENCES_LANGUAGE_KEY, newLocale.getLanguage());
    	editor.putString(APP_LOCALE_SHARED_PREFERENCES_COUNTRY_KEY, newLocale.getCountry());
    	editor.putString(APP_LOCALE_SHARED_PREFERENCES_VARIANT_KEY, newLocale.getVariant());
    	editor.commit();
    	AppLocaleAide aide = support.getAppLocaleAide();
    	if(aide != null) {
    		aide.checkAppLocale(context, false);
    	} else {
    		Log.w(TAG, "AppLocaleAide setAppLocale() not use AppLocaleAideSupport.");
    	}
    }
    
    /**
     * Returns application locale.
     */
    public static Locale getAppLocale(Context context) {
        String language = PreferenceManager.getDefaultSharedPreferences(context)
        		.getString(APP_LOCALE_SHARED_PREFERENCES_LANGUAGE_KEY, APP_LOCALE_SHARED_PREFERENCES_NO_LANGUAGE);
        String country = PreferenceManager.getDefaultSharedPreferences(context)
        		.getString(APP_LOCALE_SHARED_PREFERENCES_COUNTRY_KEY, APP_LOCALE_SHARED_PREFERENCES_NO_COUNTRY);
        String variant = PreferenceManager.getDefaultSharedPreferences(context)
        		.getString(APP_LOCALE_SHARED_PREFERENCES_VARIANT_KEY, APP_LOCALE_SHARED_PREFERENCES_NO_VARIANT);
        if(language.equals(APP_LOCALE_SHARED_PREFERENCES_NO_LANGUAGE)
        		&& country.equals(APP_LOCALE_SHARED_PREFERENCES_NO_COUNTRY)
        		&& variant.equals(APP_LOCALE_SHARED_PREFERENCES_NO_VARIANT)) {
        	// Locale.getDefault() and setDefault() only affect the application locale setting, not system locale setting.
        	return Locale.getDefault();
        } else {
        	return new Locale(language, country, variant);
        }
    }
    
    /**
     * Whether current app locale is English.
     */
    public static boolean isAppLocaleEn(Context context) {
    	Locale loc = getAppLocale(context);
    	// Locale.equals() returns true only same language, country and variant.
    	// In case same language different country, we only compare language here: 
    	if(loc.getLanguage().equals("en"))
    		return true;
    	return false;
    }
    
    /**
     * Whether current app locale is Chinese.
     */
    public static boolean isAppLocaleZh(Context context) {
    	Locale loc = getAppLocale(context);
    	// Locale.equals() returns true only same language, country and variant.
    	// In case same language different country, we only compare language here: 
    	if(loc.getLanguage().equals("zh"))
    		return true;
    	return false;
    }
    
    /**
     * Maintain configuration locale onCreate() to avoid configuration has been reset by OS.
     * Do NOT call getResources().getXXX() before calling AppLocaleAide.maintainAppLocale().
     */
    public void maintainAppLocale(Context context) {
		mLocale = getAppLocale(context);
		Log.d(TAG, getClass().getName() + " getAppLocale() : " + mLocale);
		setAppLocale(context, mLocale, mAppLocaleAideSupport);
    }
    
    /**
     * Check whether the locale has changed onResume().
     * Check locale onResume() instead of onRestart() since some activities don't have onRestart(), 
     * e.g. activities embedded inside ActivityGroup; or fragments don't have onRestart().
     */
    public void checkAppLocale(Context context) {
    	checkAppLocale(context, true);
    }
    
    private void checkAppLocale(Context context, boolean onResume) {
		Locale appLocale = getAppLocale(context);
		Log.d(TAG, "Check previous locale " + mLocale + " with current locale " + appLocale);
    	if(!mLocale.equals(appLocale)) {
    		mLocale = appLocale;
    		if(mAppLocaleAideSupport != null) {
    			if(onResume) {
    				Log.w(TAG, getClass().getName() + " locale changed onResume().");
    			} else {
    				Log.w(TAG, getClass().getName() + " locale changed resumed.");
    			}
    			mAppLocaleAideSupport.onLocaleChanged();
    		} else {
    			Log.w(TAG, getClass().getName() + " AppLocaleAideSupport is null.");
    		}
    	} else {
    		Log.d(TAG, getClass().getName() + " checkAppLocale not changed.");
    	}
    }

    ////////////////////// end of "Application Locale Configuration" //////////////////////
	
}
