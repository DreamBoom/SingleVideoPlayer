package com.seiko.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.seiko.player.utils.ConvertUtils;
import com.seiko.player.utils.AnimHelper;
import com.seiko.player.utils.CommonPlayerUtils;
import com.seiko.player.utils.TimeFormatUtils;
import com.seiko.player.widgets.BottomBarView;
import com.seiko.player.widgets.DialogScreenShot;
import com.seiko.player.widgets.top.SettingPlayerView;
import com.seiko.player.subtitle.SubtitleSettingView;
import com.seiko.player.widgets.SkipTipView;
import com.seiko.player.widgets.top.TopBarView;
import com.seiko.player.ijkplayer.R;
import com.seiko.player.media.IjkVideoView;
import com.seiko.player.media.MediaPlayerParams;
import com.seiko.player.media.VideoInfoTrack;
import com.seiko.player.subtitle.SubtitleParser;
import com.seiko.player.subtitle.SubtitleView;
import com.seiko.player.subtitle.format.TimedTextObject;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;


/**
 * Created by xyoye on 2019/5/9.
 */

public class IjkPlayerView extends FrameLayout implements PlayerViewListener {
    //??????????????????????????????
    private static final int HIDE_VIEW_ALL = 0;
    //??????????????????????????????????????????????????????????????????????????????
    private static final int HIDE_VIEW_AUTO = 1;
    //??????????????????????????????????????????
    private static final int HIDE_VIEW_LOCK_SCREEN = 2;
    //?????????????????????????????????????????????
    private static final int HIDE_VIEW_END_GESTURE = 3;
    //?????????????????????????????????????????????????????????????????????????????????
    private static final int HIDE_VIEW_EDIT = 4;

    // ??????????????????
    private static final int MAX_VIDEO_SEEK = 1000;
    // ???????????????????????????
    private static final int DEFAULT_HIDE_TIMEOUT = 5000;
    // ??????????????????
    private static final int MSG_UPDATE_SEEK = 10086;
    // ????????????????????????
    private static final int MSG_ENABLE_ORIENTATION = 10087;
    // ??????????????????
    private static final int MSG_UPDATE_SUBTITLE = 10088;
    //???????????????
    private static final int MSG_SET_SUBTITLE_SOURCE = 10089;
    // ????????????
    private static final int INVALID_VALUE = -1;

    //??????View
    private IjkVideoView mVideoView;
    //??????View
    private SubtitleView mSubtitleView;
    //????????????
    private TopBarView topBarView;
    //????????????
    private BottomBarView bottomBarView;
    //????????????
    private SkipTipView skipTipView;
    //??????????????????
    private SkipTipView skipSubView;
    // ??????
    private ProgressBar mLoadingView;
    // ??????
    private TextView mTvVolume;
    // ??????
    private TextView mTvBrightness;
    // ?????????????????????
    private TextView mSkipTimeTv;
    // ??????????????????
    private FrameLayout mFlTouchLayout;
    // ????????????????????????
    private FrameLayout mFlVideoBox;
    // ?????????
    private ImageView mIvPlayerLock;
    // ?????????
    private ImageView mIvScreenShot;

    // ?????????Activity
    private Activity mAttachActivity;
    // ????????????
    private AudioManager mAudioManager;
    // ????????????
    private GestureDetector mGestureDetector;
    // ????????????????????????
    private OrientationEventListener mOrientationListener;
    // ???????????????
    private OnOutsideListener mOutsideListener;

    // ????????????
    private int mMaxVolume;
    // ??????
    private boolean mIsForbidTouch = false;
    // ?????????????????????
    private boolean mIsShowBar = true;
    // ???????????????????????????
    private boolean mIsSeeking;
    // ????????????
    private long mTargetPosition = INVALID_VALUE;
    // ????????????
    private long mCurPosition = INVALID_VALUE;
    // ????????????
    private int mCurVolume = INVALID_VALUE;
    // ????????????
    private float mCurBrightness = INVALID_VALUE;
    // ????????????
    private int mInitHeight;
    // ?????????/??????
    private int mWidthPixels;
    // ??????????????????
    private boolean mIsNeverPlay = true;
    //????????????????????????
    private long mSkipPosition = INVALID_VALUE;
    //??????????????????
    private boolean isShowSubtitle = false;
    // ??????????????????
    private boolean mIsPlayComplete = false;
    //????????????surfaceView
    private boolean isUseSurfaceView;
    //?????????????????????
    private boolean isUsemediaCodeC;
    //????????????h265?????????
    private boolean isUsemediaCodeCH265;
    //????????????OpenSLES
    private boolean isUseOpenSLES;
    //????????????????????????
    private int usePlayerType;
    //????????????????????????
    private String usePixelFormat;

    //????????????????????????
    private boolean isQueryNetworkSubtitle = false;
    //??????????????????????????????
    private boolean isAutoLoadLocalSubtitle = false;
    //??????????????????????????????
    private boolean isAutoLoadNetworkSubtitle = false;

    //???????????????
    private List<String> cloudFilterList = new ArrayList<>();
    //???????????????
    private List<VideoInfoTrack> audioTrackList = new ArrayList<>();
    //???????????????
    private List<VideoInfoTrack> subtitleTrackList = new ArrayList<>();

    /**
     * ?????????????????????Runnable
     */
    private Runnable mHideBarRunnable = () -> hideView(HIDE_VIEW_AUTO);

    /**
     * ????????????????????????????????????Runnable
     */
    private Runnable mHideTouchViewRunnable = () -> hideView(HIDE_VIEW_END_GESTURE);

    /**
     * ???????????????????????????????????????Runnable
     */
    private Runnable mHideSkipTipRunnable = this::_hideSkipTip;

    /**
     * ??????????????????????????????Runnable
     */
    private Runnable mHideSkipSubRunnable = this::_hideSkipSub;

