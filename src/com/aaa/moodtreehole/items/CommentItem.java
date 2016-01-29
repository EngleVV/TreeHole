/*
 * CommentItem.java
 * https://github.com/EngleVV/MyRepository
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.aaa.moodtreehole.items;

/**
 * @author Engle
 * 
 */
public class CommentItem {
	private String uuid;

	private String itemUuid;

	private String content;

	private String username;

	private String date;

	private int praises;

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
	 * @return the item_uuid
	 */
	public String getItemUuid() {
		return itemUuid;
	}

	/**
	 * @param item_uuid
	 *            the item_uuid to set
	 */
	public void setItemUuid(String itemUuid) {
		this.itemUuid = itemUuid;
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

}
