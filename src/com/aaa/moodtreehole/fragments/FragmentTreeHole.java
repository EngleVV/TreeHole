/*
 * FragmentTreeHole.java
 * https://github.com/EngleVV/MyRepository
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.aaa.moodtreehole.fragments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaa.moodtreehole.GlobleData;
import com.aaa.moodtreehole.R;
import com.aaa.moodtreehole.ServerResult;
import com.aaa.moodtreehole.activities.AddActivity;
import com.aaa.moodtreehole.activities.LoginActivity;
import com.aaa.moodtreehole.activities.UserInfoActivity;
import com.aaa.moodtreehole.adapters.TreeHoleAdapter;
import com.aaa.moodtreehole.common.utils.HttpUtil;
import com.aaa.moodtreehole.common.utils.NetworkUtil;
import com.aaa.moodtreehole.common.utils.StringUtil;
import com.aaa.moodtreehole.enums.MessageWhatEnum;
import com.aaa.moodtreehole.items.TreeHoleItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * @author Engle
 * 
 */
public class FragmentTreeHole extends Fragment {

	/** 页面视图 */
	private View rootView;

	/** 树洞列表 */
	private ListView listViewTreeHole;

	/** 下拉刷新列表 */
	private PullToRefreshListView refreshableView;

	/** 无网络时显示页面 */
	private ImageView imageViewWifiStatus;

	/** 无网络时的刷新按钮 */
	private TextView textViewRefresh;

	/** 登陆按钮 */
	private TextView textViewLogin;

	/** 加载时的动画 */
	private RelativeLayout relativeLayoutLoading;

	/** treeHole数据 */
	private TreeHoleAdapter treeHoleAdapter;

	/** 编辑按钮 */
	private ImageView imageViewEdit;

	private List<TreeHoleItem> treeHoleHistoryList = null;

	private int pageIndex = 0;

	private GlobleData globleData = (GlobleData) getActivity().getApplication();

