package com.android.iflyings.player.shader

import android.content.Context
import android.opengl.GLES20
import com.android.iflyings.R
import com.android.iflyings.player.utils.ShaderUtils
import com.android.iflyings.player.utils.TextureUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class FrameShader(context: Context) {
    private val mVertexShader = ShaderUtils.readRawTextFile(context, R.raw.buffer_vertex_shader)!!
    private val mFragmentShader = ShaderUtils.readRawTextFile(context, R.raw.buffer_fragment_shader)!!

    private val vertexBuffer = ByteBuffer.allocateDirect(12 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(floatArrayOf(-1.0f, 1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f)).position(0)
    private val textureBuffer = ByteBuffer.allocateDirect(8 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f)).position(0)
    private val drawBuffer = ByteBuffer.allocateDirect(6 * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(shortArrayOf(0, 1, 2, 0, 2, 3)).position(0)

    private var mProgramHandle = 0
    private var aPositionHandle = 0
    private var aTexCoordHandle = 0
    private var uTextureSamplerHandle = 0
    private var uiTypeHandle = 0
    private var uvInputHandle = 0

    fun destroy() {
        if (mProgramHandle != 0) {
            ShaderUtils.destroyProgram(mProgramHandle)
            mProgramHandle = 0
        }
    }

    fun create() {
        if (mProgramHandle == 0) {
            mProgramHandle = ShaderUtils.createProgram(mVertexShader, mFragmentShader)
            aPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "aPosition")
            aTexCoordHandle = GLES20.glGetAttribLocation(mProgramHandle, "aTexCoord")
            uTextureSamplerHandle = GLES20.glGetUniformLocation(mProgramHandle, "sTexture")
            uiTypeHandle = GLES20.glGetUniformLocation(mProgramHandle, "uiType")
            uvInputHandle = GLES20.glGetUniformLocation(mProgramHandle, "ufPosition")
        }
    }

    fun draw(textureId: Int, textureIndex: Int, inputVector: FloatArray, type: Int) {
        if (mProgramHandle == 0) {
            throw IllegalStateException("FrameShader is not created")
        }
        GLES20.glUseProgram(mProgramHandle)

        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glEnableVertexAttribArray(aTexCoordHandle)
        GLES20.glEnableVertexAttribArray(uTextureSamplerHandle)
        GLES20.glEnableVertexAttribArray(uiTypeHandle)
        GLES20.glEnableVertexAttribArray(uvInputHandle)

        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        GLES20.glVertexAttribPointer(aTexCoordHandle, 2, GLES20.GL_FLOAT, false, 8, textureBuffer)
        TextureUtils.bindTexture2D(textureId, GLES20.GL_TEXTURE0 + textureIndex, uTextureSamplerHandle, textureIndex)
        GLES20.glUniform1i(uiTypeHandle, type)
        GLES20.glUniform4fv(uvInputHandle, 1, inputVector, 0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, drawBuffer)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aTexCoordHandle)
        GLES20.glDisableVertexAttribArray(uTextureSamplerHandle)
        GLES20.glDisableVertexAttribArray(uiTypeHandle)
        GLES20.glDisableVertexAttribArray(uvInputHandle)
    }
}