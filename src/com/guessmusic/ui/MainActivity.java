package com.guessmusic.ui;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.guessmusic.R;
import com.guessmusic.data.Const;
import com.guessmusic.model.GetSong;
import com.guessmusic.model.WordButton;
import com.guessmusic.model.iDialogButtonListener;
import com.guessmusic.model.iWordButtonClickListener;
import com.guessmusic.myui.MyGridView;
import com.guessmusic.tools.FileOperate;
import com.guessmusic.tools.MyPlayer;
import com.guessmusic.tools.Tools;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements iWordButtonClickListener {
	
	public final static int COUNTS_WORDS = 24;

	// answer status
	public final static int ANSWER_RIGHT = 1;
	public final static int ANSWER_WRONG = 2;
	public final static int ANSWER_LACK = 3;

	public final static int ID_DIALOG_DELET_WORD = 1;
	public final static int ID_DIALOG_TIP_WORD = 2;
	public final static int ID_DIALOG_LACK_COINS_WORD = 3;

	// 
	public final static int SPARD_TIME = 6;

	// 
	private Animation mDiscAnim, mDiscBarInAnim, mDiscBarOutAnim;
	private LinearInterpolator mDiscLin, mDiscBarInLin, mDiscBarOutLin;

	// 
	private ImageView mViewDisc, mViewDiscBar;
	private ImageButton mbtnGameStart;

	// 
	private boolean mIsRunning = false;

	// 
	private ArrayList<WordButton> mAllWords, mSelWords;

	// 
	private MyGridView mMyGridView;

	// 
	private LinearLayout mViewWordsContainer;

	// 
	private int mCurrentIndex;
	private TextView mTextPassIndex;
	private TextView mTextCurrentIndex;

	// 
	private GetSong mCurrentSong;
	private TextView mTextCurrentPassSongName;

	// 
	private LinearLayout mPassEvent;

	// 
	private ImageButton mBtnTip, mBtnDelet;

	//
	private TextView mTextCurrentCoin;
	private int mCurrentCoins;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/**
		 * 
		 */
		// 
		mViewDisc = (ImageView) findViewById(R.id.view_disc);
		mViewDiscBar = (ImageView) findViewById(R.id.view_disc_bar);
		// 
		mbtnGameStart = (ImageButton) findViewById(R.id.btn_game_start);
		// 
		mMyGridView = (MyGridView) findViewById(R.id.gridview);
		//
		mViewWordsContainer = (LinearLayout) findViewById(R.id.layout_word_sel);
		// 
		mBtnTip = (ImageButton) findViewById(R.id.btn_tip_word);
		mBtnDelet = (ImageButton) findViewById(R.id.btn_delet_word);

		/**
		 * 
		 */
		// 
		mDiscAnim = AnimationUtils.loadAnimation(this, R.anim.disc_rotate);
		mDiscLin = new LinearInterpolator();
		mDiscAnim.setInterpolator(mDiscLin);
		mDiscAnim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mViewDiscBar.startAnimation(mDiscBarOutAnim); // 
			}
		});
		// 
		mDiscBarInAnim = AnimationUtils.loadAnimation(this,
				R.anim.disc_bar_in_rotate);
		mDiscBarInLin = new LinearInterpolator();
		mDiscBarInAnim.setInterpolator(mDiscBarInLin);
		mDiscBarInAnim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mViewDisc.startAnimation(mDiscAnim); // 
				// 
				MyPlayer.playSong(MainActivity.this,
						mCurrentSong.getSongFileName());
			}
		});
		// 
		mDiscBarOutAnim = AnimationUtils.loadAnimation(this,
				R.anim.disc_bar_out_rotate);
		mDiscBarOutLin = new LinearInterpolator();
		mDiscBarOutAnim.setInterpolator(mDiscBarOutLin);
		mDiscBarOutAnim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mbtnGameStart.setVisibility(View.VISIBLE); // 
				mIsRunning = false;
			}
		});

		// 
		mbtnGameStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 
				handlePlayButton();
			}
		});

		// 
		mCurrentIndex = FileOperate.dataLoad(MainActivity.this)[FileOperate.GAME_LEVEL];

		// current level index
		mTextCurrentIndex = (TextView) findViewById(R.id.text_level);
		mTextCurrentIndex.setText((mCurrentIndex + 1) + "");
 
		mCurrentCoins = FileOperate.dataLoad(MainActivity.this)[FileOperate.GAME_COINS];
 
		mTextCurrentCoin = (TextView) findViewById(R.id.text_bar_coins);
		mTextCurrentCoin.setText(mCurrentCoins + "");
 
		mMyGridView.registOnWordButtonClick(this);

		initCurrentData();
 
		handleDeletWord();

		handleTipWord();

	}

	@Override
	protected void onPause() {
		// pause animation
		mViewDisc.clearAnimation();

		// pause song
		MyPlayer.stopSong(MainActivity.this);
		super.onPause();

		// 
		FileOperate.dataSave(MainActivity.this, mCurrentIndex, mCurrentCoins);
	}

	/**
	 * 
	 */
	private void handlePlayButton() {
		if (mViewDiscBar != null) {
			if (!mIsRunning) {
				// 
				mViewDiscBar.startAnimation(mDiscBarInAnim);
				mbtnGameStart.setVisibility(View.INVISIBLE);
				mIsRunning = true;
			}
		}
		//MyPlayer.playSong(MainActivity.this, mCurrentSong.getSongFileName());
	}

	/**
	 * load current level info
	 */
	private void initCurrentData() {

		// 
		mSelWords = initSelectWord();
		// 
		LayoutParams params = new LayoutParams(120, 120);

		// clear old data
		mViewWordsContainer.removeAllViews();

		// add new data
		for (int i = 0; i < mSelWords.size(); i++) {
			mViewWordsContainer.addView(mSelWords.get(i).mViewButton, params);
		}

		// display current data
		mAllWords = initGetWord();
		mMyGridView.updateData(mAllWords);

		// call music function 
		handlePlayButton();

	}

	/**
	 * 
	 */
	private ArrayList<WordButton> initSelectWord() {

		mCurrentSong = initCurrentSong(); 

		ArrayList<WordButton> data = new ArrayList<WordButton>();

		for (int i = 0; i < mCurrentSong.getSongNameLenth(); i++) {
			View view = Tools.getView(MainActivity.this,
					R.layout.self_ui_gridview_item);

			final WordButton btnWord = new WordButton();
			btnWord.mViewButton = (Button) view
					.findViewById(R.id.btn_gridview_item);
			btnWord.mViewButton.setTextColor(Color.WHITE);
			btnWord.mViewButton.setText("");
			btnWord.mIsVisible = false;
			btnWord.mViewButton
					.setBackgroundResource(R.drawable.game_wordblank); // 
			data.add(btnWord);
			btnWord.mViewButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					cleanSelectWord(btnWord);
					// play the cancel tone
					MyPlayer.playTone(MainActivity.this,
							MyPlayer.INDEX_TONE_CANCEL);
				}
			});
		}
		return data;
	}

	/**
	 * 
	 */
	private ArrayList<WordButton> initGetWord() {
		ArrayList<WordButton> data = new ArrayList<WordButton>();

		// 
		String[] allWord = generateAllWord();

		// 
		for (int i = 0; i < COUNTS_WORDS; i++) {
			WordButton btn = new WordButton();
			btn.mWordString = allWord[i];
			data.add(btn);
		}
		return data;
	}

	@Override
	/**
	 * 
	 */
	public void onWordButtonClickListener(WordButton wordButton) {
		// 
		setSelectWord(wordButton);

		// 
		int checkAnswer = checkAnswer();

		// 
		if (checkAnswer == ANSWER_RIGHT) {
			for (int i = 0; i < mSelWords.size(); i++) {
				mSelWords.get(i).mViewButton.setTextColor(Color.WHITE);
			}
			handlePassEvent();
		}
		// 
		else if (checkAnswer == ANSWER_WRONG) {
			sparkWord();
		}
		
		else if (checkAnswer == ANSWER_LACK) {
			for (int i = 0; i < mSelWords.size(); i++) {
				mSelWords.get(i).mViewButton.setTextColor(Color.WHITE);
			}
		}

	}

	/**
	 * 
	 */
	private GetSong initCurrentSong() {
		GetSong song = new GetSong();
		String temp[] = Const.SONG_INFO[mCurrentIndex];
		song.setSongFileName(temp[Const.INDEX_FILE_NAME]);
		song.setSongName(temp[Const.INDEX_SONG_NAME]);

		return song;
	}

	/**
	 * 
	 */
	private char getRandomWord() {
//		String word = "";
//		int hight, low;

//		Random random = new Random();
//
//		hight = (176 + Math.abs(random.nextInt(30)));
//		low = (161 + Math.abs(random.nextInt(70)));
//
//		byte[] wordByte = new byte[2];
//		wordByte[0] = (Integer.valueOf(hight)).byteValue();
//		wordByte[1] = (Integer.valueOf(low)).byteValue();
//		try {
//			word = new String(wordByte, "GBK");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
		Random r = new Random();
		char c = (char) (r.nextInt(25)+65);

		return c;
	}

	/**
	 * 
	 */
	private String[] generateAllWord() {

		String allWord[] = new String[COUNTS_WORDS];
		Random random = new Random();
		//
		for (int i = 0; i < mCurrentSong.getSongNameLenth(); i++) {
			allWord[i] = mCurrentSong.getSongNameChar()[i] + "";
		}
		//
		for (int i = mCurrentSong.getSongNameLenth(); i < COUNTS_WORDS; i++) {
			allWord[i] = getRandomWord() + "";
		}
		// 
		for (int i = COUNTS_WORDS - 1; i >= 0; i--) {
			int randomIndex = random.nextInt(i + 1);
			String temp;
			temp = allWord[randomIndex];
			allWord[randomIndex] = allWord[i];
			allWord[i] = temp;
		}
		return allWord;
	}

	/**
	 * 
	 */
	private void setSelectWord(WordButton wordButton) {
		for (int i = 0; i < mCurrentSong.getSongNameLenth(); i++) {
			// store string name 
			if (mSelWords.get(i).mWordString.length() == 0) {
				mSelWords.get(i).mViewButton.setText(wordButton.mWordString);
				mSelWords.get(i).mWordString = wordButton.mWordString;

				// store index
				mSelWords.get(i).mIndex = wordButton.mIndex;
				setAllWordVisible(wordButton);

				break;
			}
		}
	}

	/**
	 * set visibility according to view 
	 */
	private void setAllWordVisible(WordButton wordButton) {
		wordButton.mIsVisible = false;
		wordButton.mViewButton.setVisibility(View.INVISIBLE);
	}

	/**
	 * clear the selected word, string to empty, visibility to invisible
	 */
	private void cleanSelectWord(WordButton wordButton) {
		wordButton.mWordString = "";
		wordButton.mViewButton.setText("");

		mAllWords.get(wordButton.mIndex).mViewButton
				.setVisibility(View.VISIBLE);
		mAllWords.get(wordButton.mIndex).mIsVisible = true;

	}

	/**
	 * 
	 */
	private int checkAnswer() {
		// check length
		for (int i = 0; i < mSelWords.size(); i++) { 
			if (mSelWords.get(i).mWordString.length() == 0) {
				return ANSWER_LACK;
			}
		}

		// check validation
		StringBuffer strBuf = new StringBuffer();
		for (int i = 0; i < mSelWords.size(); i++) {
			strBuf.append(mSelWords.get(i).mWordString);
		}
		return (strBuf.toString().equals(mCurrentSong.getSongName())) ? ANSWER_RIGHT
				: ANSWER_WRONG;
	}

	/**
	 * 
	 */
	private void sparkWord() {
		
		TimerTask task = new TimerTask() {
			boolean change = false;
			int spardTimes = 0;

			@Override
			public void run() {
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (++spardTimes > SPARD_TIME) {
							return;
						}

						for (int i = 0; i < mSelWords.size(); i++) {
							mSelWords.get(i).mViewButton
									.setTextColor(change ? Color.RED
											: Color.WHITE);
						}

						change = !change;
					}
				});
			}
		};
		Timer timer = new Timer();
		timer.schedule(task, 1, 150);
	}

	/**
	 * 
	 */
	private void handlePassEvent() {
		//display the pass-event interface
		mPassEvent = (LinearLayout) this.findViewById(R.id.layout_pass_event);
		mPassEvent.setVisibility(View.VISIBLE);

		//stop the disc
		mViewDisc.clearAnimation();

		// 
		MyPlayer.stopSong(MainActivity.this);

		MyPlayer.playTone(MainActivity.this, MyPlayer.INDEX_TONE_COIN);

		mCurrentCoins += Const.PASS_AWARD_COINS;
		mTextCurrentCoin.setText(mCurrentCoins + "");
		
		mTextCurrentIndex.setText((++mCurrentIndex + 1) + "");

		//current level index
		mTextPassIndex = (TextView) findViewById(R.id.text_pass_level);
		mTextPassIndex.setText((mCurrentIndex + 1) + "");

		//display song name
		mTextCurrentPassSongName = (TextView) findViewById(R.id.text_pass_song_name);
		mTextCurrentPassSongName.setText(mCurrentSong.getSongName());

	
		ImageButton btnPass = (ImageButton) findViewById(R.id.btn_pass_next);
		btnPass.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				MyPlayer.playTone(MainActivity.this, MyPlayer.INDEX_TONE_ENTER);
				if (judegIsPassed()) {// transfer from current view to all pass view
					Tools.startIntent(MainActivity.this, AllPassView.class);
				} else {
					mPassEvent.setVisibility(View.INVISIBLE);
					initCurrentData();
				}
			}
		});

	}

	/**
	 * check whether meet the end
	 */
	private boolean judegIsPassed() {
		return mCurrentIndex == Const.SONG_INFO.length;
	}

	/**
	 * read data from res file
	 */
	private int getDeletWordCoin() {
		return this.getResources().getInteger(R.integer.pay_delete_word);
	}

	/**
	 * 
	 */
	private int getTipWordCoin() {
		return this.getResources().getInteger(R.integer.pay_tip_answer);
	}

	/**
	 * 
	 */
	private void handleCoins(int data) {

		mCurrentCoins += data;
		mTextCurrentCoin.setText(mCurrentCoins + "");

	}

	/**
	 * delete one wrong word
	 */
	private void deletOneWord() {

		if (mCurrentCoins - getDeletWordCoin() >= 0) {
			if (findNotAnswer() != null) {
				setAllWordVisible(findNotAnswer());

				// 
				handleCoins(-getDeletWordCoin());
			}
		} else {
			showConfirmDialog(ID_DIALOG_LACK_COINS_WORD);
		}
	}

	/**
	 * 
	 */
	private void tipOneWord() {
		boolean tipWord = false;

		if (mCurrentCoins - getTipWordCoin() >= 0) {
			for (int i = 0; i < mSelWords.size(); i++) {
				// 
				if (mSelWords.get(i).mWordString.length() == 0) {
					onWordButtonClickListener(findIsAnswer(i));
					tipWord = true;

					// 
					handleCoins(-getTipWordCoin());
					break;
				}
			}
		} else {
			showConfirmDialog(ID_DIALOG_LACK_COINS_WORD);
		}
		// 
		if (!tipWord) {
			// 
			sparkWord();
		}

	}

	/**
	 * 
	 */
	private WordButton findNotAnswer() {
		Random random = new Random();
		WordButton buf = null;
		int count = 0;
		while (true) {
			// 
			int index = random.nextInt(COUNTS_WORDS);
			buf = mAllWords.get(index);

			if (buf.mIsVisible && !isTheAnswerWord(buf)) {
				return buf;
			}
			count++;
			if (count > 1000) {
				return null;
			}
		}
	}

	/**
	 * 
	 */
	private WordButton findIsAnswer(int index) {
		WordButton buf = null;

		for (int i = 0; i < COUNTS_WORDS; i++) {
			buf = mAllWords.get(i);

			// 
			if (buf.mWordString.equals(mCurrentSong.getSongNameChar()[index]
					+ "")) {
				return buf;
			}
		}

		return null;
	}

	/**
	 * 
	 */
	private boolean isTheAnswerWord(WordButton word) {
		boolean result = false;

		for (int i = 0; i < mCurrentSong.getSongNameLenth(); i++) {
			if (word.mWordString.equals("" + mCurrentSong.getSongNameChar()[i])) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * 
	 */
	private void handleDeletWord() {
		mBtnDelet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MyPlayer.playTone(MainActivity.this, MyPlayer.INDEX_TONE_ENTER);
				showConfirmDialog(ID_DIALOG_DELET_WORD);
			}
		});
	}

	/**
	 * 
	 */
	private void handleTipWord() {
		mBtnTip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MyPlayer.playTone(MainActivity.this, MyPlayer.INDEX_TONE_ENTER);
				showConfirmDialog(ID_DIALOG_TIP_WORD);
			}
		});
	}

	/**
	 * 
	 */
	// 
	private iDialogButtonListener mBtnConfirmDeletWordListener = new iDialogButtonListener() {

		@Override
		public void onClick() {
			deletOneWord();
		}
	};

	// ʾ
	private iDialogButtonListener mBtnConfirmTipWordListener = new iDialogButtonListener() {

		@Override
		public void onClick() {
			tipOneWord();
		}
	};

	// 
	private iDialogButtonListener mBtnConfirmLackCoinsListener = new iDialogButtonListener() {

		@Override
		public void onClick() {

		}
	};

	/**
	 * 
	 */
	private void showConfirmDialog(int id) {
		switch (id) {
		case ID_DIALOG_DELET_WORD:
			Tools.showDialog(MainActivity.this, "Spend " + getDeletWordCoin()
					+ " coins to delete a wrong letter", mBtnConfirmDeletWordListener);
			break;
		case ID_DIALOG_TIP_WORD:
			Tools.showDialog(MainActivity.this, "Spend " + getTipWordCoin()
					+ " coins to get a right letter", mBtnConfirmTipWordListener);
			break;
		case ID_DIALOG_LACK_COINS_WORD:
			Tools.showDialog(MainActivity.this, "Not enough coins",
					mBtnConfirmLackCoinsListener);
			break;
		default:
			break;
		}
	}
}
