package com.downloader;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baselibrary.MessageBus;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadConnectListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DownLoadListActivity extends AppCompatActivity {

    private static final String TAG = "DownLoadListActivity";
    private RecyclerView recyclerView;
    private List<TasksManagerModel> modelList = new ArrayList<>();
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load_list);

        if (!FileDownloader.getImpl().isServiceConnected()) {
            FileDownloader.getImpl().bindService();
            registerServiceConnectionListener(new WeakReference<>(this));
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        modelList = DownLoadManager.getInstance().getAllTasks();
        recyclerView.setAdapter(taskAdapter = new TaskAdapter());

        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MessageBus bus) {
        if (bus.getCodeType().equals(MessageBus.msgId_download_pending)) {
            BaseDownloadTask task = (BaseDownloadTask) bus.getParam1();
            int soFarBytes = (int) bus.getParam2();
            int totalBytes = (int) bus.getParam3();
            taskAdapter.pending(task, soFarBytes, totalBytes);
        } else if (bus.getCodeType().equals(MessageBus.msgId_download_started)) {
            BaseDownloadTask task = (BaseDownloadTask) bus.getParam1();
            taskAdapter.started(task);
        } else if (bus.getCodeType().equals(MessageBus.msgId_download_connected)) {
            BaseDownloadTask task = (BaseDownloadTask) bus.getParam1();
            String etag = (String) bus.getParam2();
            boolean isContinue = (boolean) bus.getParam3();
            int soFarBytes = (int) bus.getParam4();
            int totalBytes = (int) bus.getParam5();
            taskAdapter.connected(task, etag, isContinue, soFarBytes, totalBytes);
        } else if (bus.getCodeType().equals(MessageBus.msgId_download_started)) {
            BaseDownloadTask task = (BaseDownloadTask) bus.getParam1();
            taskAdapter.started(task);
        } else if (bus.getCodeType().equals(MessageBus.msgId_download_progress)) {
            BaseDownloadTask task = (BaseDownloadTask) bus.getParam1();
            int soFarBytes = (int) bus.getParam2();
            int totalBytes = (int) bus.getParam3();
            taskAdapter.progress(task, soFarBytes, totalBytes);
        } else if (bus.getCodeType().equals(MessageBus.msgId_download_completed)) {
            BaseDownloadTask task = (BaseDownloadTask) bus.getParam1();
            taskAdapter.completed(task);
        } else if (bus.getCodeType().equals(MessageBus.msgId_download_paused)) {
            BaseDownloadTask task = (BaseDownloadTask) bus.getParam1();
            int soFarBytes = (int) bus.getParam2();
            int totalBytes = (int) bus.getParam3();
            taskAdapter.paused(task, soFarBytes, totalBytes);
        } else if (bus.getCodeType().equals(MessageBus.msgId_download_error)) {
            BaseDownloadTask task = (BaseDownloadTask) bus.getParam1();
            Throwable e = (Throwable) bus.getParam2();
            taskAdapter.error(task, e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterServiceConnectionListener();
    }

    private FileDownloadConnectListener listener;

    private void registerServiceConnectionListener(final WeakReference<DownLoadListActivity> activityWeakReference) {
        if (listener != null) {
            FileDownloader.getImpl().removeServiceConnectListener(listener);
        }

        listener = new FileDownloadConnectListener() {

            @Override
            public void connected() {
                if (activityWeakReference == null
                        || activityWeakReference.get() == null) {
                    return;
                }

                activityWeakReference.get().postNotifyDataChanged();
            }

            @Override
            public void disconnected() {
                if (activityWeakReference == null
                        || activityWeakReference.get() == null) {
                    return;
                }

                activityWeakReference.get().postNotifyDataChanged();
            }
        };

        FileDownloader.getImpl().addServiceConnectListener(listener);
    }

    public void postNotifyDataChanged() {
        if (taskAdapter != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (taskAdapter != null) {
                        taskAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void unregisterServiceConnectionListener() {
        FileDownloader.getImpl().removeServiceConnectListener(listener);
        listener = null;
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskItemViewHolder> {

        private View.OnClickListener taskActionOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() == null) {
                    return;
                }

                TaskItemViewHolder holder = (TaskItemViewHolder) v.getTag();

                CharSequence action = ((TextView) v).getText();
                if (action.equals(v.getResources().getString(R.string.pause))) {
                    // to pause
                    FileDownloader.getImpl().pause(holder.id);
                } else if (action.equals(v.getResources().getString(R.string.start))) {
                    Log.i(TAG, "onClick: ");
                    FileDownloader.getImpl().pause(holder.id);
                    // to start
                    TasksManagerModel model = get(holder.position);
                    DownLoadManager.getInstance().addTesk(model.getUrl(), model.getPath(), model.getName());

                } else if (action.equals(v.getResources().getString(R.string.delete))) {
//                    // to delete
                    new File(get(holder.position).getPath()).delete();
                    holder.taskActionBtn.setEnabled(true);
                    holder.updateNotDownloaded(FileDownloadStatus.INVALID_STATUS, 0, 0);
                    DownLoadManager.getInstance().removeTaskToDB(String.valueOf(holder.id));

                } else if (action.equals(v.getResources().getString(R.string.completed))) {
                    openFile(DownLoadListActivity.this, new File(get(holder.position).getPath()));

                }
            }
        };


        public void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            final TaskItemViewHolder tag = viewHolderSparseArray.get(task.getId());
            if (tag == null) {
                return;
            }
            tag.updateDownloading(FileDownloadStatus.pending, soFarBytes
                    , totalBytes);
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_pending);
        }

        public void started(BaseDownloadTask task) {
            final TaskItemViewHolder tag = viewHolderSparseArray.get(task.getId());
            if (tag == null) {
                return;
            }
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_started);
        }

        public void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
            final TaskItemViewHolder tag = viewHolderSparseArray.get(task.getId());
            if (tag == null) {
                return;
            }
            tag.updateDownloading(FileDownloadStatus.connected, soFarBytes, totalBytes);
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_connected);
        }

        public void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            TaskItemViewHolder tag = viewHolderSparseArray.get(task.getId());
            if (tag == null) {
                return;
            }
            tag.updateDownloading(FileDownloadStatus.progress, soFarBytes, totalBytes);
        }

        public void error(BaseDownloadTask task, Throwable e) {
            final TaskItemViewHolder tag = viewHolderSparseArray.get(task.getId());
            if (tag == null) {
                return;
            }
            tag.updateNotDownloaded(FileDownloadStatus.error, task.getLargeFileSoFarBytes(), task.getLargeFileTotalBytes());
        }

        public void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            final TaskItemViewHolder tag = viewHolderSparseArray.get(task.getId());
            if (tag == null) {
                return;
            }
            tag.updateNotDownloaded(FileDownloadStatus.paused, soFarBytes, totalBytes);
            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_paused);
        }

        public void completed(BaseDownloadTask task) {
            final TaskItemViewHolder tag = viewHolderSparseArray.get(task.getId());
            if (tag == null) {
                return;
            }
            tag.updateDownloaded();
        }

        @NonNull
        @Override
        public TaskItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_down_load, null);
            return new TaskItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final TaskItemViewHolder holder, final int position) {
            final TasksManagerModel model = modelList.get(position);

            holder.update(model.getId(), position);
            holder.taskActionBtn.setTag(holder);
            holder.taskNameTv.setText(model.getName());

            addTaskForViewHolder(holder.id, holder);

            holder.taskActionBtn.setEnabled(true);

            if (isReady()) {
                final int status = getStatus(model.getId(), model.getPath());
                if (status == FileDownloadStatus.pending || status == FileDownloadStatus.started ||
                        status == FileDownloadStatus.connected) {
                    // start task, but file not created yet
                    holder.updateDownloading(status, getSoFar(model.getId()), getTotal(model.getId()));
                } else if (!new File(model.getPath()).exists() &&
                        !new File(FileDownloadUtils.getTempPath(model.getPath())).exists()) {
                    // not exist file
                    holder.updateNotDownloaded(status, 0, 0);
                } else if (isDownloaded(status)) {
                    // already downloaded and exist
                    holder.updateDownloaded();
                } else if (status == FileDownloadStatus.progress) {
                    // downloading
                    holder.updateDownloading(status, getSoFar(model.getId()), getTotal(model.getId()));
                } else {
                    // not start
                    holder.updateNotDownloaded(status, getSoFar(model.getId()), getTotal(model.getId()));
                }
            } else {
                holder.taskStatusTv.setText(R.string.tasks_manager_demo_status_loading);
                holder.taskActionBtn.setEnabled(false);
            }
            holder.taskActionBtn.setOnClickListener(taskActionOnClickListener);
            holder.viewLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ShowConfirmDialog(model, position);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return modelList.size();
        }

        private SparseArray<TaskItemViewHolder> viewHolderSparseArray = new SparseArray<>();

        public void addTaskForViewHolder(int id, final TaskItemViewHolder viewHolder) {
            viewHolderSparseArray.put(id, viewHolder);
        }

        public void removeTaskForViewHolder(final int id) {
            viewHolderSparseArray.remove(id);
        }

        public void releaseTask() {
            viewHolderSparseArray.clear();
        }

        public boolean isReady() {
            return FileDownloader.getImpl().isServiceConnected();
        }

        public TasksManagerModel get(final int position) {
            return modelList.get(position);
        }

        public TasksManagerModel getById(final int id) {
            for (TasksManagerModel model : modelList) {
                if (model.getId() == id) {
                    return model;
                }
            }

            return null;
        }

        public boolean isDownloaded(final int status) {
            return status == FileDownloadStatus.completed;
        }

        public int getStatus(final int id, String path) {
            return FileDownloader.getImpl().getStatus(id, path);
        }

        public long getTotal(final int id) {
            return FileDownloader.getImpl().getTotal(id);
        }

        public long getSoFar(final int id) {
            return FileDownloader.getImpl().getSoFar(id);
        }
    }

    private class TaskItemViewHolder extends RecyclerView.ViewHolder {

        private TextView taskNameTv;
        private TextView taskStatusTv;
        private ProgressBar taskPb;
        private Button taskActionBtn;
        private View viewLayout;

        public TaskItemViewHolder(View itemView) {
            super(itemView);
            taskNameTv = itemView.findViewById(R.id.task_name_tv);
            taskStatusTv = itemView.findViewById(R.id.task_status_tv);
            taskPb = itemView.findViewById(R.id.task_pb);
            taskActionBtn = itemView.findViewById(R.id.task_action_btn);
            viewLayout = itemView.findViewById(R.id.viewLayout);
        }

        private int position;

        private int id;

        public void update(final int id, final int position) {
            this.id = id;
            this.position = position;
        }

        public void updateDownloaded() {
            taskPb.setMax(1);
            taskPb.setProgress(1);

            taskStatusTv.setText(R.string.tasks_manager_demo_status_completed);
            taskActionBtn.setText(R.string.completed);
        }

        public void updateNotDownloaded(final int status, final long sofar, final long total) {
            if (sofar > 0 && total > 0) {
                final float percent = sofar
                        / (float) total;
                taskPb.setMax(100);
                taskPb.setProgress((int) (percent * 100));
            } else {
                taskPb.setMax(1);
                taskPb.setProgress(0);
            }

            switch (status) {
                case FileDownloadStatus.error:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_error);
                    break;
                case FileDownloadStatus.paused:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_paused);
                    break;
                default:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_not_downloaded);
                    break;
            }
            taskActionBtn.setText(R.string.start);
        }

        public void updateDownloading(final int status, final long sofar, final long total) {
            final float percent = sofar
                    / (float) total;
            taskPb.setMax(100);
            taskPb.setProgress((int) (percent * 100));

            switch (status) {
                case FileDownloadStatus.pending:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_pending);
                    break;
                case FileDownloadStatus.started:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_started);
                    break;
                case FileDownloadStatus.connected:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_connected);
                    break;
                case FileDownloadStatus.progress:
                    taskStatusTv.setText(R.string.tasks_manager_demo_status_progress);
                    break;
                default:
                    taskStatusTv.setText(getString(R.string.tasks_manager_demo_status_downloading, status));
                    break;
            }

            taskActionBtn.setText(R.string.pause);
        }
    }

    private final String[][] MIME_MapTable = new String[][]{{".3gp", "video/3gpp"}, {".apk", "application/vnd.android.package-archive"}, {".asf", "video/x-ms-asf"}, {".avi", "video/x-msvideo"}, {".bin", "application/octet-stream"}, {".bmp", "image/bmp"}, {".c", "text/plain"}, {".class", "application/octet-stream"}, {".conf", "text/plain"}, {".cpp", "text/plain"}, {".doc", "application/msword"}, {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"}, {".xls", "application/vnd.ms-excel"}, {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}, {".exe", "application/octet-stream"}, {".gif", "image/gif"}, {".gtar", "application/x-gtar"}, {".gz", "application/x-gzip"}, {".h", "text/plain"}, {".htm", "text/html"}, {".html", "text/html"}, {".jar", "application/java-archive"}, {".java", "text/plain"}, {".jpeg", "image/jpeg"}, {".jpg", "image/jpeg"}, {".js", "application/x-javascript"}, {".log", "text/plain"}, {".m3u", "audio/x-mpegurl"}, {".m4a", "audio/mp4a-latm"}, {".m4b", "audio/mp4a-latm"}, {".m4p", "audio/mp4a-latm"}, {".m4u", "video/vnd.mpegurl"}, {".m4v", "video/x-m4v"}, {".mov", "video/quicktime"}, {".mp2", "audio/x-mpeg"}, {".mp3", "audio/x-mpeg"}, {".mp4", "video/mp4"}, {".mpc", "application/vnd.mpohun.certificate"}, {".mpe", "video/mpeg"}, {".mpeg", "video/mpeg"}, {".mpg", "video/mpeg"}, {".mpg4", "video/mp4"}, {".mpga", "audio/mpeg"}, {".msg", "application/vnd.ms-outlook"}, {".ogg", "audio/ogg"}, {".pdf", "application/pdf"}, {".png", "image/png"}, {".pps", "application/vnd.ms-powerpoint"}, {".ppt", "application/vnd.ms-powerpoint"}, {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"}, {".prop", "text/plain"}, {".rc", "text/plain"}, {".rmvb", "audio/x-pn-realaudio"}, {".rtf", "application/rtf"}, {".sh", "text/plain"}, {".tar", "application/x-tar"}, {".tgz", "application/x-compressed"}, {".txt", "text/plain"}, {".wav", "audio/x-wav"}, {".wma", "audio/x-ms-wma"}, {".wmv", "audio/x-ms-wmv"}, {".wps", "application/vnd.ms-works"}, {".xml", "text/plain"}, {".z", "application/x-compress"}, {".zip", "application/x-zip-compressed"}, {"", "*/*"}};

    public void openFile(Context context, File file) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction("android.intent.action.VIEW");
            String type = this.getMIMEType(file);
            if (Build.VERSION.SDK_INT >= 24) {
                Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                intent.setDataAndType(fileUri, type);
                grantUriPermission(context, fileUri, intent);
            } else {
                intent.setDataAndType(Uri.fromFile(file), type);
            }

            context.startActivity(intent);
        } catch (Exception var6) {
            var6.printStackTrace();
//            Toast.makeText(context, "File corrupted, download again please.", Toast.LENGTH_SHORT).show();
        }

    }

    private static void grantUriPermission(Context context, Uri fileUri, Intent intent) {
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        Iterator var4 = resInfoList.iterator();

        while (var4.hasNext()) {
            ResolveInfo resolveInfo = (ResolveInfo) var4.next();
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

    }

    private String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        } else {
            String end = fName.substring(dotIndex, fName.length()).toLowerCase();
            if (end == "") {
                return type;
            } else {
                for (int i = 0; i < this.MIME_MapTable.length; ++i) {
                    if (end.equals(this.MIME_MapTable[i][0])) {
                        type = this.MIME_MapTable[i][1];
                    }
                }

                return type;
            }
        }
    }

    public void ShowConfirmDialog(final TasksManagerModel model, final int position) {
        final AlertDialog dialog = new AlertDialog.Builder(DownLoadListActivity.this, AlertDialog.THEME_HOLO_DARK).create();
        dialog.setCancelable(true);
        dialog.show();
        Window window = dialog.getWindow();
        window.getDecorView().setBackgroundColor(Color.parseColor("#00000000"));
        window.setContentView(R.layout.view_confirm_dialog_alert);
        TextView tv_content = window.findViewById(R.id.content);
        TextView tvLeft = window.findViewById(R.id.tv_left);
        TextView tvRight = window.findViewById(R.id.tv_right);
        tv_content.setText("确认要删除？");
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileDownloader.getImpl().pause(model.getId());
                new File(model.getPath()).delete();
                DownLoadManager.getInstance().removeTaskToDB(String.valueOf(model.getId()));
                modelList.remove(position);
                taskAdapter.notifyDataSetChanged();
                dialog.cancel();
            }
        });
    }
}
