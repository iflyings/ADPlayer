package com.android.iflyings.player.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Ads on 2016/11/8.
 */
public class BitmapUtils {

    public static void sendImage(int width, int height, Context context) {
        final IntBuffer pixelBuffer = IntBuffer.allocate(width * height);

        //depends on the resolution of screen, about 20-50ms (1280x720)
        long start = System.nanoTime();
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                pixelBuffer);
        long end = System.nanoTime();

        Log.d("TryOpenGL", "glReadPixels time: " + (end - start)/1000000+" ms");

        //about 700-4000ms(png) 200-1000ms(jpeg)
        //use jpeg instead of png to save time
        //it will consume large memory and may take a long time, depends on the phone
        new SaveBitmapTask(pixelBuffer,width,height,context).execute();
    }

    static class SaveBitmapTask extends AsyncTask<Void, Integer, Boolean>{
        long start;

        IntBuffer rgbaBuf;
        int width, height;
        Context context;

        String filePath;

        public SaveBitmapTask(IntBuffer rgbaBuf, int width, int height, Context context) {
            this.rgbaBuf = rgbaBuf;
            this.width = width;
            this.height = height;
            this.context = context;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            File sdRoot = Environment.getExternalStorageDirectory();
            String dir = "/Pano360Screenshots/";
            File mkDir = new File(sdRoot, dir);
            if (!mkDir.exists())
                mkDir.mkdir();
            String filename="/PanoScreenShot_" +width + "_" + height + "_" + simpleDateFormat.format(new Date())+".jpg";
            filePath= mkDir.getAbsolutePath()+filename;
        }

        @Override
        protected void onPreExecute() {
            start = System.nanoTime();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            saveRgb2Bitmap(rgbaBuf, filePath , width, height);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Log.d("TryOpenGL", "saveBitmap time: " + (System.nanoTime() - start)/1000000+" ms");
            Toast.makeText(context,"ScreenShot is saved to "+filePath,Toast.LENGTH_LONG).show();
            super.onPostExecute(aBoolean);
        }
    }
    public static void saveRgb2Bitmap(IntBuffer buf, String filePath, int width, int height) {
        final int[] pixelMirroredArray = new int[width * height];
        Log.d("TryOpenGL", "Creating " + filePath);
        BufferedOutputStream bos = null;
        try {
            int[] pixelArray = buf.array();
            // rotate 180 deg with x axis because y is reversed
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    pixelMirroredArray[(height - i - 1) * width + j] = pixelArray[i * width + j];
                }
            }
            bos = new BufferedOutputStream(new FileOutputStream(filePath));
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(IntBuffer.wrap(pixelMirroredArray));
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            bmp.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveBitmap(Bitmap bitmap, String savePath, Bitmap.CompressFormat format) {
        if (format == null) {
            format = Bitmap.CompressFormat.JPEG;
        }
        // 保存图片
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(savePath));
            bitmap.compress(format, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    public static Bitmap loadBitmapFromText(String text, int textSize) {
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.LEFT);// 若设置为center，则文本左半部分显示不全 paint.setColor(Color.RED);
        paint.setAntiAlias(true);// 消除锯齿
        paint.setTextSize(textSize);
        paint.setColor(Color.RED);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float width = paint.measureText(text);
        float height = fontMetrics.bottom - fontMetrics.top;
        float baseline = -fontMetrics.top;
        Rect showRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), showRect);
        Bitmap whiteBitmap = Bitmap.createBitmap((int)Math.ceil(width), (int)Math.ceil(height), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(whiteBitmap);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawText(text, 0, baseline, paint) ;
        canvas.save();
        canvas.restore();
        return whiteBitmap;
    }

    public static Bitmap loadBitmapFromPath(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inJustDecodeBounds = true;
        //options.inPreferredConfig = Bitmap.Config.RGB_565;//将Config设为RGB565
        //BitmapFactory.decodeFile(path, options);
        //options.inSampleSize = calculateInSample(options,targetWidth,targetWidth);
        //options.inJustDecodeBounds = false;
        options.inScaled = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap loadBitmapFromAssets(Context context, String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = context.getResources().getAssets().open(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inputStream == null) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeStream(inputStream);
    }

    public static Bitmap loadBitmapFromRaw(Context context, int resourceId){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(context.getResources(),resourceId,options);
    }
}
