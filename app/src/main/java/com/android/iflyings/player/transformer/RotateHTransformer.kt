package com.android.iflyings.player.transformer

import com.android.iflyings.player.MediaWindow
import com.android.iflyings.player.info.MediaInfo

class RotateHTransformer: MediaWindow.MediaTransformer {

    override fun transformMedia(mediaInfo: MediaInfo, position: Float) {
        when {
            position <= -0.5 -> { // [-Infinity,-1)
                mediaInfo.hide()
            }
            position < 0 -> { // [-1,0]
                mediaInfo.reset()
                mediaInfo.setRotate(180 * position, 0.0f, 1.0f, 0.0f)
            }
            position == 0f -> {
                mediaInfo.reset()
            }
            position < 0.5 -> { // (0,1]
                mediaInfo.reset()
                mediaInfo.setRotate(-180 * position, 0.0f, 1.0f, 0.0f)
            }
            else -> { // (1,+Infinity]
                mediaInfo.hide()
            }
        }
    }
}