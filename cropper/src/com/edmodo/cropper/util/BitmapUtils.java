package com.edmodo.cropper.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.IOException;
import java.io.InputStream;

/**
 * 获取Bitmap的工具类
 */
public class BitmapUtils {

    private static BitmapFactory.Options options = null;
    /**
     * 判断图片是否已经损坏
     */
    public static boolean getEffective(String path) {
        if (options == null) {
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
        }
        BitmapFactory.decodeFile(path, options); //filePath代表图片路径
        if (options.mCancel || options.outWidth == -1
                || options.outHeight == -1) {
            //表示图片已损毁
            return false;
        }
        return true;
    }

    /**
     * 通过BitmapDrawable来获取Bitmap
     * @param mContext
     * @param fileName
     * @return
     */
    public static Bitmap getBitmapFromBitmapDrawable(Context mContext, String fileName) {
        BitmapDrawable bmpMeizi = null;
        try {
            bmpMeizi = new BitmapDrawable(mContext.getAssets().open(fileName));//"pic_meizi.jpg"
            Bitmap mBitmap = bmpMeizi.getBitmap();
            return mBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过资源ID获取Bitmap
     * @param res
     * @param resId
     * @return
     */
    public static Bitmap getBitmapFromResource(Resources res, int resId) {
        return BitmapFactory.decodeResource(res, resId);
    }

    /**
     * 通过文件路径来获取Bitmap
     * @param pathName
     * @return
     */
    public static Bitmap getBitmapFromFile(String pathName) {
        return BitmapFactory.decodeFile(pathName);
    }

    /**
     * 通过字节数组来获取Bitmap
     * @param b
     * @return
     */
    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    /**
     * 通过输入流InputStream来获取Bitmap
     * @param inputStream
     * @return
     */
    public static Bitmap getBitmapFromStream(InputStream inputStream) {
        return BitmapFactory.decodeStream(inputStream);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int picheight = options.outHeight;
        final int picwidth = options.outWidth;

        int targetheight = picheight;
        int targetwidth = picwidth;
        int inSampleSize = 1;

        if (targetheight > reqHeight || targetwidth > reqWidth) {
            while (targetheight >= reqHeight && targetwidth >= reqWidth) {
                inSampleSize += 1;
                targetheight = picheight / inSampleSize;
                targetwidth = picwidth / inSampleSize;
            }
        }
        return inSampleSize;
    }

    public static int calculateInSampleSize(Bitmap bitmap, int reqWidth, int reqHeight) {
        final int picheight = bitmap.getHeight();
        final int picwidth = bitmap.getWidth();

        int targetheight = picheight;
        int targetwidth = picwidth;
        int inSampleSize = 1;

        if (targetheight > reqHeight || targetwidth > reqWidth) {
            while (targetheight >= reqHeight && targetwidth >= reqWidth) {
                inSampleSize += 1;
                targetheight = picheight / inSampleSize;
                targetwidth = picwidth / inSampleSize;
            }
        }
        return inSampleSize;
    }
}