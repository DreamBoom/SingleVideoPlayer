package com.seiko.singlevideoplayer.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import com.seiko.player.ExoPlayerViewBuilder;
import com.seiko.player.OnOutsideListener;
import com.seiko.player.PlayerViewListener;
import com.seiko.player.PlayerConstants;
import com.seiko.singlevideoplayer.data.PlayParam;
import com.seiko.singlevideoplayer.receiver.ScreenBroadcastReceiver;
import com.seiko.player.widgets.CommonDialog;
import com.seiko.singlevideoplayer.utils.Constants;

/**
 * Created by xyoye on 2018/7/4 0004.
 * <p>
 * 播放器页面（不需支持换肤）
 *
 * @author xyoye
 */
public class PlayerActivity extends AppCompatActivity implements ScreenBroadcastReceiver.PlayerReceiverListener {

    private static final String PREF_NAME = "Prefs_Player_Setting";

    PlayerViewListener mPlayer;

    private boolean isInit = false;
    private String videoPath;
    private String videoTitle;
    private long currentPosition;

    //锁屏广播
    private ScreenBroadcastReceiver screenReceiver;

    /**
     * 内部事件回调
     */
    private OnOutsideListener onOutsideListener;

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        //注册监听广播
        screenReceiver = new ScreenBroadcastReceiver(this);
        registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        //获取播放参数
        PlayParam playParam = getIntent().getParcelableExtra("video_data");
        if (playParam == null) {
            return;
        }

        videoPath = playParam.getVideoPath();
        videoTitle = playParam.getVideoTitle();
        currentPosition = playParam.getCurrentPosition();

        //初始化接口
        initListener();

        //初始化播放器
        initPlayer();
    }

    private void initListener() {
        onOutsideListener = (what, extra) -> {
            switch (what) {
                case PlayerConstants.INTENT_SAVE_CURRENT:
                    break;
                case PlayerConstants.INTENT_RESET_FULL_SCREEN:
                    setFullScreen();
                    break;
                case PlayerConstants.INTENT_PLAY_FAILED:
                    CommonDialog.Builder builder = new CommonDialog
                            .Builder(this)
                            .setDismissListener(dialog -> {
                                PlayerActivity.this.finish();
                            })
                            .setAutoDismiss()
                            .setNightSkin()
                            .setTouchNotCancel();
                        builder.setHideOk(true)
                                .build()
                                .show("播放失败，请尝试更改播放器设置，或者切换其它播放内核",
                                        "播放器设置",
                                        "退出播放");
                    break;
//                case PlayerConstants.INTENT_PLAY_END:
//                    break;
                case PlayerConstants.INTENT_PLAY_FINISH:
                    mPlayer.onDestroy();
                    finish();
                    break;
                default:
            }
        };
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        mPrefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int playerType = mPrefs.getInt(Constants.PlayerConfig.SHARE_PLAYER_TYPE, PlayerConstants.EXO_PLAYER);
        mPlayer = getPlayListener(playerType);
        if (!(mPlayer instanceof View)) {
            throw new RuntimeException("PlayerView must be View.");
        }
        setContentView((View) mPlayer);
        mPlayer.start();
        isInit = true;
    }

    /**
     *
     * @param type 播放器类型
     * @return PlayerView
     */
    private PlayerViewListener getPlayListener(int type) {
//        if (type == PlayerConstants.EXO_PLAYER) {
            return new ExoPlayerViewBuilder(this, videoPath)
                    .setTitle(videoTitle)
                    .setOnInfoListener(onOutsideListener)
                    .setSkipTip(currentPosition)
                    .setSharedPreferences(mPrefs)
                    .setShowDialogOnBack(true)
                    .build();
//        } else {
//            return new IjkPlayerViewBuilder(this, videoPath)
//                    .setTitle(videoTitle)
//                    .setOnInfoListener(onOutsideListener)
//                    .setSkipTip(currentPosition)
//                    .setSharedPreferences(mPrefs)
//                    .build();
//        }
    }

    @Override
    protected void onDestroy() {
        mPlayer.onDestroy();
        unregisterReceiver(screenReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (isInit) {
            mPlayer.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (isInit) {
            mPlayer.onPause();
        }
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mPlayer.onKeyDown(keyCode) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
//        if (mPlayer.onBackPressed()) {
//            return;
//        }
//        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPlayer.configurationChanged(newConfig);
    }

    @Override
    public void onScreenLocked() {
        mPlayer.onScreenLocked();
    }

    /**
     * 设置全屏
     */
    private void setFullScreen() {
        //沉浸式状态栏
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //开启屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

}
