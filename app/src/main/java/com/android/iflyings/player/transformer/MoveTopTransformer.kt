package com.android.iflyings.player.transformer

import com.android.iflyings.player.MediaWindow
import com.android.iflyings.player.info.MediaInfo

class MoveTopTransformer: MediaWindow.MediaTransformer {

    override fun transformMedia(mediaInfo: MediaInfo, position: Float) {
        when {
            position <= -1 -> { // [-Infinity,-1)
                mediaInfo.hide()
            }
            position < 0 -> { // [-1,0]
                mediaInfo.reset()
                mediaInfo.setAlpha(1 + position)
            }
            position == 0f -> {
                mediaInfo.reset()
            }
            position < 1 -> { // (0,1]
                mediaInfo.reset()
                mediaInfo.setRect(1.0f, 1.0f - position)
            }
            else -> { // (1,+Infinity]
                mediaInfo.hide()
            }
        }
    }
}