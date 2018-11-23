package com.android.iflyings.player.shader

import android.content.Context
import android.opengl.GLES20
import com.android.iflyings.player.R
import com.android.iflyings.player.model.FrameModel
import com.android.iflyings.player.utils.ShaderUtils
import com.android.iflyings.player.utils.TextureUtils

internal class FrameShader(context: Context) {
    private val mVertexShader = ShaderUtils.readRawTextFile(context, R.raw.buffer_vertex_shader)!!
    private val mFragmentShader = ShaderUtils.readRawTextFile(context, R.raw.buffer_fragment_shader)!!

    private var mProgramId = 0
    private var aPositionHandle = 0
    private var aTextureCoordHandle = 0
    private var uPosMatrixHandle = 0
    private var uTexMatrixHandle = 0

    private var uTextureSamplerHandle = 0
    private var uiTypeHandle = 0
    private var uvInputHandle = 0

    fun destroy() {
        if (mProgramId != 0) {
            ShaderUtils.destroyProgram(mProgramId)
            mProgramId = 0
        }
    }

    fun create(): FrameShader {
        if (mProgramId == 0) {
            mProgramId = ShaderUtils.createProgram(mVertexShader, mFragmentShader)
            aPositionHandle = GLES20.glGetAttribLocation(mProgramId, "aPosition")
            aTextureCoordHandle = GLES20.glGetAttribLocation(mProgramId, "aTexCoord")
            uPosMatrixHandle = GLES20.glGetUniformLocation(mProgramId, "uPosMatrix")
            uTexMatrixHandle = GLES20.glGetUniformLocation(mProgramId, "uTexMatrix")
            uTextureSamplerHandle = GLES20.glGetUniformLocation(mProgramId, "sTexture")

            uiTypeHandle = GLES20.glGetUniformLocation(mProgramId, "uiType")
            uvInputHandle = GLES20.glGetUniformLocation(mProgramId, "ufPosition")
        }
        return this
    }

    fun draw(frameModel: FrameModel, textureId: Int, textureIndex: Int) {
        if (mProgramId == 0) {
            throw IllegalStateException("FrameShader is not created")
        }
        GLES20.glUseProgram(mProgramId)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle)
        GLES20.glEnableVertexAttribArray(uTextureSamplerHandle)
        GLES20.glEnableVertexAttribArray(uPosMatrixHandle)
        GLES20.glEnableVertexAttribArray(uTexMatrixHandle)
        GLES20.glEnableVertexAttribArray(uiTypeHandle)
        GLES20.glEnableVertexAttribArray(uvInputHandle)

        TextureUtils.bindTexture2D(textureId, GLES20.GL_TEXTURE0 + textureIndex, uTextureSamplerHandle, textureIndex)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 12, frameModel.vertexBuffer)
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 8, frameModel.textureBuffer)
        GLES20.glUniformMatrix4fv(uPosMatrixHandle, 1, false, frameModel.positionMatrix, 0)
        GLES20.glUniformMatrix4fv(uTexMatrixHandle, 1, false, frameModel.textureMatrix, 0)
        GLES20.glUniform1i(uiTypeHandle, frameModel.shaderType)
        GLES20.glUniform4fv(uvInputHandle, 1, frameModel.shaderVector, 0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, frameModel.drawBuffer)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aTextureCoordHandle)
        GLES20.glDisableVertexAttribArray(uTextureSamplerHandle)
        GLES20.glDisableVertexAttribArray(uPosMatrixHandle)
        GLES20.glDisableVertexAttribArray(uTexMatrixHandle)
        GLES20.glDisableVertexAttribArray(uiTypeHandle)
        GLES20.glDisableVertexAttribArray(uvInputHandle)
    }
}