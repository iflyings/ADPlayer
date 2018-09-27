package com.android.iflyings.player.shader

import android.content.Context
import com.android.iflyings.player.model.MediaModel

class ShaderManager constructor(context: Context) {
    private val mImageShader: ImageShader = ImageShader(context)
    private val mVideoShader: VideoShader = VideoShader(context)
    private val mFrameShader: FrameShader = FrameShader(context)

    init {
        if (mInstance == null) {
            synchronized(ShaderManager::class.java) {
                if (mInstance == null) {
                    mInstance = this
                }
            }
        }
    }

    fun createShader() {
        mImageShader.create()
        mVideoShader.create()
        mFrameShader.create()
    }

    fun drawImage(mediaModel: MediaModel, textureId: Int, textureIndex: Int) {
        mImageShader.draw(mediaModel, textureId, textureIndex)
    }
    fun drawVideo(mediaModel: MediaModel, textureId: Int, textureIndex: Int) {
        mVideoShader.draw(mediaModel, textureId, textureIndex)
    }

    companion object {

        private var mInstance: ShaderManager? = null

        val instance: ShaderManager
            get() {
                if (mInstance == null) {
                    throw IllegalArgumentException("MediaShader is not create")
                }
                return mInstance!!
            }

        fun destroy() {
            synchronized(ShaderManager::class.java) {
                if (mInstance != null) {
                    mInstance!!.mImageShader.destroy()
                    mInstance!!.mVideoShader.destroy()
                    mInstance!!.mFrameShader.destroy()
                    mInstance = null
                }
            }
        }
    }
}
