package com.seiko.singlevideoplayer.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.seiko.singlevideoplayer.R

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btnTest).setOnClickListener(this)
        playTestVideo()
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.btnTest -> playTestVideo()
        }
    }

    private fun playTestVideo() {
        PlayerManagerActivity.launchPlayerSmb(this, "测试视频",
            "https://v-cdn.zjol.com.cn/276982.mp4")
    }
}
