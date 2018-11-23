package com.android.iflyings.player.transformer

import com.android.iflyings.player.MediaWindow
import com.android.iflyings.player.info.MediaInfo

class ZoomOutTransformer: MediaWindow.MediaTransformer {

    override fun transformMedia(mediaInfo: MediaInfo, position: Float) {
        when {
            position <= -1 -> { // [-Infinity,-1)
                mediaInfo.hide()
            }
            position < 0 -> { // [-1,0]
                // Use the default slide transition when moving to the left page
                mediaInfo.reset()
                mediaInfo.setAlpha(1 + position)
            }
            position == 0f -> {
                mediaInfo.reset()
            }
            position < 1 -> { // (0,1]
                // Fade the page out.
                mediaInfo.reset()
                mediaInfo.setScale(1.0f - position, 1.0f - position, 1.0f)
            }
            else -> { // (1,+Infinity]
                mediaInfo.hide()
            }
        }
    }
}