package com.android.iflyings.player

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.android.iflyings.R
import com.android.iflyings.player.info.MediaInfo
import com.android.iflyings.player.utils.FileUtils

class PlayerActivity : AppCompatActivity(), MediaWindow.WindowCallback {
    private lateinit var mGLPlayerView: GLPlayerView
    private lateinit var mWaitingView: View

    private val mMediaModelLists = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        mWaitingView = findViewById(R.id.ll_container)
        mGLPlayerView = findViewById(R.id.psv_player)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        checkSelfPermissions()
    }

    private fun checkSelfPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
            }
        } else {
            mGLPlayerView.post { initData() }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.app_name).setIcon(R.mipmap.ic_launcher)
                            .setMessage(R.string.no_permission_to_read_external_storage)
                            .setPositiveButton(android.R.string.ok) { _, _ -> this@PlayerActivity.finish() }
                            .show()
                } else {
                    initData()
                }
            }
        }
    }

    private fun initData() {
        mWaitingView.visibility = View.VISIBLE
        Thread(Runnable {
            mMediaModelLists.clear()
            //"/mnt/usb/D626-03AD"
            //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
            val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
            mMediaModelLists.addAll(FileUtils.getAllFileInFolder(filePath, resources.getStringArray(R.array.type_image)))
            //mMediaModelLists.addAll(FileUtils.getAllFileInFolder(filePath, resources.getStringArray(R.array.type_video)))

            runOnUiThread {
                val window = MediaWindow(this)
                window.setScreenSize(mGLPlayerView.width, mGLPlayerView.height)
                mMediaModelLists.forEach { path -> window.add(this@PlayerActivity, path) }
                mGLPlayerView.addWindow(window)
                mGLPlayerView.start()
                mWaitingView.visibility = View.INVISIBLE
                mGLPlayerView.postDelayed({ window.setFontInfo("java中国话yunÃÇŸŒú", 200) }, 3000)
            }
        }).start()
    }

    override fun onBackPressed() {
        mGLPlayerView.stop()
        finish()
    }

    override fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    override fun runInGLThread(r: Runnable) {
        mGLPlayerView.queueEvent(r)
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 236
    }
}
