package com.seiko.player;

/**
 * 播放器行为回调
 * @author seiko
 */
public interface OnOutsideListener {
    /**
     * 行为
     * @param action 行为code
     * @param extra 额外参数
     */
    void onAction(int action, long extra);
}
