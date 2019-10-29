package com.downloader;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.baselibrary.MessageBus;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class DownLoadManager {
    private static DownLoadManager mInstance;
    public static DownLoadManager getInstance() {
        if (mInstance == null) {
            synchronized (DownLoadManager.class) {
                if (mInstance == null) {
                    mInstance = new DownLoadManager();
                }
            }
        }
        return mInstance;
    }

    public List<TasksManagerModel> getAllTasks() {
        final Cursor c = DownLoaderDBHelper.getInstance().getWritableDatabase().rawQuery("SELECT * FROM " + TasksManagerModel.TABLE_NAME, null);
        final List<TasksManagerModel> list = new ArrayList<>();
        try {
            if (!c.moveToLast()) {
                return list;
            }
            do {
                TasksManagerModel model = new TasksManagerModel();
                model.setId(c.getInt(c.getColumnIndex(TasksManagerModel.ID)));
                model.setName(c.getString(c.getColumnIndex(TasksManagerModel.NAME)));
                model.setUrl(c.getString(c.getColumnIndex(TasksManagerModel.URL)));
                model.setPath(c.getString(c.getColumnIndex(TasksManagerModel.PATH)));
                list.add(model);
            } while (c.moveToPrevious());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    public BaseDownloadTask addTesk(String url, String path,String name) {
        BaseDownloadTask task = FileDownloader.getImpl().create(url)
                .setPath(path)
                .setCallbackProgressTimes(1000)
                .setListener(taskDownloadListener);
        task.start();
        addTaskToDB(url, path,name);
        return task;
    }

    public TasksManagerModel addTaskToDB(final String url, final String path, final String name) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(path)) {
            return null;
        }

        // have to use FileDownloadUtils.generateId to associate TasksManagerModel with FileDownloader
        final int id = FileDownloadUtils.generateId(url, path);

        TasksManagerModel model = new TasksManagerModel();
        model.setId(id);
        model.setName(name);
        model.setUrl(url);
        model.setPath(path);
        Cursor cur = DownLoaderDBHelper.getInstance().getWritableDatabase().rawQuery("SELECT * FROM " + TasksManagerModel.TABLE_NAME + " WHERE id='" + id + "'", null);
        if (!cur.moveToNext()) {
            final boolean succeed = DownLoaderDBHelper.getInstance().getWritableDatabase().insert(TasksManagerModel.TABLE_NAME, null, model.toContentValues()) != -1;
            return succeed ? model : null;
        }else {
            return model;
        }
    }

    public void removeTaskToDB(String id) {
        DownLoaderDBHelper.getInstance().getWritableDatabase().delete(TasksManagerModel.TABLE_NAME, TasksManagerModel.ID + " = ?", new String[]{id});
    }

    FileDownloadListener taskDownloadListener = new FileDownloadListener() {

        MessageBus.Builder builder = new MessageBus.Builder();

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            MessageBus messageBus = builder
                    .codeType(MessageBus.msgId_download_pending)
                    .param1(task)
                    .param2(soFarBytes)
                    .param3(totalBytes)
                    .build();
            EventBus.getDefault().post(messageBus);
        }

        @Override
        protected void started(BaseDownloadTask task) {
            super.started(task);
            MessageBus messageBus = builder
                    .codeType(MessageBus.msgId_download_started)
                    .param1(task)
                    .build();
            EventBus.getDefault().post(messageBus);
        }

        @Override
        protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
            super.connected(task, etag, isContinue, soFarBytes, totalBytes);
            MessageBus messageBus = builder
                    .codeType(MessageBus.msgId_download_connected)
                    .param1(task)
                    .param2(etag)
                    .param3(isContinue)
                    .param4(soFarBytes)
                    .param5(totalBytes)
                    .build();
            EventBus.getDefault().post(messageBus);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            int percent = (int) ((double) soFarBytes / (double) totalBytes * 100);
            MessageBus messageBus = builder
                    .codeType(MessageBus.msgId_download_progress)
                    .param1(task)
                    .param2(soFarBytes)
                    .param3(totalBytes)
                    .build();
            EventBus.getDefault().post(messageBus);
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            MessageBus messageBus = builder
                    .codeType(MessageBus.msgId_download_completed)
                    .param1(task)
                    .build();
            EventBus.getDefault().post(messageBus);
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            MessageBus messageBus = builder
                    .codeType(MessageBus.msgId_download_paused)
                    .param1(task)
                    .param2(soFarBytes)
                    .param3(totalBytes)
                    .build();
            EventBus.getDefault().post(messageBus);
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            MessageBus messageBus = builder
                    .codeType(MessageBus.msgId_download_completed)
                    .param1(task)
                    .param2(e)
                    .build();
            EventBus.getDefault().post(messageBus);
        }

        @Override
        protected void warn(BaseDownloadTask task) {
            continueDownLoad(task);//如果存在了相同的任务，那么就继续下载
            MessageBus messageBus = builder
                    .codeType(MessageBus.msgId_download_warn)
                    .param1(task)
                    .build();
            EventBus.getDefault().post(messageBus);
        }
    };

    private void continueDownLoad(BaseDownloadTask task) {
        while (task.getSmallFileSoFarBytes() != task.getSmallFileTotalBytes()) {
            int percent = (int) ((double) task.getSmallFileSoFarBytes() / (double) task.getSmallFileTotalBytes() * 100);
        }
    }

}
