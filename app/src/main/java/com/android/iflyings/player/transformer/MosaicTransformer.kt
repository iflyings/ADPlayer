package com.android.iflyings.player.transformer

import com.android.iflyings.player.MediaWindow
import com.android.iflyings.player.info.MediaInfo
import com.orhanobut.logger.Logger

class MosaicTransformer: MediaWindow.MediaTransformer {

    override fun transformMedia(mediaInfo: MediaInfo, position: Float) {
        when {
            position <= -0.5 -> { // [-Infinity,-1)
                mediaInfo.hide()
            }
            position < 0 -> { // [-1,0]
                mediaInfo.reset()
                mediaInfo.setMosaic(-position)
            }
            position == 0f -> {
                mediaInfo.reset()
            }
            position <= 0.5 -> { // (0,1]
                mediaInfo.reset()
                mediaInfo.setMosaic(position)
            }
            else -> { // (1,+Infinity]
                mediaInfo.hide()
            }
        }
    }
}