package com.pccw.nowplayer.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * JSONObject {k:v, k:v, ...}
 * 
 * k is String, and v can be
 * JSONArray, [...]
 * JSONObject, {...}
 * String, k:v
 * Boolean, k:v
 * Number, k:v
 * NULL, k:v
 * 
 * @author AlfredZhong
 * @version 2012-07-05
 */
public class JsonUtils {

	private static final String TAG= JsonUtils.class.getSimpleName();
	
	public JsonUtils() {
		// Cannot instantiate.
	}
	
	public static Object get(JSONObject jsonObj, String name) {
		try {
			// if no such mapping exists will throw JSONException.
			return jsonObj.get(name);
		} catch (JSONException e) {
			Log.e(TAG, "Can not get value of the " + name + ", " + e);
			return null;
		}
	}
	
	public static boolean getBoolean(JSONObject jsonObj, String name, boolean defaultValue) {
		try {
			// if the mapping doesn't exist or cannot be coerced to a boolean will throw JSONException.
			return jsonObj.getBoolean(name);
		} catch (JSONException e) {
			Log.e(TAG, "Can not get value of the " + name + ", " + e);
			return defaultValue;
		}
	}
	
	public static int getInt(JSONObject jsonObj, String name, int defaultValue) {
		try {
			return jsonObj.getInt(name);
		} catch (JSONException e) {
			Log.e(TAG, "Can not get value of the " + name + ", " + e);
			return defaultValue;
		}
	}
	
	public static long getLong(JSONObject jsonObj, String name, long defaultValue) {
		try {
			return jsonObj.getLong(name);
		} catch (JSONException e) {
			Log.e(TAG, "Can not get value of the " + name + ", " + e);
			return defaultValue;
		}
	}
	
	public static float getFloat(JSONObject jsonObj, String name, float defaultValue) {
		try {
			return (float)jsonObj.getDouble(name);
		} catch (JSONException e) {
			Log.e(TAG, "Can not get value of the " + name + ", " + e);
			return defaultValue;
		}
	}
	
	public static double getDouble(JSONObject jsonObj, String name, double defaultValue) {
		try {
			return jsonObj.getDouble(name);
		} catch (JSONException e) {
			Log.e(TAG, "Can not get value of the " + name + ", " + e);
			return defaultValue;
		}
	}
	
	public static String getString(JSONObject jsonObj, String name) {
		try {
			return jsonObj.getString(name);
		} catch (JSONException e) {
			Log.e(TAG, "Can not get value of the " + name + ", " + e);
			return null;
		}
	}
	
	public static JSONObject getJSONObject(JSONObject jsonObj, String name) {
		try {
			return jsonObj.getJSONObject(name);
		} catch (JSONException e) {
			Log.e(TAG, "Can not get value of the " + name + ", " + e);
			return null;
		}
	}
	
	public static JSONArray getJSONArray(JSONObject jsonObj, String name) {
		try {
			return jsonObj.getJSONArray(name);
		} catch (JSONException e) {
			Log.e(TAG, "Can not get value of the " + name + ", " + e);
			return null;
		}
	}
	
}
