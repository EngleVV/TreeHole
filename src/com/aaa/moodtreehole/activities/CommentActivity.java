package com.aaa.moodtreehole.activities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.TextView;

import com.aaa.moodtreehole.R;
import com.aaa.moodtreehole.items.CommentItem;
import com.aaa.moodtreehole.utils.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class CommentActivity extends Activity {

	private PullToRefreshListView refreshView;
	private ListView listViewComments;
	private CommentsAdapter commentsAdapter;
	private List<CommentItem> commentsHistoryList;
	private int pageIndex = 0;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (0x001 == msg.what) {
				Bundle bundle = msg.getData();
				Gson gson = new Gson();
				String strJson = bundle.getString("strJson");
				List<CommentItem> commentsList = gson.fromJson(strJson,
						new TypeToken<List<CommentItem>>() {
						}.getType());

				convertTime(commentsList);

				if (null == commentsHistoryList) {
					commentsHistoryList = commentsList;
				} else {
					commentsHistoryList.addAll(commentsList);
				}

				commentsAdapter.setCommentsList(commentsHistoryList);
				pageIndex++;
				commentsAdapter.notifyDataSetChanged();
				refreshView.onRefreshComplete();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		final String treeHoleUuid = bundle.getString("itemUuid");
		String username = bundle.getString("username");
		String content = bundle.getString("content");
		int praises = bundle.getInt("praises");
		int comments = bundle.getInt("comments");
		TextView textViewTreeHoleUsername = (TextView) findViewById(R.id.treehole_username);
		textViewTreeHoleUsername.setText(username);
		TextView textViewTreeHoleContent = (TextView) findViewById(R.id.treehole_content);
		textViewTreeHoleContent.setText(content);
		TextView textViewTreeHolePraises = (TextView) findViewById(R.id.treehole_praise_count);
		textViewTreeHolePraises.setText("点赞" + praises);
		TextView textViewTreeHoleComments = (TextView) findViewById(R.id.treehole_comment_count);
		textViewTreeHoleComments.setText(" · 评论 " + comments);

		ImageView imageViewBack = (ImageView) findViewById(R.id.comment_title_btn_back);
		imageViewBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		refreshView = (PullToRefreshListView) findViewById(R.id.commentList);
		refreshView.setMode(Mode.BOTH);
		listViewComments = refreshView.getRefreshableView();

		commentsAdapter = new CommentsAdapter(null, this);
		listViewComments.setAdapter(commentsAdapter);

		refreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {

				if (refreshView.isHeaderShown()) {
					// 下拉刷新目的是获取最新的数据,所以分页数置零,清除历史数据
					pageIndex = 0;
					commentsHistoryList = null;

				} else if (refreshView.isFooterShown()) {
					// 上拉获取更多数据
				}
				getCommentsData(handler, treeHoleUuid);
			}
		});

		getCommentsData(handler, treeHoleUuid);
	}

	private void convertTime(List<CommentItem> list) {

	}

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
					message.what = 0x001;
					handler.sendMessage(message);
				} catch (Exception e) {
					e.printStackTrace();
				}

				Looper.loop();
			}
		}.start();
	}

	public class CommentsAdapter extends BaseAdapter {

		private List<CommentItem> commentsList;

		private LayoutInflater inflater;

		public CommentsAdapter(List<CommentItem> list, Context context) {
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
		 * 
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
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CommentHolder holder;
			if (null == convertView) {
				convertView = inflater.inflate(R.layout.simple_item_comment,
						null);
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
			holder.textViewContent.setText(commentsList.get(position)
					.getContent());
			holder.textViewTime.setText(commentsList.get(position).getDate());
			holder.textViewFloor.setText(String.valueOf(position + 1));

			return convertView;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	public class CommentHolder {
		TextView textViewUsername;
		TextView textViewContent;
		TextView textViewTime;
		TextView textViewFloor;
	}
}
