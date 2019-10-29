package com.media;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.media.utils.MediaCommonUtil;

public class MediaGlideLoader {

    public static void LoderGalleryImage(Context context, String url, ImageView view) {
        if (!MediaCommonUtil.isBlank(url)) {
            LoderGalleryImage(context, url.startsWith("http") ? url : "file://" + url, view, 0);
        }
    }

    public static void LoderGalleryImage(Context context, String url, ImageView view, int round) {
        Glide.with(context)
                .load(url)
                .fitCenter()
                .thumbnail(0.1f)
                .placeholder(R.mipmap.media_ic_picture_default)
                .error(R.mipmap.media_ic_picture_default)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }

    public static void LoderDrawable(Context context, int drawable, ImageView view, int round) {
        Glide.with(context)
                .load(drawable)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }

    public static void LoderLoadImageType(Context context, String url, ImageView view) {
        Glide.with(context)
                .load("file://" + url)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }

    public static void LoderMedia(Context context, String url, ImageView view) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .centerCrop()
                .placeholder(R.color.colorMediaTheme)
                .error(R.color.colorMediaTheme)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }

}
