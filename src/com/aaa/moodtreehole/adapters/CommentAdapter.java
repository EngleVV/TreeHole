/*
 * CommentAdapter.java
 * https://github.com/EngleVV/MyRepository
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.aaa.moodtreehole.adapters;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aaa.moodtreehole.R;
import com.aaa.moodtreehole.items.CommentItem;

/**
 * @author Engle
 * 
 */
public class CommentAdapter extends BaseAdapter {

	private List<CommentItem> commentsList;

	private LayoutInflater inflater;

	public CommentAdapter(List<CommentItem> list, Context context) {
		commentsList = list;
		inflater = LayoutInflater.from(context);
	}

	/**
	 * @return the commentsList
	 */
	public List<CommentItem> getCommentsList() {
		return commentsList;
	}

	/**
	 * @param commentsList
	 *            the commentsList to set
	 */
	public void setCommentsList(List<CommentItem> commentsList) {
		this.commentsList = commentsList;
	}

	/**
	 * @return the inflater
	 */
	public LayoutInflater getInflater() {
		return inflater;
	}

	/**
	 * @param inflater
	 *            the inflater to set
	 */
	public void setInflater(LayoutInflater inflater) {
		this.inflater = inflater;
	}

	/*
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		if (null == commentsList)
			return 0;
		else {
			System.out.println(commentsList.size());
			return commentsList.size();
		}
	}

	/*
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CommentHolder holder;
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.simple_item_comment, null);
			holder = new CommentHolder();
			holder.textViewUsername = (TextView) convertView
					.findViewById(R.id.comment_username);
			holder.textViewContent = (TextView) convertView
					.findViewById(R.id.comment_content);
			holder.textViewTime = (TextView) convertView
					.findViewById(R.id.comment_time);
			holder.textViewFloor = (TextView) convertView
					.findViewById(R.id.comment_floor);
			convertView.setTag(holder);
		} else {
			holder = (CommentHolder) convertView.getTag();
		}

		holder.textViewUsername.setText(commentsList.get(position)
				.getUsername());
		holder.textViewContent.setText(commentsList.get(position).getContent());
		holder.textViewTime.setText(commentsList.get(position).getDate());
		holder.textViewFloor.setText(String.valueOf(position + 1));

		return convertView;
	}

	/*
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return null;
	}

	/*
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return 0;
	}

	public class CommentHolder {
		TextView textViewUsername;
		TextView textViewContent;
		TextView textViewTime;
		TextView textViewFloor;
	}
}