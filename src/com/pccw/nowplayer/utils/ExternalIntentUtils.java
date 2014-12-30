package com.pccw.nowplayer.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;

/**
 * A helper class to launch a new activity with Android System intent.
 * 
 * @author PCCW ITSI (IT Software Integration) Productization Team
 * @version 2012-05-15
 */
public class ExternalIntentUtils {
	
	private static final String TAG = ExternalIntentUtils.class.getSimpleName();
	
	/**
	 * Launch SMS activity.
	 * <p>Need permission: android.permission.SEND_SMS
	 * @param context
	 * @param to null or empty string "" if you don't need any receiver.
	 * @param msg
	 */
	public static void lanuchSMSActvity(Context context, String to, String msg){
		Log.d(TAG, "Send SMS : " + msg);
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.putExtra("sms_body", msg); // msg null is OK.
		sendIntent.putExtra("address", to);
		sendIntent.setType("vnd.android-dir/mms-sms");
		startActivity(context, sendIntent, true);
		/* Can not setType
		Uri uri = Uri.parse("smsto:" + to);    
		Intent it = new Intent(Intent.ACTION_SENDTO, uri);    
		it.putExtra("sms_body", msg);
		context.startActivity(it); 
		*/
	}
	
	/**
	 * Send SMS.
	 * <p>Need permission: android.permission.SEND_SMS
	 * @param to
	 * @param msg
	 */
	public static void sendSMS(String to, String msg) {
		Log.d(TAG, "Send SMS to : " + to + ", msg : " + msg);
		SmsManager smsManager = SmsManager.getDefault();
		if(msg.length() > 70) {
			Log.d(TAG, "Message length is larger than 70, divide it.");
			List<String> msgs = smsManager.divideMessage(msg);
			for(String sms : msgs) {
				smsManager.sendTextMessage(to, null, sms, null, null);
			}
		} else {
			smsManager.sendTextMessage(to, null, msg, null, null);
		}
	}

	private static String formatPhoneNumber(String phoneNumber) {
		if(phoneNumber == null)
			return null;
		phoneNumber = phoneNumber.replace(" ", "");
		Log.d(TAG, "Phone number is " + phoneNumber);
		if(!phoneNumber.startsWith("tel:"))
			phoneNumber = "tel:" + phoneNumber;
		return phoneNumber;
	}

	/**
	 * Show the dialer with the number being dialed.
	 * <p>Need permission: android.permission.CALL_PHONE
	 * @param context
	 * @param phoneNumber
	 */
	public static void showDialer(Context context, String phoneNumber) {
		Uri uri = Uri.parse(formatPhoneNumber(phoneNumber));
		Intent it = new Intent(Intent.ACTION_DIAL, uri);
		startActivity(context, it, true);
	}
	
	/**
	 * Perform a call to someone specified by the data.
	 * <p>Need permission: android.permission.CALL_PHONE
	 * @param context
	 * @param phoneNumber
	 */
	public static void phoneCall(Context context, String phoneNumber) {
		Uri uri = Uri.parse(formatPhoneNumber(phoneNumber));
		Intent it = new Intent(Intent.ACTION_CALL, uri);
		startActivity(context, it, true);
	}
	
	/**
	 * Open the giving link.
	 * @param context
	 * @param link
	 */
	public static void openLink(Context context, String link) {
		Log.d(TAG, "Open link " + link);
		if (!link.toLowerCase().startsWith("http://") && !link.toLowerCase().startsWith("https://")) {
			link = "http://" + link;
		}
		Uri uri = Uri.parse(link);
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(context, it, true);
	}
	
	/**
	 * Launch email activity to send text content type email.
	 * @param context
	 * @param to
	 * @param subject
	 * @param body
	 */
	public static void sendTextEmail(Context context, String to, String subject, String body) {
		Log.d(TAG, "Send text email to : " + to + ", subject : " + subject);
		Log.d(TAG, "body : " + body);
		Intent i = new Intent(Intent.ACTION_SEND);  
		i.setType("text/plain"); // use this line for testing in the emulator  
		//i.setType("message/rfc822"); // use from live device
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
		i.putExtra(Intent.EXTRA_SUBJECT, subject);
		i.putExtra(Intent.EXTRA_TEXT, body);
		startActivity(context, i, true);
		//context.startActivity(Intent.createChooser(i, "Select email application"));
	}
	
