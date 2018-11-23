package com.android.iflyings.player.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

import java.io.IOException
import java.io.InputStream

object BitmapUtils {

    fun loadBitmapFromText(text: String, textSize: Int): Bitmap {
        val paint = Paint()
        paint.textAlign = Paint.Align.LEFT// 若设置为center，则文本左半部分显示不全 paint.setColor(Color.RED);
        paint.isAntiAlias = true// 消除锯齿
        paint.textSize = textSize.toFloat()
        paint.color = Color.RED
        val fontMetrics = paint.fontMetrics
        val width = paint.measureText(text)
        val height = fontMetrics.bottom - fontMetrics.top
        val baseline = -fontMetrics.top
        val showRect = Rect()
        paint.getTextBounds(text, 0, text.length, showRect)
        val whiteBitmap = Bitmap.createBitmap(Math.ceil(width.toDouble()).toInt(), Math.ceil(height.toDouble()).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(whiteBitmap)
        canvas.drawColor(Color.BLACK)
        canvas.drawText(text, 0f, baseline, paint)
        canvas.save()
        canvas.restore()
        return whiteBitmap
    }

    fun loadBitmapFromPath(path: String): Bitmap {
        val options = BitmapFactory.Options()
        //options.inJustDecodeBounds = true;
        //options.inPreferredConfig = Bitmap.Config.RGB_565;//将Config设为RGB565
        //BitmapFactory.decodeFile(path, options);
        //options.inSampleSize = calculateInSample(options,targetWidth,targetWidth);
        //options.inJustDecodeBounds = false;
        options.inScaled = false
        return BitmapFactory.decodeFile(path, options)
    }

    fun loadBitmapFromAssets(context: Context, filePath: String): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.resources.assets.open(filePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (inputStream == null) return null
        val options = BitmapFactory.Options()
        options.inScaled = false
        return BitmapFactory.decodeStream(inputStream)
    }

    fun loadBitmapFromRaw(context: Context, resourceId: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inScaled = false
        return BitmapFactory.decodeResource(context.resources, resourceId, options)
    }
}
