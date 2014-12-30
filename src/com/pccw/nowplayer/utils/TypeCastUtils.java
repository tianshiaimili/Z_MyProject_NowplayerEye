package com.pccw.nowplayer.utils;

/**
 * @author AlfredZhong
 * @version 2013-02-21
 */
public class TypeCastUtils {

	private TypeCastUtils() {
		
	}

	private static final String handleStringSpace(String value) {
		if(value != null) {
			// trim() only replace two edges.
			value = value.replace(" ", "");
		}
		// if value is null, just return it.
		return value;
	}
	
	/**
	 * Parses the specified string as a signed decimal integer value. 
	 * The ASCII character \u002d ('-') is recognized as the minus sign.
	 * 
	 * @param value the string representation of an integer value.
	 * @param defaultValue default integer value if cast failed.
	 * @return the integer value represented by string.
	 */
	public static final Integer strToInt(String value, Integer defaultValue) {
		try {
			return Integer.parseInt(handleStringSpace(value));
		} catch (Exception e) {
			// you can set default value null if you want to know exception occurs and handle it outside.
			return defaultValue;
		}
	}
	
	/**
	 * Parses the specified string as a double value.
	 * 
	 * @param value the string representation of a double value.
	 * @param defaultValue default double value if cast failed.
	 * @return the double value represented by string.
	 */
	public static final Double strToDouble(String value, Double defaultValue) {
		try {
			return Double.parseDouble(handleStringSpace(value));
		} catch (Exception e) {
			// you can set default value null if you want to know exception occurs and handle it outside.
			return defaultValue;
		}
	}
	
}