	/**
	 * Launch email activity to send html content type email.
	 * @param context
	 * @param to
	 * @param subject
	 * @param body
	 */
	public static void sendHtmlEmail(Context context, String to, String subject, String body) {
		Log.d(TAG, "Send html email to : " + to + ", subject : " + subject);
		Log.d(TAG, "body : " + body);
		Intent i = new Intent(Intent.ACTION_SEND);  
		//i.setType("text/plain"); // use this line for testing in the emulator  
		i.setType("text/html"); // use from live device
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
		i.putExtra(Intent.EXTRA_SUBJECT, subject);
		/*
		 * It's up to the individual mail app to properly handle the String that it receives as the EXTRA_TEXT. 
		 * A well-behaved mail app will see the mimetype and handle the EXTRA_TEXT appropriately, but not all mail apps do.
		 * If a mail app not support it, it will formated in plain text.
		 */
		i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body));
		startActivity(context, i, true);
		//context.startActivity(Intent.createChooser(i, "Select email application"));
	}
	
	/**
	 * Launch email activity to send html content type email.
	 * @param context
	 * @param to
	 * @param subject
	 * @param body
	 */
	public static void sendHtmlEmailToEmailClients(Context context, String to, String subject, String body) {
		Log.d(TAG, "Send html email to : " + to + ", subject : " + subject);
		Log.d(TAG, "body : " + body);
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		//shareIntent.setType("text/plain"); // use this line for testing in the emulator  
		shareIntent.setType("text/html"); // use from live device

		String[] strAllowPackageNames = { "com.android.email",
				"com.google.android.gm",
				"com.hotmail.Z7",
				"com.yahoo.mobile.client.android.mail" };
		List<Intent> targetedShareIntents = new ArrayList<Intent>();
		List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(shareIntent, 0);
		if (!resInfo.isEmpty()) {
			for (ResolveInfo resolveInfo : resInfo) {
				String packageName = resolveInfo.activityInfo.packageName;
				if (packageName != null && packageName.length() > 0) {
					for (int i=0; i<strAllowPackageNames.length; i++) {
						if (packageName
								.equalsIgnoreCase(strAllowPackageNames[i])) {
							Intent targetedShareIntent = new Intent(
									Intent.ACTION_SEND);
							targetedShareIntent.setType("text/html");

							targetedShareIntent.putExtra(Intent.EXTRA_EMAIL,
									new String[] { to });
							targetedShareIntent.putExtra(Intent.EXTRA_SUBJECT,
									subject);
							/*
							 * It's up to the individual mail app to properly handle the String that it receives as the EXTRA_TEXT. 
							 * A well-behaved mail app will see the mimetype and handle the EXTRA_TEXT appropriately, but not all mail apps do.
							 * If a mail app not support it, it will formated in plain text.
							 */
							targetedShareIntent.putExtra(Intent.EXTRA_TEXT,
									Html.fromHtml(body));
							targetedShareIntent.setPackage(packageName);
							targetedShareIntents.add(targetedShareIntent);
							break;
						}
					}
				}
			}
			if (targetedShareIntents != null && !targetedShareIntents.isEmpty()) {
				Intent chooserIntent = Intent.createChooser(
						targetedShareIntents.remove(0), null);
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
						targetedShareIntents.toArray(new Parcelable[] {}));
				startActivity(context, chooserIntent, true);
			}
		}
	}
	
	
	/**
	 * Launch calendar event activity.
	 * @param context
	 * @param beginTime
	 * @param endTime
	 * @param eventSubject
	 * @param description
	 * @param eventLocation
	 */
	public static void addCalendarEvent(Context context, long beginTime, long endTime, 
			String eventSubject, String description, String eventLocation) {
		Log.d(TAG, "Add calendar event, beginTime : " + beginTime + ", endTime : " + endTime);
		Log.d(TAG, "EventTitle : " + eventSubject + ", description :" + description + ", eventLocation : " + eventLocation);
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra("beginTime", beginTime);
		intent.putExtra("endTime", endTime);
		intent.putExtra("title", eventSubject);
		intent.putExtra("eventLocation", eventLocation);
		intent.putExtra("description", description);
		startActivity(context, intent, true);
	}
	
	public static void goToMarket(Context context, String url){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(context, intent, true);
	}
	
	public static void toastActivityNotFound(Context context) {
		String toast = null;
		/*
		 * The language codes are two-letter lowercase ISO language codes (such as "en") as defined by
		 * <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1</a>.
		 * The country codes are two-letter uppercase ISO country codes (such as "US") as defined by
		 * <a href="http://en.wikipedia.org/wiki/ISO_3166-1_alpha-3">ISO 3166-1</a>.
		 * The variant codes are unspecified.
		 */
		Locale loc = Locale.getDefault();
		String language = loc.getLanguage();
		String country = loc.getCountry();
		System.out.println("Default locale is " + language + "_" + country);
		if(language.equals("zh")) {
			if(country.equals("TW") || country.equals("HK")) {
				// traditional Chinese
				toast = "無法找到相關應用程序";
			} else {
				// simplified Chinese
				toast = "无法找到相关应用程序";
			}
		} else {
			// non zh, use en.
			toast = "Can't find relative application";
		}
		android.widget.Toast.makeText(context, toast, android.widget.Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Launch a new activity with ActivityNotFoundException caught.
	 * 
	 * @param context
	 * @param intent
	 * @param toast true if your want to show a message toast when ActivityNotFoundException occurs.
	 * @return
	 */
	public static boolean startActivity(Context context, Intent intent, boolean toast) {
		try {
			context.startActivity(intent);
			return true;
		} catch (android.content.ActivityNotFoundException e) {
			if(toast) {
				toastActivityNotFound(context);
			}
			return false;
		}
	}
	
	/**
	 * Return matching activities list or an empty list if no matching.
	 * 
	 * @param context
	 * @param intent
	 * @return
	 */
	public static List<ResolveInfo> queryMatchIntent(Context context, Intent intent) {
		// You can use ResolveInfo.activityInfo.packageName or ResolveInfo.activityInfo.name to filter intents.
		return context.getPackageManager().queryIntentActivities(intent,
		    android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
	}
	
	/**
	 * Create action chooser with given intent list.
	 * 
	 * @param context
	 * @param intents
	 * @param title, if null will use Android default title.
	 */
	public static void createActionChooser(Context context, final List<Intent> intents, String title) {
		if(intents == null || intents.size() == 0) {
			Log.w(TAG, "Action intents is null or empty.");
			return;
		}
		// Fetch the last intent to create ACTION_CHOOSER.
		Intent lastIntent = intents.remove(intents.size() - 1);
        Intent chooserIntent = Intent.createChooser(lastIntent, title);
        // Add other intents as a Parcelable[] before last intent.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[] {}));
        startActivity(context, chooserIntent, true);
	}
	
	/**
	 * Note that there is no pin on the map.
	 */
	public static Uri getMapAppIntentData(double lat, double lng, int zoom) {
		// http://developer.android.com/guide/appendix/g-app-intents.html
		// geo:latitude,longitude
		// geo:latitude,longitude?z=zoom
		// geo:0,0?q=my+street+address
		// geo:0,0?q=business+near+city
		// use "geo:0,0?q=" if you just want to get map app list.
		String mapData = String.format("geo:%s,%s?z=%s", lat, lng, zoom);
		Log.d(TAG, "Map app intent data : " + mapData);
		return Uri.parse(mapData);
	}
	
	/**
	 * Returns the Google Maps app with pin intent data.
	 * Note that only Google Maps support q with geo format.
	 * So you should only use this method for calling Google Maps, not for other map apps.
	 */
	public static Uri getGoogleMapsWithPinIntentData(double lat, double lng, int zoom) {
		// q, the query parameter. 
		String mapData = String.format("geo:%s,%s?z=%s&q=%s,%s", lat, lng, zoom, lat, lng);
		return Uri.parse(mapData);
	}
	
	/**
	 * Show application(not including browsers) chooser dialog about Map.
	 * 
	 * @param context
	 * @param lat
	 * @param lng
	 */
	public static void openMapAppChooser(Context context, double lat, double lng, int zoom) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(getMapAppIntentData(lat, lng, zoom));
		startActivity(context, intent, true);
	}

	/**
	 * Show browsers and Google Map application chooser dialog.
	 * 
	 * @param context
	 * @param lat
	 * @param lng
	 */
	public static void openGoogleMap(Context context, double lat, double lng, int zoom) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		// Google map app and browsers.
		intent.setData(Uri.parse(GoogleMapsUtils.getGoogleMapsUrl(360, 360, GoogleMapsUtils.GoogleMapsType.MAP, 
				GoogleMapsUtils.getQuerySpecifiedByLoc(lat, lng), zoom)));
		startActivity(context, intent, true);
	}
	
	/**
	 * Show browsers and Map applications(GoogleMap & 3rd party Maps) chooser dialog.
	 * 
	 * @param context
	 * @param lat
	 * @param lng
	 */
	public static void openMapChooser(Context context, double lat, double lng, int zoom) {
		List<Intent> intents = new ArrayList<Intent>();
		Set<String> apps = new HashSet<String>();
		Intent targetIntent = null;
		// data1
		Intent intent1 = new Intent(Intent.ACTION_VIEW);
		Uri uri1 = getMapAppIntentData(lat, lng, zoom);
		intent1.setData(uri1);
		List<ResolveInfo> list1 = queryMatchIntent(context, intent1);
		for(ResolveInfo info : list1) {
			targetIntent = new Intent(Intent.ACTION_VIEW);
			if("com.google.android.apps.maps".equals(info.activityInfo.packageName)) {
				targetIntent.setData(getGoogleMapsWithPinIntentData(lat, lng, zoom));
			} else {
				targetIntent.setData(uri1);
			}
			targetIntent.setPackage(info.activityInfo.packageName);
			intents.add(targetIntent);
			apps.add(info.activityInfo.packageName);
		}
		// data2
		Intent intent2 = new Intent(Intent.ACTION_VIEW);
		Uri uri2 = Uri.parse(GoogleMapsUtils.getGoogleMapsUrl(360, 360, GoogleMapsUtils.GoogleMapsType.MAP, 
				GoogleMapsUtils.getQuerySpecifiedByLoc(lat, lng), zoom));
		intent2.setData(uri2);
		List<ResolveInfo> list2 = queryMatchIntent(context, intent2);
		for(ResolveInfo info : list2) {
			if(apps.contains(info.activityInfo.packageName)) {
				// already found in map app intent.
				continue;
			}
			targetIntent = new Intent(Intent.ACTION_VIEW);
			targetIntent.setData(uri2);
			targetIntent.setPackage(info.activityInfo.packageName);
			intents.add(targetIntent);
		}
		// System.out.println(intents);
		createActionChooser(context, intents, null);
	}
	
	public static boolean isPDFAppsExists(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // we just query the app exists or not, no need the Uri data.
        intent.setDataAndType(null, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // queryMatchIntent() never return null.
        return queryMatchIntent(context, intent).size() > 0;
	}
	
	public static void openPDF(Context context, File file) {
		Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(path, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(context, intent, true);
	}
	
	public static boolean isAdobeReaderInstalled(Context context) {
		return PackageUtils.checkPackageInstalled(context, "com.adobe.reader");
	}
	
	public static void openPDFWithAdobeReader(Context context, String url) {
	    Intent pdfDownloadIntent = null;
	    try {
	        pdfDownloadIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
	        pdfDownloadIntent.setPackage("com.adobe.reader");
	        pdfDownloadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    } catch (URISyntaxException e) {
	    }
	    startActivity(context, pdfDownloadIntent, true);
	}
	
}