    SubtitleSettingView mSubtitleSettingView;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //??????????????????
                case MSG_UPDATE_SEEK:
                    long pos = _setProgress();
                    if (!mIsSeeking && mIsShowBar && mVideoView.isPlaying()) {
                        msg = obtainMessage(MSG_UPDATE_SEEK);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                //????????????????????????
                case MSG_ENABLE_ORIENTATION:
                    setOrientationEnable(true);
                    break;
                //??????????????????
                case MSG_UPDATE_SUBTITLE:
                    if (mSubtitleSettingView.isLoadSubtitle() && isShowSubtitle) {
                        long position = mVideoView.getCurrentPosition() + (int) (mSubtitleSettingView.getTimeOffset() * 1000);
                        mSubtitleView.seekTo(position);
                        msg = obtainMessage(MSG_UPDATE_SUBTITLE);
                        sendMessageDelayed(msg, 1000);
                    }
//                    if (topBarView.getSubtitleSettingView().isLoadSubtitle() && isShowSubtitle) {
//                        long position = mVideoView.getCurrentPosition() + (int) (topBarView.getSubtitleSettingView().getTimeOffset() * 1000);
//                        mSubtitleView.seekTo(position);
//                        msg = obtainMessage(MSG_UPDATE_SUBTITLE);
//                        sendMessageDelayed(msg, 1000);
//                    }
                    break;
                //???????????????
                case MSG_SET_SUBTITLE_SOURCE:
                    TimedTextObject subtitleObj = (TimedTextObject) msg.obj;
                    isShowSubtitle = true;
                    mSubtitleSettingView.setLoadSubtitle(true);
                    mSubtitleSettingView.setSubtitleLoadStatus(true);
//                    topBarView.getSubtitleSettingView().setLoadSubtitle(true);
//                    topBarView.getSubtitleSettingView().setSubtitleLoadStatus(true);
                    mSubtitleView.setData(subtitleObj);
                    mSubtitleView.start();
                    Toast.makeText(getContext(), "??????????????????", Toast.LENGTH_LONG).show();
                    break;
                default:
            }
        }
    };

    public IjkPlayerView(Context context) {
        this(context, null);
    }

    public IjkPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViewBefore(context);
        initView();
        initViewCallBak();
        initViewAfter();
    }

    //
    private SharedPreferences mPrefs;

    public void setSharedPreferences(SharedPreferences prefs) {
        mPrefs = prefs;
    }

    public SharedPreferences getPrefs() {
        if (mPrefs == null) {
            mPrefs = getContext().getSharedPreferences(PlayerConstants.PLAYER_CONFIG, Context.MODE_PRIVATE);
        }
        return mPrefs;
    }
    //

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mInitHeight == 0) {
            mInitHeight = getHeight();
            mWidthPixels = getResources().getDisplayMetrics().widthPixels;
        }
    }

    private void initViewBefore(Context context) {
        if (!(context instanceof AppCompatActivity)) {
            throw new IllegalArgumentException("Context must be AppCompatActivity");
        }
        //???????????????Activity??????
        mAttachActivity = (AppCompatActivity) context;
        //????????????
        View.inflate(context, R.layout.layout_ijk_player_view_v2, this);
        //??????????????????
        mOrientationListener = new OrientationEventListener(mAttachActivity) {
            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onOrientationChanged(int orientation) {
                if (mIsNeverPlay) {
                    return;
                }
                // ??????????????????????????????
                if (orientation >= 60 && orientation <= 120) {
                    mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                } else if (orientation >= 240 && orientation <= 300) {
                    mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        };
        //???????????????
        mAudioManager = (AudioManager) mAttachActivity.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager != null) {
            mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        //????????????
        try {
            int brightness = Settings.System.getInt(mAttachActivity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            float progress = 1.0F * (float) brightness / 255.0F;
            WindowManager.LayoutParams layout = mAttachActivity.getWindow().getAttributes();
            layout.screenBrightness = progress;
            mAttachActivity.getWindow().setAttributes(layout);
        } catch (Settings.SettingNotFoundException var7) {
            var7.printStackTrace();
        }

        //????????????surfaceView
        isUseSurfaceView = getPrefs().getBoolean("surface_renders", false);
        //?????????????????????
        isUsemediaCodeC = getPrefs().getBoolean("media_code_c", false);
        //????????????h265?????????
        isUsemediaCodeCH265 = getPrefs().getBoolean("media_code_c_h265", false);
        //????????????OpenSLES
        isUseOpenSLES = getPrefs().getBoolean("open_sles", false);
        //????????????????????????
        usePlayerType = getPrefs().getInt("player_type", PlayerConstants.IJK_PLAYER);
        //????????????????????????
        usePixelFormat = getPrefs().getString("pixel_format", "fcc-_es2");
    }

    private void initView() {
        //??????????????????????????????????????????
        mVideoView = findViewById(R.id.ijk_video_view);
//        mDanmakuView = findViewById(R.id.sv_danmaku);
        mSubtitleView = findViewById(R.id.subtitle_view);
        //??????????????????????????????
        bottomBarView = findViewById(R.id.bottom_bar_view);
        topBarView = findViewById(R.id.top_bar_view);
        skipTipView = findViewById(R.id.skip_tip_view);
        skipSubView = findViewById(R.id.skip_subtitle_view);
        //loading?????????????????????????????????
        mLoadingView = findViewById(R.id.pb_loading);
        mTvVolume = findViewById(R.id.tv_volume);
        mTvBrightness = findViewById(R.id.tv_brightness);
        mSkipTimeTv = findViewById(R.id.tv_skip_time);
        mFlTouchLayout = findViewById(R.id.fl_touch_layout);
        //???????????????
        mFlVideoBox = findViewById(R.id.fl_video_box);
        //???????????????
        mIvPlayerLock = findViewById(R.id.iv_player_lock);
        mIvScreenShot = findViewById(R.id.iv_player_shot);
        // ??????????????????
        mSubtitleSettingView = findViewById(R.id.subtitle_setting_view);
        //???????????????????????????
        initSubtitleSettingView();
        //??????????????????????????????
        initPlayerSettingView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViewCallBak() {
        //???????????????????????????
        View.OnTouchListener mPlayerTouchListener = (v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mHandler.removeCallbacks(mHideBarRunnable);
                    break;
                case MotionEvent.ACTION_UP:
                    _endGesture();
                    break;
                default:
            }
            return mGestureDetector.onTouchEvent(event);
        };
        //??????????????????
        GestureDetector.OnGestureListener mPlayerGestureListener = new GestureDetector.SimpleOnGestureListener() {
            // ???????????????????????????????????????????????????true??????????????????false???????????????
            private boolean isDownTouch;
            // ??????????????????,????????????????????????true??????????????????false???????????????
            private boolean isVolume;
            // ?????????????????????????????????????????????true??????????????????false???????????????
            private boolean isLandscape;

            @Override
            public boolean onDown(MotionEvent e) {
                isDownTouch = true;
                return super.onDown(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float diStanceX, float diStanceY) {
                if (!mIsForbidTouch && !mIsNeverPlay) {
                    float mOldX = e1.getX(), mOldY = e1.getY();
                    float deltaY = mOldY - e2.getY();
                    float deltaX = mOldX - e2.getX();
                    if (isDownTouch) {
                        // ???????????????????????????
                        isLandscape = Math.abs(diStanceX) >= Math.abs(diStanceY);
                        // ??????????????????????????????
                        isVolume = mOldX > getResources().getDisplayMetrics().widthPixels * 0.5f;
                        isDownTouch = false;
                    }

                    if (isLandscape) {
                        _onProgressSlide(-deltaX / mVideoView.getWidth());
                    } else {
                        float percent = deltaY / mVideoView.getHeight();
                        if (isVolume) {
                            _onVolumeSlide(percent);
                        } else {
                            _onBrightnessSlide(percent);
                        }
                    }
                }
                return super.onScroll(e1, e2, diStanceX, diStanceY);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                _toggleControlBar();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // ???????????????????????????????????????????????????????????????????????????
                if (mIsNeverPlay) {
                    return true;
                }
                if (!mIsForbidTouch) {
                    _refreshHideRunnable();
                    _togglePlayStatus();
                }
                return true;
            }
        };
        //???????????????
        SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

            private long curPosition;

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
                mIsSeeking = true;
                _showControlBar(3600000);
                mHandler.removeMessages(MSG_UPDATE_SEEK);
                curPosition = mVideoView.getCurrentPosition();
            }

            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                long duration = mVideoView.getDuration();
                // ??????????????????
                mTargetPosition = (duration * progress) / MAX_VIDEO_SEEK;
                int deltaTime = (int) ((mTargetPosition - curPosition) / 1000);
                String desc;
                // ??????????????????????????????????????????
                if (mTargetPosition > curPosition) {
                    desc = TimeFormatUtils.generateTime(mTargetPosition) + "/" + TimeFormatUtils.generateTime(duration) + "\n" + "+" + deltaTime + "???";
                } else {
                    desc = TimeFormatUtils.generateTime(mTargetPosition) + "/" + TimeFormatUtils.generateTime(duration) + "\n" + deltaTime + "???";
                }
                setSkipTimeText(desc);
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                hideView(HIDE_VIEW_AUTO);
                mIsSeeking = false;
                // ????????????
                seekTo((int) mTargetPosition);
                mTargetPosition = INVALID_VALUE;
                _setProgress();
                _showControlBar(DEFAULT_HIDE_TIMEOUT);
            }
        };
        //???????????????????????????
        IMediaPlayer.OnPreparedListener ijkPreparedCallback = iMediaPlayer -> {
            ITrackInfo[] info = mVideoView.getTrackInfo();
            if (info != null) {
                int selectAudioTrack = mVideoView.getSelectedTrack(IjkTrackInfo.MEDIA_TRACK_TYPE_AUDIO);
                int audioN = 1;
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getTrackType() == IjkTrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                        VideoInfoTrack videoInfoTrack = new VideoInfoTrack();
                        String name = TextUtils.isEmpty(info[i].getTitle())
                                ? (TextUtils.isEmpty(info[i].getLanguage())
                                ? "UND"
                                : info[i].getLanguage())
                                : info[i].getTitle();
                        videoInfoTrack.setName("?????????#" + audioN + "???" + name + "???");
                        videoInfoTrack.setStream(i);
                        if (i == selectAudioTrack) {
                            videoInfoTrack.setSelect(true);
                        }
                        audioN++;
                        audioTrackList.add(videoInfoTrack);
                    }
                }
                VideoInfoTrack videoInfoTrack = new VideoInfoTrack();
                videoInfoTrack.setName("IJK?????????????????????????????????");
                videoInfoTrack.setStream(-1);
                videoInfoTrack.setSelect(true);
                subtitleTrackList.add(videoInfoTrack);
                //ijk???????????????????????????
                mSubtitleSettingView.setInnerSubtitleCtrl(false);
//                topBarView.getSubtitleSettingView().setInnerSubtitleCtrl(false);
                topBarView.getPlayerSettingView().setSubtitleTrackList(subtitleTrackList);
                topBarView.getPlayerSettingView().setVideoTrackList(audioTrackList);
            }
        };
        //???????????????????????????
        IMediaPlayer.OnErrorListener ijkErrorCallback = (iMediaPlayer, i, i1) -> {
            mOutsideListener.onAction(PlayerConstants.INTENT_PLAY_FAILED, 0);
            mLoadingView.setVisibility(GONE);
            return false;
        };
        //???????????????????????????
        IMediaPlayer.OnInfoListener ijkPlayInfoCallback = (iMediaPlayer, status, extra) -> {
            _switchStatus(status);
            return true;
        };

        //??????view????????????
        TopBarView.TopBarListener topBarCallBack = new TopBarView.TopBarListener() {
            @Override
            public void onBack() {
                stop();
                mAttachActivity.finish();
            }

            @Override
            public void onSubTitleSetting() {
                AnimHelper.viewTranslationX(mSubtitleSettingView, 0);
            }

            @Override
            public void topBarItemClick() {
                hideView(HIDE_VIEW_ALL);
            }
        };

        //??????view????????????
        BottomBarView.BottomBarListener bottomBarCallBack = this::_togglePlayStatus;

        //????????????????????????
        SkipTipView.SkipTipListener skipTipCallBack = new SkipTipView.SkipTipListener() {
            @Override
            public void onCancel() {
                mHandler.removeCallbacks(mHideSkipTipRunnable);
                _hideSkipTip();
            }

            @Override
            public void onSkip() {
                mLoadingView.setVisibility(VISIBLE);
                // ????????????
                seekTo(mSkipPosition);
                mHandler.removeCallbacks(mHideSkipTipRunnable);
                _hideSkipTip();
                _setProgress();
            }
        };

        //????????????????????????
        SkipTipView.SkipTipListener skipSubCallBack = new SkipTipView.SkipTipListener() {
            @Override
            public void onCancel() {
                mHandler.removeCallbacks(mHideSkipSubRunnable);
                _hideSkipSub();
            }

            @Override
            public void onSkip() {
                _hideSkipSub();
                mOutsideListener.onAction(PlayerConstants.INTENT_SELECT_SUBTITLE, 0);
            }
        };

        mFlVideoBox.setOnTouchListener(mPlayerTouchListener);
        mGestureDetector = new GestureDetector(mAttachActivity, mPlayerGestureListener);
        bottomBarView.setSeekCallBack(mSeekListener);
        mVideoView.setOnPreparedListener(ijkPreparedCallback);
        mVideoView.setOnErrorListener(ijkErrorCallback);
        mVideoView.setOnInfoListener(ijkPlayInfoCallback);

        topBarView.setCallBack(topBarCallBack);
        bottomBarView.setCallBack(bottomBarCallBack);
        skipTipView.setCallBack(skipTipCallBack);
        skipSubView.setCallBack(skipSubCallBack);

        mIvPlayerLock.setOnClickListener(v -> _togglePlayerLock());
        mIvScreenShot.setOnClickListener(v -> {
            if (!isUseSurfaceView) {
                pause();
                new DialogScreenShot(mAttachActivity, mVideoView.getScreenshot()).show();
            } else {
                Toast.makeText(getContext(), "??????????????????????????????", Toast.LENGTH_SHORT).show();
            }
        });

        //????????????????????????????????????????????????View
        bottomBarView.setSeekBarTouchCallBack((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                hideView(HIDE_VIEW_END_GESTURE);
            }
            return false;
        });
        //?????????view???????????????
        mVideoView.setOnTouchListener((v, event) -> {
            //???????????????view
            //???????????????????????????????????????
            //?????????????????????????????????view???touch??????
            if (isEditViewVisible()) {
                hideView(HIDE_VIEW_EDIT);
                return true;
            }
            return false;
        });
    }

    private void initViewAfter() {
        mVideoView.setIsUsingMediaCodec(isUsemediaCodeC);
        mVideoView.setIsUsingMediaCodecH265(isUsemediaCodeCH265);
        mVideoView.setPixelFormat(usePixelFormat);
        mVideoView.setIsUsingOpenSLES(isUseOpenSLES);
        mVideoView.setIsUsingSurfaceRenders(isUseSurfaceView);
        mVideoView.setIsUsingPlayerType(usePlayerType);

        //??????openSLES???MediaPlayer?????????????????????
        if (isUseOpenSLES || usePlayerType == PlayerConstants.IJK_ANDROID_PLAYER) {
            topBarView.getPlayerSettingView().setSpeedCtrlLLVis(false);
        }

        //????????????????????????
        bottomBarView.setSeekMax(MAX_VIDEO_SEEK);
        //?????????????????????????????????
        mHandler.post(mHideBarRunnable);
        //?????????????????????
        mFlVideoBox.setClickable(true);
    }

    /**
     * ?????????????????????View
     */
    public void initSubtitleSettingView() {
        int languageType = getPrefs().getInt(PlayerConstants.SUBTITLE_LANGUAGE, SubtitleView.LANGUAGE_TYPE_CHINA);
        int subtitleChineseProgress = getPrefs().getInt(PlayerConstants.SUBTITLE_CHINESE_SIZE, 50);
        int subtitleEnglishProgress = getPrefs().getInt(PlayerConstants.SUBTITLE_ENGLISH_SIZE, 50);
        float subtitleChineseSize = (float) subtitleChineseProgress / 100 * ConvertUtils.dp2px(getContext(), 18);
        float subtitleEnglishSize = (float) subtitleEnglishProgress / 100 * ConvertUtils.dp2px(getContext(), 18);
        mSubtitleView.setTextSize(subtitleChineseSize, subtitleEnglishSize);
        mSubtitleSettingView
//        topBarView.getSubtitleSettingView()
                .initSubtitleLanguageType(languageType)
                .initSubtitleCnSize(subtitleChineseProgress)
                .initSubtitleEnSize(subtitleEnglishProgress)
                .initListener(new SubtitleSettingView.SettingSubtitleListener() {
                    @Override
                    public void setSubtitleSwitch(Switch switchView, boolean isChecked) {
                        if (!mSubtitleSettingView.isLoadSubtitle() && isChecked) {
                            switchView.setChecked(false);
                            Toast.makeText(getContext(), "??????????????????", Toast.LENGTH_LONG).show();
                        }
                        if (isChecked) {
                            isShowSubtitle = true;
                            mSubtitleView.show();
                            mHandler.sendEmptyMessage(MSG_UPDATE_SUBTITLE);
                        } else {
                            isShowSubtitle = false;
                            mSubtitleView.hide();
                        }
                    }

                    @Override
                    public void setSubtitleCnSize(int progress) {
                        float calcProgress = (float) progress;
                        float textSize = (calcProgress / 100) * ConvertUtils.dp2px(getContext(), 18);
                        mSubtitleView.setTextSize(SubtitleView.LANGUAGE_TYPE_CHINA, textSize);
                        getPrefs().edit().putInt(PlayerConstants.SUBTITLE_CHINESE_SIZE, progress).apply();
                    }

                    @Override
                    public void setInterSubtitleSize(int progress) {

                    }

                    @Override
                    public void setInterBackgroundType(int type) {

                    }

                    @Override
                    public void setSubtitleEnSize(int progress) {
                        float calcProgress = (float) progress;
                        float textSize = (calcProgress / 100) * ConvertUtils.dp2px(getContext(), 18);
                        mSubtitleView.setTextSize(SubtitleView.LANGUAGE_TYPE_ENGLISH, textSize);
                        getPrefs().edit().putInt(PlayerConstants.SUBTITLE_ENGLISH_SIZE, progress).apply();
                    }

                    @Override
                    public void setOpenSubtitleSelector() {
                        pause();
                        hideView(HIDE_VIEW_ALL);
                        mOutsideListener.onAction(PlayerConstants.INTENT_OPEN_SUBTITLE, 0);
                    }

                    @Override
                    public void setSubtitleLanguageType(int type) {
                        getPrefs().edit().putInt(PlayerConstants.SUBTITLE_LANGUAGE, type).apply();
                        mSubtitleView.setLanguage(type);
                    }

                    @Override
                    public void onShowNetworkSubtitle() {
                        hideView(HIDE_VIEW_ALL);
                        if (mOutsideListener != null) {
                            mOutsideListener.onAction(PlayerConstants.INTENT_SELECT_SUBTITLE, 0);
                        }
                    }
                })
                .init();
    }

    /**
     * ????????????????????????View
     */
    private void initPlayerSettingView() {
        boolean allowOrientationChange = getPrefs().getBoolean(PlayerConstants.ORIENTATION_CHANGE, true);
        topBarView.getPlayerSettingView()
                .setOrientationAllow(allowOrientationChange)
                .setSettingListener(new SettingPlayerView.SettingVideoListener() {
                    @Override
                    public void selectTrack(int streamId, String language, boolean isAudio) {
                        mVideoView.selectTrack(streamId);
                        mVideoView.seekTo(mVideoView.getCurrentPosition());
                    }

                    @Override
                    public void deselectTrack(int streamId, String language, boolean isAudio) {
                        mVideoView.deselectTrack(streamId);
                    }

                    @Override
                    public void setSpeed(float speed) {
                        mVideoView.setSpeed(speed);
                    }

                    @Override
                    public void setAspectRatio(int type) {
                        mVideoView.setAspectRatio(type);
                    }

                    @Override
                    public void setOrientationStatus(boolean isEnable) {
                        if (mOrientationListener != null) {
                            if (isEnable) {
                                mOrientationListener.enable();
                            } else {
                                mOrientationListener.disable();
                            }
                        }
                    }
                });
    }

    /**
     * Activity.onResume() ?????????
     */
    @Override
    public void onResume() {
        if (mCurPosition != INVALID_VALUE) {
            seekTo(mCurPosition);
            mCurPosition = INVALID_VALUE;
        }
    }

    /**
     * Activity.onPause() ?????????
     */
    @Override
    public void onPause() {
        mCurPosition = mVideoView.getCurrentPosition();
        pause();
    }

    /**
     * Activity.onDestroy() ??????????????????????????????
     */
    @Override
    public void onDestroy() {
        mVideoView.destroy();
        // ??????????????????
        mAttachActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????
     */
    @Override
    public boolean onKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            _setVolume(true);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            _setVolume(false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void configurationChanged(Configuration newConfig) {
        _refreshOrientationEnable();
        hideView(HIDE_VIEW_ALL);
    }

    /**
     * ??????????????????????????????
     */
    @Override
    public boolean onBackPressed() {
        if (isEditViewVisible()) {
            hideView(HIDE_VIEW_EDIT);
            return true;
        }
        stop();
        mAttachActivity.finish();
        return true;
    }

    /**
     * ???????????????
     */
    @Override
    public void setSubtitlePath(String subtitlePath) {
        isShowSubtitle = false;
        mSubtitleSettingView.setLoadSubtitle(false);
        mSubtitleSettingView.setSubtitleLoadStatus(false);
//        topBarView.getSubtitleSettingView().setLoadSubtitle(false);
//        topBarView.getSubtitleSettingView().setSubtitleLoadStatus(false);
        new Thread(() -> {
            TimedTextObject subtitleObj = new SubtitleParser(subtitlePath).parser();
            if (subtitleObj != null) {
                Message message = new Message();
                message.what = MSG_SET_SUBTITLE_SOURCE;
                message.obj = subtitleObj;
                mHandler.sendMessage(message);
            }
        }).start();
    }

//    /**
//     * ?????????????????????
//     */
//    @Override
//    public void onSubtitleQuery(int size) {
//        topBarView.getSubtitleSettingView().setNetwoekSubtitleVisible(true);
//        if (isAutoLoadNetworkSubtitle) {
//            mOutsideListener.onAction(PlayerConstants.INTENT_AUTO_SUBTITLE, 0);
//        } else {
//            _showSkipSub(size);
//        }
//    }

    /**
     * ???????????????
     */
    @Override
    public void onScreenLocked() {

    }

    /**
     * ??????????????????
     */
    public void setVideoPath(String videoPath) {
        loadDefaultSubtitle(videoPath);
        mVideoView.setVideoURI(Uri.parse(videoPath));
        seekTo(0);
    }

    /**
     * ????????????
     */
    public void setTitle(String title) {
        if (topBarView != null) {
            topBarView.setTitleText(title);
        }
    }

    /**
     * ??????????????????
     */
    public void setOnInfoListener(OnOutsideListener listener) {
        mOutsideListener = listener;
    }

    /**
     * ??????????????????
     */
    public void setSkipTip(long targetPositionMs) {
        if (targetPositionMs > 0) {
            mSkipPosition = targetPositionMs;
        }
    }

//    /**
//     * ????????????????????????
//     */
//    public IjkPlayerView setNetworkSubtitle(boolean isOpen) {
//        isQueryNetworkSubtitle = isOpen;
//        return this;
//    }
//
//    /**
//     * ??????????????????????????????
//     */
//    public IjkPlayerView setAutoLoadLocalSubtitle(boolean isAuto) {
//        isAutoLoadLocalSubtitle = isAuto;
//        return this;
//    }
//
//    /**
//     * ??????????????????????????????
//     */
//    public IjkPlayerView setAutoLoadNetworkSubtitle(boolean isAuto) {
//        isAutoLoadNetworkSubtitle = isAuto;
//        return this;
//    }

    /**
     * ????????????
     */
    @Override
    public void start() {
        //??????????????????
        if (mIsNeverPlay) {
            mIsNeverPlay = false;
            //??????????????????
            mLoadingView.setVisibility(VISIBLE);
            mIsShowBar = false;
            //??????????????????
            if (isQueryNetworkSubtitle && mOutsideListener != null) {
                mOutsideListener.onAction(PlayerConstants.INTENT_QUERY_SUBTITLE, 0);
            }
        }
        //????????????????????????
        bottomBarView.setPlayIvStatus(true);
        //??????????????????
        if (!mVideoView.isPlaying()) {
            mVideoView.start();
            // ????????????
            mHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
        }

        //?????????????????????????????????????????????????????????????????????
        if (mIsPlayComplete) {
            mIsPlayComplete = false;
        }
        //?????????????????????????????????
        if (mSubtitleSettingView.isLoadSubtitle()) {
            mSubtitleView.start();
        }
//        if (topBarView.getSubtitleSettingView().isLoadSubtitle()) {
//            mSubtitleView.start();
//        }
    }

    /**
     * ??????
     */
    public void pause() {
        bottomBarView.setPlayIvStatus(false);
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
        if (mSubtitleSettingView.isLoadSubtitle()) {
            mSubtitleView.pause();
        }
//        if (topBarView.getSubtitleSettingView().isLoadSubtitle()) {
//            mSubtitleView.pause();
//        }
    }

    /**
     * ??????
     */
    public void stop() {
        pause();
        // ??????????????????
        if (mOutsideListener != null) {
            mOutsideListener.onAction(PlayerConstants.INTENT_SAVE_CURRENT, mVideoView.getCurrentPosition());
            mOutsideListener.onAction(PlayerConstants.INTENT_PLAY_END, 0);
        }
        mVideoView.stopPlayback();
    }

    /**
     * ??????
     */
    public void seekTo(long position) {
        mVideoView.seekTo((int) position);
    }

    /**
     * ??????????????????????????????
     */
    private void _setControlBarVisible(boolean isShowBar) {
        if (mIsForbidTouch) {
            hideShowLockScreen(isShowBar);
        } else {
            if (isShowBar) {
                // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                topBarView.updateSystemTime();
                hideShowBar(true);
                hideShowLockScreen(true);
            } else {
                hideView(HIDE_VIEW_AUTO);
            }
        }
    }

    /**
     * ???????????????????????????????????????
     */
    private void _toggleControlBar() {
        mIsShowBar = !mIsShowBar;
        _setControlBarVisible(mIsShowBar);
        if (mIsShowBar) {
            // ????????????????????????????????????
            mHandler.postDelayed(mHideBarRunnable, DEFAULT_HIDE_TIMEOUT);
            // ???????????? Seek ??????
            mHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
        }
    }

    /**
     * ???????????????
     */
    private void _showControlBar(int timeout) {
        if (!mIsShowBar) {
            _setProgress();
            mIsShowBar = true;
        }
        _setControlBarVisible(true);
        mHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
        // ???????????????????????? Runnable????????? timeout=0 ???????????????????????????
        mHandler.removeCallbacks(mHideBarRunnable);
        if (timeout != 0) {
            mHandler.postDelayed(mHideBarRunnable, timeout);
        }
    }

    /**
     * ??????????????????????????????????????????
     */
    private void _togglePlayStatus() {
        if (mVideoView.isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    /**
     * ??????????????????????????????
     */
    private void _refreshHideRunnable() {
        mHandler.removeCallbacks(mHideBarRunnable);
        mHandler.postDelayed(mHideBarRunnable, DEFAULT_HIDE_TIMEOUT);
    }

    /**
     * ???????????????
     */
    private void _togglePlayerLock() {
        mIsForbidTouch = !mIsForbidTouch;
        mIvPlayerLock.setSelected(mIsForbidTouch);
        if (mIsForbidTouch) {
            setOrientationEnable(false);
            hideView(HIDE_VIEW_LOCK_SCREEN);
        } else {
            if (topBarView.getPlayerSettingView().isAllowScreenOrientation()) {
                setOrientationEnable(true);
            }
            hideShowBar(true);
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????3000ms??????????????????????????????????????????
     */
    private void _refreshOrientationEnable() {
        if (topBarView.getPlayerSettingView().isAllowScreenOrientation()) {
            setOrientationEnable(false);
            mHandler.removeMessages(MSG_ENABLE_ORIENTATION);
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_ORIENTATION, 3000);
        }
    }

    /**
     * ???????????????
     */
    private long _setProgress() {
        if (mVideoView == null || mIsSeeking) {
            return 0;
        }
        // ???????????????????????????
        long position = mVideoView.getCurrentPosition();
        // ??????????????????
        long duration = mVideoView.getDuration();
        if (duration > 0) {
            // ????????? Seek ??????????????????
            long pos = (long) MAX_VIDEO_SEEK * position / duration;
            bottomBarView.setSeekProgress((int) pos);
        }
        // ????????????????????????????????????????????? Seek ????????????
        int percent = mVideoView.getBufferPercentage();
        bottomBarView.setSeekSecondaryProgress(percent * 10);
        // ??????????????????
        bottomBarView.setEndTime(TimeFormatUtils.generateTime(duration));
        bottomBarView.setCurTime(TimeFormatUtils.generateTime(position));
        // ????????????????????????
        return position;
    }

    /**
     * ????????????????????????
     */
    private void setSkipTimeText(String time) {
        if (mFlTouchLayout.getVisibility() == View.GONE) {
            mFlTouchLayout.setVisibility(View.VISIBLE);
        }
        if (mSkipTimeTv.getVisibility() == View.GONE) {
            mSkipTimeTv.setVisibility(View.VISIBLE);
        }
        mSkipTimeTv.setText(time);
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????? SeekBar
     */
    private void _onProgressSlide(float percent) {
        long position = mVideoView.getCurrentPosition();
        long duration = mVideoView.getDuration();
        // ??????????????????????????????100?????????????????????1/2
        long deltaMax = Math.min(100 * 1000, duration / 2);
        // ??????????????????
        long delta = (long) (deltaMax * percent);
        // ????????????
        mTargetPosition = delta + position;
        if (mTargetPosition > duration) {
            mTargetPosition = duration;
        } else if (mTargetPosition <= 0) {
            mTargetPosition = 0;
        }
        int deltaTime = (int) ((mTargetPosition - position) / 1000);
        String desc;
        // ??????????????????????????????????????????
        if (mTargetPosition > position) {
            desc = TimeFormatUtils.generateTime(mTargetPosition) + "/" + TimeFormatUtils.generateTime(duration) + "\n" + "+" + deltaTime + "???";
        } else {
            desc = TimeFormatUtils.generateTime(mTargetPosition) + "/" + TimeFormatUtils.generateTime(duration) + "\n" + deltaTime + "???";
        }
        setSkipTimeText(desc);
    }

    /**
     * ????????????????????????
     */
    @SuppressLint("SetTextI18n")
    private void _setVolumeInfo(int volume) {
        if (mFlTouchLayout.getVisibility() == View.GONE) {
            mFlTouchLayout.setVisibility(View.VISIBLE);
        }
        if (mTvVolume.getVisibility() == View.GONE) {
            mTvVolume.setVisibility(View.VISIBLE);
        }
        mTvVolume.setText((volume * 100 / mMaxVolume) + "%");
    }

    /**
     * ??????????????????????????????
     */
    private void _onVolumeSlide(float percent) {
        if (mCurVolume == INVALID_VALUE) {
            mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mCurVolume < 0) {
                mCurVolume = 0;
            }
        }
        int index = (int) (percent * mMaxVolume) + mCurVolume;
        if (index > mMaxVolume) {
            index = mMaxVolume;
        } else if (index < 0) {
            index = 0;
        }
        // ????????????
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        // ???????????????
        _setVolumeInfo(index);
    }


    /**
     * ???????????????????????????????????????????????????????????? 1/15
     */
    private void _setVolume(boolean isIncrease) {
        int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (isIncrease) {
            curVolume += mMaxVolume / 15;
        } else {
            curVolume -= mMaxVolume / 15;
        }
        if (curVolume > mMaxVolume) {
            curVolume = mMaxVolume;
        } else if (curVolume < 0) {
            curVolume = 0;
        }
        // ????????????
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
        // ???????????????
        _setVolumeInfo(curVolume);
        mHandler.removeCallbacks(mHideTouchViewRunnable);
        mHandler.postDelayed(mHideTouchViewRunnable, 1000);
    }

    /**
     * ????????????????????????
     */
    @SuppressLint("SetTextI18n")
    private void _setBrightnessInfo(float brightness) {
        if (mFlTouchLayout.getVisibility() == View.GONE) {
            mFlTouchLayout.setVisibility(View.VISIBLE);
        }
        if (mTvBrightness.getVisibility() == View.GONE) {
            mTvBrightness.setVisibility(View.VISIBLE);
        }
        mTvBrightness.setText(Math.ceil(brightness * 100) + "%");
    }

    /**
     * ????????????????????????
     */
    private void _onBrightnessSlide(float percent) {
        if (mCurBrightness < 0) {
            mCurBrightness = mAttachActivity.getWindow().getAttributes().screenBrightness;
            if (mCurBrightness < 0.0f) {
                mCurBrightness = 0.5f;
            } else if (mCurBrightness < 0.01f) {
                mCurBrightness = 0.01f;
            }
        }
        WindowManager.LayoutParams attributes = mAttachActivity.getWindow().getAttributes();
        attributes.screenBrightness = mCurBrightness + percent;
        if (attributes.screenBrightness > 1.0f) {
            attributes.screenBrightness = 1.0f;
        } else if (attributes.screenBrightness < 0.01f) {
            attributes.screenBrightness = 0.01f;
        }
        _setBrightnessInfo(attributes.screenBrightness);
        mAttachActivity.getWindow().setAttributes(attributes);
    }

    /**
     * ??????????????????
     */
    private void _endGesture() {
        if (mTargetPosition >= 0 && mTargetPosition != mVideoView.getCurrentPosition() && mVideoView.getDuration() != 0) {
            // ????????????????????????
            seekTo((int) mTargetPosition);
            bottomBarView.setSeekProgress((int) (mTargetPosition * MAX_VIDEO_SEEK / mVideoView.getDuration()));
            mTargetPosition = INVALID_VALUE;
        }
        // ??????????????????????????????
        hideView(HIDE_VIEW_END_GESTURE);
        _refreshHideRunnable();
        mCurVolume = INVALID_VALUE;
        mCurBrightness = INVALID_VALUE;
    }

    /**
     * ????????????????????????
     */
    private void _switchStatus(int status) {
        switch (status) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (!mIsNeverPlay) {
                    mLoadingView.setVisibility(View.VISIBLE);
                }
            case MediaPlayerParams.STATE_PREPARING:
                break;

            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                mLoadingView.setVisibility(View.GONE);
                // ????????????
                mHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
                if (mSkipPosition != INVALID_VALUE) {
                    _showSkipTip(); // ??????????????????
                }
                break;

//            case MediaPlayerParams.STATE_PLAYING:
//                break;
//            case MediaPlayerParams.STATE_ERROR:
//                break;
            case MediaPlayerParams.STATE_COMPLETED:
                if (mOutsideListener != null) {
                    mOutsideListener.onAction(PlayerConstants.INTENT_PLAY_FINISH, 0);
                }
                pause();
                mIsPlayComplete = true;
                break;
            default:
        }
    }

    /**
     * ??????????????????
     */
    private void _showSkipTip() {
        if (mSkipPosition != INVALID_VALUE && skipTipView.getVisibility() == GONE) {
            skipTipView.setVisibility(VISIBLE);
            skipTipView.setSkipTime(TimeFormatUtils.generateTime(mSkipPosition));
            AnimHelper.doSlide(skipTipView, mWidthPixels, 0, 800);
            mHandler.postDelayed(mHideSkipTipRunnable, DEFAULT_HIDE_TIMEOUT * 3);
        }
    }

    /**
     * ??????????????????
     */
    private void _hideSkipTip() {
        if (skipTipView.getVisibility() == GONE) {
            return;
        }
        ViewCompat.animate(skipTipView).translationX(-skipTipView.getWidth()).alpha(0).setDuration(500)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        skipTipView.setVisibility(GONE);
                    }
                }).start();
        mSkipPosition = INVALID_VALUE;
    }

    /**
     * ????????????????????????
     */
    private void _showSkipSub(int size) {
        if (skipSubView.getVisibility() == GONE) {
            skipSubView.setVisibility(VISIBLE);
            skipSubView.setSkipContent(size);
            AnimHelper.doSlide(skipSubView, mWidthPixels, 0, 800);
            mHandler.postDelayed(mHideSkipSubRunnable, DEFAULT_HIDE_TIMEOUT * 3);
        }
    }

    /**
     * ????????????????????????
     */
    private void _hideSkipSub() {
        if (skipSubView.getVisibility() == GONE) {
            return;
        }
        ViewCompat.animate(skipSubView).translationX(-skipSubView.getWidth()).alpha(0).setDuration(500)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        skipSubView.setVisibility(GONE);
                    }
                }).start();
    }

    /**
     * ????????????View?????????
     */
    private void hideView(int hideType) {
        //???????????????????????????????????????????????????
        if (hideType == HIDE_VIEW_END_GESTURE) {
            if (mFlTouchLayout.getVisibility() == VISIBLE) {
                mFlTouchLayout.setVisibility(GONE);
                mSkipTimeTv.setVisibility(View.GONE);
                mTvVolume.setVisibility(View.GONE);
                mTvBrightness.setVisibility(View.GONE);
            }
            return;
        }

        //???????????????
        if (topBarView.getTopBarVisibility() == VISIBLE && hideType != HIDE_VIEW_EDIT) {
            hideShowBar(false);
        }
        //??????
        if (mIvPlayerLock.getVisibility() == VISIBLE && hideType != HIDE_VIEW_LOCK_SCREEN && hideType != HIDE_VIEW_EDIT) {
            hideShowLockScreen(false);
        }
        //????????????
        if (isItemShowing() && hideType != HIDE_VIEW_AUTO) {
           hideItemView();
        }
        if (mOutsideListener != null) {
            mOutsideListener.onAction(PlayerConstants.INTENT_RESET_FULL_SCREEN, 0);
        }
    }

    private boolean isItemShowing() {
        return topBarView.isItemShowing() || mSubtitleSettingView.getTranslationX() == 0;
    }

    private void hideItemView() {
        topBarView.hideItemView();
        if (mSubtitleSettingView.getTranslationX() == 0) {
            AnimHelper.viewTranslationX(mSubtitleSettingView);
        }
    }

    /**
     * ??????????????????????????????view????????????
     */
    private boolean isEditViewVisible() {
        return isItemShowing();
    }

    /**
     * ?????????????????????????????????????????????
     */
    private void hideShowBar(boolean isShow) {
        if (isShow) {
            AnimHelper.viewTranslationY(bottomBarView, 0);
            bottomBarView.setVisibility(View.VISIBLE);
            topBarView.setTopBarVisibility(true);
            mIsShowBar = true;
        } else {
            AnimHelper.viewTranslationY(bottomBarView, bottomBarView.getHeight());
            topBarView.setTopBarVisibility(false);
            mIsShowBar = false;
            if (mOutsideListener != null) {
                mOutsideListener.onAction(PlayerConstants.INTENT_RESET_FULL_SCREEN, 0);
            }
        }
        //???????????????????????????????????????????????????
        if (isShow) {
            AnimHelper.viewTranslationX(mIvScreenShot, 0, 300);
        } else {
            AnimHelper.viewTranslationX(mIvScreenShot, ConvertUtils.dp2px(getContext(), 60), 300);
        }
    }

    /**
     * ???????????????????????????
     */
    private void hideShowLockScreen(boolean isShow) {
        if (isShow) {
            AnimHelper.viewTranslationX(mIvPlayerLock, 0, 300);
        } else {
            AnimHelper.viewTranslationX(mIvPlayerLock, -ConvertUtils.dp2px(getContext(), 60), 300);
        }
    }

    /**
     * ??????????????????????????????
     */
    public void loadDefaultSubtitle(String videoPath) {
        if (!isAutoLoadLocalSubtitle) {
            return;
        }
        String subtitlePath = CommonPlayerUtils.getSubtitlePath(videoPath);
        if (!TextUtils.isEmpty(subtitlePath)) {
            //??????????????????????????????????????????????????????
            isAutoLoadNetworkSubtitle = false;
            setSubtitlePath(subtitlePath);
        }
    }

    /**
     * ??????????????????
     */
    private void setOrientationEnable(boolean isEnable) {
        if (topBarView.getPlayerSettingView() != null) {
            topBarView.getPlayerSettingView().setOrientationChangeEnable(isEnable);
        }
    }
}
