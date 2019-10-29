package com.downloader;


public interface DownLoadListener {

    /**
     * (开始下载文件)
     *
     * @param model 下载任务对象
     */
    public void onStart(TasksManagerModel model);

    /**
     * (文件下载进度情况)
     *
     * @param model 下载任务对象
     */
    public void onProgress(TasksManagerModel model);

    /**
     * (停止下载完毕)
     *
     * @param model 下载任务对象
     */
    public void onStop(TasksManagerModel model);

    /**
     * (文件下载失败)
     *
     * @param model 下载任务对象
     */
    public void onError(TasksManagerModel model);


    /**
     * (文件下载成功)
     *
     * @param model 下载任务对象
     */
    public void onSuccess(TasksManagerModel model);
}
