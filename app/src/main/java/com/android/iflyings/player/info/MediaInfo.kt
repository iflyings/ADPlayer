package com.android.iflyings.player.info

import android.graphics.Rect
import com.android.iflyings.player.MediaWindow
import com.android.iflyings.player.model.MediaModel
import com.android.iflyings.player.model.TextureData
import com.android.iflyings.player.model.WindowData
import com.android.iflyings.player.shader.ShaderManager
import com.android.iflyings.player.transformer.*
import kotlin.random.Random

abstract class MediaInfo internal constructor() {
    private val mMediaModel = MediaModel()
    private val mTextureData = TextureData()
    private lateinit var mWindowData: WindowData
    private lateinit var mOnMediaListener: OnMediaListener

    val windowLeft
        get() = mWindowData.left
    val windowRight
        get() = mWindowData.right
    val windowTop
        get() = mWindowData.top
    val windowBottom
        get() = mWindowData.bottom
    val windowWidth
        get() = mWindowData.width
    val windowHeight
        get() = mWindowData.height
    val textureLeft
        get() = mTextureData.left
    val textureRight
        get() = mTextureData.right
    val textureTop
        get() = mTextureData.top
    val textureBottom
        get() = mTextureData.bottom
    val textureWidth
        get() = mTextureData.width
    val textureHeight
        get() = mTextureData.height

    val textureMatrix
        get() = mMediaModel.textureMatrix

    fun setWindowData(winData: WindowData) {
        mWindowData = winData
    }
    fun setOnMediaListener(listener: OnMediaListener) {
        mOnMediaListener = listener
    }
    fun getMediaTransformer(): MediaWindow.MediaTransformer {
        return ColourElapseTransformer()
        val type = Random.nextInt(10)
        return when (type) {
            0 -> FadeOutTransformer()
            1 -> ZoomOutTransformer()
            2 -> CircleOutTransformer()
            3 -> MoveLeftTransformer()
            4 -> MoveTopTransformer()
            5 -> RotateHTransformer()
            6 -> RotateVTransformer()
            7 -> ShutterHVTransformer()
            8 -> MosaicTransformer()
            9 -> ColourElapseTransformer()
            else -> FadeOutTransformer()
        }
    }

    fun draw(textureIndex: Int): Int {
        synchronized(this) {
            mediaDraw(textureIndex)
        }
        return textureIndex + 1
    }

    abstract fun mediaCreate()
    abstract fun mediaDraw(textureIndex: Int): Int
    abstract fun mediaDestroy()

    protected fun drawImage(textureId: Int, textureIndex: Int): Int {
        ShaderManager.drawImage(mMediaModel, textureId, textureIndex)
        return textureIndex + 1
    }
    protected fun drawVideo(textureId: Int, textureIndex: Int): Int {
        ShaderManager.drawVideo(mMediaModel, textureId, textureIndex)
        return textureIndex + 1
    }

    protected fun postInGLThread(runnable: Runnable) {
        mOnMediaListener.runInGLThread(runnable)
    }

    protected fun setTextureSize(texWidth: Int, texHeight: Int) {
        mTextureData.setTextureSize(texWidth, texHeight)
        mMediaModel.notifyMediaModelUpdated(mWindowData, mTextureData)
    }
    fun setTextureShow(rect: Rect?) {
        mTextureData.setTextureShow(rect)
        mMediaModel.notifyMediaModelUpdated(mWindowData, mTextureData)
    }
    fun notifyMediaModelUpdated() {
        mMediaModel.notifyMediaModelUpdated(mWindowData, mTextureData)
    }

    protected fun notifyWindowShowChanged(rect: Rect?) {
        mMediaModel.vertexBuffer.put(mWindowData.getVertexBuffer(rect)).position(0)
    }
    protected fun notifyTextureShowChanged(rect: Rect?) {
        mMediaModel.textureBuffer.put(mTextureData.getTextureBuffer(rect)).position(0)
    }

    protected fun notifyMediaCreated() {
        mMediaModel.reset()
        mOnMediaListener.onCreated(this)
    }
    protected fun notifyMediaCompleted() {
        mOnMediaListener.onCompleted(this)
    }
    protected fun notifyMediaDestroyed() {
        mOnMediaListener.onDestroyed(this)
    }
    protected fun notifyMediaFailed(message: String) {
        mOnMediaListener.onFailed(this, message)
    }

    fun reset() {
        mMediaModel.reset()
    }
    fun hide() {
        mMediaModel.hide()
    }
    fun setScale(x: Float, y: Float, z: Float) {
        mMediaModel.setScale(x, y, z)
    }
    fun setShutter(position: Float) {
        mMediaModel.setShutter(position, mWindowData)
    }
    fun setRotate(a: Float, x: Float, y: Float, z: Float) {
        mMediaModel.setRotate(a, x, y, z)
    }
    fun setRect(x: Float, y: Float) {
        mMediaModel.setRect(x, y, mWindowData)
    }
    fun setMosaic(r: Float) {
        mMediaModel.setMosaic(r, mTextureData)
    }
    fun setAlpha(a: Float) {
        mMediaModel.setAlpha(a)
    }
    fun setCircle(l: Float) {
        mMediaModel.setCircle(l, mWindowData)
    }
    fun setBright(l: Float) {
        mMediaModel.setBright(l)
    }
    fun setThreshold(r: Float, g: Float, b: Float) {
        mMediaModel.setThreshold(r, g, b, true)
    }

    interface OnMediaListener {

        fun runInGLThread(r: Runnable)

        fun onCreated(m: MediaInfo)

        fun onCompleted(m: MediaInfo)

        fun onFailed(m: MediaInfo, msg: String)

        fun onDestroyed(m: MediaInfo)

    }

}
