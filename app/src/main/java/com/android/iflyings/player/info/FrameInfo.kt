package com.android.iflyings.player.info

import android.opengl.GLES20.*
import android.opengl.Matrix
import com.android.iflyings.player.MediaWindow
import com.android.iflyings.player.model.FrameModel
import com.android.iflyings.player.shader.ShaderManager
import com.android.iflyings.player.utils.ShaderUtils
import com.android.iflyings.player.utils.TextureUtils
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.IntBuffer

class FrameInfo {
    private val mWindowLists = mutableListOf<MediaWindow>()

    private var mFrameBufferId = 0
    private var mTextureId = 0
    private var mRenderBufferId = 0

    private val mFrameModel = FrameModel()
    private var mFrameWidth = 0
    private var mFrameHeight = 0

    private var isPlaying = false

    fun frameCreate(width: Int, height: Int) {
        mFrameWidth = width
        mFrameHeight = height
        val maxSize = IntBuffer.allocate(1)
        glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE, maxSize)
        if (width > maxSize.get(0) || height > maxSize.get(0)) {
            throw IllegalStateException("render buffer size is too short")
        }

        mTextureId = TextureUtils.createBitmapTexture()
        glBindTexture(GL_TEXTURE_2D, mTextureId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)

        mRenderBufferId = TextureUtils.createRenderBuffer()
        glBindRenderbuffer(GL_RENDERBUFFER, mRenderBufferId)
        ShaderUtils.checkGlError("Changed 1")
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height)
        ShaderUtils.checkGlError("Changed 2")

        mFrameBufferId = TextureUtils.createFrameBuffer()
        glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferId)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mTextureId, 0)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, mRenderBufferId)

        val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw IllegalStateException("status = $status msg = Frame Buffer Create Failed")
        }

        glBindTexture(GL_TEXTURE_2D, 0)//解绑Texture
        glBindFramebuffer(GL_FRAMEBUFFER, 0)//解绑FrameBuffer
        glBindRenderbuffer(GL_RENDERBUFFER, 0)//解绑RenderBuffer

        openPlayer()
    }

    fun frameDraw(textureIndex: Int) {
        if (isPlaying) {
            glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferId)
            glClear(GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            var index = textureIndex
            for (window in mWindowLists) {
                index = window.draw(index)
            }

            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            glBindRenderbuffer(GL_RENDERBUFFER, mRenderBufferId)
            ShaderManager.drawFrame(mFrameModel, mTextureId, index)
        }
    }

    fun frameDestroy() {
        if (mFrameBufferId != 0) {
            TextureUtils.unloadFrameBuffer(mFrameBufferId)
            mFrameBufferId = 0
        }
        if (mRenderBufferId != 0) {
            TextureUtils.unloadRenderBuffer(mRenderBufferId)
            mRenderBufferId = 0
        }
        if (mTextureId != 0) {
            TextureUtils.unloadTexture(mTextureId)
            mTextureId = 0
        }
    }

    fun start() {
        isPlaying = true
        openPlayer()
    }

    private fun openPlayer() {
        if (mFrameWidth > 0 && mFrameHeight > 0 && isPlaying) {
            for (mediaWindow in mWindowLists) {
                mediaWindow.setScreenSize(mFrameWidth, mFrameHeight)
                mediaWindow.start()
            }
        }
    }

    fun playEffect(frameEffect: FrameEffect) {
        GlobalScope.launch(Dispatchers.Default) {
            val perDistance = 1f / (500 / 20)
            var position = 0f
            while (position <= 1f) {
                frameEffect.update(mFrameModel, position)
                delay(20)
                position += perDistance
            }
            position = 1f
            while (position > 0) {
                frameEffect.update(mFrameModel, position)
                delay(20)
                position -= perDistance
            }
        }
    }

    fun stop() {
        for (mediaWindow in mWindowLists) {
            mediaWindow.setScreenSize(mFrameWidth, mFrameHeight)
            mediaWindow.stop()
        }
    }

    fun add(mediaWindow: MediaWindow) {
        mWindowLists.add(mediaWindow)
    }

    sealed class FrameEffect {

        object HdrEffect : FrameEffect() {
            override fun update(frameModel: FrameModel, position: Float) {
                frameModel.reset()
                frameModel.setHDR()
            }
        }

        object GrayEffect : FrameEffect() {
            override fun update(frameModel: FrameModel, position: Float) {
                frameModel.reset()
                frameModel.setGrayPhoto()
            }
        }

        object OldEffect : FrameEffect() {
            override fun update(frameModel: FrameModel, position: Float) {
                frameModel.reset()
                frameModel.setOldPhoto()
            }
        }

        object EmbossEffect : FrameEffect() {
            override fun update(frameModel: FrameModel, position: Float) {
                frameModel.reset()
                //frameModel.setEmboss(mFrameWidth, mFrameHeight)
            }
        }

        object ShakeEffect : FrameEffect() {
            override fun update(frameModel: FrameModel, position: Float) {
                frameModel.shaderType = 5
                frameModel.shaderVector[0] = 0.01f * position
                Matrix.setIdentityM(frameModel.positionMatrix, 0)
                Matrix.scaleM(frameModel.positionMatrix, 0, 1f + 0.5f * position, 1f + 0.5f * position, 1f)
            }
        }

        object BurrsEffect : FrameEffect() {
            override fun update(frameModel: FrameModel, position: Float) {
                frameModel.shaderType = 6
                frameModel.shaderVector[0] = 0.1f * position
                frameModel.shaderVector[1] = 0.2f
                frameModel.shaderVector[2] = 0.01f
                Matrix.setIdentityM(frameModel.positionMatrix, 0)
            }
        }

        abstract fun update(frameModel: FrameModel, position: Float)

    }
}