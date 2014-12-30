package com.pccw.nowplayer.utils;

/**
 * @author AlfredZhong
 * @version2013-04-27
 * @see http://maps.google.com/support?hl=zh-CN
 */
public class GoogleMapsUtils {

	public static final String GOOGLE_MAPS_URL = "http://maps.google.com/maps";
	
	private GoogleMapsUtils() {
		
	}
	
	public enum GoogleMapsType {
		
		MAP, SATELLITE, HYBRID, TERRAIN, GOOGLE_EARTH;
		
		public String getType() {
			switch(this) {
			case MAP:
				return "m";
			case SATELLITE:
				return "k";
			case HYBRID:
				return "h";
			case TERRAIN:
				return "p";
			case GOOGLE_EARTH:
				return "e";
			}
			return "m";
		}
		
	}
	
	/**
	 * samples:
	 * getGoogleMapsUrl(360, 360, GoogleMapsUtils.GoogleMapsType.MAP, "loc:22.2800049+114.183749", 16);
	 * getGoogleMapsUrl(22.2800049, 114.183749, GoogleMapsUtils.GoogleMapsType.MAP, "", 16);
	 * 
	 * @param lat set number not in [-90, 90] if you don't need it, lat and lng should be valid at the same time
	 * @param lng set number not in [-180, 180] if you don't need it, lat and lng should be valid at the same time
	 * @param type
	 * @param query 
	 * @param zoom
	 * @return
	 */
	public static String getGoogleMapsUrl(double lat, double lng, GoogleMapsType type, String query, int zoom) {
		/*
		 * iOS scheme : http://developer.apple.com/library/ios/#featuredarticles/iPhoneURLScheme_Reference/Articles/MapLinks.html
		 * q = The query parameter. This parameter is treated as if it had been typed into the query box by the user on the maps page.
		 * near = The location part of the query.
		 * ll = The latitude and longitude points (in decimal format, comma separated, and in that order) for the map center point.
		 * sll = The latitude and longitude points from which a business search should be performed.
		 * spn = The approximate latitude and longitude span.
		 * sspn = A custom latitude and longitude span format used by Apple.
		 * t = The type of map to display.
		 * z = The zoom level.
		 * saddr = The source address, which is used when generating driving directions
		 * daddr = The destination address, which is used when generating driving directions.
		 * 
		 * simple scheme:
		 * ll is the latitude and longitude points (in decimal format, comma separated, and in that order) for the map center point.
		 * t is the map type ("m" map, "k" satellite, "h" hybrid, "p" terrain, "e" GoogleEarth)
		 * q is the search query, if it is prefixed by loc: then google assumes it is a lat lon separated by a "+"
		 * z is the zoom level (1-20)
		 */
		String url = GOOGLE_MAPS_URL + "?ll=%s,%s&";
		if(lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
			url = url.replace("ll=%s,%s&", "");
		} else {
			url = String.format(url, lat, lng);
		}
		// Use "loc:", otherwise if just a lat lng will puts a green pin at the lat/long and then a red pin at the nearest search result.
		url += "t=%s&q=%s&z=%s";
		url = String.format(url, type.getType(), query, zoom);
		System.out.println("Google Maps URL = " + url);
		return url;
	}
	
	public static String getQuerySpecifiedByLoc(double lat, double lng) {
		return "loc:" + lat + "+" + lng;
	}
	
}
