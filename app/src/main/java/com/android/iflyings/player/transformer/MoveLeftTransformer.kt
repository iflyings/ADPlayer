package com.android.iflyings.player.transformer

import com.android.iflyings.player.MediaWindow
import com.android.iflyings.player.info.MediaInfo

class MoveLeftTransformer: MediaWindow.MediaTransformer {

    override fun transformMedia(mediaInfo: MediaInfo, position: Float) {
        when {
            position < -1 -> { // [-Infinity,-1)
                mediaInfo.hide()
            }
            position < 0 -> { // [-1,0]
                mediaInfo.setAlpha(1 + position)
            }
            position == 0f -> {
                mediaInfo.reset()
            }
            position < 1 -> { // (0,1]
                mediaInfo.reset()
                mediaInfo.setRect(1 - position, 1.0f)
            }
            else -> { // (1,+Infinity]
                mediaInfo.hide()
            }
        }
    }
}