package com.seiko.player.subtitle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by xyoye on 2019/2/22.
 */

public class SubtitleSettingView extends LinearLayout implements View.OnClickListener {

    //网络字幕
    private RelativeLayout networkSubtitleRl;
    //开关
    private Switch subtitleSwitch;
    //加载状态
    private TextView subtitleLoadStatusTv;
    //语言设置
    private TextView onlyCnShowTv, onlyUsShowTv, bothLanguageTv;
    private EditText subExtraTimeEt;

    //内置字幕设置
    private LinearLayout interSizeLL;
    private SeekBar subtitleOtherSB;
    private TextView subtitleOtherSizeTv;
    private TextView bgTB, bgTW, bgBW, bgWB, bgTT;

    //外置字幕设置
    private LinearLayout outerSizeLL, outerTimeLL;
    private SeekBar subtitleCnSB;
    private TextView subtitleCnSizeTv;
    private SeekBar subtitleUSSB;
    private TextView subtitleUSSizeTv;

    //时间偏移量
    private float timeOffset;
    //是否已加载字幕
    private boolean isLoadSubtitle = false;
    //是否显示内置字幕控制View
    private boolean isShowInnerCtrl = false;
    //控制回调
    private SettingSubtitleListener settingListener = null;

    public SubtitleSettingView(Context context) {
        this(context, null);
    }

