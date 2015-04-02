package com.guessmusic.tools;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

/**
 * Music player
 */
public class MyPlayer {

	// display music, singleton design pattern
	public final static int INDEX_TONE_ENTER = 0;
	public final static int INDEX_TONE_CANCEL = 1;
	public final static int INDEX_TONE_COIN = 2;
	// constant
	private final static String[] TONE_NAMES = { "enter.mp3", "cancel.mp3",
			"coin.mp3" };

	private static MediaPlayer mMusicMediaPlayer;
	private static MediaPlayer[] mToneMediaPlayer = new MediaPlayer[TONE_NAMES.length];

	/**
	 * play music
	 * 
	 * @param context
	 * @param fileName, music name
	 */
	public static void playSong(Context context, String fileName) {
		if (mMusicMediaPlayer == null) {
			mMusicMediaPlayer = new MediaPlayer();
		}

		// new player reset
		mMusicMediaPlayer.reset();

		// load file from asset
		AssetManager assetManager = context.getAssets();
		try {
			AssetFileDescriptor fileDescriptor = assetManager.openFd(fileName);
			mMusicMediaPlayer
					.setDataSource(fileDescriptor.getFileDescriptor(),
							fileDescriptor.getStartOffset(),
							fileDescriptor.getLength());

			mMusicMediaPlayer.prepare();
			mMusicMediaPlayer.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void stopSong(Context context) {
		if (mMusicMediaPlayer != null) {
			mMusicMediaPlayer.stop();
		}
	}
 
	public static void playTone(Context context, int index) {
		if (mToneMediaPlayer[index] == null) {
			mToneMediaPlayer[index] = new MediaPlayer();
			
			AssetManager assetManager = context.getAssets();
			try {
				AssetFileDescriptor fileDescriptor = assetManager
						.openFd(TONE_NAMES[index]);
				mToneMediaPlayer[index].setDataSource(
						fileDescriptor.getFileDescriptor(),
						fileDescriptor.getStartOffset(),
						fileDescriptor.getLength());

				mToneMediaPlayer[index].prepare();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// multiple times call start
		mToneMediaPlayer[index].start();
	}

}
