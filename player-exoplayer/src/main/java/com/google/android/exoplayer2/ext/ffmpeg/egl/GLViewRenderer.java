package com.google.android.exoplayer2.ext.ffmpeg.egl;

/**
 * Created by Chilling on 2016/12/29.
 */

public interface GLViewRenderer {

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onDrawFrame();
}
