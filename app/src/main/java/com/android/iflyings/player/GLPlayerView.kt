package com.android.iflyings.player

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.widget.Toast
import com.android.iflyings.player.shader.ShaderManager
import tv.danmaku.ijk.media.player.IjkMediaPlayer

import java.util.ArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLPlayerView : GLSurfaceView, GLSurfaceView.Renderer, MediaWindow.WindowCallback {
    override fun showMessage(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    override fun runInGLThread(r: Runnable) {
        queueEvent(r)
    }

    private val mWinLists = ArrayList<MediaWindow>()
    private lateinit var mShaderManager: ShaderManager
    //private lateinit var mFrameManager: FrameManager

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun init() {
        // 创建一个OpenGL ES 2.0 context
        setEGLContextClientVersion(2)
        setRenderer(this)
        // 只有在绘制数据改变时才绘制view
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        //为了可以激活log和错误检查，帮助调试3D应用，需要调用setDebugFlags()。
        debugFlags = GLSurfaceView.DEBUG_CHECK_GL_ERROR or GLSurfaceView.DEBUG_LOG_GL_CALLS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            holder.setFormat(PixelFormat.RGBA_8888)
        } else {
            holder.setFormat(PixelFormat.RGB_565)
        }
        mShaderManager = ShaderManager(context)
        //mFrameManager = FrameManager()

        IjkMediaPlayer.loadLibrariesOnce(null)
        IjkMediaPlayer.native_profileBegin("libijkplayer.so")
    }

    override fun onDetachedFromWindow() {
        ShaderManager.destroy()
        for (playWindow in mWinLists) {
            playWindow.release()
        }
        super.onDetachedFromWindow()
    }

    fun start() {
        for (win in mWinLists) {
            win.start()
        }
    }
    fun stop() {
        for (win in mWinLists) {
            win.release()
        }
        ShaderManager.destroy()
    }
    fun addWindow(mediaWindow: MediaWindow) {
        mWinLists.add(mediaWindow)
    }

    private var countStart = false
    private var frameCount = 0
    private var previousTime = 0L
    private fun calculateFPS() {
        frameCount ++
        if (!countStart) {
            countStart = true
            previousTime = System.currentTimeMillis()
        }
        val intervalTime = System.currentTimeMillis() - previousTime
        if (intervalTime >= 1000) {
            countStart = false
            val fps = frameCount / (intervalTime / 1000f)
            frameCount = 0
            Log.w("zw", "FPS: $fps")
        }
    }

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        GLES20.glEnable(GLES20.GL_TEXTURE_2D)
        GLES20.glEnable(GLES20.GL_BLEND)
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        mShaderManager.createShader()
    }
    override fun onSurfaceChanged(gl10: GL10, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
        //mFrameManager.create(w, h)
    }
    override fun onDrawFrame(gl10: GL10) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        var textureIndex = 0
        //GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameManager.framebufferTextureId())
        for (window in mWinLists) {
            textureIndex = window.draw(textureIndex)
        }
        //mFrameManager.draw(textureIndex)

        //calculateFPS()
    }
}
