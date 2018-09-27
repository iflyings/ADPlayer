package com.android.iflyings.player.model

import android.opengl.GLES20
import com.android.iflyings.player.shader.ShaderManager
import java.nio.IntBuffer

class FrameModel(texWidth: Int, texHeight: Int) {
    private val mFrameBufferIds = IntArray(1)
    private val mFrameTextureIds = IntArray(1)
    private val mFrameRenderIds = IntArray(1)

    private var mTexWidth = texWidth
    private var mTexHeight = texHeight

    private val mShaderVector = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
    private var mShaderType: Int = 0

    fun create(width: Int, height: Int) {
        mTexWidth = width
        mTexHeight = height
        val maxSize = IntBuffer.allocate(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_RENDERBUFFER_SIZE, maxSize)
        if (width > maxSize.get(0) || height > maxSize.get(0)) {
            throw IllegalStateException("Render Buffer Size is too short")
        }

        destroy()

        GLES20.glGenRenderbuffers(1, mFrameRenderIds, 0)
        GLES20.glGenFramebuffers(1, mFrameBufferIds, 0)
        GLES20.glGenTextures(1, mFrameTextureIds, 0)
        //绑定Texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameTextureIds[0])
        //创建纹理
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        //绑定FrameBuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferIds[0])
        //绑定RenderBuffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mFrameRenderIds[0])
        //分配buffer给FBO使用
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT, width, height)
        //绑定纹理到FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFrameTextureIds[0], 0)
        //将renderbuffer与FBO绑定
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mFrameRenderIds[0])
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw IllegalStateException("Framebuffer Create Fail")
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)//解绑Texture
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)//解绑FrameBuffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0)//解绑RenderBuffer
    }

    fun draw(textureIndex: Int) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mFrameTextureIds[0])
        //ShaderManager.instance.drawFrameBuffer(mFrameTextureIds[0], textureIndex,
        //        floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f), 0)
    }

    fun destroy() {
        if (mFrameRenderIds[0] != 0) {
            GLES20.glDeleteRenderbuffers(1, mFrameRenderIds, 0)
            mFrameRenderIds[0] = 0
        }
        if (mFrameBufferIds[0] != 0) {
            GLES20.glDeleteFramebuffers(1, mFrameBufferIds, 0)
            mFrameBufferIds[0] = 0
        }
        if (mFrameTextureIds[0] != 0) {
            GLES20.glDeleteTextures(1, mFrameTextureIds, 0)
            mFrameTextureIds[0] = 0
        }
    }

    fun framebufferTextureId(): Int {
        return mFrameTextureIds[0]
    }

    fun reset() {
        mShaderType = 0
    }
    fun setHDR() {
        mShaderType = 1
        mShaderVector[0] = 1.1f
    }
    fun setGrayPhoto() {
        mShaderType = 2
    }
    fun setOldPhoto() {
        mShaderType = 3
    }
    fun setEmboss() {
        mShaderType = 4
        mShaderVector[0] = mTexWidth.toFloat()
        mShaderVector[1] = mTexHeight.toFloat()
    }
}