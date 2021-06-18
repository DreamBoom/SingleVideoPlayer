package com.seiko.player;

import android.content.Context;
import android.content.SharedPreferences;

public class ExoPlayerViewBuilder {

    private final Context mContext;
    private final String mVideoPath;
    private String mVideoTitle;
    private OnOutsideListener mOutSideListener;
    private long mCurrentPosition;
    private SharedPreferences mPrefs;
    private boolean mShowDialogOnBack;

    public ExoPlayerViewBuilder(Context context, String videoPath) {
        mContext = context;
        mVideoPath = videoPath;
    }

    public ExoPlayerViewBuilder setTitle(String title) {
        mVideoTitle = title;
        return this;
    }

    public ExoPlayerViewBuilder setOnInfoListener(OnOutsideListener listener) {
        mOutSideListener = listener;
        return this;
    }

    /**
     * 跳转至上一次播放进度
     * @param currentPosition 上一次进度
     * @return this
     */
    public ExoPlayerViewBuilder setSkipTip(long currentPosition) {
        mCurrentPosition = currentPosition;
        return this;
    }

    public ExoPlayerViewBuilder setSharedPreferences(SharedPreferences prefs) {
        mPrefs = prefs;
        return this;
    }

    public ExoPlayerViewBuilder setShowDialogOnBack(boolean showDialogOnBack) {
        mShowDialogOnBack = showDialogOnBack;
        return this;
    }


    public ExoPlayerView build() {
        ExoPlayerView playerView = new ExoPlayerView(mContext);
        playerView.setVideoPath(mVideoPath);
        playerView.setTitle(mVideoTitle);
        playerView.setOnInfoListener(mOutSideListener);
        playerView.setSkipTip(mCurrentPosition);
        playerView.setSharedPreferences(mPrefs);
        playerView.setShowDialogOnBack(mShowDialogOnBack);
        return playerView;
    }
}
