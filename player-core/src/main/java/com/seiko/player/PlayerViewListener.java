package com.seiko.player;

import android.content.res.Configuration;

/**
 * Created by xyoye on 2019/4/26.
 * @author seiko
 */
public interface PlayerViewListener {

    /**
     * 开始播放
     */
    void start();

    /**
     * 重启
     */
    void onResume();

    /**
     * 暂停
     */
    void onPause();

    /**
     * 注销
     */
    void onDestroy();

    /**
     * 按键监听
     * @param keyCode 按键code
     * @return bool
     */
    boolean onKeyDown(int keyCode);

    /**
     * 返回监听
     * @return bool
     */
    boolean onBackPressed();

    /**
     * 转屏
     * @param configuration 参数
     */
    void configurationChanged(Configuration configuration);

    void setSubtitlePath(String subtitlePath);

    void onScreenLocked();

}
