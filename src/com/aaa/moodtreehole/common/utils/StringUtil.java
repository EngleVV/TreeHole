/*
 * https://github.com/EngleVV/MyRepository
 * Copyright (c) 2004-2015 All Rights Reserved.
 */

package com.aaa.moodtreehole.common.utils;

/**
 * @author Engle
 * 
 */
public class StringUtil {
	private StringUtil() {

	}

	/**
	 * 返回两个String是否相等
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static Boolean equals(String str1, String str2) {
		if (str1 == null) {
			if (str2 == null) {
				return true;
			} else {
				return false;
			}
		} else {
			if (str1.equals(str2)) {
				return true;
			}
			return false;
		}
	}

	/**
	 * 判断字符串是否为空,str=null或者str=""均视为空
	 * 
	 * @param str
	 *            字符串
	 * @return 空返回true, 不空返回false
	 */
	public static Boolean isBlank(String str) {
		if (null != str) {
			if ("" == str) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public static Boolean contains(String strParent, String strChild) {
		if (StringUtil.isBlank(strParent)) {
			return false;
		} else {
			return strParent.contains(strChild);
		}
	}
}
