package com.edmodo.cropper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class CropFileUtils {

    public static File createTempFile(Context context, String fileName) {
        try {
            File file = new File(context.getExternalFilesDir(null) + File.separator + "media" + File.separator + fileName);
            file.getParentFile().mkdirs();
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String saveBitmap(Context context,Bitmap bitmap, int width, int height, int option) {

        String jpegName = System.currentTimeMillis() + "_cover.jpg";
        File jpegPath = createTempFile(context,jpegName);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = BitmapUtils.calculateInSampleSize(bitmap, width, height);
            options.inJustDecodeBounds = false;
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

            int inSampleSize = 100;//个人喜欢从80开始,
            bitmap.compress(Bitmap.CompressFormat.JPEG, inSampleSize, baos);
            while (baos.toByteArray().length / 1024 > option) {
                baos.reset();
                inSampleSize -= 5;
                if (inSampleSize < 0) {
                    inSampleSize = 5;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, inSampleSize, baos);
                    break;
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, inSampleSize, baos);
            }

            FileOutputStream fos = new FileOutputStream(jpegPath);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
            baos.flush();
            baos.close();
            return jpegPath.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
