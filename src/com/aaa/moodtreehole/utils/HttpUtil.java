/**
 * 
 */
package com.aaa.moodtreehole.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 */
public class HttpUtil {
	// public static HttpClient httpClient = new DefaultHttpClient();
	public static final String BASE_URL = "http://192.168.1.104:8080/Test/";

	public static DefaultHttpClient getThreadSafeClient() {
		DefaultHttpClient client = new DefaultHttpClient();
		ClientConnectionManager mgr = client.getConnectionManager();
		HttpParams params = client.getParams();
		client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
				mgr.getSchemeRegistry()), params);
		return client;

	}

	/**
	 * 
	 * @param url
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getRequest(String url) throws Exception {
		HttpClient httpClient = getThreadSafeClient();
		HttpGet get = new HttpGet(url);
		HttpResponse httpResponse = httpClient.execute(get);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			String result = EntityUtils.toString(httpResponse.getEntity());
			return result;
		}
		return null;
	}

	/**
	 */
	public static String postRequest(String url, Map<String, String> rawParams)
			throws Exception {
		HttpClient httpClient = getThreadSafeClient();
		HttpPost post = new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (String key : rawParams.keySet()) {
			params.add(new BasicNameValuePair(key, rawParams.get(key)));
		}
		post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
		HttpResponse httpResponse = httpClient.execute(post);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			try {
				String result = EntityUtils.toString(httpResponse.getEntity(),
						"utf-8");
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
