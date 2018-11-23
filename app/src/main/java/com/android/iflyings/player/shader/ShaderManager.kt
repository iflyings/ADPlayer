package com.android.iflyings.player.shader

import android.content.Context
import com.android.iflyings.player.model.FrameModel
import com.android.iflyings.player.model.MediaModel

object ShaderManager {

    private var mImageShader: ImageShader? = null
    private var mVideoShader: VideoShader? = null
    private var mFrameShader: FrameShader? = null

    fun create(context: Context) {
        synchronized(ShaderManager::class.java) {
            if (mImageShader == null) {
                mImageShader = ImageShader(context).create()
            }
            if (mVideoShader == null) {
                mVideoShader = VideoShader(context).create()
            }
            if (mFrameShader == null) {
                mFrameShader = FrameShader(context).create()
            }
        }
    }

    fun drawImage(mediaModel: MediaModel, textureId: Int, textureIndex: Int) {
        mImageShader?.draw(mediaModel, textureId, textureIndex)
    }
    fun drawVideo(mediaModel: MediaModel, textureId: Int, textureIndex: Int) {
        mVideoShader?.draw(mediaModel, textureId, textureIndex)
    }
    fun drawFrame(frameModel: FrameModel, textureId: Int, textureIndex: Int) {
        mFrameShader?.draw(frameModel, textureId, textureIndex)
    }

    fun destroy() {
        synchronized(ShaderManager::class.java) {
            mImageShader?.destroy()
            mImageShader = null
            mVideoShader?.destroy()
            mVideoShader = null
            mFrameShader?.destroy()
            mFrameShader = null
        }
    }
}