	/** 读取shareReferences */
	private SharedPreferences sharedPreferences;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MessageWhatEnum.GET_TREEHOLE_SUCCESS.getCode()) {
				Bundle bundle = msg.getData();
				String strJson = bundle.getString("strJson");
				Gson gson = new Gson();
				List<TreeHoleItem> treeHoleList = gson.fromJson(strJson,
						new TypeToken<List<TreeHoleItem>>() {
						}.getType());
				// 用treeHoleHistoryList存储所有待显示数据
				if (null == treeHoleHistoryList) {
					treeHoleHistoryList = treeHoleList;
				} else {
					treeHoleHistoryList.addAll(treeHoleList);
				}
				treeHoleAdapter.setTreeHoleList(treeHoleHistoryList);
				treeHoleAdapter.notifyDataSetChanged();
				pageIndex++;
				refreshableView.onRefreshComplete();
				relativeLayoutLoading.setVisibility(View.GONE);
			} else if (msg.what == MessageWhatEnum.PRAISE_SUCCESS.getCode()) {
				Bundle bundle = msg.getData();
				treeHoleHistoryList.get(bundle.getInt("position")).setPraises(
						bundle.getInt("praises"));
				treeHoleAdapter.setTreeHoleList(treeHoleHistoryList);
				treeHoleAdapter.notifyDataSetChanged();
			} else if (msg.what == MessageWhatEnum.NETWORK_EXCEPTION.getCode()) {
				// 网络异常
				refreshableView.onRefreshComplete();
				if (isInternetConnected()) {
					// 本机网络连通的情况网络异常,则是服务器异常
					setAbnormalNetworkView();
					Toast.makeText(getActivity(), "服务器异常", Toast.LENGTH_SHORT)
							.show();
				}
			} else if (msg.what == MessageWhatEnum.LOGIN_SUCCESS.getCode()) {
				// 成功登陆,将文本设置为用户名,暂不可点击
				globleData.setIsLogin(true);
				setLoginWithUser();
			} else if (msg.what == MessageWhatEnum.LOGIN_FAILURE.getCode()) {
				// 登陆失败,将文本设置为登陆,可点击进入登陆页面
				globleData.setIsLogin(false);
				setLoginWithButton();
			}
		}
	};

	/**
	 * 初始化页面视图控件
	 * 
	 * @param inflater
	 *            加载视图
	 */
	@SuppressLint("InflateParams")
	private void initView(LayoutInflater inflater) {
		rootView = inflater.inflate(R.layout.fragment_treehole, null);
		refreshableView = (PullToRefreshListView) rootView
				.findViewById(R.id.refreshable_view);
		relativeLayoutLoading = (RelativeLayout) rootView
				.findViewById(R.id.treehole_loading);
		imageViewWifiStatus = (ImageView) rootView
				.findViewById(R.id.wifi_status);
		textViewRefresh = (TextView) rootView
				.findViewById(R.id.treehole_refresh);
		imageViewEdit = (ImageView) rootView.findViewById(R.id.treehole_add);
		textViewLogin = (TextView) rootView.findViewById(R.id.main_page_login);
		sharedPreferences = getActivity().getSharedPreferences("login_info",
				Context.MODE_PRIVATE);
	}

	/**
	 * 设置刷新按钮
	 */
	private void setRefreshButton() {
		textViewRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isInternetConnected();
				getDataFromServer(handler);
			}
		});
	}

	/**
	 * 设置下上下拉刷新列表
	 */
	private void setRefreshView() {
		// 可上拉可下拉刷新
		refreshableView.setMode(Mode.BOTH);

		refreshableView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if (refreshableView.isHeaderShown()) {
					// 下拉刷新目的是获取最新的数据,所以分页数置零,清除历史数据
					pageIndex = 0;
					if (null != treeHoleHistoryList) {
						treeHoleHistoryList = null;
					}
				}
				getDataFromServer(handler);
			}
		});
		listViewTreeHole = refreshableView.getRefreshableView();
		listViewTreeHole.setDivider(getResources().getDrawable(R.color.sixd));
		listViewTreeHole.setDividerHeight(4);
		setAdapterForList(null);
	}

	/**
	 * 设置编辑按钮
	 */
	private void setEditButton() {
		imageViewEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 登陆状态则进入编辑页面,非登录状态则登录页面
				Intent intent = null;
				if (((GlobleData) getActivity().getApplication()).getIsLogin()) {
					intent = new Intent(getActivity(), AddActivity.class);
				} else {
					intent = new Intent(getActivity(), LoginActivity.class);
				}
				startActivity(intent);
			}
		});
	}

	/**
	 * 设置登陆按钮
	 */
	private void setLogin() {
		// application中并未加载到username和sessionId
		if (null == globleData.getIsLogin()) {
			textViewLogin.setText("加载中...");
		} else if (globleData.getIsLogin()) {
			// 已登录状态,设置用户名,点击进入用户详情
			setLoginWithUser();
		} else {
			// 未登录状态,设置点击登陆
			setLoginWithButton();
		}
	}

	/**
	 * 设置登陆按钮为用户名
	 */
	private void setLoginWithUser() {
		textViewLogin.setText(globleData.getUsername());
		textViewLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						UserInfoActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 设置登陆按钮
	 */
	private void setLoginWithButton() {
		textViewLogin.setText("登录");
		textViewLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), LoginActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 给listView设置adapter
	 * 
	 * @param treeHoleList
	 *            数据list
	 */
	private void setAdapterForList(List<TreeHoleItem> treeHoleList) {
		treeHoleAdapter = new TreeHoleAdapter(treeHoleList, getActivity(),
				handler);
		listViewTreeHole.setAdapter(treeHoleAdapter);
	}

	/**
	 * 检测网络是否连接
	 * 
	 * @return 是否连接网络
	 */
	private Boolean isInternetConnected() {
		if (NetworkUtil.isConnectingToInternet(getActivity())) {
			setNormalNetworkView();
			return true;
		} else {
			setAbnormalNetworkView();
			Toast.makeText(getActivity(), "请检查网络连接", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	/**
	 * 网络正常状态视图
	 */
	private void setNormalNetworkView() {
		textViewRefresh.setVisibility(View.GONE);
		imageViewWifiStatus.setVisibility(View.GONE);
		refreshableView.setVisibility(View.VISIBLE);
		relativeLayoutLoading.setVisibility(View.VISIBLE);
	}

	/**
	 * 网络不正常时视图
	 */
	private void setAbnormalNetworkView() {
		textViewRefresh.setVisibility(View.VISIBLE);
		imageViewWifiStatus.setVisibility(View.VISIBLE);
		refreshableView.setVisibility(View.GONE);
		relativeLayoutLoading.setVisibility(View.GONE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// 初始化视图控件
		initView(inflater);

		// 刷新按钮的点击事件
		setRefreshButton();

		// 设置刷新视图
		setRefreshView();

		// 设置编辑按钮
		setEditButton();

		// 获取数据
		getDataFromServer(handler);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		// 获取登陆状态
		setLoginState(handler);

		// 设置登录按钮
		setLogin();
	}

	/**
	 * 设置登陆装填
	 * 
	 * @param handler
	 *            消息处理
	 */
	private void setLoginState(final Handler handler) {

		final String username = sharedPreferences.getString("username", null);
		final String sessionId = sharedPreferences.getString("sessionId", null);
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				if (!StringUtil.isBlank(username)
						&& !StringUtil.isBlank(sessionId)) {
					// 向服务器验证
					Map<String, String> map = new HashMap<String, String>();
					map.put("username", username);
					map.put("sessionId", sessionId);
					String url = HttpUtil.BASE_URL + "CheckSession.action";
					ServerResult result = new ServerResult();
					Gson gson = new Gson();
					try {
						result = gson.fromJson(HttpUtil.postRequest(url, map),
								ServerResult.class);
						if (result.getResult()) {
							// 登录成功状态
							handler.sendEmptyMessage(MessageWhatEnum.LOGIN_SUCCESS
									.getCode());
						} else {
							// 登录失败状态
							handler.sendEmptyMessage(MessageWhatEnum.LOGIN_FAILURE
									.getCode());
						}
					} catch (Exception e) {
						// 网络异常
						handler.sendEmptyMessage(MessageWhatEnum.LOGIN_FAILURE
								.getCode());
					}
				} else {
					handler.sendEmptyMessage(MessageWhatEnum.LOGIN_FAILURE
							.getCode());
				}
				Looper.loop();
			}
		}.start();
	}

	/**
	 * 从服务器获取数据
	 * 
	 * @param handler
	 *            消息处理
	 */
	private void getDataFromServer(final Handler handler) {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				try {
					String url = HttpUtil.BASE_URL
							+ "ClientLoadTreeHole.action";
					Map<String, String> map = new HashMap<String, String>();
					map.put("pageIndex", String.valueOf(pageIndex));
					String strJson = HttpUtil.postRequest(url, map);
					Bundle bundle = new Bundle();
					bundle.putString("strJson", strJson);
					Message msg = new Message();
					msg.setData(bundle);
					msg.what = MessageWhatEnum.GET_TREEHOLE_SUCCESS.getCode();
					handler.sendMessage(msg);
				} catch (Exception e) {
					// 网络异常
					handler.sendEmptyMessage(MessageWhatEnum.NETWORK_EXCEPTION
							.getCode());
				}
				Looper.loop();
			}
		}.start();
	}
}
