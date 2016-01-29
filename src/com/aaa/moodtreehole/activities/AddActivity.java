package com.aaa.moodtreehole.activities;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import com.aaa.moodtreehole.items.TreeHoleItem;
import com.aaa.moodtreehole.utils.HttpUtil;
import com.google.gson.Gson;

public class AddActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (0x010 == msg.what) {
					Bundle bundle = msg.getData();
					String strJson = bundle.getString("strJson");
					Gson gson = new Gson();
					ServerResult serverResult = gson.fromJson(strJson,
							ServerResult.class);
					if (serverResult.getResult()) {
						// 发布成功
						Toast.makeText(AddActivity.this, "发布成功",
								Toast.LENGTH_SHORT).show();
						finish();
					} else {
						// 发布失败,服务器异常
						Toast.makeText(AddActivity.this, "网络异常,请稍后重试",
								Toast.LENGTH_SHORT).show();
					}
				} else if (0x011 == msg.what) {
					// 本地异常
					Toast.makeText(AddActivity.this, "发布失败", Toast.LENGTH_SHORT)
							.show();
				}
			}
		};

		ImageView imageViewBack = (ImageView) findViewById(R.id.add_title_btn_back);
		imageViewBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		final EditText editTextContent = (EditText) findViewById(R.id.add_edit_content);

		ImageView imageViewIssue = (ImageView) findViewById(R.id.add_title_btn_issue);
		imageViewIssue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String strContent = editTextContent.getText().toString();
				if (validateInput(strContent)) {
					issue(handler, strContent);
				}
			}
		});
	}

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
					message.what = 0x010;
					handler.sendMessage(message);
				} catch (Exception e) {
					handler.sendEmptyMessage(0x011);
					e.printStackTrace();
				}
				Looper.loop();
			}
		}.start();
	}

	private Boolean validateInput(String strInput) {
		return true;
	}
}
