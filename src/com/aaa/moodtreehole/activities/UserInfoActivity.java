package com.aaa.moodtreehole.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.aaa.moodtreehole.GlobleData;
import com.aaa.moodtreehole.R;

/**
 * 个人信息页面
 * 
 * @author Engle
 * 
 */
public class UserInfoActivity extends Activity {

	/** 存储配置 */
	private SharedPreferences sharedPreferences;

	/** 读写配置 */
	private SharedPreferences.Editor editor;

	/** 返回按钮 */
	private ImageView imageViewBack;

	/** 登出按钮 */
	private Button buttonLogout;

	/**
	 * 初始化视图控件
	 */
	@SuppressLint("CommitPrefEdits")
	private void initView() {
		imageViewBack = (ImageView) findViewById(R.id.user_info_title_btn_back);
		buttonLogout = (Button) findViewById(R.id.user_info_btn_logout);

		sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
		editor = sharedPreferences.edit();
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
	 * 设置退出登录按钮事件
	 */
	private void setLogoutButton() {
		buttonLogout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editor.putString("username", null);
				editor.putString("sessionId", null);
				editor.apply();
				GlobleData globleData = (GlobleData) getApplication();
				globleData.clear();
				Toast.makeText(getApplicationContext(), "成功退出登录",
						Toast.LENGTH_SHORT).show();
				finish();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info);

		// 初始化视图控件
		initView();

		// 设置返回按钮
		setBackButton();

		// 设置登出按钮
		setLogoutButton();
	}

}
