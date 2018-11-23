package com.android.iflyings.player.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20.*
import android.util.AttributeSet
import android.view.TextureView
import com.android.iflyings.player.MediaWindow
import com.android.iflyings.player.info.FrameInfo
import com.android.iflyings.player.shader.ShaderManager
import com.android.iflyings.player.utils.ShaderUtils

// https://blog.csdn.net/lb377463323/article/details/77096652
class GLTextureView : TextureView{

    private var mGLRenderThread: GLRenderThread? = null

    private val mFrameInfo = FrameInfo()

    constructor(context: Context):
            super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet):
            super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int):
            super(context, attrs, defStyleAttr, defStyleRes) {
        initView()
    }

    private fun initView() {
        surfaceTextureListener = mSurfaceTextureListener
    }

    fun queueEvent(runnable: Runnable) {
        mGLRenderThread?.post(runnable)
    }
    fun start() {
        mFrameInfo.start()
    }
    fun stop() {
        mFrameInfo.stop()
    }
    fun add(mediaWindow: MediaWindow) {
        mFrameInfo.add(mediaWindow)
    }

    private val mRenderer = object: GLRenderThread.Renderer {
        override fun onSurfaceCreated() {
            ShaderManager.create(context)
            glEnable(GL_BLEND) //因为这里是两个图层，所以开启混合模式
            //GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glViewport(0,0, width, height)
            mFrameInfo.frameCreate(width, height)
            ShaderUtils.checkGlError("onSurfaceCreated")
        }

        override fun onDrawFrame() {
            mFrameInfo.frameDraw(0)
            ShaderUtils.checkGlError("onDrawFrame")
        }

        override fun onSurfaceDestroyed() {
            mFrameInfo.frameDestroy()
            ShaderManager.destroy()
        }

    }

    private val mSurfaceTextureListener = object: SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            //大小发生变化
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            //更新
            //Logger.i("onSurfaceTextureUpdated")
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            mGLRenderThread?.stopThread()
            //被销毁
            return false
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            //创建成功
            mGLRenderThread = GLRenderThread(surface).apply {
                setRenderer(mRenderer)
                startThread()
            }
        }
    }



}