    public SubtitleSettingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubtitleSettingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        View.inflate(context, R.layout.view_setting_subtitle, this);


        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.SubtitleSettingView, defStyle, 0);
        int itemBackgroundResId = typedArray.getResourceId(
                R.styleable.SubtitleSettingView_item_background,
                0);
        Drawable mItemBackgroundDrawable;
        if (itemBackgroundResId == 0) {
            mItemBackgroundDrawable = ContextCompat.getDrawable(context, R.drawable.sel_item_background);
        } else {
            mItemBackgroundDrawable = typedArray.getDrawable(itemBackgroundResId);
        }
        typedArray.recycle();

        subtitleSwitch = findViewById(R.id.subtitle_sw);
        subtitleLoadStatusTv = findViewById(R.id.subtitle_load_status_tv);
        subtitleCnSizeTv = findViewById(R.id.subtitle_chinese_size_tv);
        subtitleCnSB = findViewById(R.id.subtitle_chinese_size_sb);
        subtitleUSSizeTv = findViewById(R.id.subtitle_english_size_tv);
        subtitleUSSB = findViewById(R.id.subtitle_english_size_sb);
        subtitleOtherSizeTv = findViewById(R.id.subtitle_other_size_tv);
        subtitleOtherSB = findViewById(R.id.subtitle_other_size_sb);
        onlyCnShowTv = findViewById(R.id.only_chinese_tv);
        onlyUsShowTv = findViewById(R.id.only_english_tv);
        bothLanguageTv = findViewById(R.id.both_language_tv);
        subExtraTimeEt = findViewById(R.id.subtitle_extra_time_et);
        networkSubtitleRl = findViewById(R.id.subtitle_network_rl);

        bgBW = findViewById(R.id.inter_bg_black_white);
        bgWB = findViewById(R.id.inter_bg_white_black);
        bgTB = findViewById(R.id.inter_bg_tran_black);
        bgTW = findViewById(R.id.inter_bg_tran_white);
        bgTT = findViewById(R.id.inter_bg_tran_tran);

        interSizeLL = findViewById(R.id.inter_size_ll);
        outerSizeLL = findViewById(R.id.outer_size_LL);
        outerTimeLL = findViewById(R.id.outer_time_LL);


        bgBW.setBackground(mItemBackgroundDrawable);
        bgWB.setBackground(mItemBackgroundDrawable);
        bgTB.setBackground(mItemBackgroundDrawable);
        bgTW.setBackground(mItemBackgroundDrawable);
        bgTT.setBackground(mItemBackgroundDrawable);
        onlyCnShowTv.setBackground(mItemBackgroundDrawable);
        onlyUsShowTv.setBackground(mItemBackgroundDrawable);
        bothLanguageTv.setBackground(mItemBackgroundDrawable);

        onlyCnShowTv.setOnClickListener(this);
        onlyUsShowTv.setOnClickListener(this);
        bothLanguageTv.setOnClickListener(this);

        bgBW.setOnClickListener(this);
        bgWB.setOnClickListener(this);
        bgTB.setOnClickListener(this);
        bgTW.setOnClickListener(this);
        bgTT.setOnClickListener(this);

        findViewById(R.id.subtitle_change_source_tv).setOnClickListener(this);
        findViewById(R.id.add_encoding_tv).setOnClickListener(this);
        findViewById(R.id.subtitle_extra_time_add).setOnClickListener(this);
        findViewById(R.id.subtitle_extra_time_reduce).setOnClickListener(this);
        findViewById(R.id.subtitle_network_tv).setOnClickListener(this);

        subExtraTimeEt.setImeOptions(EditorInfo.IME_ACTION_DONE);
        subExtraTimeEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        subExtraTimeEt.setSingleLine(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init() {
        //切换字幕显示状态
        subtitleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isLoadSubtitle && isChecked) {
                if (interSizeLL.getVisibility() == View.VISIBLE) {
                    interSizeLL.setVisibility(GONE);
                }
                outerTimeLL.setVisibility(VISIBLE);
                outerSizeLL.setVisibility(VISIBLE);
            } else {
                if (isShowInnerCtrl) {
                    interSizeLL.setVisibility(VISIBLE);
                }
                outerTimeLL.setVisibility(GONE);
                outerSizeLL.setVisibility(GONE);
            }
            settingListener.setSubtitleSwitch(subtitleSwitch, isChecked);
        });

        //设置偏移时间
        subExtraTimeEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    String offset = subExtraTimeEt.getText().toString().trim();
                    timeOffset = Float.valueOf(offset);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "请输入正确的时间", Toast.LENGTH_LONG).show();
                    return true;
                }
                subExtraTimeEt.clearFocus();
                return false;
            }
            return false;
        });

        //中文文字大小
        subtitleCnSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                }
                subtitleCnSizeTv.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0) {
                    progress = 1;
                }
                settingListener.setSubtitleCnSize(progress);
            }
        });

        //英文文字大小
        subtitleUSSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                }
                subtitleUSSizeTv.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0) {
                    progress = 1;
                }
                settingListener.setSubtitleEnSize(progress);
            }
        });

        //内置字幕文字大小
        subtitleOtherSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                }
                subtitleOtherSizeTv.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0) {
                    progress = 1;
                }
                settingListener.setInterSubtitleSize(progress);
            }
        });

        this.setOnTouchListener((v, event) -> true);
    }

    public SubtitleSettingView initListener(SettingSubtitleListener settingListener) {
        this.settingListener = settingListener;
        return this;
    }

    /**
     * 初始化中文文字大小
     */
    @SuppressLint("SetTextI18n")
    public SubtitleSettingView initSubtitleCnSize(int progress) {
        subtitleCnSB.setMax(100);
        subtitleCnSizeTv.setText(progress + "%");
        subtitleCnSB.setProgress(progress);
        return this;
    }

    public SubtitleSettingView initSubtitleLanguageType(int type) {
        setSubtitleLanguageType(type);
        return this;
    }

    /**
     * 初始化英文文字大小
     */
    @SuppressLint("SetTextI18n")
    public SubtitleSettingView initSubtitleEnSize(int progress) {
        subtitleUSSB.setMax(100);
        subtitleUSSizeTv.setText(progress + "%");
        subtitleUSSB.setProgress(progress);
        return this;
    }

    /**
     * 是否显示内置字幕控制View，根据字幕流决定
     */
    @SuppressLint("SetTextI18n")
    public void setInnerSubtitleCtrl(boolean hasInnerSubtitle) {
        isShowInnerCtrl = hasInnerSubtitle;
        interSizeLL.setVisibility(isShowInnerCtrl ? VISIBLE : GONE);
        if (isShowInnerCtrl) {
            subtitleOtherSizeTv.setText("50%");
            subtitleOtherSB.setMax(100);
            subtitleOtherSB.setProgress(50);
        }
    }

    /**
     * 设置语言类型
     */
    private void setSubtitleLanguageType(int type) {
        onlyCnShowTv.setSelected(type == SubtitleView.LANGUAGE_TYPE_CHINA);
        onlyUsShowTv.setSelected(type == SubtitleView.LANGUAGE_TYPE_ENGLISH);
        bothLanguageTv.setSelected(type == SubtitleView.LANGUAGE_TYPE_BOTH);
//        switch (type) {
//            case SubtitleView.LANGUAGE_TYPE_ENGLISH:
//                onlyCnShowTv.setBackground(mItemBackgroundDrawable);
//                onlyUsShowTv.setBackground(mItemBackgroundSelectDrawable);
//                bothLanguageTv.setBackground(mItemBackgroundDrawable);
//                break;
//            case SubtitleView.LANGUAGE_TYPE_BOTH:
//                onlyCnShowTv.setBackground(mItemBackgroundDrawable);
//                onlyUsShowTv.setBackground(mItemBackgroundDrawable);
//                bothLanguageTv.setBackground(mItemBackgroundSelectDrawable);
//                break;
//            default:
//                onlyCnShowTv.setBackground(mItemBackgroundSelectDrawable);
//                onlyUsShowTv.setBackground(mItemBackgroundDrawable);
//                bothLanguageTv.setBackground(mItemBackgroundDrawable);
//                break;
//        }
    }

    /**
     * 设置内置字幕背景和字体颜色
     */
    public void setInterBg(int type) {
        bgBW.setSelected(type == 0);
        bgWB.setSelected(type == 1);
        bgTB.setSelected(type == 2);
        bgTW.setSelected(type == 3);
        bgTT.setSelected(type == 4);
//        bgBW.setBackground(mItemBackgroundDrawable);
//        bgWB.setBackground(mItemBackgroundDrawable);
//        bgTB.setBackground(mItemBackgroundDrawable);
//        bgTW.setBackground(mItemBackgroundDrawable);
//        bgTT.setBackground(mItemBackgroundDrawable);
//        switch (type) {
//            case 0:
//                bgBW.setBackground(mItemBackgroundSelectDrawable);
//                break;
//            case 1:
//                bgWB.setBackground(mItemBackgroundSelectDrawable);
//                break;
//            case 2:
//                bgTB.setBackground(mItemBackgroundSelectDrawable);
//                break;
//            case 3:
//                bgTW.setBackground(mItemBackgroundSelectDrawable);
//                break;
//            case 4:
//                bgTT.setBackground(mItemBackgroundSelectDrawable);
//                break;
//            default:
//                bgBW.setBackground(mItemBackgroundSelectDrawable);
//        }
        settingListener.setInterBackgroundType(type);
    }

    /**
     * 设置外置字幕加载状态
     */
    public void setSubtitleLoadStatus(boolean isLoad) {
        if (isLoad) {
            subtitleSwitch.setChecked(true);
            subtitleLoadStatusTv.setText("（已加载）");
            subtitleLoadStatusTv.setTextColor(ContextCompat.getColor(getContext(), R.color.theme_color));
        } else {
            subtitleSwitch.setChecked(false);
            subtitleLoadStatusTv.setText("（未加载）");
            subtitleLoadStatusTv.setTextColor(ContextCompat.getColor(getContext(), R.color.text_red));
        }
    }

    /**
     * 获取偏移时间
     */
    public float getTimeOffset() {
        return timeOffset;
    }

    public boolean isLoadSubtitle() {
        return isLoadSubtitle;
    }

    public void setLoadSubtitle(boolean loadDanmu) {
        isLoadSubtitle = loadDanmu;
    }

    public void setNetwoekSubtitleVisible(boolean isShow) {
        networkSubtitleRl.setVisibility(isShow ? VISIBLE : GONE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.subtitle_change_source_tv) {
            settingListener.setOpenSubtitleSelector();
        } else if (id == R.id.only_chinese_tv) {
            settingListener.setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_CHINA);
            setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_CHINA);
        } else if (id == R.id.only_english_tv) {
            settingListener.setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_ENGLISH);
            setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_ENGLISH);
        } else if (id == R.id.both_language_tv) {
            settingListener.setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_BOTH);
            setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_BOTH);
        } else if (id == R.id.subtitle_extra_time_reduce) {
            timeOffset -= 0.5f;
            subExtraTimeEt.setText(String.valueOf(timeOffset));
        } else if (id == R.id.subtitle_extra_time_add) {
            timeOffset += 0.5f;
            subExtraTimeEt.setText(String.valueOf(timeOffset));
        } else if (id == R.id.inter_bg_black_white) {
            setInterBg(0);
        } else if (id == R.id.inter_bg_white_black) {
            setInterBg(1);
        } else if (id == R.id.inter_bg_tran_black) {
            setInterBg(2);
        } else if (id == R.id.inter_bg_tran_white) {
            setInterBg(3);
        } else if (id == R.id.inter_bg_tran_tran) {
            setInterBg(4);
        } else if (id == R.id.subtitle_network_tv) {
            settingListener.onShowNetworkSubtitle();
        }
    }

    public interface SettingSubtitleListener {

        void setSubtitleSwitch(Switch switchView, boolean isChecked);

        void setSubtitleCnSize(int progress);

        void setSubtitleEnSize(int progress);

        void setOpenSubtitleSelector();

        void setSubtitleLanguageType(int type);

        void setInterSubtitleSize(int progress);

        void setInterBackgroundType(int type);

        void onShowNetworkSubtitle();
    }

}
