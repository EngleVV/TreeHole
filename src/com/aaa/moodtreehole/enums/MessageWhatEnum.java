/*
 * MessageWhatEnum.java
 * https://github.com/EngleVV/MyRepository
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.aaa.moodtreehole.enums;

/**
 * 访问服务器时,服务器返回结果发送异步消息给Handler 以下枚举量则是对应不同场景的what
 * 
 * @author Engle
 * 
 */
public enum MessageWhatEnum {
	/**
	 * 登录成功
	 */
	LOGIN_SUCCESS(0x001),

	/**
	 * 登陆失败
	 */
	LOGIN_FAILURE(0x002),

	/**
	 * 获取treeHole成功
	 */
	GET_TREEHOLE_SUCCESS(0x010),

	/**
	 * 点赞成功
	 */
	PRAISE_SUCCESS(0x020),

	/**
	 * 发布成功
	 */
	ISSUE_SUCCESS(0x030),

	/**
	 * 获取评论成功
	 */
	GET_COMMENT_SUCCESS(0x040),

	/**
	 * 网络异常
	 */
	NETWORK_EXCEPTION(0x100),

	;

	private int code;

	MessageWhatEnum(int code) {
		this.code = code;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}

}
