package com.seiko.singlevideoplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenBroadcastReceiver extends BroadcastReceiver {

    private PlayerReceiverListener mListener;

    public ScreenBroadcastReceiver(PlayerReceiverListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            mListener.onScreenLocked();
        }
    }

    public interface PlayerReceiverListener {

        void onScreenLocked();

    }
}
