package com.apicloud;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.apicloud.code.zxing.encode.EncodingHandler;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class CreateCodeActivity extends Activity implements View.OnClickListener {

    private String codeUrl = "";

    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_code);

        codeUrl = getIntent().getStringExtra("gittext");
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        createCode();

    }

    private void createCode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                create2Code(codeUrl);
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int backId = R.id.back;
        if (id == backId) {
            finish();
        }
    }

    private Bitmap create2Code(String codeUrl) {
        File imgFile = FileUtils.createTmpFile("codeImage" + ".jpg");
        if (imgFile.getAbsolutePath() == null) {
        }
        Bitmap qrCode = null;
        try {
            //生成二维码
            qrCode = EncodingHandler.create2Code(codeUrl, 400);
            compressBitmapToFile(qrCode, imgFile);

           Intent resultData = new Intent();
            JSONObject json = new JSONObject();
            try {
                json.put("sendfile", imgFile.getAbsolutePath());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            resultData.putExtra("result", json.toString());
            setResult(RESULT_OK, resultData);
            finish();

        } catch (WriterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return qrCode;
    }

    public static void compressBitmapToFile(Bitmap bitmap, File file) {
        if (null != bitmap && null != file) {
            try {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getBitmap(String url) {
        Bitmap bm = null;
        try {
            URL iconUrl = new URL(url);
            URLConnection conn = iconUrl.openConnection();
            HttpURLConnection http = (HttpURLConnection) conn;

            int length = http.getContentLength();

            conn.connect();
            // 获得图像的字符流
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, length);
            bm = BitmapFactory.decodeStream(bis);

//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeStream(bis, null, options);
//            options.outWidth = 60;
//            options.outHeight = 60;
//            options.inJustDecodeBounds = false;
//            bm = BitmapFactory.decodeStream(bis, null, options);

            bis.close();
            is.close();// 关闭流
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bm;
    }

}
