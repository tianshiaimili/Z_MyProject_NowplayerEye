package com.pccw.nowplayer.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A helper class to handle URL.
 * 
 * @author AlfredZhong
 * @version 2012-08-08
 */
public class URLUtils {

	private URLUtils() {}
	
	/**
	 * Encode URL string.
	 * 注意: 类似'/', ':', '?'等都会给encode的, 所以encode url的get, post参数值即可(参数名不需encode),
	 * 不能encode整个url, 否则url经过encode会导致MalformedURLException.
	 * 
	 * @param url
	 * @return
	 * @see http://www.w3schools.com/tags/ref_urlencode.asp
	 */
	public static String encodeURL(String url) {
		/*
		 * Note:
		 * 1. URLs can only be sent over the Internet using the ASCII character-set(a 7-bit character set containing 128 characters).
		 * 2. Since URLs often contain characters outside the ASCII set, the URL has to be converted into a valid ASCII format.
		 * 3. URL encoding replaces unsafe ASCII characters with a "%" followed by two hexadecimal digits.
		 * 4. URLs cannot contain spaces. URL encoding normally replaces a space with a + sign.
		 * 
		 * 关于replace()和replaceAll():
		 * 1. replace()和replaceAll()都是全部替换,即把源字符串中的某一字符或字符串全部换成指定的字符或字符串;
		 *    但要注意replace()的参数是直接的字符串, replaceAll()的参数是正则表达式的regex.
		 *    因此这里要用replace("+", "%20")或者replaceAll("\\+", "%20").
		 * 2. replace()和replaceAll()的性能取决于要处理的字符串的量, 
		 *    当字符串的量比较大时，例如说成千上万字, replaceAll()性能优于replace(),
		 *    一般情况下, 字符串较短时, replace()性能略高于replaceAll().
		 *    由于url一般比较简短, 使用replace()比较适合.
		 */
		return URLEncoder.encode(url).replace("+", "%20");
	}
	
	/**
	 * Encodes s using the Charset named by charsetName.
	 * 注意: 类似'/', ':', '?'等都会给encode的, 所以encode url的get, post参数值即可(参数名不需encode),
	 * 不能encode整个url, 否则url经过encode会导致MalformedURLException.
	 * 
	 * @param url
	 * @param charsetName
	 * @return
	 * @throws UnsupportedEncodingException
	 * @see http://www.w3schools.com/tags/ref_urlencode.asp
	 */
	public static String encodeURL(String url, String charsetName) throws UnsupportedEncodingException {
		return URLEncoder.encode(url, charsetName).replace("+", "%20");
	}
	
}
