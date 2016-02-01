package com.aaa.moodtreehole.activities;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aaa.moodtreehole.GlobleData;
import com.aaa.moodtreehole.LoginSession;
import com.aaa.moodtreehole.R;
import com.aaa.moodtreehole.common.utils.HttpUtil;
import com.aaa.moodtreehole.common.utils.InputCheckUtil;
import com.aaa.moodtreehole.common.utils.NetworkUtil;
import com.aaa.moodtreehole.common.utils.StringUtil;
import com.aaa.moodtreehole.enums.MessageWhatEnum;
import com.google.gson.Gson;

public class LoginActivity extends Activity {

	/** 读取preferences */
	private SharedPreferences sharedPreferences;

	/** 写入preferences */
	private Editor editor;

	/** 登录按钮 */
	private Button buttonLogin;

	/** 注册按钮 */
	private TextView textViewRegister;

	/** 返回按钮 */
	private ImageView imageViewBack;

	/** 用户名输入框 */
	private EditText editTextUsername;

	/** 密码输入框 */
	private EditText editTextPassword;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MessageWhatEnum.LOGIN_SUCCESS.getCode()) {
				Toast.makeText(getApplicationContext(), "登陆成功",
						Toast.LENGTH_SHORT).show();
				finish();
			} else if (msg.what == MessageWhatEnum.LOGIN_FAILURE.getCode()) {
				// 登陆失败
				buttonLogin.setText("登录");
				Toast.makeText(getApplicationContext(), "密码错误,请重试",
						Toast.LENGTH_SHORT).show();

			} else if (msg.what == MessageWhatEnum.NETWORK_EXCEPTION.getCode()) {
				// 网络异常
				buttonLogin.setText("登录");
				if (NetworkUtil.isConnectingToInternet(getApplicationContext())) {
					Toast.makeText(getApplicationContext(), "服务器异常,请重试",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "请检查网络连接",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	/**
	 * 初始化视图页面
	 */
	@SuppressLint("CommitPrefEdits")
	private void initView() {
		buttonLogin = (Button) findViewById(R.id.login_btn_login);
		textViewRegister = (TextView) findViewById(R.id.login_register);
		imageViewBack = (ImageView) findViewById(R.id.login_title_btn_back);
		sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
		editor = sharedPreferences.edit();

		editTextUsername = (EditText) findViewById(R.id.login_username);
		editTextPassword = (EditText) findViewById(R.id.login_password);

	}

	/**
	 * 返回按钮
	 */
	private void setButtonBack() {
		imageViewBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * 设置登陆按钮
	 */
	private void setLoginButton() {
		buttonLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String username = editTextUsername.getText().toString();
				final String password = editTextPassword.getText().toString();

				if (InputCheckUtil.CheckUsername(username)) {
					if (InputCheckUtil.CheckPassword(password)) {
						new Thread() {
							@Override
							public void run() {
								Looper.prepare();
								login(username, password);
								Looper.loop();
							}
						}.start();
						buttonLogin.setText("登录中");
					} else {
						// 提示密码输入不合理
						Toast.makeText(getApplicationContext(), "密码长度应为6~16位",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					// 提示用户名输入不合理
					Toast.makeText(getApplicationContext(), "用户名长度应为4~16位",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	/**
	 * 设置注册按钮
	 */
	private void setRegisterButton() {
		textViewRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// 初始化视图控件
		initView();

		// 设置返回按钮
		setButtonBack();

		// 设置登陆按钮响应事件
		setLoginButton();

		// 注册按钮
		setRegisterButton();
	}

	/**
	 * 登录验证
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 */
	private void login(String username, String password) {
		// 登录代码
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("password", password);
		String url = HttpUtil.BASE_URL + "ClientLogin.action";
		try {
			final GlobleData globleData = (GlobleData) getApplication();

			LoginSession loginSession = new LoginSession();
			Gson gson = new Gson();
			loginSession = gson.fromJson(HttpUtil.postRequest(url, map),
					LoginSession.class);
			// 返回sessionid则登录成功
			Log.i(username, loginSession.getSessionId());
			if (!StringUtil.isBlank(loginSession.getSessionId())) {
				globleData.setUsername(username);
				globleData.setSessionId(loginSession.getSessionId());
				globleData.setIsLogin(true);
				editor.putString("username", username);
				editor.putString("sessionId", loginSession.getSessionId());
				editor.apply();

				handler.sendEmptyMessage(MessageWhatEnum.LOGIN_SUCCESS
						.getCode());
			} else {
				// 登录失败代码,密码错误
				handler.sendEmptyMessage(MessageWhatEnum.LOGIN_FAILURE
						.getCode());
			}
		} catch (Exception e) {
			// 网络异常
			handler.sendEmptyMessage(MessageWhatEnum.NETWORK_EXCEPTION
					.getCode());
		}
	}
}
