package com.aaa.moodtreehole.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.aaa.moodtreehole.R;
import com.aaa.moodtreehole.fragments.FragmentTreeHole;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		LayoutInflater inflater = LayoutInflater.from(this);
		FragmentTabHost fragmentTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		fragmentTabHost.setup(this, getSupportFragmentManager(),
				R.id.realtabcontent);

		String strTabText[] = new String[] { "树洞", "个人中心" };
		int iTabImage[] = new int[] { R.drawable.tab_btn_home,
				R.drawable.tab_btn_account };
		Class<?> fragments[] = new Class[] { FragmentTreeHole.class,
				FragmentTreeHole.class };
		for (int i = 0; i < 2; i++) {
			TabSpec tabSpec = fragmentTabHost.newTabSpec(strTabText[i])
					.setIndicator(
							getTabItemView(i, strTabText[i], iTabImage[i],
									inflater));
			fragmentTabHost.addTab(tabSpec, fragments[i], null);
		}
	}

	@SuppressLint("InflateParams")
	private View getTabItemView(int i, String text, int image,
			LayoutInflater inflater) {
		View view = inflater.inflate(R.layout.tab_item_view, null);
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(image);
		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText(text);
		return view;
	}
}
