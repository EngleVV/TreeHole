package com.aaa.moodtreehole.activities;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.aaa.moodtreehole.GlobleData;
import com.aaa.moodtreehole.R;
import com.aaa.moodtreehole.ServerResult;
import com.aaa.moodtreehole.common.utils.CalendarUtils;
import com.aaa.moodtreehole.common.utils.HttpUtil;
import com.aaa.moodtreehole.common.utils.NetworkUtil;
import com.aaa.moodtreehole.common.utils.StringUtil;
import com.aaa.moodtreehole.enums.MessageWhatEnum;
import com.aaa.moodtreehole.items.TreeHoleItem;
import com.google.gson.Gson;

/**
 * 发布页面
 * 
 * @author Engle
 * 
 */
public class AddActivity extends Activity {

	/** 消息处理 */
	@SuppressLint("HandlerLeak")
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (MessageWhatEnum.ISSUE_SUCCESS.getCode() == msg.what) {
				Bundle bundle = msg.getData();
				String strJson = bundle.getString("strJson");
				Gson gson = new Gson();
				ServerResult serverResult = gson.fromJson(strJson,
						ServerResult.class);
				if (serverResult.getResult()) {
					// 发布成功
					Toast.makeText(AddActivity.this, "发布成功", Toast.LENGTH_SHORT)
							.show();
					finish();
				} else {
					// 发布失败,服务器异常
					Toast.makeText(AddActivity.this, "发布失败,服务器出错",
							Toast.LENGTH_SHORT).show();
				}
			} else if (MessageWhatEnum.NETWORK_EXCEPTION.getCode() == msg.what) {
				// 本地异常
				if (NetworkUtil.isConnectingToInternet(getApplicationContext())) {
					// 服务器异常
					Toast.makeText(AddActivity.this, "发布失败,服务器异常",
							Toast.LENGTH_SHORT).show();
				} else {
					// 网络未连接
					Toast.makeText(AddActivity.this, "发布失败,请检查网络连接",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	/** 返回按钮 */
	private ImageView imageViewBack;

	/** 编辑输入框 */
	private EditText editTextContent;

	/** 发布按钮 */
	private ImageView imageViewIssue;

	/**
	 * 初始化视图
	 */
	private void initView() {
		imageViewBack = (ImageView) findViewById(R.id.add_title_btn_back);
		editTextContent = (EditText) findViewById(R.id.add_edit_content);
		imageViewIssue = (ImageView) findViewById(R.id.add_title_btn_issue);
	}

	/**
	 * 设置返回按钮
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
	 * 设置发布按钮
	 */
	private void setIssueButton() {
		imageViewIssue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String strContent = editTextContent.getText().toString();
				if (validateInput(strContent)) {
					issue(handler, strContent);
				} else {
					Toast.makeText(getApplicationContext(), "请输入内容再发布",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		// 初始化视图控件
		initView();

		// 设置返回按钮点击事件
		setBackButton();

		// 设置发布按钮点击事件
		setIssueButton();
	}

	/**
	 * 发布treeHole
	 * 
	 * @param handler
	 *            消息处理
	 * @param strContent
	 *            发布内容
	 */
	private void issue(final Handler handler, final String strContent) {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				try {
					GlobleData globleData = ((GlobleData) getApplication());
					TreeHoleItem treeHoleItem = new TreeHoleItem();
					treeHoleItem.setUuid(UUID.randomUUID().toString());
					treeHoleItem.setContent(strContent);
					treeHoleItem.setUsername(globleData.getUsername());
					treeHoleItem.setDate(CalendarUtils
							.toStandardDateString(Calendar.getInstance()));
					treeHoleItem.setPraises(0);
					treeHoleItem.setComments(0);
					Gson gson = new Gson();
					String url = HttpUtil.BASE_URL + "ClientIssueTreeHole";
					Map<String, String> map = new HashMap<String, String>();
					map.put("username", globleData.getUsername());
					map.put("sessionId", globleData.getSessionId());
					String str = gson.toJson(treeHoleItem, TreeHoleItem.class);
					map.put("treeHoleItem", str);
					String strJson = HttpUtil.postRequest(url, map);
					Message message = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("strJson", strJson);
					message.setData(bundle);
					message.what = MessageWhatEnum.ISSUE_SUCCESS.getCode();
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
	 * 判断发布内容
	 * 
	 * @param strInput
	 *            发布内容
	 * @return 是否允许发布
	 */
	private Boolean validateInput(String strInput) {
		if (!StringUtil.isBlank(strInput))
			return true;
		else
			return false;
	}
}
