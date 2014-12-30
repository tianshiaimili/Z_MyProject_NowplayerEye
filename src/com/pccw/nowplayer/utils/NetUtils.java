package com.pccw.nowplayer.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

/**
 * <pre>
 * A Helper class to handle Network operation:
 * 1. get HttpURLConnection or HttpClient and other net operation about HTTP/HTTPS.
 * 2. monitor/check network status.
 * NOTE: 
 * HTTP Request/Response : HTTP Header -- connect -- HTTP Content -- send.
 * 
 * You may need permission:
 * &lt;uses-permission android:name="android.permission.INTERNET" />
 * &lt;uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 * &lt;uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * </pre>
 * @author AlfredZhong
 * @version 1.0, 2012-01-04
 */
public class NetUtils {

	private static final String TAG = NetUtils.class.getSimpleName();
	/**
	 * Since this InputStream is HttpURLConnection InputStream, 
	 * read(int) often blocks several KB data(if the buffer is enough) before read.
	 * So 2KB is enough to buffer the data,
	 * a larger space is useless and waste memory space.
	 */
	private static final int REMOTE_STREAM_BUFFER = 2048;

	private NetUtils() {
		// Cannot instantiate.
	}
	
	/**
	 * Not to verify the hostname.
	 */
	private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	
	/** 
	 * Set the HttpsURLConnection default SSLSocketFactory trust every server. 
	 * Don't check for any certificate.
	 */ 
	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains.
		TrustManager[] trustAllCerts = new TrustManager[] { new AllTrustedX509TrustManager() };
		// Install the all-trusting trust manager.
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			// It is javax.net.ssl.SSLSocketFactory, not the org.apache.http.conn.ssl.SSLSocketFactory.
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			Log.e(TAG, "Fail to trustAllHosts.", e);
		}
	}
	
	/**
	 * Set HttpURLConnection Header to make server regard this HTTP request as a request from common browser.
	 * @param conn
	 */
	public static void setHeader(HttpURLConnection conn) {
		// NOTE: all these request property can reset before HttpURLConnection.connect().
		try {
			// Imitate a request as IE.
//			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
//			// Indicates the content has been URL encoded.
//			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
//			conn.setRequestProperty("Accept", "*/*");
//			conn.setRequestProperty("Connection", "keep-alive");
//			conn.setRequestProperty("Pragma", "no-cache");
//			conn.setRequestProperty("Cache-Control", "no-cache");
//			System.setProperty("http.keepAlive", "false");
			// Default request method is GET.
			conn.setRequestMethod("GET");
			// not to use caches in POST Method. Default true.
			conn.setUseCaches(false); 
			// URLConnection.setFollowRedirects() takes effect on every instance.
			conn.setInstanceFollowRedirects(true);
			// Whether allows connection to get InputStream. Default true.
			conn.setDoInput(true);
			// Whether allows connection to get OutputStream. Default false.
			conn.setDoOutput(false);
			conn.setConnectTimeout(0);
			conn.setReadTimeout(0);
		} catch (Exception e) {
			Log.e(TAG, "Fail to setHeader.", e);
		}
	}

	/**
	 * Get the specified URL HttpURLConnection no matter the protocol is "HTTP" or "HTTPS".
	 * 
	 * NOTE:
	 * The HttpURLConnection returned has not been call HttpURLConnection.connect().
	 * You should call it by yourself if you need and you can set request properties before calling connect().
	 * Besides, note that using HttpURLConnection may face the redirect problems.
	 * 
	 * @param urlStr
	 * @return HttpURLConnection
	 * @throws IOException
	 */
	public static HttpURLConnection getHttpURLConnection(String urlStr) throws IOException {
		URL url = null;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException : Can not create an URL from an incorrect specification of url " + urlStr);
			// Catch MalformedURLException just to record the problematic url, so throw an IOException here.
			// If not throw an IOException here, the following code will throw NullPointerException which caller may not catch.
			throw new IOException("NetUtils.getHttpURLConnection() failed with an incorrect specification url.");
		}
		HttpURLConnection http = null;
		if (url.getProtocol().toLowerCase().equals("https")) {
			// setDefaultSSLSocketFactory for HttpsURLConnection to trust every host.
			trustAllHosts();
			HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
			// Not to verify the hostname.
			https.setHostnameVerifier(DO_NOT_VERIFY);
			http = https;
		} else {
			// get a connection.
			http = (HttpURLConnection) url.openConnection();
		}
		setHeader(http);
		return http;
	}
	
	public static HttpURLConnection getHttpURLConnection(String urlStr, Map<String, String> requestProperties) throws IOException {
		// Add this function on 2012-05-07.
		HttpURLConnection conn = getHttpURLConnection(urlStr);
		if(requestProperties != null && requestProperties.size() > 0) {
			Iterator<Entry<String, String>> iterator = requestProperties.entrySet().iterator();
			Entry<String, String> entry = null;
			while(iterator.hasNext()) {
				entry = iterator.next();
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		return conn;
	}
	
	public static InputStream getInputStream(String urlStr) throws IOException {
		HttpURLConnection conn = getHttpURLConnection(urlStr);
		// connect before sending requests.
		conn.connect();
		// Send HTTP request.
		return checkInputStream(conn);
	}
	
	public static InputStream getInputStream(String urlStr, Map<String, String> requestProperties) throws IOException {
		HttpURLConnection conn = getHttpURLConnection(urlStr, requestProperties);
		conn.connect();
		return checkInputStream(conn);
	}
	
	/**
	 * @param urlStr
	 * @param requestProperties
	 * @param connectTimeout the connecting timeout in milliseconds, 0 stands for an infinite timeout.
	 * @param readTimeout the reading timeout in milliseconds, 0 stands for an infinite timeout.
	 * @param attemptTimes total attempt times of getInputStream().
	 * @param interval retry interval in milliseconds.
	 * @return InputStream or null.
	 */
	public static InputStream getInputStream(String urlStr, Map<String, String> requestProperties, 
			int connectTimeout, int readTimeout, int attemptTimes, int interval) {
		// Add this function on 2012-05-07.
		int retry = 0;
		int maxTime = attemptTimes - 1;
		if(maxTime < 0) {
			maxTime = 0;
		}
		InputStream is = null;
		HttpURLConnection conn = null;
		while(retry <= maxTime) {
			try {
				conn = getHttpURLConnection(urlStr, requestProperties);
				conn.setConnectTimeout(connectTimeout);
				conn.setReadTimeout(readTimeout);
				conn.connect();
				is = checkInputStream(conn);
			} catch(Exception e) {
				Log.w(TAG, "getInputStream() failed with url : " + urlStr, e);
				is = null;
			}
			if(is != null) {
				break;
			} else {
				Log.w(TAG, "Attempt times(starts from 0) = " + retry);
				retry++;
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					Log.w(TAG, "Thread.sleep() occurs exception " + e);
				}
			}
		}
		return is;
	}
	
	/**
	 * Read InputStream by bytes.
	 * NOTE: If the returned bytes length is too big, it may throw OutOfMemoryError.
	 * Not to read bytes larger than 8M on Android platform.
	 * @param is
	 * @return byte[] or null.
	 * @throws IOException
	 */
	public static byte[] getByteArray(InputStream is) throws IOException {
    	if(is == null)
    		return null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[REMOTE_STREAM_BUFFER];  
        int len = 0;
        while( (len = is.read(buffer)) != -1){    
        	// Since here write bytes to memory, it may throw OutOfMemoryError if bytes too much.
        	// FileOutputStream won't have OOM problem, since it writes bytes to disk instead of memory.
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        is.close();
        return outStream.toByteArray();
    }
	
	/**
	 * Read InputStream by bytes.
	 * NOTE: If the returned bytes length is too big, it may throw OutOfMemoryError.
	 * Not to read bytes larger than 8M on Android platform.
	 * @param urlStr
	 * @return byte[] or null.
	 * @throws IOException 
	 */
	public static byte[] getByteArray(String urlStr) throws IOException {
		return getByteArray(getInputStream(urlStr));
	}
	
	/**
	 * @param urlStr
	 * @param requestProperties
	 * @param connectTimeout the connecting timeout in milliseconds, 0 stands for an infinite timeout.
	 * @param readTimeout the reading timeout in milliseconds, 0 stands for an infinite timeout.
	 * @param attemptTimes total attempt times of getByteArray().
	 * @param interval retry interval in milliseconds.
	 * @return byte[] or null.
	 */
	public static byte[] getByteArray(String urlStr, Map<String, String> requestProperties, 
			int connectTimeout, int readTimeout, int attemptTimes, int interval) {
		// Add this function on 2012-05-07.
		int retry = 0;
		int maxTime = attemptTimes - 1;
		if(maxTime < 0) {
			maxTime = 0;
		}
		byte[] bytes = null;
		HttpURLConnection conn = null;
		while(retry <= maxTime) {
			try {
				conn = getHttpURLConnection(urlStr, requestProperties);
				conn.setConnectTimeout(connectTimeout);
				conn.setReadTimeout(readTimeout);
				conn.connect();
				bytes = getByteArray(checkInputStream(conn));
			} catch(Exception e) {
				Log.w(TAG, "getByteArray() failed with url : " + urlStr, e);
				bytes = null;
			}
			if(bytes != null) {
				break;
			} else {
				Log.w(TAG, "Attempt times(starts from 0) = " + retry);
				retry++;
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					Log.w(TAG, "Thread.sleep() occurs exception " + e);
				}
			}
		}
		return bytes;
	}
	
	/**
	 * Get range bytes of InputStream from startPos to endPos in byte.
	 * 
	 * @param urlStr
	 * @param start
	 * @param end
	 * @return
	 * @throws IOException
	 */
	public static InputStream getRangeInputStream(String urlStr, Map<String, String> requestProperties,
			int startPos, int endPos) throws IOException {
		if(requestProperties != null) {
			requestProperties.put("Range", "bytes=" + startPos + "-" + (endPos == 0 ? "" : endPos));
			return getInputStream(urlStr, requestProperties);
		}
		HttpURLConnection conn = getHttpURLConnection(urlStr);
		conn.setRequestProperty("Range", "bytes=" + startPos + "-" + (endPos == 0 ? "" : endPos));
		conn.connect();
		return checkInputStream(conn);
	}
	
	/**
	 * Construct a GZIPInputStream to read from GZIP data from the underlying stream with the specified HttpURLConnection.
	 * @param urlStr
	 * @return
	 * @throws IOException 
	 */
	public static InputStream getGZIPInputStream(HttpURLConnection conn) throws IOException {
		conn.addRequestProperty("Accept-Encoding", "gzip, deflate");
		conn.connect();
		return new GZIPInputStream(checkInputStream(conn));
	}
	
	/**
	 * Construct a GZIPInputStream to read from GZIP data from the underlying stream with the specified url.
	 * @param urlStr
	 * @return
	 * @throws IOException 
	 */
	public static InputStream getGZIPInputStream(String urlStr) throws IOException {
		HttpURLConnection conn = getHttpURLConnection(urlStr);
		conn.addRequestProperty("Accept-Encoding", "gzip, deflate");
		conn.connect();
		return new GZIPInputStream(checkInputStream(conn));
	}
	
	/**
	 * Check InputStream is at EOF.
	 * Note that this InputStream will read one line in this function,
	 * so you can only use this function when you don't need the InputStream,
	 * but just to check it.
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static boolean checkInputStreamEOF(InputStream is) throws IOException {
		boolean retval = false;
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(reader);
		if(br.readLine() == null) {
			Log.e(TAG, "InputStream has already reached the end.");
			retval = true;
		} else {
			Log.d(TAG, "InputStream is readable.");
			retval = false;
		}
		is.close();
		reader.close();
		br.close();
		return retval;
	}
	
	/**
	 * Read InputStream by line.
	 * @param is
	 * @param encoding such as "utf-8".
	 * @return InputStream content.
	 * @throws Exception
	 */
	public static String readInputStream(InputStream is, String encoding) throws IOException {
		StringBuilder text = new StringBuilder("");
		InputStreamReader reader = new InputStreamReader(is, encoding);
		BufferedReader br = new BufferedReader(reader);
		String str = null;
		while ((str = br.readLine()) != null) {
			if (text.length() > 0)
				text.append("\n");
			text.append(str);
		}
		is.close();
		reader.close();
		br.close();
		return text.toString();
	}
	
	/**
	 * Print the content of InputStream and return a copy of this InputStream.
	 * <p>You'd better not to print large size InputStream.
	 * @param is
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static InputStream printInputStream(InputStream is, String encoding) throws IOException {
		byte[] bytes = getByteArray(is);
		System.out.println(new String(bytes, encoding));
		return new ByteArrayInputStream(bytes);
	}
	
	/**
	 * A helper class which is used for constructing HTTP POST request which enctype="multipart/form-data"
	 * (No characters are encoded) and writes data to URLConnection.
	 * <p>
	 * HTTP header requires:
	 * <p>
	 * Content-Type: multipart/form-data; boundary=---------------------------305241254917469
	 * <p>
	 * multipart/form-data example:
	 * <pre>
	 * -----------------------------305241254917469
	 * Content-Disposition: form-data; name="username"
	 * \r\n
	 * This is StringPart data1.
	 * -----------------------------305241254917469
	 * Content-Disposition: form-data; name="playlistname"
	 * \r\n
	 * This is StringPart data2.
	 * -----------------------------305241254917469
	 * Content-Disposition: form-data; name="playlist"; filename="playlist.txt"
	 * Content-Type: text/plain; charset=utf-8
	 * \r\n
	 * This is FilePart data.
	 * -----------------------------305241254917469
	 * Content-Disposition: form-data; name="files"
	 * Content-Type: multipart/mixed; boundary=TZOTZTOF20120215
	 * \r\n
	 * --TZOTZTOF20120215
	 * Content-Disposition: file; filename="file1.txt"
	 * Content-Type: text/plain
	 * \r\n
	 * This is FilePart data1 in mixed.
	 * --TZOTZTOF20120215
	 * Content-Disposition: file; filename="file2.gif"
	 * Content-Type: image/gif
	 * Content-Transfer-Encoding: binary
	 * \r\n
	 * This is FilePart data2 in mixed.
	 * --TZOTZTOF20120215--
	 * -----------------------------305241254917469--
	 * \r\n
	 * </pre>
	 * <pre>
	 * NOTE That: 
	 * 1. Before sending form-data, you have to set http header request method "POST" and Content-Type.
	 * 2. "---------------------------305241254917469" is a boundary which for server to find the start position of every form content.
	 *    The boundary is just a random key. Every key is OK if it is complex enough.
	 * 3. "multipart/form-data" may contains many start_boundary represented "--boundary", 
	 *    but only one end boundary represented "--boundary--".
	 * 4. You can not neglect the "\r\n".
	 * 5. Every part from the boundary to the next boundary(not including this boundary) is a form-data item.
	 * 6. If enctype="application/x-www-form-urlencoded"(The default enctype of HTTP form),
	 *    all characters are encoded before sent(spaces are converted to "+" symbols, 
	 *    and special characters are converted to ASCII HEX values).
	 *    Just write parameters like "a=xxx&b=yyy&c=zzz" to an URLConnetion.
	 *    "application/x-www-form-urlencoded" post example:
	 *    Content-Type: application/x-www-form-urlencoded 
	 *    Content-Length: 52
	 *    name=yourname&mail=yourmail&comment=Alfred+%26+Aaron
	 *    \r\n
	 * 7. If enctype="text/plain", Spaces are converted to "+" symbols, but no special characters are encoded.
	 *    Note: They are not reliably interpretable by computer, as the format is ambiguous(for example, 
	 *    there is no way to distinguish a literal newline in a value from the newline at the end of the value).
	 *    "text/plain" post example:
	 *    name=alfred
	 *    mail=alfred@msn.com
	 *    comment=helloworld
	 *    \r\n
	 * 
	 * For details on how to interpret multipart/form-data payloads, see RFC 2388 and 1867.
	 * http://www.ietf.org/rfc/rfc2388.txt
	 * </pre>
	 * 
	 * @author AlfredZhong
	 * @version 1.0, 2012-01-17
	 * @version 1.1, 2012-02-15, support Content-Type "multipart/mixed".
	 */
	public static final class HttpMultipart {
		
		// static fields
		private static final String LINE_SEPERATOR = "\r\n";
		private static final String BOUNDARY = "---------------------------305241254917469";
		private static final String START_BOUNDARY = "--" + BOUNDARY + LINE_SEPERATOR;
		private static final String END_BOUNDARY = "--" + BOUNDARY + "--" + LINE_SEPERATOR;
		private static final String BOUNDARY_MIXED = "TZOTZTOF20120215"; // boundary for multipart/mixed.
		private static final String START_BOUNDARY_MIXED = "--" + BOUNDARY_MIXED + LINE_SEPERATOR;
		private static final String END_BOUNDARY_MIXED = "--" + BOUNDARY_MIXED + "--" + LINE_SEPERATOR;
		public static final String HTTP_CONTENT_TYPE = "multipart/form-data; boundary=" + BOUNDARY;
		// form-data fields.
		private String name;
		private String filename;
		private String contentType;
		private String contentTransferEncoding;
		// May be we should add field File file to support InputStream.
		private byte[] data; 
		private List<HttpMultipart> mixed;
		
		/**
		 * Create multipart/form-data item.
		 * 
		 * <p>For example, to send form as this class mentioned above, you can code as follow:
		 * <pre>
		 * List&lt;HttpMultipart> postbody = new ArrayList&lt;HttpMultipart>();
		 * postbody.add(new HttpMultipart("username", null, null, null, "This is StringPart data1.".getBytes()));
		 * postbody.add(new HttpMultipart("playlistname", null, null, null, "This is StringPart data2.".getBytes()));
		 * postbody.add(new HttpMultipart("playlist", "playlist.txt", "text/plain; charset=utf-8", null, "This is FilePart data.".getBytes()));
		 * List&lt;HttpMultipart> mixed = new ArrayList&lt;HttpMultipart>();
		 * postbody.add(new HttpMultipart("files", null, "multipart/mixed; boundary=TZOTZTOF20120215", null, mixed));
		 * mixed.add(new HttpMultipart(null, "file1.txt", "text/plain", null, "This is FilePart data1 in mixed.".getBytes()));
		 * mixed.add(new HttpMultipart(null, "file2.gif", "image/gif", "binary", "This is FilePart data2 in mixed.".getBytes()));
		 * </pre>
		 * 
		 * @param name multipart/form-data content name.
		 * @param filename multipart/form-data content filename.
		 * @param contentType multipart/form-data Content-Type, which defaults to text/plain.  
		 * If the contents of a file are returned via filling out a form, then the file input is
		 * identified as the appropriate media type, if known, or "application/octet-stream".  
		 * If multiple files are to be returned as the result of a single form entry, 
		 * they should be represented as a "multipart/mixed" part embedded within the "multipart/form-data".
		 * @param contentTransferEncoding multipart/form-data Content-Transfer-Encoding.
		 * Each part may be encoded and the "content-transfer-encoding" header supplied 
		 * if the value of that part does not conform to the default encoding.
		 * @param data StringPart as bytes or file binary data.
		 */
		public HttpMultipart(String name, String filename, String contentType, 
				String contentTransferEncoding, byte[] data) {
			this.name = name;
			this.filename = filename;
			this.contentType = contentType;
			this.contentTransferEncoding = contentTransferEncoding;
			this.data = data;
		}
		
		/**
		 * Create form-data item which Content-Type is multipart/mixed.
		 * 
		 * @param name
		 * @param filename
		 * @param contentType
		 * @param contentTransferEncoding
		 * @param mixed multipart/mixed contents
		 * 
		 * @see #HttpMultipart(String, String, String, String, byte[])
		 */
		public HttpMultipart(String name, String filename, String contentType, 
				String contentTransferEncoding, List<HttpMultipart> mixed) {
			this.name = name;
			this.filename = filename;
			this.contentType = contentType;
			this.contentTransferEncoding = contentTransferEncoding;
			this.mixed = mixed;
		}
		
		/**
		 * Write mixed form-data.
		 * Pay attention to Content-Type in the owner, and Content-Disposition in every parts.
		 */
		private static void writeMixed(OutputStream out, List<HttpMultipart> postbody, String charsetName)
				throws UnsupportedEncodingException, IOException {
			for (HttpMultipart formdata : postbody) {
				out.write(START_BOUNDARY_MIXED.getBytes(charsetName));
				if(formdata.filename != null) {
					// Mixed part is file, and does not have name.
					out.write(("Content-Disposition: file; " + "filename=\"" + formdata.filename 
							+ "\"" + LINE_SEPERATOR).getBytes(charsetName));
				}
				if(formdata.contentType != null) {
					out.write(("Content-Type: " + formdata.contentType + LINE_SEPERATOR).getBytes(charsetName));
				}
				if(formdata.contentTransferEncoding != null) {
					out.write(("Content-Transfer-Encoding: " + formdata.contentTransferEncoding).getBytes(charsetName));
					out.write(LINE_SEPERATOR.getBytes(charsetName));
				}
				out.write(LINE_SEPERATOR.getBytes(charsetName)); // can not be ignored.
				// begin writing data.
				out.write(formdata.data);
				// data does not have LINE_SEPERATOR, add it.
				out.write(LINE_SEPERATOR.getBytes(charsetName)); 
			}
			// end of writing mixed parts, add end_boundary_mixed here.
			out.write(END_BOUNDARY_MIXED.getBytes(charsetName));
		}
		
		/**
		 * Write multipart/form-data to an URLConnection.
		 * 
		 * @param out an OutputStream for writing data to an URLConnection. 
		 * @param postbody multipart/form-data.
		 * @param charsetName the form accept-charset.
		 * @throws UnsupportedEncodingException
		 * @throws IOException
		 */
		private static void writePost(OutputStream out, List<HttpMultipart> postbody, String charsetName)
				throws UnsupportedEncodingException, IOException {
			if (out == null || postbody == null || postbody.size() == 0) {
				System.out.println("out == null || postbody == null || postbody.size() == 0.");
				return;
			}
			for (HttpMultipart formdata : postbody) {
				out.write(START_BOUNDARY.getBytes(charsetName));
				if(formdata.filename != null) {
					// FilePart, usually form input type is "file".
					out.write(("Content-Disposition: form-data; name=\"" + formdata.name + "\"; " 
							+ "filename=\"" + formdata.filename + "\"" + LINE_SEPERATOR).getBytes(charsetName));
				} else {
					// StringPart, usually form input type is "text".
					out.write(("Content-Disposition: form-data; name=\"" + formdata.name + "\"" + LINE_SEPERATOR).getBytes(charsetName));
					// no need to write Content-Type.
				}
				if(formdata.contentType != null) {
					if(formdata.contentType.contains("multipart/mixed")) {
						// Replace boundary if contentType is mixed, use local mixed boundary.
						formdata.contentType = "multipart/mixed; boundary=" + BOUNDARY_MIXED;
					}
					out.write(("Content-Type: " + formdata.contentType + LINE_SEPERATOR).getBytes(charsetName));
				}
				if(formdata.contentTransferEncoding != null) {
					out.write(("Content-Transfer-Encoding: " + formdata.contentTransferEncoding).getBytes(charsetName));
					out.write(LINE_SEPERATOR.getBytes(charsetName));
				}
				out.write(LINE_SEPERATOR.getBytes(charsetName)); // can not be ignored.
				// begin writing data.
				if(formdata.data != null) {
					out.write(formdata.data);
					// data does not have LINE_SEPERATOR, add it.
					out.write(LINE_SEPERATOR.getBytes(charsetName)); 
				} else if(formdata.mixed != null && formdata.mixed.size() > 0) {
					// data is multipart/mixed.
					writeMixed(out, formdata.mixed, charsetName);
				}
			}
			// end of writing parts, add end_boundary here.
			out.write(END_BOUNDARY.getBytes(charsetName));
			out.write(LINE_SEPERATOR.getBytes(charsetName)); // can be ignored.
		}

	} // end of inner class HttpMultipart
	
	/**
	 * Make an HTTP POST request which form enctype="multipart/form-data" and get connection InputStream. 
	 * @param urlStr
	 * @param requestProperties HttpURLConnection request properties.
	 * @param postbody form multipart.
	 * @param charsetName the specified charset of the multipart content.
	 * @return the InputStream of HttpURLConnection.
	 * @throws IOException
	 */
	public static InputStream postMultipartForm(String urlStr, Map<String, String> requestProperties,
			List<HttpMultipart> postbody, String charsetName) throws IOException {
		HttpURLConnection conn = getHttpURLConnection(urlStr);
		// set request method POST, otherwise will get response code "405". 
		conn.setRequestMethod("POST");
		// allow output.
		conn.setDoOutput(true);
		boolean multipart = false;
		if (requestProperties != null && requestProperties.size() > 0) {
			Iterator<Entry<String, String>> iterator = requestProperties.entrySet().iterator();
			Entry<String, String> entry = null;
			while (iterator.hasNext()) {
				entry = iterator.next();
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		if(postbody != null && postbody.size() > 0) {
			multipart = true;
		}
		if(multipart) {
			Log.d(TAG, "getPostInputStream :: POST parameters not null.");
			// NOTE: set request properties before connect. otherwise will cause exception:
			// java.lang.IllegalStateException: Cannot set method after connection is made
			conn.setRequestProperty("Content-Type", HttpMultipart.HTTP_CONTENT_TYPE);
			// conn.setRequestProperty("Content-Length", Integer.toString(bytes.length));
		}
		// NOTE: establish connection before writing data.
		conn.connect();
		// write post parameters(HTTP request content).
		if(multipart) {
			/*
			 * If "read input" action before "write output", will throw
			 * java.net.ProtocolException: can't open OutputStream after reading from an inputStream
			 * NOTE: HttpURLConnection.getResponseCode() is one of the "read input" action.
			 */
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			HttpMultipart.writePost(out, postbody, charsetName);
			out.flush();
			out.close();
		}
		// send request.
		return checkInputStream(conn);
	}
	
	/**
	 * NOTE: the params value will be URL encoded, you should not encode them.
	 * @param params
	 * @param getMethod whether the params posted by GET method, if false by POST method.
	 * @return the parameters string for GET(such as"?a=xxx&b=yyy") 
	 * 		   or "application/x-www-form-urlencoded" POST(such as"a=xxx&b=yyy") method.
	 */
	public static String getParams(Map<String, String> params, boolean getMethod) {
		if(params == null)
			return null;
		StringBuilder paramsStr = null;
		int size = 0;
		if (params != null && (size = params.size()) > 0) {
			paramsStr = new StringBuilder();
			List<String> parameters = new ArrayList<String>();
			Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
			Entry<String, String> entry = null;
			while (iterator.hasNext()) {
				entry = iterator.next();
				// remember encoding the param value.
				parameters.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue()));
			}
			if(getMethod) {
				// paramsStr is get params like "?a=xxx&b=yyy&c=zzz".
				paramsStr.append("?");
			}
			// paramsStr is post params like "a=xxx&b=yyy&c=zzz". Do not add "?" before the first param. 
			paramsStr.append(parameters.get(0));
			for(int i=1; i<size; i++) {
				paramsStr.append("&").append(parameters.get(i));
			}
		}
		if(paramsStr != null) {
			return paramsStr.toString();
		}
		return null;
	}
	
	/**
	 * Make an HTTP POST request which form enctype="application/x-www-form-urlencoded" and get connection InputStream. 
	 * @param urlStr
	 * @param requestProperties HttpURLConnection request properties.
	 * @param postParams form parameters.
	 * @param charsetName the specified charset of the multipart content.
	 * @return the InputStream of HttpURLConnection.
	 * @throws IOException
	 */
	public static InputStream postApplicationForm(String urlStr, Map<String, String> requestProperties,
			String postParams, String charsetName) throws IOException {
		HttpURLConnection conn = getHttpURLConnection(urlStr);
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		if (requestProperties != null && requestProperties.size() > 0) {
			Iterator<Entry<String, String>> iterator = requestProperties.entrySet().iterator();
			Entry<String, String> entry = null;
			while (iterator.hasNext()) {
				entry = iterator.next();
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		conn.connect();
		if(postParams != null) {
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.write(postParams.getBytes(charsetName));
			out.flush();
			out.close();
		}
		return checkInputStream(conn);
	}
	
	/**
	 * Print response code and its meaning.
	 * @param responseCode
	 * @return
	 * @see http://www.askapache.com/htaccess/apache-status-code-headers-errordocument.html
	 */
	private static String checkResponseCode(int responseCode) {
		String message = null;
		switch(responseCode) {
		// 1xx, Info/Informational
		case 100:
			// The server has received the request headers, and that the client should proceed to send the request body.
			message = "Continue";
			break;
		case 101:
			// The requester has asked the server to switch protocols and the server is acknowledging that it will do so.
			message = "Switching Protocols";
			break;
		case 102:
			message = "Processing";
			break;
		// 2xx, Success/OK
		case 200:
			// Standard response for successful HTTP requests.
			message = "OK";
			break;
		case 201:
			// The request has been fulfilled and resulted in a new resource being created. 
			message = "Created";
			break;
		case 202:
			// The request has been accepted for processing, but the processing has not been completed.
			message = "Accepted";
			break;
		case 203:
			// The server successfully processed the request, but is returning information that may be from another source.
			message = "Non-Authoritative Information";
			break;
		case 204:
			// The server successfully processed the request, but is not returning any content.
			message = "No Content";
			break;
		case 205:
			// The server successfully processed the request, but is not returning any content. 
			// Unlike a 204 response, this response requires that the requester reset the document view. 
			message = "Reset Content";
			break;
		case 206:
			// The server is delivering only part of the resource due to a range header sent by the client.
			message = "Partial Content";
			break;
		case 207:
			/*
			 * The message body that follows is an XML message and can contain a number of separate response codes, 
			 * depending on how many sub-requests were made.
			 */
			message = "Multi-Status";
			break;
		case 226:
			/*
			 * The server has fulfilled a GET request for the resource, and the response is a representation 
			 * of the result of one or more instance-manipulations applied to the current instance. 
			 * The actual current instance might not be available except by combining this response 
			 * with other previous or future responses, as appropriate for the specific instance-manipulation(s). 
			 */
			message = "IM Used";
			break;
		// 3xx, Redirect
		case 300:
			/*
			 * Indicates multiple options for the resource that the client may follow. 
			 * It, for instance, could be used to present different format options for video, 
			 * list files with different extensions, or word sense disambiguation. 
			 */
			message = "Multiple Choices";
			break;
		case 301:
			// This and all future requests should be directed to the given URI.
			message = "Moved Permanently";
			break;
		case 302:
			/*
			 * This is the most popular redirect code[citation needed], but also an example of industrial practice contradicting the standard. 
			 * HTTP/1.0 specification (RFC 1945 ) required the client to perform a temporary redirect 
			 * (the original describing phrase was "Moved Temporarily"), but popular browsers implemented it as a 303 See Other. 
			 * Therefore, HTTP/1.1 added status codes 303 and 307 to disambiguate between the two behaviours. 
			 * However, the majority of Web applications and frameworks still use the 302 status code as if it were the 303.  
			 */
			message = "Found";
			break;
		case 303:
			/*
			 * The response to the request can be found under another URI using a GET method. 
			 * When received in response to a PUT, it should be assumed that 
			 * the server has received the data and the redirect should be issued with a separate GET message. 
			 */
			message = "See Other";
			break;
		case 304:
			/*
			 * Indicates the resource has not been modified since last requested. 
			 * Typically, the HTTP client provides a header like the If-Modified-Since header to provide a time against which to compare. 
			 * Utilizing this saves bandwidth and reprocessing on both the server and client, as only the header data must be sent 
			 * and received in comparison to the entirety of the page being re-processed by the server, 
			 * then resent using more bandwidth of the server and client. 
			 */
			message = "Not Modified";
			break;
		case 305:
			/*
			 * Many HTTP clients (such as Mozilla[4] and Internet Explorer) do not correctly 
			 * handle responses with this status code, primarily for security reasons. 
			 */
			message = "Use Proxy";
			break;
		case 306:
			// No longer used.
			message = "Switch Proxy";
			break;
		case 307:
			/*
			 * In this occasion, the request should be repeated with another URI, but future requests can still use the original URI. 
			 * In contrast to 303, the request method should not be changed when reissuing the original request. 
			 * For instance, a POST request must be repeated using another POST request. 
			 */
			message = "Temporary Redirect";
			break;
		// 4xx, Client Error
		case 400:
			// The request contains bad syntax or cannot be fulfilled.
			message = "Bad Request";
			break;
		case 401:
			/*
			 * Similar to 403 Forbidden, but specifically for use when authentication is possible but has failed or not yet been provided. 
			 * The response must include a WWW-Authenticate header field containing a challenge applicable to the requested resource. 
			 * See Basic access authentication and Digest access authentication.
			 */
			message = "Unauthorized";
			break;
		case 402:
			/*
			 * The original intention was that this code might be used as part of some form of digital cash or micropayment scheme, 
			 * but that has not happened, and this code has never been used.
			 */
			message = "Payment Required";
			break;
		case 403:
			/*
			 * The request was a legal request, but the server is refusing to respond to it. 
			 * Unlike a 401 Unauthorized response, authenticating will make no difference.
			 */
			message = "Forbidden";
			break;
		case 404:
			/*
			 * The requested resource could not be found but may be available again in the future. 
			 * Subsequent requests by the client are permissible. 
			 */
			message = "Not Found";
			break;
		case 405:
			/*
			 * A request was made of a resource using a request method not supported by that resource; 
			 * for example, using GET on a form which requires data to be presented via POST, or using PUT on a read-only resource.
			 */
			message = "Method Not Allowed";
			break;
		case 406:
			/*
			 * The requested resource is only capable of generating content not acceptable 
			 * according to the Accept headers sent in the request.
			 */
			message = "Not Acceptable";
			break;
		case 407:
			// Required.
			message = "Proxy Authentication Required";
			break;
		case 408:
			// The server timed out waiting for the request.
			message = "Request Timeout";
			break;
		case 409:
			// Indicates that the request could not be processed because of conflict in the request, such as an edit conflict. 
			message = "Conflict";
			break;
		case 410:
			/*
			 * Indicates that the resource requested is no longer available and will not be available again. 
			 * This should be used when a resource has been intentionally removed; 
			 * however, it is not necessary to return this code and a 404 Not Found can be issued instead. 
			 * Upon receiving a 410 status code, the client should not request the resource again in the future. 
			 * Clients such as search engines should remove the resource from their indexes. 
			 */
			message = "Gone";
			break;
		case 411:
			// The request did not specify the length of its content, which is required by the requested resource.
			message = "Length Required";
			break;
		case 412:
			// The server does not meet one of the preconditions that the requester put on the request.
			message = "Precondition Failed";
			break;
		case 413:
			// The request is larger than the server is willing or able to process.
			message = "Request Entity Too Large";
			break;
		case 414:
			// The URI provided was too long for the server to process.
			message = "Request-URI Too Long";
			break;
		case 415:
			/*
			 * The request did not specify any media types that the server or resource supports.
			 * For example the client specified that an image resource should be served as image/svg+xml,
			 * but the server cannot find a matching version of the image. 
			 */
			message = "Unsupported Media Type";
			break;
		case 416:
			/*
			 * The client has asked for a portion of the file, but the server cannot supply that portion 
			 * (for example, if the client asked for a part of the file that lies beyond the end of the file). 
			 */
			message = "Requested Range Not Satisfiable";
			break;
		case 417:
			// The server cannot meet the requirements of the Expect request-header field. 
			message = "Expectation Failed";
			break;
		case 418:
			/*
			 * The HTCPCP server is a teapot. The responding entity MAY be short and stout. 
			 * Defined by the April Fools specification RFC 2324. 
			 * See Hyper Text Coffee Pot Control Protocol for more information. 
			 */
			message = "I'm a teapot";
			break;
		case 422:
			// The request was well-formed but was unable to be followed due to semantic errors.
			message = "Unprocessable Entity";
			break;
		case 423:
			// The resource that is being accessed is locked.
			message = "Locked";
			break;
		case 424:
			// The request failed due to failure of a previous request (e.g. a PROPPATCH).
			message = "Failed Dependency";
			break;
		case 425:
			/*
			 * Defined in drafts of WebDav Advanced Collections, but not present in 
			 * "Web Distributed Authoring and Versioning (WebDAV) Ordered Collections Protocol" (RFC 3648). 
			 */
			message = "Unordered Collection";
			break;
		case 426:
			// The client should switch to TLS/1.0.
			message = "Upgrade Required";
			break;
		case 449:
			// A Microsoft extension. The request should be retried after doing the appropriate action.
			message = "Retry With";
			break;
		// 5xx, Sever Error
		case 500:
			// A generic error message, given when no more specific message is suitable.
			message = "Internal Server Error";
			break;
		case 501:
			// The server either does not recognise the request method, or it lacks the ability to fulfil the request. 
			message = "Not Implemented";
			break;
		case 502:
			// The server was acting as a gateway or proxy and received an invalid response from the upstream server.
			message = "Bad Gateway";
			break;
		case 503:
			// The server is currently unavailable (because it is overloaded or down for maintenance). Generally, this is a temporary state.
			message = "Service Unavailable";
			break;
		case 504:
			// The server was acting as a gateway or proxy and did not receive a timely request from the upstream server.
			message = "Gateway Timeout";
			break;
		case 505:
			// The server does not support the HTTP protocol version used in the request.
			message = "HTTP Version Not Supported";
			break;
		case 506:
			// (RFC 2295) - Transparent content negotiation for the request, results in a circular reference.
			message = "Variant Also Negotiates";
			break;
		case 507:
			// RFC 4918
			message = "Insufficient Storage";
			break;
		case 509:
			// (Apache bw/limited extension) - This status code, while used by many servers, is not specified in any RFCs.
			message = "Bandwidth Limit Exceeded";
			break;
		case 510:
			// (RFC 2774) - Further extensions to the request are required for the server to fulfil it.
			message = "Not Extended";
		default:
			message = "No valid response code";
		}
		return "HTTP status response code is " + responseCode + " " + message;
	}
	
	/**
	 * Check InputStream. 
	 * @param is
	 * @return
	 * @throws Exception 
	 */
	public static InputStream checkInputStream(HttpURLConnection conn) throws IOException {
		final int invalidResponseCode = -1;
		int responseCode = conn.getResponseCode();
		String codeMsg = checkResponseCode(responseCode);
		InputStream is = conn.getInputStream();
		/*
		 * Currently, InputStream can be the inner class instance of 
		 * org.apache.harmony.luni.internal.net.www.protocol.http.HttpURLConnectionImpl:
		 * ChunkedInputStream
		 * LimitedInputStream
		 * LocalCloseInputStream(Android add)
		 * 
		 * Also, sometimes may get
		 * UnknownLengthHttpInputStream
		 */
		String inputStreamInstanceName = is.getClass().getName();
		if(inputStreamInstanceName.startsWith("org.apache.harmony.luni.internal.net.www.protocol.http.HttpURLConnectionImpl"))
			inputStreamInstanceName = is.getClass().getSimpleName();
		// After called conn.getInputStream(), conn.getURL() can return redirected url if the request redirected.
		String requestUrl = conn.getURL().toString();
		// Check response code and InputStream type.
		String log = requestUrl + "\r\n" + codeMsg + ", InputStream is " + inputStreamInstanceName;
		if(responseCode >= 200 && responseCode <= 299) {
			Log.d(TAG, log);
		} else {
			if(responseCode == invalidResponseCode) {
				Log.e(TAG, log + ". ResponseCode is -1, abort it.");
				if(is != null)
					checkInputStreamEOF(is);
				return null;
			} else {
				Log.w(TAG, log);
			}
		} 
		return is;
	}
	
	/**
	 * Get HttpClient without checking certificate and hostname.
	 * @return org.apache.http.client.HttpClient
	 */
	public static HttpClient getHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			// no checking so no need to load KeyStore file.
			trustStore.load(null, null);
			// use custom SocketFactory which extends to SSLSocketFactory or SecureProtocolSocketFactory
			// custom SocketFactory use custom X509TrustManager
			org.apache.http.conn.ssl.SSLSocketFactory sf = new ApacheSSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			// register the custom SocketFactory to handle https
			registry.register(new Scheme("https", sf, 443));
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			Log.w(TAG, "Fail to create DefaultHttpClient with parameters.");
			return new DefaultHttpClient();
		}
	}
	
	public static InputStream httpGet(String urlStr) throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(urlStr);
		// send request and get InputStream.
		org.apache.http.HttpResponse response = getHttpClient().execute(request);
		Log.d(TAG, "HttpGet response status line is " + response.getStatusLine());
		return response.getEntity().getContent();
	}
	
	public static InputStream httpPostApplicationForm(String urlStr, Map<String, String> params, 
			String charsetName) throws IllegalStateException, IOException {
		HttpPost request = new HttpPost(urlStr);
		// get parameters and set http body.
		List<NameValuePair> names = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			names.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		request.setEntity(new UrlEncodedFormEntity(names, charsetName));
		// send request and get InputStream.
		HttpResponse response = getHttpClient().execute(request);
		Log.d(TAG, "HttpGet response status line is " + response.getStatusLine());
		return response.getEntity().getContent();
	}
	
	/**
	 * A helper BroadcastReceiver to receive the network connectivity change event. 
	 * 
	 * <p>NOTE:
	 * Register Receiver in code, we can control receiver's life;
	 * Register Receiver in XML, it works until user uninstall the app. 
	 * 
	 * @author AlfredZhong
	 * @version 2012-02-04, updated 2012-03-11
	 */
	public static final class ConnectivityReceiver extends BroadcastReceiver {

		private static final String TAG = ConnectivityReceiver.class.getSimpleName();
		private static ConnectivityReceiver mReceiver;
		private static IntentFilter mIntentFilter;
		private static boolean connected;
		private static boolean wifiConnected;
		private static boolean mobileNetworkConnected;
		private static ReferenceQueue<Object> queue;
		private static Map<String, WeakReference<Handler>> handlerMap;
		// map to store context connectivity.
		private static Map<String, Boolean> contextConnectivity;
		/**
		 * This message indicates the overall connectivity with boolean value in message.obj.
		 * If network(mobile network or WIFI) connected, value is true; otherwise false. 
		 * 
		 * @see #registerReceiver
		 */
		public static final int MESSAGE_OVERALL_CONNECTIVITY = 1001;
		/**
		 * This message indicates the WIFI connectivity with boolean value in message.obj.
		 * If WIFI connected, value is true; otherwise false.
		 * 
		 * @see #registerReceiver
		 */
		public static final int MESSAGE_WIFI_CONNECTIVITY = 1002;
		/**
		 * This message indicates the mobile network connectivity with boolean value in message.obj.
		 * If mobile network connected, value is true; otherwise false. 
		 * 
		 * @see #registerReceiver
		 */
		public static final int MESSAGE_MOBILE_NETWORK_CONNECTIVITY = 1003;
		/**
		 * This message indicates the overall connectivity changed with boolean value in message.obj.
		 * If network(mobile network or WIFI) connected, value is true; otherwise false. 
		 * 
		 * @see #registerReceiver
		 */
		public static final int MESSAGE_OVERALL_CONNECTIVITY_CHANGED = 1004;
		
		static {
			mReceiver = new ConnectivityReceiver();
			mIntentFilter = new IntentFilter();
			mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			Log.w(TAG, "Instantiate the BroadcastReceiver and its IntentFilter.");
			// instantiate ReferenceQueue and handler HashMap.
			queue = new ReferenceQueue<Object>(); 
			handlerMap = new HashMap<String, WeakReference<Handler>>();
			contextConnectivity = new HashMap<String, Boolean>();
		}
		
		private ConnectivityReceiver() {
			// Cannot instantiate.
		}
		
		/**
		 * Register a BroadcastReceiver to be run in the main activity thread to monitor connectivity.
		 * <p>NOTE:
		 * <ol>
		 * <li>context is usually Activity or Service context; If you use getApplicationContext(), 
		 * please make sure Applicaion context never use this receiver.</li>
		 * <li>If register receiver with the same context, then the privious handler 
		 * will replace by the new one, and the privious register can not get message from its handler.</li>
		 * </ol>
		 * 
		 * @param context 
		 * @param handler the handler to receive connectivity message. 
		 * 
		 * @see #MESSAGE_OVERALL_CONNECTIVITY
		 * @see #MESSAGE_WIFI_CONNECTIVITY
		 * @see #MESSAGE_MOBILE_NETWORK_CONNECTIVITY
		 */
		public static void registerReceiver(Context context, Handler handler) {
			context.registerReceiver(mReceiver, mIntentFilter);
			Log.w(TAG, context + " register ConnectivityReceiver.");
			String key = getContextKey(context);
			if(handler != null) {
				handlerMap.put(key, new WeakReference<Handler>(handler, queue));
				Log.d(TAG, "registerReceiver::Handler is not null, key is " + key);
			}
			initConectivity(context); // Only cost several seconds.
			Log.w(TAG, "INIT -> OVERALL " + connected + ", WIFI " + wifiConnected + ", Mobile network " + mobileNetworkConnected);
			contextConnectivity.put(key, connected);
		}
		
		/**
		 * Unregister the broadcast receiver.
		 * <p>NOTE: 
		 * <ol>
		 * <li>There will be a leaked IntentReceiver if someone missing a call to unregisterReceiver().</li>
		 * <li>You should be aware that though you call unregisterReceiver() in onStop(If you have), 
		 * this context's handler may not be able to GC because onDestroy() will be called 
		 * after the new Activity comes out.</li>
		 * </ol>
		 * 
		 * @param context
		 */
		public static void unregisterReceiver(Context context) {
			try {
				context.unregisterReceiver(mReceiver);
				Log.w(TAG, context + " unregister ConnectivityReceiver.");
				contextConnectivity.remove(getContextKey(context));
			} catch (Exception e) {
				Log.e(TAG, context + " unregisterReceiver fail.", e);
			}
		}

		/**
		 * Only do something within 10s, otherwise will ANR.
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, context + " receive action: " + action);
			/*
			 * Receive action when WIFI/mobile network connectivity changed. 
			 * NOTE: Once register, receive an action.
			 * Specially, when a network is on, enable/disable other network, may receive this action 0 to some times.
			 * For example(following case is will depends on the timing; it is just one of the possible case):
			 * 1. WIFI on, enable mobile -> [mobile off action(no need to use, auto off),] WIFI on action
			 * 2. WIFI on, disable mobile -> no action(because mobile already off when enabled) or WIFI on action
			 * 3. mobile on, enable WIFI -> WIFI on action, mobile off action, WIFI on action
			 * 4. mobile on, disable WIFI -> WIFI off action, mobile on action
			 */
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				String key = getContextKey(context);
				Handler handler = getHandler(key);
				// NOTE: If use getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
				// The default value must be false, because EXTRA_NO_CONNECTIVITY is not present means connected.
				// If there is a EXTRA_NO_CONNECTIVITY, means no connectivity.
				boolean noConnectivity = intent.hasExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
				connected = !noConnectivity;
				Log.v(TAG, "Is connected? -> " + connected);
				if(handler != null) {
					handler.obtainMessage(MESSAGE_OVERALL_CONNECTIVITY, connected).sendToTarget();
				} 
				// check overall connectivity changed.
				Boolean previousConn = contextConnectivity.get(key);
				Log.d(TAG, "Previous connectivity is " + previousConn + ", current is " + connected);
				if(handler != null && previousConn != null && previousConn != connected) {
					handler.obtainMessage(MESSAGE_OVERALL_CONNECTIVITY_CHANGED, connected).sendToTarget();
				}
				contextConnectivity.put(key, connected);
				// check affected network.
				NetworkInfo affectedNetwork  = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				Log.v(TAG, "affected network: " + affectedNetwork);
				Log.v(TAG, "other network: " + intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO));
				if(affectedNetwork != null) {
					// affected network state is CONNECTED or DISCONNECTED, we need check it.
					if(affectedNetwork.getType() != ConnectivityManager.TYPE_WIFI) {
						// mobile network
						mobileNetworkConnected = affectedNetwork.isConnected();
						if(handler != null) {
							handler.obtainMessage(MESSAGE_MOBILE_NETWORK_CONNECTIVITY, mobileNetworkConnected).sendToTarget();
						} 
					} else {
						// WIFI
						wifiConnected = affectedNetwork.isConnected();
						if(handler != null) {
							handler.obtainMessage(MESSAGE_WIFI_CONNECTIVITY, wifiConnected).sendToTarget();
						} 
					}
				} else {
					Log.e(TAG, "NetworkInfo is null retrieve with ConnectivityManager.EXTRA_NETWORK_INFO.");
				}
				handler = null;
			} 
			Log.d(TAG, "onReceive -> OVERALL " + connected + ", WIFI " + wifiConnected + ", Mobile network " + mobileNetworkConnected);
		}
		
		private static String getContextKey(Context context) {
			String key = context.getClass().getSimpleName() + "@" + Integer.toHexString(context.hashCode());
			context = null;
			return key;
		}
		
		/**
		 * Get handler from HashMap.
		 * @param context
		 * @return handler or null if this context does not have handler.
		 */
		private static Handler getHandler(String key) {
			// Help GC to remove unreachable referece.
			checkQueue();
			// Whether this context register with handler.
			WeakReference<Handler> handlerReferent = null;
			if((handlerReferent = handlerMap.get(key)) != null) {
				Log.d(TAG, key + " has handler");
				return handlerReferent.get();
			}
			Log.w(TAG, key + " does not have handler");
			return null;
		}
		
		/**
		 * Check ReferenceQueue to remove reference from ReferenceQueue and HashMap.
		 */
		private static void checkQueue() {
			// Since System.gc() cost about 100ms, we don't GC_EXPLICIT here.
			// Only cost several seconds to check queue.
			Reference<?> ref = queue.poll();
			if (ref != null) {
				Log.w(TAG, "Find reference in queue: " + ref);
		    	for(Entry<?, ?> entry : handlerMap.entrySet()) {
		    		if(entry.getValue() == ref) {
		    			Log.w(TAG, "Find reference in map, bound to remove handler in " + entry.getKey());
		    			handlerMap.remove(entry.getKey());
		    			break;
		    		}
		    	}
			}
		}
		
		/**
		 * Whether network(WIFI, Mobile networks) connected or not.
		 * <p>NOTE: You can call this function only if you registered this receiver.
		 * @return overall network connection or always false if no context register this receiver.
		 */
		public static boolean isConnected() {
			Log.v(TAG, "isConnected " + connected);
			return connected;
		}
		
		/**
		 * Whether WIFI connected or not.
		 * <p>NOTE: You can call this function only if you registered this receiver.
		 * @return WIFI connection or always false if no context register this receiver.
		 */
		public static boolean isWIFIConnected() {
			Log.v(TAG, "isWIFIConnected " + wifiConnected);
			return wifiConnected;
		}
		
		/**
		 * Whether mobile networks(data network, such as GPRS, 3G and so on) connected or not.
		 * <p>NOTE: You can call this function only if you registered this receiver.
		 * @return mobile network connection or always false if no context register this receiver.
		 */
		public static boolean isMobileNetworkConnected() {
			Log.v(TAG, "isMobileNetworkConnected " + mobileNetworkConnected);
			return mobileNetworkConnected;
		}
		
		/**
		 * Initial the connectivy of overall, WIFI and mobile network.
		 * @param context
		 */
		private static void initConectivity(Context context) {
			NetworkInfo info = getActiveNetwork(context);
			if(info != null && info.isConnected()) {
				connected = true;
				if (info.getType() == ConnectivityManager.TYPE_WIFI)
					wifiConnected = true;
				else
					mobileNetworkConnected = true;
	        } else {
	        	connected = false;
	        	wifiConnected = false;
	        	mobileNetworkConnected = false;
	        }
		}
		
	} // end of inner class ConnectivityReceiver
	
	/**
	 * use ConnectivityManager instance need permission:
	 * &lt;uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
	 */
	public static NetworkInfo getActiveNetwork(Context context) {
		NetworkInfo info = null;
		try {
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			info = connMgr.getActiveNetworkInfo();
		} catch (Exception e) {
			Log.e(TAG, "checkNetwork fail.", e);
		}
		return info;
	}
	
	/**
	 * Whether network(WIFI, Mobile networks) connected or not.
	 */
	public static boolean checkConnectivity(Context context) {
		NetworkInfo info = getActiveNetwork(context);
        if(info != null && info.isConnected()) {
        	Log.w(TAG, "Network connected. Network type is " + info.getTypeName());
            return true;
        }
        // or check info.getState() == State.CONNECTED; info.getState() == State.CONNECTING depends.
        Log.w(TAG, "No connected network.");
        return false;
	}
	
	/**
	 * Whether current active network is the specified network type.
	 * @param context
	 * @param type
	 * @return
	 * @throws IllegalStateException if no connected network.
	 */
	private static boolean checkNetworkType(Context context, int type) throws IllegalStateException {
		NetworkInfo info = getActiveNetwork(context);
		if(info != null && info.isConnected()) {
			Log.d(TAG, "Network connected. Network type is " + info.getTypeName());
			if (info.getType() == type)
				return true;
			else
				return false;
        }
		throw new IllegalStateException();
	}
	
	/**
	 * Whether WIFI connected or not.
	 */
	public static boolean isWIFIConnected(Context context) {
		try { 
			return checkNetworkType(context, ConnectivityManager.TYPE_WIFI);
		} catch (IllegalStateException e) {
			// no connected network.
			return false;
		}
	}
	
	/**
	 * Whether mobile networks(data network, such as GPRS, 3G and so on) connected or not.
	 */
	public static boolean isMobileNetworkConnected(Context context) {
		try { 
			// MOBILE has many types, check WIFI instead.
			return !checkNetworkType(context, ConnectivityManager.TYPE_WIFI);
		} catch (IllegalStateException e) {
			// no connected network.
			return false;
		}
	}

	/**
	 * Whether the specific SSID WIFI existed or not.
	 * NOTE:
	 * use WifiManager instance need permission:
	 * &lt;uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
	 */
	public static boolean isSSIDWifiExisted(Context context, String ssid) throws Exception {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo != null) {
			String wifiSSID = wifiInfo.getSSID();
			if (wifiSSID != null && wifiSSID.equals(ssid)) {
				Log.v(TAG, "SSID " + ssid + " WIFI exists.");
				return true;
			}
		}
		Log.v(TAG, "No SSID " + ssid + " WIFI.");
		return false;
	}
	
	/**
	 * Open wireless and network setting.
	 */
	public static void openWirelessSettings(Context context) {
		 Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
		 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 context.startActivity(intent);  	
	}
	
	/**
	 * Open WLAN settings.
	 */
	public static void openWlanSettings(Context context) {
		 Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
		 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 context.startActivity(intent);  	
	}	
	
	/**
	 * Open Mobile network settings.
	 * @param context
	 */
	public static void openMobileNetworkSettings(Context context) {
		Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
		/*
		 * There is a bug in ACTION_DATA_ROAMING_SETTINGS when API level below 9.
		 * We have to set component in intent.
		 */
		ComponentName cName = new ComponentName("com.android.phone","com.android.phone.Settings");
		intent.setComponent(cName); 
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	/**
	 * Enable or disable Wi-Fi.
	 * <p>NOTE:
	 * <p>use WifiManager instance need permission:
	 * <p>&lt;uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
	 * 
	 * @param context
	 * @param enabled true to enable, false to disable.
	 * @return true if the operation succeeds (or if the existing state is the same as the requested state). 
	 */
	public static boolean setWifiEnabled(Context context, boolean enabled) {
		WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return wifiMgr.setWifiEnabled(enabled);
	}

}  // end of class NetUtil

/**
 * The X509TrustManager implementing class which does not check certificate.
 * @author AlfredZhong
 * @version 1.0, 2012-01-04
 */
final class AllTrustedX509TrustManager implements javax.net.ssl.X509TrustManager {

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		// do not check client certificate.
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		// do not check server certificate.
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		// return new java.security.cert.X509Certificate[] {}; is OK too.
		return null;
	}
	
} // end of class AllTrustedX509TrustManager

/**
 * The SSLSocketFactory implemented in the Apache HttpClient way, not the Java HttpURLConnection way.
 * @author AlfredZhong
 * @version 1.0, 2012-01-04
 */
final class ApacheSSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory {
	
	private javax.net.ssl.SSLContext sslContext = SSLContext.getInstance("TLS");

	public ApacheSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, 
			KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		super(truststore);
		TrustManager[] trustAllCerts = new TrustManager[] { new AllTrustedX509TrustManager() };
		// the third param can be "new java.security.SecureRandom()".
		sslContext.init(null, trustAllCerts, null);
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		// javax.net.ssl.SSLSocketFactory
		// use HttpsURLConnection.getDefaultSSLSocketFactory() can get the default SSL socket factory .
		return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}
	
} // end of class ApacheSSLSocketFactory
