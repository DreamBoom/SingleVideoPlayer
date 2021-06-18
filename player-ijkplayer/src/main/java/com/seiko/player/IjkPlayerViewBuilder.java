package com.seiko.player;

import android.content.Context;
import android.content.SharedPreferences;

public class IjkPlayerViewBuilder {

    private final Context mContext;
    private final String mVideoPath;
    private String mVideoTitle;
    private OnOutsideListener mOutSideListener;
    private long mCurrentPosition;
    private SharedPreferences mPrefs;

    public IjkPlayerViewBuilder(Context context, String videoPath) {
        mContext = context;
        mVideoPath = videoPath;
    }

    public IjkPlayerViewBuilder setTitle(String title) {
        mVideoTitle = title;
        return this;
    }

    public IjkPlayerViewBuilder setOnInfoListener(OnOutsideListener listener) {
        mOutSideListener = listener;
        return this;
    }

    /**
     * 跳转至上一次播放进度
     * @param currentPosition 上一次进度
     * @return this
     */
    public IjkPlayerViewBuilder setSkipTip(long currentPosition) {
        mCurrentPosition = currentPosition;
        return this;
    }

    public IjkPlayerViewBuilder setSharedPreferences(SharedPreferences prefs) {
        mPrefs = prefs;
        return this;
    }

    public IjkPlayerView build() {
        IjkPlayerView playerView = new IjkPlayerView(mContext);
        playerView.setVideoPath(mVideoPath);
        playerView.setTitle(mVideoTitle);
        playerView.setOnInfoListener(mOutSideListener);
        playerView.setSkipTip(mCurrentPosition);
        playerView.setSharedPreferences(mPrefs);
        return playerView;
    }
}
