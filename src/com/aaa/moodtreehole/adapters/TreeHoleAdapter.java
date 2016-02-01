/*
 * TreeHoleAdapter.java
 * https://github.com/EngleVV/MyRepository
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.aaa.moodtreehole.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaa.moodtreehole.PraiseResult;
import com.aaa.moodtreehole.R;
import com.aaa.moodtreehole.activities.CommentActivity;
import com.aaa.moodtreehole.common.utils.HttpUtil;
import com.aaa.moodtreehole.common.utils.StringUtil;
import com.aaa.moodtreehole.enums.MessageWhatEnum;
import com.aaa.moodtreehole.items.TreeHoleItem;
import com.google.gson.Gson;


/**
 * @author Engle
 */

public class TreeHoleAdapter extends BaseAdapter {
	/** 用于显示的数据集合 */
	private List<TreeHoleItem> mTreeHoleList;

	/** 对应activity */
	private Context mContext;

	/** 处理消息的handler */
	private Handler mHandler;

	public TreeHoleAdapter(List<TreeHoleItem> list, Context ctx, Handler handler) {
		this.mTreeHoleList = list;
		this.mContext = ctx;
		this.mHandler = handler;
	}

	/**
	 * @return the mTreeHoleList
	 */
	public List<TreeHoleItem> getTreeHoleList() {
		return mTreeHoleList;
	}

	/**
	 * @param mTreeHoleList
	 *            the mTreeHoleList to set
	 */
	public void setTreeHoleList(List<TreeHoleItem> mTreeHoleList) {
		this.mTreeHoleList = mTreeHoleList;
	}

	/**
	 * @return the mActiviryContext
	 */
	public Context getmContext() {
		return mContext;
	}

	/**
	 * @param mActiviryContext
	 *            the mActiviryContext to set
	 */
	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}

	/**
	 * @return the mHandler
	 */
	public Handler getmHandler() {
		return mHandler;
	}

	/**
	 * @param mHandler
	 *            the mHandler to set
	 */
	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	/*
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		if (null != mTreeHoleList)
			return mTreeHoleList.size();
		else
			return 0;
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

	/*
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final TreeHoleHolder treeHoleHolder;
		if (null == convertView) {
			treeHoleHolder = new TreeHoleHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.simple_item_treehole, null);
			treeHoleHolder.textViewUsername = (TextView) convertView
					.findViewById(R.id.treehole_username);
			treeHoleHolder.textViewContent = (TextView) convertView
					.findViewById(R.id.treehole_content);
			treeHoleHolder.textViewPraises = (TextView) convertView
					.findViewById(R.id.treehole_praise_count);
			treeHoleHolder.textViewComments = (TextView) convertView
					.findViewById(R.id.treehole_comment_count);
			treeHoleHolder.imageViewPraises = (ImageView) convertView
					.findViewById(R.id.treehole_btn_praise);
			treeHoleHolder.imageViewComments = (ImageView) convertView
					.findViewById(R.id.treehole_btn_comment);
			convertView.setTag(treeHoleHolder);

		} else {
			treeHoleHolder = (TreeHoleHolder) convertView.getTag();
		}

		treeHoleHolder.textViewUsername.setText(mTreeHoleList.get(position)
				.getUsername());
		treeHoleHolder.textViewContent.setText(mTreeHoleList.get(position)
				.getContent());
		treeHoleHolder.textViewPraises.setText("点赞 "
				+ mTreeHoleList.get(position).getPraises());
		treeHoleHolder.textViewComments.setText(" · 评论 "
				+ mTreeHoleList.get(position).getComments());
		treeHoleHolder.imageViewPraises
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						praise(mHandler, mTreeHoleList.get(position).getUuid(),
								position);
					}
				});
		treeHoleHolder.imageViewComments
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext,
								CommentActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("itemUuid", mTreeHoleList
								.get(position).getUuid());
						bundle.putString("username", mTreeHoleList
								.get(position).getUsername());
						bundle.putString("content", mTreeHoleList.get(position)
								.getContent());
						bundle.putInt("praises", mTreeHoleList.get(position)
								.getPraises());
						bundle.putInt("comments", mTreeHoleList.get(position)
								.getComments());
						intent.putExtras(bundle);
						mContext.startActivity(intent);
					}
				});
		return convertView;
	}

	private void praise(final Handler handler, final String uuid,
			final int position) {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				try {
					String url = HttpUtil.BASE_URL + "ClientPraise";
					Map<String, String> map = new HashMap<String, String>();
					map.put("uuid", uuid);
					String strJson = HttpUtil.postRequest(url, map);
					if (StringUtil.contains(strJson, "\"result\":\"false\"")) {
						// 服务器返回异常
						handler.sendEmptyMessage(MessageWhatEnum.NETWORK_EXCEPTION
								.getCode());
					} else {
						Gson gson = new Gson();
						PraiseResult praiseResult = gson.fromJson(strJson,
								PraiseResult.class);
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putInt("praises", praiseResult.getPraises());
						bundle.putInt("position", position);
						message.setData(bundle);
						message.what = MessageWhatEnum.PRAISE_SUCCESS.getCode();
						handler.sendMessage(message);
					}
				} catch (Exception e) {
					// 网络异常
					handler.sendEmptyMessage(MessageWhatEnum.NETWORK_EXCEPTION
							.getCode());
				}
				Looper.loop();
			}
		}.start();
	}

	public class TreeHoleHolder {
		private TextView textViewUsername;
		private TextView textViewContent;
		private TextView textViewPraises;
		private TextView textViewComments;
		private ImageView imageViewPraises;
		private ImageView imageViewComments;
	}
}
