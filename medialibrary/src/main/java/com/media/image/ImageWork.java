package com.media.image;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.media.MediaGlideLoader;
import com.media.R;

import java.lang.ref.WeakReference;

public class ImageWork {

    private int mImageWidth;//图片宽度
    private int mImageHeight;//图片高度
    private Resources mResources;
    private Bitmap mLoadingBitmap;//图片正在加载时显示的图片
    private ImageCache mImageCache;
    private final Object mPauseWorkLock = new Object();//锁
    protected boolean mPauseWork = false;//是否暂停加载图片的任务
    private Context context;


    public ImageWork(Context context) {
        this.context = context;
        mResources = context.getResources();
        mImageWidth = mResources.getDimensionPixelSize(R.dimen.media_image_thumbnail_size);
        mImageHeight = mResources.getDimensionPixelSize(R.dimen.media_image_thumbnail_size);
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, R.color.colorMediaTheme);
        mImageCache = new ImageCache();
    }

    /**
     * 根据路径从硬盘中读取图片
     *
     * @param path      图片路径
     * @param reqWidth  请求宽度（显示宽度）
     * @param reqHeight 请求高度（显示高度）
     * @return 图片Bitmap
     */
    public Bitmap decodeBitmapFromDisk(String path, int reqWidth, int reqHeight) {
        // BEGIN_INCLUDE (read_bitmap_dimensions)
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateBitmapSize(options, reqWidth, reqHeight);
        // END_INCLUDE (read_bitmap_dimensions)

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (ImageUtils.hasHoneycomb()) {
            addInBitmapOptions(options);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private void addInBitmapOptions(BitmapFactory.Options options) {

        // inBitmap only works with mutable bitmaps so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;

        if (mImageCache != null) {
            // Try and find a bitmap to use for inBitmap
            Bitmap inBitmap = mImageCache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
                options.inBitmap = inBitmap;
            }
        }
    }

    /**
     * 计算压缩率
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateBitmapSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

           /* final int halfHeight = height / 1;
            final int halfWidth = width / 1;*/

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((height / inSampleSize) > reqHeight
                    && (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 加载图片
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, BitmapDrawable> {
        private String mType;
        private String mPath;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(String type,String path, ImageView imageView) {
            mType = type;
            mPath = path;
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected BitmapDrawable doInBackground(String... params) {

            Bitmap bitmap = null;
            BitmapDrawable drawable = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            //如果task未取消，并且task对应的imagewview所对应的task和当前task是一样的
            //则继续执行任务，获取图片
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null) {
                if (mType.equals(ImageModel.TYPE_IMAGE)){
                    bitmap = decodeBitmapFromDisk(mPath, mImageWidth, mImageHeight);
                }else {
                    bitmap = ThumbnailUtils.createVideoThumbnail(mPath, MediaStore.Video.Thumbnails.MINI_KIND); //用于获取视频的缩略图
                }
            }

            //Bitmap转换成BitmapDrawable，并加入缓存
            if (bitmap != null) {
                drawable = new BitmapDrawable(mResources, bitmap);
                if (mImageCache != null) {
                    mImageCache.addBitmapToMemCache(mPath, drawable);
                }
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(BitmapDrawable value) {
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled()) {
                value = null;
            }

            //再次半点task和imageview的对应关系是否存在
            final ImageView imageView = getAttachedImageView();
            if (value != null && imageView != null) {
                setImageDrawable(imageView, value);
            }
        }

        @Override
        protected void onCancelled(BitmapDrawable value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * 获取imageview所对应的task，判断该task是否和当前task一致，如果一直证明imageview和task有对应关系
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    /**
     * Called when the processing is complete and the final drawable should be
     * set on the ImageView.
     *
     * @param imageView
     * @param drawable
     */
    private void setImageDrawable(ImageView imageView, Drawable drawable) {
        final TransitionDrawable td =
                new TransitionDrawable(new Drawable[]{
                        new ColorDrawable(mResources.getColor(R.color.media_transparent)),
                        drawable
                });
        // Set background to loading bitmap
        imageView.setBackgroundDrawable(
                new BitmapDrawable(mResources, mLoadingBitmap));

        imageView.setImageDrawable(td);
        td.startTransition(200);
    }

    public void loadImage(String type, String path, ImageView imageView) {
        if (path == null || path.equals("")) {
            return;
        }
        BitmapDrawable bitmapDrawable = null;

        //先从缓存读取
        if (mImageCache != null) {
            bitmapDrawable = mImageCache.getBitmapFromMemCache(path);
        }
        //读取不到再开启任务去硬盘读取
        if (bitmapDrawable != null) {
//            imageView.setImageDrawable(bitmapDrawable);
            if (type.equals(ImageModel.TYPE_IMAGE)){
                MediaGlideLoader.LoderMedia(context,path,imageView);
            }else {
                imageView.setImageDrawable(bitmapDrawable);
            }
        } else if (cancelPotentialWork(path, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(type,path, imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mLoadingBitmap, task);
//            imageView.setImageDrawable(asyncDrawable);
            if (type.equals(ImageModel.TYPE_IMAGE)){
                MediaGlideLoader.LoderMedia(context,path,imageView);
            }else {
                imageView.setImageDrawable(asyncDrawable);
            }
            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR);
        }
    }

    public static boolean cancelPotentialWork(String path, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.mPath;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || !bitmapData.equals(path)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * 持有AsyncTask弱引用的BitmapDrawable
     */
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p>
     * If work is paused, be sure setPauseWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

}
