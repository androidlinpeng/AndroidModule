package com.media.image;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.media.utils.MediaCommonUtil;

import java.util.ArrayList;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * 获取视频
     *
     * @param context
     * @return
     */
    public static ArrayList<ImageModel> getVideos(Context context) {
        ArrayList<ImageModel> list = new ArrayList<ImageModel>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.MINI_THUMB_MAGIC,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DURATION};
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " desc";
        Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);
        int vId = cursor.getColumnIndex(MediaStore.Video.Media._ID);
        int vPath = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
        int vThumb = cursor.getColumnIndex(MediaStore.Video.Media.MINI_THUMB_MAGIC);
        int vSize = cursor.getColumnIndex(MediaStore.Video.Media.SIZE);
        int vWidth = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH);
        int vHeight = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT);
        int durationId = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            int thumbId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
            //视频缩略图路径
            String albumPath = "";
            Cursor thumbCursor = context.getApplicationContext().getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, null, MediaStore.Video.Thumbnails.VIDEO_ID + "=" + thumbId, null, null);
            if (thumbCursor.moveToFirst()) {
                albumPath = thumbCursor.getString(thumbCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                Log.i(TAG, "getVideos: albumPath "+albumPath);
            }

            String id = cursor.getString(vId);
            String path = cursor.getString(vPath);
            String thumb = cursor.getString(vThumb);
            int size = cursor.getInt(vSize);
            int width = cursor.getInt(vWidth);
            int height = cursor.getInt(vHeight);
            int duration = cursor.getInt(durationId);
            Log.i(TAG, "getVideos: size=" + size + " width=" + width + " height=" + height);
            ImageModel imageModel = new ImageModel.Builder()
                    .id(id)
                    .path(path)
                    .thumb(thumb)
                    .duration(duration)
                    .type(ImageModel.TYPE_VIDEO)
                    .size(size)
                    .width(width)
                    .height(height)
                    .build();

            list.add(imageModel);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    /**
     * 获取照片
     *
     * @param context
     * @return
     */
    public static ArrayList<ImageModel> getImages(Context context) {
        ArrayList<ImageModel> list = new ArrayList<ImageModel>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MINI_THUMB_MAGIC,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " desc";
        Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);
        int iId = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        int iPath = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        int iThumb = cursor.getColumnIndex(MediaStore.Images.Media.MINI_THUMB_MAGIC);
        int bucket_display_name = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        int iSize = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
        int iWidth = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH);
        int iHeight = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String id = cursor.getString(iId);
            String path = cursor.getString(iPath);
            String thumb = cursor.getString(iThumb);
            String bucketName = cursor.getString(bucket_display_name);
            int size = cursor.getInt(iSize);
            int width = cursor.getInt(iWidth);
            int height = cursor.getInt(iHeight);
            if (!path.endsWith(".gif") &&
                    !MediaCommonUtil.isBlank(size) && size > 0 &&
                    !MediaCommonUtil.isBlank(width) && width > 0 &&
                    !MediaCommonUtil.isBlank(height) && height > 0) {
                Log.i(TAG, "getImages: size=" + size + " width=" + width + " height=" + height);
                Log.i(TAG, "getImages: thumb=" + thumb);
                ImageModel imageModel = new ImageModel.Builder()
                        .id(id)
                        .path(path)
                        .thumb(thumb)
                        .fileName(bucketName)
                        .type(ImageModel.TYPE_IMAGE)
                        .size(size)
                        .width(width)
                        .height(height)
                        .build();
                list.add(imageModel);
            }
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    /**
     * 获取图片文件夹
     *
     * @param context
     * @return
     */
    public static ArrayList<ImageModel> getTypeImageslist(Context context) {
        ArrayList<ImageModel> imageFolders = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        /*查询id、  缩略图、原图、文件夹ID、 文件夹名、 文件夹分类的图片总数*/
        String[] columns = {MediaStore.Images.Media._ID, MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, "COUNT(1) AS count"};
        String selection = "0==0) GROUP BY (" + MediaStore.Images.Media.BUCKET_ID;
        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED;
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, selection, null, sortOrder);
            if (cursor != null && cursor.moveToFirst()) {

                int columnPath = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int columnId = cursor.getColumnIndex(MediaStore.Images.Media._ID);

                int columnFileName = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int columnCount = cursor.getColumnIndex("count");

                do {
                    ImageModel imageModel = new ImageModel.Builder()
                            .id(cursor.getString(columnId))
                            .path(cursor.getString(columnPath))
                            .pisNum(cursor.getInt(columnCount))
                            .fileName(cursor.getString(columnFileName))
                            .type(ImageModel.TYPE_IMAGE)
                            .build();
                    String bucketName = cursor.getString(columnFileName);

                    if (!Environment.getExternalStorageDirectory().getPath().contains(bucketName)) {
                        imageFolders.add(0, imageModel);
                    }
                } while (cursor.moveToNext());
            }
            return imageFolders;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static BitmapFactory.Options options = null;

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

}
