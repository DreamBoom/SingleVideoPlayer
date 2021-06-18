package com.seiko.singlevideoplayer.utils;

public class Constants {

    /**
     * 播放器设置
     */
    public static class PlayerConfig {
        //播放器类型
        public static final String SHARE_PLAYER_TYPE = "player_type";
        //像素格式
        public static final String SHARE_PIXEL_FORMAT = "pixel_format";
        //硬解码
        public static final String SHARE_MEDIA_CODE_C = "media_code_c";
        //h265硬解码
        public static final String SHARE_MEDIA_CODE_C_H265 = "media_code_c_h265";
        //openSL es
        public static final String SHARE_OPEN_SLES = "open_sles";
        //surface view
        public static final String SHARE_SURFACE_RENDERS = "surface_renders";
        //自动加载网络弹幕
        public static final String AUTO_LOAD_DANMU = "auto_load_danmu";
        //是否使用网络字幕
        public static final String USE_NETWORK_SUBTITLE = "use_network_subtitle";
        //自动加载同名字幕
        public static final String AUTO_LOAD_LOCAL_SUBTITLE = "auto_load_local_subtitle";
        //自动加载网络字幕
        public static final String AUTO_LOAD_NETWORK_SUBTITLE = "auto_load_network_subtitle";
        //展示外链视频选择弹幕弹窗
        public static final String SHOW_OUTER_CHAIN_DANMU_DIALOG = "show_outer_chain_danmu_dialog";
        //外链打开时是否选择弹幕
        public static final String OUTER_CHAIN_DANMU_SELECT = "outer_chain_danmu_select";
        //在线播放日志开关
        public static final String ONLINE_PLAY_LOG = "online_play_log";

        //像素格式子项
        public static final String PIXEL_AUTO = "";
        public static final String PIXEL_RGB565 = "fcc-rv16";
        public static final String PIXEL_RGB888 = "fcc-rv24";
        public static final String PIXEL_RGBX8888 = "fcc-rv32";
        public static final String PIXEL_YV12 = "fcc-yv12";
        public static final String PIXEL_OPENGL_ES2 = "fcc-_es2";
    }

}
