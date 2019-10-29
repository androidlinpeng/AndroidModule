/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apicloud;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.code.utils.Constant;
import com.apicloud.code.zxing.ScanListener;
import com.apicloud.code.zxing.ScanManager;
import com.apicloud.code.zxing.decode.DecodeThread;
import com.apicloud.code.zxing.decode.Utils;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 二维码扫描使用
 */
public class CommonScanActivity extends Activity implements ScanListener, View.OnClickListener {

    SurfaceView scanPreview = null;
    View scanContainer;
    View scanCropView;
    ImageView scanLine;
    ScanManager scanManager;
    TextView iv_light;
    boolean openlight = false;
    final int PHOTOREQUESTCODE = 1111;

    ImageView scan_image;
    ImageView authorize_return;
    TextView photo;
    private int scanMode;//扫描模型（条形，二维码，全部）

    TextView scan_hint;
    TextView tv_scan_result;

    String state;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title bar  即隐藏标题栏
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_scan_code);
        scan_image = (ImageView) findViewById(R.id.scan_image);
        authorize_return = (ImageView) findViewById(R.id.back);
        photo = (TextView) findViewById(R.id.photo_albnm);
        scan_hint = (TextView) findViewById(R.id.scan_hint);
        tv_scan_result = (TextView) findViewById(R.id.tv_scan_result);

        scanMode = getIntent().getIntExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_BARCODE_MODE);
        state = getIntent().getStringExtra("state");

        initView();

    }

    void initView() {
        switch (scanMode) {
            case DecodeThread.BARCODE_MODE:
                scan_hint.setText("将条形码对入取景框，即可自动扫描");
                break;
            case DecodeThread.QRCODE_MODE:
                scan_hint.setText("将二维码对入取景框，即可自动扫描");
                break;
            case DecodeThread.ALL_MODE:
                scan_hint.setText("将二维码对入取景框，即可自动扫描");
                break;
        }

        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        scanContainer = findViewById(R.id.capture_container);
        scanCropView = findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
        iv_light = (TextView) findViewById(R.id.iv_light);
        iv_light.setOnClickListener(this);
        authorize_return.setOnClickListener(this);
        photo.setOnClickListener(this);
        //构造出扫描管理器
        scanManager = new ScanManager(this, scanPreview, scanContainer, scan_image, scanLine, scanMode, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        scanManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        scanManager.onPause();
    }


    public void scanResult(Result rawResult, Bundle bundle) {

        Intent resultData = new Intent();
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        array.put(rawResult.getText());
        try {
//            json.put("type", "jsyes");
//            json.put("sendback", array);
            json.put("ScanningResult", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        resultData.putExtra("result", json.toString());
        resultData.putExtra("result", rawResult.getText());
        setResult(RESULT_OK, resultData);
        finish();
    }

    public void startScan() {
        scanManager.reScan();
    }

    @Override
    public void scanError(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        //相机扫描出错时
        if (e.getMessage() != null && e.getMessage().startsWith("相机")) {
            scanPreview.setVisibility(View.INVISIBLE);
        }
    }

    public void showPictures(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String photo_path;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTOREQUESTCODE:
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = this.getContentResolver().query(data.getData(), proj, null, null, null);
                    if (cursor.moveToFirst()) {
                        int colum_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        photo_path = cursor.getString(colum_index);
                        if (photo_path == null) {
                            photo_path = Utils.getPath(getApplicationContext(), data.getData());
                        }
                        scanManager.scanningImage(photo_path);
                    }
            }
        }
    }

    @Override
    public void onClick(View v) {

        int iv_lightId = R.id.iv_light;
        int authorize_returnId = R.id.back;
        int photoId = R.id.photo_albnm;
        int i = v.getId();
        if (i == iv_lightId) {
            scanManager.switchLight();
            if (openlight) {
                openlight = false;
                iv_light.setBackgroundResource(R.drawable.scan2code_icon_light_sel);
            } else {
                openlight = true;
                iv_light.setBackgroundResource(R.drawable.scan2code_icon_light_nor);
            }
        } else if (i == authorize_returnId) {
//            Intent resultData = new Intent();
//            JSONObject json = new JSONObject();
//            JSONArray array = new JSONArray();
//            array.put("");
//            try {
//                json.put("type", "jsno");
//                json.put("sendback", array);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            resultData.putExtra("result", json.toString());
//            setResult(RESULT_OK, resultData);
            finish();
        } else if (i == photoId) {
            showPictures(PHOTOREQUESTCODE);
        }
    }

}