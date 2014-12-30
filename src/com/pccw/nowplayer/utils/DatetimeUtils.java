package com.pccw.nowplayer.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatetimeUtils {

	private DatetimeUtils() {}
	
	public static Date parseDateString(String dateStr, String pattern) {
		return parseDateString(dateStr, pattern, Locale.getDefault());
	}
	
	public static Date parseDateString(String dateStr, String pattern, Locale locale) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, locale);
		return dateFormat.parse(dateStr, new ParsePosition(0));
	}
	
	public static String formatDate(Date date, String pattern) {
		return formatDate(date, pattern, Locale.getDefault());
	}
	
	public static String formatDate(Date date, String pattern, Locale locale) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, locale);
		return dateFormat.format(date);
	}
	
    /**
     * Returns day in week in Chinese, like '一', '二', '三', '四', '五', '六', '日'.
     * Since SimpleDateFormat pattern 'E' may return like "星期x" or "周x" or other prefix,
     * you can use this function just to get 'x' and ignore the prefix.
     * 
     * @param date
     * @return
     */
	public static char getChineseDayInWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int dayInWeek = c.get(Calendar.DAY_OF_WEEK);
		switch(dayInWeek) {
		case Calendar.MONDAY:
			return '一';
		case Calendar.TUESDAY:
			return '二';
		case Calendar.WEDNESDAY:
			return '三';
		case Calendar.THURSDAY:
			return '四';
		case Calendar.FRIDAY:
			return '五';
		case Calendar.SATURDAY:
			return '六';
		case Calendar.SUNDAY:
		default:
			return '日';
		}
	}
	
	/**
	 * Returns the first letter of day in week.
	 * For example, Monday will return 'M'.
	 * @param date
	 * @return
	 */
	public static char getEnglishDayInWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int dayInWeek = c.get(Calendar.DAY_OF_WEEK);
		switch(dayInWeek) {
		case Calendar.MONDAY:
			return 'M';
		case Calendar.TUESDAY:
			return 'T';
		case Calendar.WEDNESDAY:
			return 'W';
		case Calendar.THURSDAY:
			return 'T';
		case Calendar.FRIDAY:
			return 'F';
		case Calendar.SATURDAY:
			return 'S';
		case Calendar.SUNDAY:
		default:
			return 'S';
		}
	}
	
	public static Date moveToDate(int field, int amount) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(field, amount);
		return calendar.getTime();
	}
	
	public static int getCalendarField(Date date, int field) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(field);
	}
	
	public static String transMillionToTime(long milliseconds) {
		int seconds = (int)milliseconds / 1000;
		int minute = seconds / 60;
		int second = seconds % 60;
		String minStr = minute < 10 ? "0" + minute : String.valueOf(minute);
		String secStr = second < 10 ? "0" + second : String.valueOf(second);
		String time = minStr + ":" + secStr;
		return time;
	}
}
