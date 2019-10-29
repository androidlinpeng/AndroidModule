package com.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

public class DownLoader {

    private DownLoadListener downLoadListener;
    private TasksManagerModel downLoaderModel;
    private ThreadPoolExecutor pool;
    private DownLoadThread downLoadThread;

    public DownLoader(TasksManagerModel downLoaderModel, ThreadPoolExecutor pool) {
        this.downLoaderModel = downLoaderModel;
        this.pool = pool;
    }

    public void setDownLoadListener(DownLoadListener listener) {
        downLoadListener = listener;
    }

    public void startTask() {
        if (downLoadThread == null) {
            downLoadThread = new DownLoadThread();
            pool.execute(downLoadThread);
        }
    }

    private class DownLoadThread extends Thread {

        private URL url;
        private RandomAccessFile loadFile;
        private HttpURLConnection urlConn;
        private InputStream inputStream;

        @Override
        public void run() {
            super.run();

            try {
                url = new URL(downLoaderModel.getUrl());
                url.openConnection();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
