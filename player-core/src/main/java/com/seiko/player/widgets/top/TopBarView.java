package com.seiko.player.widgets.top;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.seiko.player.utils.AnimHelper;
import com.seiko.player.utils.TimeFormatUtils;
import com.seiko.player.core.R;
import com.seiko.player.widgets.MarqueeTextView;

/**
 * @author xyoye
 */

public class TopBarView extends FrameLayout implements View.OnClickListener {

    /**
     * 电量改变中
     */
    private static final int BATTERY_STATUS_SPA = 101;
    /**
     * 低电量
     */
    private static final int BATTERY_STATUS_LOW = 102;

    /**
     * 正常电量
     */
    private static final int BATTERY_STATUS_NOR = 103;

    /**
     * 低电量临界值
     */
    private static final int BATTERY_LOW_LEVEL = 15;

    /**
     * 监听电量广播
     */
    private BroadcastReceiver batteryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            // 接收电量变化信息
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int status = intent.getIntExtra("status", BatteryManager.BATTERY_HEALTH_UNKNOWN);

                // 电量百分比
                int curPower = level * 100 / scale;

                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    setBatteryChanged(BATTERY_STATUS_SPA, curPower);
                } else if (curPower < BATTERY_LOW_LEVEL) {
                    setBatteryChanged(BATTERY_STATUS_LOW, curPower);
                } else {
                    setBatteryChanged(BATTERY_STATUS_NOR, curPower);
                }
            }
        }
    };

    private LinearLayout topBarLL;
    private MarqueeTextView titleTv;
    private ProgressBar batteryBar;
    private TextView systemTimeTv;

    private SettingPlayerView playerSettingView;
//    private SettingSubtitleView subtitleSettingView;

    private TopBarListener listener;

    public TopBarView(Context context) {
        this(context, null);
    }

    public TopBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.layout_top_bar_v2, this);

        topBarLL = this.findViewById(R.id.top_bar_ll);
        ImageView backIv = this.findViewById(R.id.iv_back);
        titleTv = this.findViewById(R.id.tv_title);
        batteryBar = this.findViewById(R.id.pb_battery);
        systemTimeTv = this.findViewById(R.id.tv_system_time);
        ImageView subtitleSettingIv = this.findViewById(R.id.subtitle_settings_iv);
        ImageView playerSettingIv = this.findViewById(R.id.player_settings_iv);

        playerSettingView = this.findViewById(R.id.player_setting_view);
//        subtitleSettingView = this.findViewById(R.id.subtitle_setting_view);

        backIv.setOnClickListener(this);
        subtitleSettingIv.setOnClickListener(this);
        playerSettingIv.setOnClickListener(this);

        updateSystemTime();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            listener.onBack();
        } else if (id == R.id.subtitle_settings_iv) {
            listener.onSubTitleSetting();
        } else if (id == R.id.player_settings_iv) {
            listener.topBarItemClick();
            AnimHelper.viewTranslationX(playerSettingView, 0);
        }
    }

    /**
     * 开始监听系统电量
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().registerReceiver(batteryBroadcastReceiver,  new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Log.d("TopBarView", "开始监听电量。");
    }

    /**
     * 停止监听系统电量
     */
    @Override
    protected void onDetachedFromWindow() {
        Log.d("TopBarView", "停止监听电量。");
        getContext().unregisterReceiver(batteryBroadcastReceiver);
        super.onDetachedFromWindow();
    }

    /**
     * 电量改变
     */
    public void setBatteryChanged(int status, int progress) {
        switch (status) {
            case BATTERY_STATUS_NOR:
                batteryBar.setSecondaryProgress(0);
                batteryBar.setProgress(progress);
                batteryBar.setBackgroundResource(R.mipmap.ic_battery);
                break;
            case BATTERY_STATUS_SPA:
                batteryBar.setSecondaryProgress(0);
                batteryBar.setProgress(progress);
                batteryBar.setBackgroundResource(R.mipmap.ic_battery_charging);
                break;
            case BATTERY_STATUS_LOW:
                batteryBar.setSecondaryProgress(progress);
                batteryBar.setProgress(0);
                batteryBar.setBackgroundResource(R.mipmap.ic_battery_red);
                break;
            default:
        }
    }


    /**
     * 回调接口
     */
    public void setCallBack(TopBarListener listener){
        this.listener = listener;
    }
    /**
     * 更新时间
     */
    public void updateSystemTime(){
        systemTimeTv.setText(TimeFormatUtils.getCurFormatTime());
    }

    /**
     * 标题文字
     */
    public void setTitleText(String title){
        titleTv.setText(title);
    }

    /**
     * 顶栏显示状态
     */
    public void setTopBarVisibility(boolean isVisible){
        if (isVisible){
            AnimHelper.viewTranslationY(topBarLL, 0);
        }else {
            AnimHelper.viewTranslationY(topBarLL, -topBarLL.getHeight());
        }
    }

    /**
     * 获取顶栏显示状态
     */
    public int getTopBarVisibility() {
        return topBarLL.getVisibility();
    }

    /**
     * 隐藏三个item布局
     */
    public void hideItemView() {
        if (playerSettingView.getTranslationX() == 0) {
            AnimHelper.viewTranslationX(playerSettingView);
        }
    }

    /**
     * 判断是否有Item布局正在显示
     */
    public boolean isItemShowing() {
        return playerSettingView.getTranslationX() == 0;
//                subtitleSettingView.getTranslationX() == 0;
    }

    /**
     * 播放器设置view
     */
    public SettingPlayerView getPlayerSettingView(){
        return playerSettingView;
    }


//    /**
//     * 字幕设置view
//     */
//    public SettingSubtitleView getSubtitleSettingView(){
//        return subtitleSettingView;
//    }

    public interface TopBarListener {
        void onBack();

        void onSubTitleSetting();

        void topBarItemClick();
    }
}
