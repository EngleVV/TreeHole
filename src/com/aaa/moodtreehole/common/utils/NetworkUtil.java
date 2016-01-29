/*
 * NetworkUtil.java
 * https://github.com/EngleVV/MyRepository
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.aaa.moodtreehole.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Engle
 * 
 */
public class NetworkUtil {
	/**
	 * 判断是可以访问网络
	 * 
	 * @return
	 */
	public static Boolean isConnectingToInternet(Context context) {

		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (null != manager) {
			NetworkInfo[] info = manager.getAllNetworkInfo();
			if (null != info) {
				for (NetworkInfo item : info) {
					if (NetworkInfo.State.CONNECTED == item.getState()) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
