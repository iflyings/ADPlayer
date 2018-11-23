package com.android.iflyings.player.shader

import android.content.Context
import android.opengl.GLES20
import com.android.iflyings.player.R
import com.android.iflyings.player.model.MediaModel
import com.android.iflyings.player.utils.ShaderUtils
import com.android.iflyings.player.utils.TextureUtils

internal class VideoShader(context: Context) {
    private val mVertexShader: String = ShaderUtils.readRawTextFile(context, R.raw.video_vertex_shader)!!
    private val mFragmentShader: String = ShaderUtils.readRawTextFile(context, R.raw.video_fragment_shader)!! +
            ShaderUtils.readRawTextFile(context, R.raw.function_fragment_shader)!!

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

    fun create(): VideoShader {
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

    fun draw(mediaModel: MediaModel, textureId: Int, textureIndex: Int) {
        if (mProgramId == 0) {
            throw IllegalStateException("ImageShader is not created")
        }
        // 使用shader程序
        GLES20.glUseProgram(mProgramId)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle)
        GLES20.glEnableVertexAttribArray(uPosMatrixHandle)
        GLES20.glEnableVertexAttribArray(uTexMatrixHandle)
        GLES20.glEnableVertexAttribArray(uTextureSamplerHandle)
        GLES20.glEnableVertexAttribArray(uiTypeHandle)
        GLES20.glEnableVertexAttribArray(uvInputHandle)

        TextureUtils.bindTextureEXTERNALOES(textureId, GLES20.GL_TEXTURE0 + textureIndex, uTextureSamplerHandle, textureIndex)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 12, mediaModel.vertexBuffer)
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 8, mediaModel.textureBuffer)
        GLES20.glUniformMatrix4fv(uPosMatrixHandle, 1, false, mediaModel.positionMatrix, 0)
        GLES20.glUniformMatrix4fv(uTexMatrixHandle, 1, false, mediaModel.textureMatrix, 0)
        GLES20.glUniform1i(uiTypeHandle, mediaModel.shaderType)
        GLES20.glUniform4fv(uvInputHandle, 1, mediaModel.shaderVector, 0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mediaModel.drawBuffer)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aTextureCoordHandle)
        GLES20.glDisableVertexAttribArray(uPosMatrixHandle)
        GLES20.glDisableVertexAttribArray(uTexMatrixHandle)
        GLES20.glDisableVertexAttribArray(uTextureSamplerHandle)
        GLES20.glDisableVertexAttribArray(uiTypeHandle)
        GLES20.glDisableVertexAttribArray(uvInputHandle)
    }
}
