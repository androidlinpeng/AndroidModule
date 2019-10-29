package com.media;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class ProgressView extends Dialog {

    private static final String TAG = "ProgressView";
    private Activity activity;

    public ProgressView(Activity activity) {
        super(activity);
        init(activity);
    }

    public ProgressView(Activity activity, int theme) {
        super(activity, theme);
        init(activity);
    }

    TextView title;

    private void init(Activity activity) {
        this.activity = activity;
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.layout_progress);
        title = findViewById(R.id.tv_progress_dialog);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

    }

    public void updateProgress(final String progress) {
        Log.d(TAG, "updateProgress: " + progress);
        title.setText(progress);
    }

    @Override
    public void show() {//开启
        super.show();
    }

    @Override
    public void dismiss() {//关闭
        super.dismiss();
    }

}
