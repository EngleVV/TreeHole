/*
 * TreeHoleItem.java
 * https://github.com/EngleVV/MyRepository
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.aaa.moodtreehole.items;

/**
 * @author Engle
 * 
 */
public class TreeHoleItem {

	/** 主键uuid */
	private String uuid;

	/** 内容 */
	private String content;

	/** 对应用户名 */
	private String username;

	/** 发布日期 */
	private String date;

	/** 被点赞的数目 */
	private int praises;

	/** 被评论的数据 */
	private int comments;

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @return the praises
	 */
	public int getPraises() {
		return praises;
	}

	/**
	 * @param praises
	 *            the praises to set
	 */
	public void setPraises(int praises) {
		this.praises = praises;
	}

	/**
	 * @return the comments
	 */
	public int getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(int comments) {
		this.comments = comments;
	}

}
