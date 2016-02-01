package com.aaa.moodtreehole.activities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aaa.moodtreehole.PraiseResult;
import com.aaa.moodtreehole.R;
import com.aaa.moodtreehole.adapters.CommentAdapter;
import com.aaa.moodtreehole.common.utils.HttpUtil;
import com.aaa.moodtreehole.common.utils.NetworkUtil;
import com.aaa.moodtreehole.common.utils.StringUtil;
import com.aaa.moodtreehole.enums.MessageWhatEnum;
import com.aaa.moodtreehole.items.CommentItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * 评论页面
 * 
 * @author Engle
 * 
 */
public class CommentActivity extends Activity {

	/** 滑动刷新列表 */
	private PullToRefreshListView refreshView;

	/** 评论内容列表 */
	private ListView listViewComments;

	/** 评论内容 */
	private CommentAdapter commentAdapter;

	/** treeHole点赞数 */
	private TextView textViewTreeHolePraises;

	/** treeHole用户名 */
	private TextView textViewTreeHoleUsername;

	/** treeHole内容 */
	private TextView textViewTreeHoleContent;

	/** 评论数 */
	private TextView textViewTreeHoleComments;

	/** 返回按钮 */
	private ImageView imageViewBack;

	/** 点赞按钮 */
	private ImageView imageViewPraise;

	/** 该条treeHole的具体数据 */
	private String treeHoleUuid;
	private String username;
	private String content;
	private int praises;
	private int comments;

	/** 评论数据 */
	private List<CommentItem> commentsHistoryList;

	/** 数据库分页 */
	private int pageIndex = 0;

	/** 消息处理 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (MessageWhatEnum.GET_COMMENT_SUCCESS.getCode() == msg.what) {
				Bundle bundle = msg.getData();
				Gson gson = new Gson();
				String strJson = bundle.getString("strJson");
				List<CommentItem> commentsList = gson.fromJson(strJson,
						new TypeToken<List<CommentItem>>() {
						}.getType());

				if (null == commentsHistoryList) {
					commentsHistoryList = commentsList;
				} else {
					commentsHistoryList.addAll(commentsList);
				}

				commentAdapter.setCommentsList(commentsHistoryList);
				pageIndex++;
				commentAdapter.notifyDataSetChanged();
				refreshView.onRefreshComplete();
			} else if (msg.what == MessageWhatEnum.PRAISE_SUCCESS.getCode()) {
				Bundle bundle = msg.getData();
				textViewTreeHolePraises
						.setText("点赞" + bundle.getInt("praises"));
			} else if (msg.what == MessageWhatEnum.NETWORK_EXCEPTION.getCode()) {
				if (NetworkUtil.isConnectingToInternet(getApplicationContext())) {
					Toast.makeText(getApplicationContext(), "服务器异常",
							Toast.LENGTH_SHORT).show();
				} else {
					// 本地网络不通
					Toast.makeText(getApplicationContext(), "请检查网络连接",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	/**
	 * 获取intent中传过来的数据
	 */
	private void getDataFromIntent() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		treeHoleUuid = bundle.getString("itemUuid");
		username = bundle.getString("username");
		content = bundle.getString("content");
		praises = bundle.getInt("praises");
		comments = bundle.getInt("comments");
	}

	/**
	 * 初始化视图控件
	 */
	private void initView() {
		textViewTreeHoleUsername = (TextView) findViewById(R.id.treehole_username);
		textViewTreeHoleContent = (TextView) findViewById(R.id.treehole_content);
		textViewTreeHoleComments = (TextView) findViewById(R.id.treehole_comment_count);
		textViewTreeHolePraises = (TextView) findViewById(R.id.treehole_praise_count);
		imageViewBack = (ImageView) findViewById(R.id.comment_title_btn_back);
		imageViewPraise = (ImageView) findViewById(R.id.treehole_btn_praise);
	}

	/**
	 * 设置treeHole数据
	 */
	private void setTreeHole() {
		textViewTreeHoleUsername.setText(username);
		textViewTreeHoleContent.setText(content);
		textViewTreeHolePraises.setText("点赞" + praises);
		textViewTreeHoleComments.setText(" · 评论 " + comments);
	}

	/**
	 * 设置返回按钮点击事件
	 */
	private void setBackButton() {
		imageViewBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * 设置滑动刷新视图
	 * 
	 * @param treeHoleUuid
	 *            该条treeHole惟一id
	 */
	private void setRefreshView(final String treeHoleUuid) {
		refreshView = (PullToRefreshListView) findViewById(R.id.commentList);
		refreshView.setMode(Mode.BOTH);
		listViewComments = refreshView.getRefreshableView();
		commentAdapter = new CommentAdapter(null, this);
		listViewComments.setAdapter(commentAdapter);
		refreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if (refreshView.isHeaderShown()) {
					// 下拉刷新目的是获取最新的数据,所以分页数置零,清除历史数据
					pageIndex = 0;
					commentsHistoryList = null;
				}
				getCommentsData(handler, treeHoleUuid);
			}
		});
	}

	/**
	 * 点赞按钮事件
	 * 
	 * @param treeHoleUuid
	 *            该条treeHole惟一id
	 */
	private void setPraiseButton(final String treeHoleUuid) {
		imageViewPraise.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				praise(handler, treeHoleUuid);
			}
		});

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment);

		// 从intent获取传过来的数据
		getDataFromIntent();

		// 初始化视图
		initView();

		// 设置treeHole
		setTreeHole();

		// 设置返回按钮
		setBackButton();

		// 设置评论列表
		setRefreshView(treeHoleUuid);

		// 点赞按钮
		setPraiseButton(treeHoleUuid);

		// 获取评论数据
		getCommentsData(handler, treeHoleUuid);
	}

	/**
	 * 从服务器获取评论数据
	 * 
	 * @param handler
	 *            消息处理
	 * @param treeHoleUuid
	 *            该条treeHole惟一id
	 */
	private void getCommentsData(final Handler handler,
			final String treeHoleUuid) {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				try {
					String url = HttpUtil.BASE_URL
							+ "ClientLoadComments.action";
					Map<String, String> map = new HashMap<String, String>();
					map.put("treeHoleUuid", treeHoleUuid);
					map.put("pageIndex", String.valueOf(pageIndex));

					String strJson = HttpUtil.postRequest(url, map);

					Message message = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("strJson", strJson);
					message.setData(bundle);
					message.what = MessageWhatEnum.GET_COMMENT_SUCCESS
							.getCode();
					handler.sendMessage(message);
				} catch (Exception e) {
					// 网络异常
					handler.sendEmptyMessage(MessageWhatEnum.NETWORK_EXCEPTION
							.getCode());
				}

				Looper.loop();
			}
		}.start();
	}

	/**
	 * 点赞并同步数据库数据
	 * 
	 * @param handler
	 *            消息处理
	 * @param uuid
	 *            该条treeHole惟一id
	 * @param position
	 */
	private void praise(final Handler handler, final String uuid) {
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
						handler.sendEmptyMessage(MessageWhatEnum.NETWORK_EXCEPTION
								.getCode());
					} else {
						Gson gson = new Gson();
						PraiseResult praiseResult = gson.fromJson(strJson,
								PraiseResult.class);
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putInt("praises", praiseResult.getPraises());
						message.setData(bundle);
						message.what = MessageWhatEnum.PRAISE_SUCCESS.getCode();
						handler.sendMessage(message);
					}
				} catch (Exception e) {
					handler.sendEmptyMessage(MessageWhatEnum.NETWORK_EXCEPTION
							.getCode());
				}
				Looper.loop();
			}
		}.start();
	}
}
