package com.android.iflyings.player

import android.Manifest
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.android.iflyings.player.info.ImageInfo
import com.android.iflyings.player.info.VideoInfo
import com.android.iflyings.player.utils.FileUtils
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity(), MediaWindow.WindowCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

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
            psvPlayer.post { initData() }
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
        waitContainer.visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.Default) {
            val mediaWindow = MediaWindow(this@PlayerActivity)
            //val filePath = "/mnt/usb/0000-0000/4K"
            val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
            //FileUtils.getAllFileInFolder(filePath, resources.getStringArray(R.array.type_video).toList())
            //        .takeIf { it.isNotEmpty() }?.forEach { mediaWindow.add(VideoInfo(it)) }
            FileUtils.getAllFileInFolder(filePath, resources.getStringArray(R.array.type_image).toList())
                    .takeIf { it.isNotEmpty() }?.forEach { mediaWindow.add(ImageInfo(it)) }
            psvPlayer.add(mediaWindow)
            psvPlayer.start()
            GlobalScope.launch(Dispatchers.Main) {
                waitContainer.visibility = View.INVISIBLE
            }
            delay(5000)
            mediaWindow.setFontInfo("java中国话yunÃÇŸŒú", 50)
        }
    }

    override fun onBackPressed() {
        psvPlayer.stop()
        finish()
    }

    override fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    override fun runInGLThread(r: Runnable) {
        psvPlayer.queueEvent(r)
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 236
    }
}
