package com.seiko.player;

/**
 * Created by xyoye on 2018/7/1.
 */
public final class PlayerConstants {
    /**
     * 打开字幕选择
     */
    public static final int INTENT_OPEN_DANMU = 1000;
    /**
     * 打开字幕选择
     */
    public static final int INTENT_OPEN_SUBTITLE = 1001;
    /**
     * 请求网络字幕
     */
    public static final int INTENT_QUERY_SUBTITLE = 1002;
    /**
     * 选择网络字幕
     */
    public static final int INTENT_SELECT_SUBTITLE = 1003;
    /**
     * 自动加载网络字幕
     */
    public static final int INTENT_AUTO_SUBTITLE = 1004;
    /**
     * 保存进度
     */
    public static final int INTENT_SAVE_CURRENT = 1005;

    /**
     * 重置全屏
     */
    public static final int INTENT_RESET_FULL_SCREEN = 1006;
    /**
     * 播放失败
     */
    public static final int INTENT_PLAY_FAILED = 1007;

    /**
     * 播放退出
     */
    public static final int INTENT_PLAY_END = 1008;

    /**
     * 播放结束
     */
    public static final int INTENT_PLAY_FINISH = 1009;



    //播放器
    public static final int EXO_PLAYER = 1;
    public static final int IJK_ANDROID_PLAYER = 2;
    public static final int IJK_PLAYER = 3;

    /**
     * 播放器配置表
     */
    public static final String PLAYER_CONFIG = "player_config";

    // 字幕
    public static final String SUBTITLE_LANGUAGE = "subtitle_language";
    public static final String SUBTITLE_CHINESE_SIZE = "subtitle_chinese_size_v2";
    public static final String SUBTITLE_ENGLISH_SIZE = "subtitle_english_size_v2";

    /**
     * 旋屏
     */
    public static final String ORIENTATION_CHANGE = "orientation_change";

    /**
     * 视频显示模式：View 是视频画面能显示的控件
     * 0 -> 填满View一边，另一边按比例缩小，整个视频显示在View里面
     * 1 -> 整个填满View，可能有一边会被裁剪一部分画面
     * 2 -> 根据原始视频大小来测量，不会超出View范围
     * 3 -> 按View的原始测量值来设定
     * 4 -> 按16：9的比例来处理，不会超出View范围
     * 5 -> 按4：3的比例来处理，不会超出View范围
     */
    public static final int AR_ASPECT_FIT_PARENT = 0; // without clip
    public static final int AR_ASPECT_FILL_PARENT = 1; // may clip
    public static final int AR_ASPECT_WRAP_CONTENT = 2;
    public static final int AR_MATCH_PARENT = 3;
    public static final int AR_16_9_FIT_PARENT = 4;
    public static final int AR_4_3_FIT_PARENT = 5;
}
