package com.android.module;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.apicloud.CommonScanActivity;
import com.apicloud.code.utils.Constant;
import com.cjt2325.cameralibrary.CameraActivity;
import com.cjt2325.cameralibrary.JCameraView;
import com.cjt2325.cameralibrary.util.Util;
import com.downloader.DownLoaderActivity;
import com.edmodo.cropper.CropImageActivity;
import com.media.MediaActivity;
import com.media.image.ImageModel;
import com.media.utils.LoadingManager;
import com.media.utils.MediaFileUtils;
import com.vincent.videocompressor.VideoCompress;

import java.net.URISyntaxException;
import java.text.DecimalFormat;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private final int requestCode = 100;

    private static final int REQUEST_MEDIA = 100;
    private static final int REQUEST_CAMERA = 200;
    private static final int REQUEST_IMAGE = 300;
    private static final int REQUEST_VIDEO = 400;
    private static final int REQUEST_CROP = 500;
    private static final int REQUEST_SCAN = 600;

    private String inputPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionsManager();

        findViewById(R.id.media).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MediaActivity.class);
                intent.putExtra("max", 9);
                intent.putExtra("type", ImageModel.TYPE_ALL);
                intent.putExtra("compressor", false);
                startActivityForResult(intent, REQUEST_MEDIA);
            }
        });
        findViewById(R.id.cropPicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                /* 开启Pictures画面Type设定为image */
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
        findViewById(R.id.Camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = JCameraView.BUTTON_STATE_BOTH;
                int minTime = 3;
                int maxTime = 10;
                CameraActivity.startCameraActivity(MainActivity.this, minTime, maxTime, "#44bf19", type, REQUEST_CAMERA);
            }
        });
        findViewById(R.id.videocompressor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                /* 开启Pictures画面Type设定为image */
//                intent.setType("image/*");
//                intent.setType("audio/*"); //选择音频
                intent.setType("video/*"); //选择视频 （mp4 3gp 是android支持的视频格式）
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_VIDEO);
            }
        });
        findViewById(R.id.Zxing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CommonScanActivity.class);
                intent.putExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_ALL_MODE);
                startActivityForResult(intent, REQUEST_SCAN);
            }
        });
        findViewById(R.id.downloader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DownLoaderActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                switch (requestCode) {
                    case REQUEST_MEDIA:
                        String resultJson = data.getStringExtra("resultJson");
                        Toast.makeText(MainActivity.this, resultJson, Toast.LENGTH_LONG).show();

                        break;
                    case REQUEST_CAMERA:
                        if (data != null) {
                            String type = data.getStringExtra("type");
                            if (type.equals("capture")) {
                                String picturePath = data.getStringExtra("picturePath");
                                String thumbnailPath = data.getStringExtra("thumbnailPath");
                            } else if (type.equals("record")) {
                                String coverPath = data.getStringExtra("coverPath");
                                String videoPath = data.getStringExtra("videoPath");
                            }
                        }
                        break;
                    case REQUEST_IMAGE:
                        try {
                            inputPath = Util.getFilePath(this, data.getData());
                            Intent intent = new Intent(MainActivity.this, CropImageActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(CropImageActivity.ARG_PATH, inputPath);
                            bundle.putBoolean(CropImageActivity.ARG_FIXED_RATIO, false);
                            bundle.putFloat(CropImageActivity.ARG_WIDTH, 3);
                            bundle.putFloat(CropImageActivity.ARG_HEIGHT, 2);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, REQUEST_CROP);

                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        break;
                    case REQUEST_VIDEO:
                        try {
                            inputPath = Util.getFilePath(this, data.getData());
                            String destPath = MediaFileUtils.createAttachmentFile(MainActivity.this, "video_" + System.currentTimeMillis() + ".mp4").getPath();
                            final VideoCompress.VideoCompressTask task = VideoCompress.compressVideoLow(inputPath, destPath, new VideoCompress.CompressListener() {
                                @Override
                                public void onStart() {
                                    LoadingManager.showProgress(MainActivity.this, String.format(getResources().getString(com.media.R.string.str_compressor_wait), "0.00%"));
                                }

                                @Override
                                public void onSuccess() {
                                    LoadingManager.hideProgress(MainActivity.this);
                                }

                                @Override
                                public void onFail() {
                                    LoadingManager.hideProgress(MainActivity.this);
                                }

                                @Override
                                public void onProgress(float percent) {
                                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                                    String strPercent = decimalFormat.format(percent);
                                    LoadingManager.updateProgress(MainActivity.this, String.format(getResources().getString(com.media.R.string.str_compressor_wait), strPercent + "%"));
                                }
                            });
                            LoadingManager.OnDismissListener(MainActivity.this, new LoadingManager.OnDismissListener() {
                                @Override
                                public void onDismiss() {
                                    task.cancel(true);
                                }
                            });
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        break;
                    case REQUEST_CROP:
                        String cropPicture = data.getStringExtra("clipPicture");
                        Toast.makeText(MainActivity.this, cropPicture, Toast.LENGTH_LONG).show();

                        break;
                    case REQUEST_SCAN:
                        final String result = data.getStringExtra("result");
                        Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    }

    @AfterPermissionGranted(requestCode)
    private void permissionsManager() {

        if (EasyPermissions.hasPermissions(this, permissions)) {

        } else {
            EasyPermissions.requestPermissions(this, "请同意下面的权限",
                    requestCode, permissions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
