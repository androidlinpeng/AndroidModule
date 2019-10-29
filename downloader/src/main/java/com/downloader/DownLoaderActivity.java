package com.downloader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;

import java.io.File;

public class DownLoaderActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DownLoaderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_loader);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.apk) {
            String url = "http://xzf.197946.com/lianmixx.apk";
            File file = new File(getExternalFilesDir(null) + File.separator + "download" + File.separator + url.substring(url.lastIndexOf("/") + 1));
            String name = "莲秘.apk";
            DownLoadManager.getInstance().addTesk(url,file.getPath(),name);

        }else if (i == R.id.apk1) {
            String url = "http://cr1.197946.com/duoshic.apk";
            File file = new File(getExternalFilesDir(null) + File.separator + "download" + File.separator + url.substring(url.lastIndexOf("/") + 1));
            String name = "多市.apk";
            DownLoadManager.getInstance().addTesk(url,file.getPath(),name);

        } else if (i == R.id.mp4) {
            Log.i(TAG, "onClick: ");
            String url = "http://web.zegukj.com/storage/edd74965e2d370888d26964a376f9b3f.mp4";
            File file = new File(getExternalFilesDir(null) + File.separator + "download" + File.separator + url.substring(url.lastIndexOf("/") + 1));
            String name = "我和我的祖国.mp4";
            DownLoadManager.getInstance().addTesk(url,file.getPath(),name);

        } else if (i == R.id.zip) {
            Log.i(TAG, "onClick: ");
            String url = "http://39.107.247.82:22221/storage/5575ec52263debf029e68da4c32ae692.zip";
            File file = new File(getExternalFilesDir(null) + File.separator + "download" + File.separator + url.substring(url.lastIndexOf("/") + 1));
            String name = "压缩包.zip";
            DownLoadManager.getInstance().addTesk(url,file.getPath(),name);

        } else if (i == R.id.download) {
            startActivity(new Intent(this,DownLoadListActivity.class));
        }
    }
}
