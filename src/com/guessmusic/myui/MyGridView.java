package com.guessmusic.myui;

import java.util.ArrayList;

import com.guessmusic.R;
import com.guessmusic.model.WordButton;
import com.guessmusic.model.iWordButtonClickListener;
import com.guessmusic.tools.MyPlayer;
import com.guessmusic.tools.Tools;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

public class MyGridView extends GridView {

	private ArrayList<WordButton> mArrayList = new ArrayList<WordButton>();

	private MyGridAdapter mAdapter;

	private Context mContext;

	private Animation mScaleAnim;

	private iWordButtonClickListener mWordButtonClickListener;

	public MyGridView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		mAdapter = new MyGridAdapter();
		this.setAdapter(mAdapter);
	}

	public void updateData(ArrayList<WordButton> list) {
		mArrayList = list;
		this.setAdapter(mAdapter);
	}

	class MyGridAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mArrayList.size();
		}

		@Override
		public Object getItem(int position) {
			return mArrayList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final WordButton btnWord; 

			if (convertView == null) {
				convertView = Tools.getView(mContext,
						R.layout.self_ui_gridview_item);

				btnWord = mArrayList.get(position); 

				mScaleAnim = AnimationUtils.loadAnimation(mContext,
						R.anim.word_scale);
				mScaleAnim.setStartOffset(position * 70);
				btnWord.mIndex = position;
				if (btnWord.mViewButton == null) {
					btnWord.mViewButton = (Button) convertView
							.findViewById(R.id.btn_gridview_item);
					btnWord.mViewButton
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									MyPlayer.playTone(mContext,
											MyPlayer.INDEX_TONE_ENTER);
									mWordButtonClickListener
											.onWordButtonClickListener(btnWord);
								}
							});
				}

				convertView.setTag(btnWord);

			} else {
				btnWord = (WordButton) convertView.getTag();
			}

			btnWord.mViewButton.setText(btnWord.mWordString);

			convertView.startAnimation(mScaleAnim);

			return convertView;
		}

	}

	/**
	 *
	 */
	public void registOnWordButtonClick(iWordButtonClickListener listener) {
		mWordButtonClickListener = listener;
	}

}
