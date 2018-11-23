package com.android.iflyings.player.transformer

import com.android.iflyings.player.MediaWindow
import com.android.iflyings.player.info.MediaInfo

class FontTransformer: MediaWindow.MediaTransformer {

    override fun transformMedia(mediaInfo: MediaInfo, position: Float) {
        mediaInfo.reset()
        mediaInfo.setThreshold(0.1f, 0.1f, 0.1f)
    }
}