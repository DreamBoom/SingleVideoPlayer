package com.seiko.singlevideoplayer.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayParam(
    var videoPath: String = "",
    var videoTitle: String = "",
    var currentPosition: Long = 0,
    var episodeId: Int = 0
) : Parcelable