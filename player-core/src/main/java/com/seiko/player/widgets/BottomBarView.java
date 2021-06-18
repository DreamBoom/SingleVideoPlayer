package com.seiko.player.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.seiko.player.core.R;

/**
 *
 * @author xyoye
 */
public class BottomBarView extends LinearLayout implements View.OnClickListener {

    /**
     * 播放键
     */
    private ImageView mIvPlay;
    /**
     * 当前时间
     */
    private TextView mTvCurTime;
    /**
     * 结束时间
     */
    private TextView mTvEndTime;

    /**
     * 播放进度条
     */
    private SeekBar mSeekBar;

    private BottomBarListener listener;

    public BottomBarView(Context context) {
        this(context, null);
    }

    public BottomBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.layout_bottom_bar_v2, this);

        mIvPlay = findViewById(R.id.iv_play);
        mTvCurTime = findViewById(R.id.tv_cur_time);
        mTvEndTime = findViewById(R.id.tv_end_time);
        mSeekBar = findViewById(R.id.danmaku_player_seek);

        mIvPlay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (listener == null) {
            return;
        }

        if (v.getId() == R.id.iv_play) {
            listener.onPlayClick();
        }
    }

    public void setPlayIvStatus(boolean isPlaying) {
        mIvPlay.setSelected(isPlaying);
    }

    public void setCurTime(String curTime) {
        mTvCurTime.setText(curTime);
    }

    public void setEndTime(String endTime) {
        mTvEndTime.setText(endTime);
    }

    public void setSeekMax(int max) {
        mSeekBar.setMax(max);
    }

    public void setSeekProgress(int progress) {
        mSeekBar.setProgress(progress);
    }

    public void setSeekSecondaryProgress(int progress) {
        mSeekBar.setSecondaryProgress(progress);
    }

    public void setSeekCallBack(SeekBar.OnSeekBarChangeListener seekCallBack) {
        mSeekBar.setOnSeekBarChangeListener(seekCallBack);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setSeekBarTouchCallBack(OnTouchListener touchListener) {
        mSeekBar.setOnTouchListener(touchListener);
    }

    public void setCallBack(BottomBarListener listener) {
        this.listener = listener;
    }

    public interface BottomBarListener {
        void onPlayClick();
    }
}
