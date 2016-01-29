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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaa.moodtreehole.GlobleData;
import com.aaa.moodtreehole.PraiseResult;
import com.aaa.moodtreehole.R;
import com.aaa.moodtreehole.ServerResult;
import com.aaa.moodtreehole.activities.AddActivity;
import com.aaa.moodtreehole.activities.CommentActivity;
import com.aaa.moodtreehole.activities.LoginActivity;
import com.aaa.moodtreehole.activities.UserInfoActivity;
import com.aaa.moodtreehole.common.utils.NetworkUtil;
import com.aaa.moodtreehole.common.utils.StringUtil;
import com.aaa.moodtreehole.items.TreeHoleItem;
import com.aaa.moodtreehole.utils.HttpUtil;
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

	private ListView listViewTreeHole;

	private PullToRefreshListView refreshableView;

	private ImageView imageViewWifiStatus;

	private TextView textViewRefresh;

	private RelativeLayout relativeLayoutLoading;

	private TreeHoleAdapter treeHoleAdapter;

	private List<TreeHoleItem> treeHoleHistoryList = null;

	private int pageIndex = 0;

	/** 读取shareReferences */
	private SharedPreferences sharedPreferences;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0x001) {
				Bundle bundle = msg.getData();
				String strJson = bundle.getString("strJson");
				Gson gson = new Gson();
				List<TreeHoleItem> treeHoleList = gson.fromJson(strJson,
						new TypeToken<List<TreeHoleItem>>() {
						}.getType());
				if (null == treeHoleHistoryList) {
					treeHoleHistoryList = treeHoleList;
				} else {
					treeHoleHistoryList.addAll(treeHoleList);
				}
				// setAdapterForList(treeHoleHistoryList);
				treeHoleAdapter.setTreeHoleList(treeHoleHistoryList);
				treeHoleAdapter.notifyDataSetChanged();
				pageIndex++;
				// Toast.makeText(getActivity(), "获取数据成功",
				// Toast.LENGTH_SHORT).show();
				refreshableView.onRefreshComplete();
				relativeLayoutLoading.setVisibility(View.GONE);
			} else if (msg.what == 0x002) {
				Bundle bundle = msg.getData();
				treeHoleHistoryList.get(bundle.getInt("position")).setPraises(
						bundle.getInt("praises"));
				treeHoleAdapter.setTreeHoleList(treeHoleHistoryList);
				treeHoleAdapter.notifyDataSetChanged();
			} else if (msg.what == 0x003) {
				refreshableView.onRefreshComplete();
				checkInternetState();
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_treehole, null);
		refreshableView = (PullToRefreshListView) rootView
				.findViewById(R.id.refreshable_view);
		relativeLayoutLoading = (RelativeLayout) rootView
				.findViewById(R.id.treehole_loading);
		imageViewWifiStatus = (ImageView) rootView
				.findViewById(R.id.wifi_status);
		textViewRefresh = (TextView) rootView
				.findViewById(R.id.treehole_refresh);
		textViewRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				checkInternetState();
				getDataFromServer(handler);
			}
		});

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
				} else if (refreshableView.isFooterShown()) {
					// 上拉获取更多数据
				}
				getDataFromServer(handler);
			}
		});
		listViewTreeHole = refreshableView.getRefreshableView();
		listViewTreeHole.setDivider(getResources().getDrawable(R.color.sixd));
		listViewTreeHole.setDividerHeight(4);
		setAdapterForList(null);

		ImageView imageViewEdit = (ImageView) rootView
				.findViewById(R.id.treehole_add);
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

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		final GlobleData globleData = (GlobleData) getActivity()
				.getApplication();
		final TextView textViewLogin = (TextView) rootView
				.findViewById(R.id.main_page_login);
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// 成功登陆,将文本设置为用户名,暂不可点击
				if (msg.what == 0x234) {
					globleData.setIsLogin(true);
					setLoginUser(textViewLogin);
				} else if (msg.what == 0x456) {
					// 登陆失败,将文本设置为登陆,可点击进入登陆页面
					globleData.setIsLogin(false);
					setLoginButton(textViewLogin);
				}
				// System.out.println(msg.what);
				// Log.i("handler", String.valueOf(msg.what));
			}
		};

		setLoginState(handler);

		// application中并未加载到username和sessionId
		if (null == globleData.getIsLogin()) {
			textViewLogin.setText("加载中...");
		} else if (globleData.getIsLogin()) {
			// 已登录状态
			setLoginUser(textViewLogin);
		} else {
			// 未登录状态
			setLoginButton(textViewLogin);
		}
		getDataFromServer(this.handler);
		// treeHoleAdapter.notifyDataSetChanged();
	}

	private void checkInternetState() {
		if (NetworkUtil.isConnectingToInternet(getActivity())) {
			textViewRefresh.setVisibility(View.GONE);
			imageViewWifiStatus.setVisibility(View.GONE);
			refreshableView.setVisibility(View.VISIBLE);
			relativeLayoutLoading.setVisibility(View.VISIBLE);
		} else {
			relativeLayoutLoading.setVisibility(View.GONE);
			refreshableView.setVisibility(View.GONE);
			imageViewWifiStatus.setVisibility(View.VISIBLE);
			textViewRefresh.setVisibility(View.VISIBLE);
			Toast.makeText(getActivity(), "请检查网络连接", Toast.LENGTH_SHORT).show();
		}

	}

	private void setLoginState(final Handler handler) {
		sharedPreferences = getActivity().getSharedPreferences("login_info",
				Context.MODE_PRIVATE);
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
							// 确实处于登陆状态
							handler.sendEmptyMessage(0x234);
						} else {
							// 登录失败状态
							handler.sendEmptyMessage(0x456);
						}
					} catch (Exception e) {
						Log.e("error", e.getMessage());
						handler.sendEmptyMessage(0x456);
					}
				} else {
					handler.sendEmptyMessage(0x456);
				}
				Looper.loop();
			}
		}.start();
	}

	/**
	 * 设置登陆按钮为用户名
	 */
	private void setLoginUser(TextView v) {
		v.setText(((GlobleData) getActivity().getApplication()).getUsername());
		v.setOnClickListener(new OnClickListener() {
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
	private void setLoginButton(TextView v) {
		v.setText("登录");
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), LoginActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 设置登陆按钮
	 * 
	 * @param v
	 *            页面视图
	 */
	private void setLoginTips(TextView v) {
		v.setText("加载中...");

	}

	private void setAdapterForList(List<TreeHoleItem> treeHoleList) {
		treeHoleAdapter = new TreeHoleAdapter(treeHoleList, getActivity());
		if (null == listViewTreeHole.getAdapter()) {
			listViewTreeHole.setAdapter(treeHoleAdapter);
		}
	}

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
					msg.what = 0x001;
					handler.sendMessage(msg);
				} catch (Exception e) {
					handler.sendEmptyMessage(0x003);
					e.printStackTrace();
				}
				Looper.loop();
			}
		}.start();
	}

	public class TreeHoleAdapter extends BaseAdapter {

		private List<TreeHoleItem> treeHoleList;

		private LayoutInflater inflater;

		public TreeHoleAdapter(List<TreeHoleItem> list, Context ctx) {
			this.treeHoleList = list;
			this.inflater = LayoutInflater.from(ctx);
		}

		/**
		 * @return the treeHoleList
		 */
		public List<TreeHoleItem> getTreeHoleList() {
			return treeHoleList;
		}

		/**
		 * @param treeHoleList
		 *            the treeHoleList to set
		 */
		public void setTreeHoleList(List<TreeHoleItem> treeHoleList) {
			this.treeHoleList = treeHoleList;
		}

		/*
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			if (null != treeHoleList)
				return treeHoleList.size();
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final TreeHoleHolder treeHoleHolder;
			if (null == convertView) {
				treeHoleHolder = new TreeHoleHolder();
				convertView = inflater.inflate(R.layout.simple_item_treehole,
						null);
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

			treeHoleHolder.textViewUsername.setText(treeHoleList.get(position)
					.getUsername());
			treeHoleHolder.textViewContent.setText(treeHoleList.get(position)
					.getContent());
			treeHoleHolder.textViewPraises.setText("点赞 "
					+ treeHoleList.get(position).getPraises());
			treeHoleHolder.textViewComments.setText(" · 评论 "
					+ treeHoleList.get(position).getComments());
			treeHoleHolder.imageViewPraises
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							praise(handler, treeHoleList.get(position)
									.getUuid(), position);
						}
					});
			treeHoleHolder.imageViewComments
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getActivity(),
									CommentActivity.class);
							Bundle bundle = new Bundle();
							bundle.putString("itemUuid",
									treeHoleList.get(position).getUuid());
							bundle.putString("username",
									treeHoleList.get(position).getUsername());
							bundle.putString("content",
									treeHoleList.get(position).getContent());
							bundle.putInt("praises", treeHoleList.get(position)
									.getPraises());
							bundle.putInt("comments", treeHoleList
									.get(position).getComments());
							intent.putExtras(bundle);
							startActivity(intent);
						}
					});
			return convertView;
		}
	}

	public class TreeHoleHolder {
		private TextView textViewUsername;
		private TextView textViewContent;
		private TextView textViewPraises;
		private TextView textViewComments;
		private ImageView imageViewPraises;
		private ImageView imageViewComments;
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
						// 服务器异常
						Toast.makeText(getActivity(), "服务器异常",
								Toast.LENGTH_SHORT).show();
					} else {
						Gson gson = new Gson();
						PraiseResult praiseResult = gson.fromJson(strJson,
								PraiseResult.class);
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putInt("praises", praiseResult.getPraises());
						bundle.putInt("position", position);
						message.setData(bundle);
						message.what = 0x002;
						handler.sendMessage(message);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(getActivity(), "异常", Toast.LENGTH_SHORT)
							.show();
				}
				Looper.loop();
			}
		}.start();
	}
}
