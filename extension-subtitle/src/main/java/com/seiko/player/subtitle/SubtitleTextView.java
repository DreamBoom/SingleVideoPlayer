package com.seiko.player.subtitle;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * 显示字幕的字体样式
 *
 * Created by xyoye on 2019/5/6.
 */

public class SubtitleTextView extends TextView implements View.OnTouchListener {

    private SubtitleClickListener listener;

    public SubtitleTextView(Context context) {
        super(context);
        init();
    }

    public SubtitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SubtitleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 默认白色字体
        setTextColor(Color.WHITE);
        setSingleLine(true);
        setShadowLayer(3, 0, 0, Color.BLUE);
        this.setOnTouchListener(this);
    }

    public void setSubtitleOnTouchListener(SubtitleClickListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (listener != null) {
                    listener.clickDown();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null) {
                    listener.clickUp();
                }
                break;
            default:
                break;
        }
        return true;
    }
}
