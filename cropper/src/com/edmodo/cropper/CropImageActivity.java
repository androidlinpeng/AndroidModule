package com.edmodo.cropper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.edmodo.cropper.util.CropFileUtils;
import com.edmodo.cropper.util.FileSizeUtil;

public class CropImageActivity extends AppCompatActivity {

    public static final String ARG_PATH = "path";
    public static final String ARG_TYPE = "type";
    public static final String ARG_FIXED_RATIO = "fixed_ratio";
    public static final String ARG_WIDTH = "width";
    public static final String ARG_HEIGHT = "height";
    public static final String ARG_CLIP_PATH = "clip_path";

    private CropImageView cropImageView;
    private View bottomView;
    private ImageView ivRotaing;
    private TextView restore;
    private TextView confirm;
    private TextView cancel;
    private String filePath;
    private boolean ratio;

    private float imgWidth;
    private float imgHeight;
    private int angle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        cropImageView = findViewById(R.id.cropImageView);
        bottomView = findViewById(R.id.bottomView);
        ivRotaing = findViewById(R.id.iv_rotaing);
        restore = findViewById(R.id.restore);
        confirm = findViewById(R.id.confirm);
        cancel = findViewById(R.id.cancel);
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            filePath = bundle.getString(ARG_PATH);
            ratio = bundle.getBoolean(ARG_FIXED_RATIO);
            imgWidth = bundle.getFloat(ARG_WIDTH);
            imgHeight = bundle.getFloat(ARG_HEIGHT);
            cropImageView.setFixedAspectRatio(ratio);

            showPic(filePath);
        } else {
            finish();
        }
        ivRotaing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle -= 90;
                showPic(filePath);
                restore.setTextColor(getResources().getColor(android.R.color.white));
            }
        });
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle = 0;
                showPic(filePath);
                restore.setTextColor(Color.parseColor("#5E5E5E"));
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Bitmap croppedImage = cropImageView.getCroppedImage();
                filePath = CropFileUtils.saveBitmap(getApplication(), croppedImage, 300, 300, 200);
                if (filePath != null) {
                    Intent intent = new Intent();
                    intent.putExtra(ARG_CLIP_PATH, filePath);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(CropImageActivity.this, "裁剪失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null) {
            bitmap.recycle();
        }
        if (returnBm != null) {
            returnBm.recycle();
        }
    }

    private Bitmap bitmap;

    private void showPic(final String picPath) {
        double ImgSize = FileSizeUtil.getFileOrFilesSize(picPath, FileSizeUtil.SIZETYPE_MB);
        if (ImgSize <= 0 ) {
            Toast.makeText(CropImageActivity.this, "图片已损坏", Toast.LENGTH_SHORT).show();
            return;
        }
        if (picPath == null || picPath.equals("")) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.with(getApplication()).asBitmap().load(picPath).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@android.support.annotation.NonNull Bitmap resource, @android.support.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                        if (resource != null) {
                            if (bitmap != null) {
                                bitmap.recycle();
                            }
                            if (returnBm != null) {
                                returnBm.recycle();
                            }
                            bitmap = rotaingImageView(angle, resource);
                            if (ratio) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(getApplication())
                                                .load(bitmap)
                                                .placeholder(R.mipmap.crop_ic_picture_default)
                                                .error(R.mipmap.crop_ic_picture_default)
                                                .into(cropImageView);
                                        float width = bitmap.getWidth();
                                        float height = bitmap.getHeight();
                                        if (imgWidth > imgHeight) {
                                            float scaleH5 = imgWidth / imgHeight;
                                            float scale = width / height;
                                            if (scaleH5 >= scale) {
                                                cropImageView.setAspectRatio((int) width, (int) (width * imgHeight / imgWidth));
                                            } else {
                                                cropImageView.setAspectRatio((int) width, (int) height);
                                            }
                                        } else if (imgWidth == imgHeight) {
                                            if (width >= height) {
                                                cropImageView.setAspectRatio((int) height, (int) height);
                                            } else {
                                                cropImageView.setAspectRatio((int) width, (int) width);
                                            }
                                        } else {
                                            float scaleH5 = imgWidth / imgHeight;
                                            float scale = width / height;
                                            if (scaleH5 >= scale) {
                                                cropImageView.setAspectRatio((int) width, (int) height);
                                            } else {
                                                cropImageView.setAspectRatio((int) (height * imgWidth / imgHeight), (int) height);
                                            }
                                        }
                                        cropImageView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                cropImageView.invalidate();
                                            }
                                        });

                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(getApplication())
                                                .load(bitmap)
                                                .placeholder(R.mipmap.crop_ic_picture_default)
                                                .error(R.mipmap.crop_ic_picture_default)
                                                .into(cropImageView);
                                    }
                                });
                            }
                            bottomView.post(new Runnable() {
                                @Override
                                public void run() {
                                    bottomView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                });
            }
        }).start();
    }

    private Bitmap returnBm;

    public Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        return returnBm;
    }

}

