package com.seiko.player.utils;

import android.content.Context;
import android.support.annotation.NonNull;

public class ConvertUtils {

    public static int dp2px(@NonNull final Context context, final float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
