package com.media.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatDialog;

import com.media.ProgressView;
import com.media.R;

public class LoadingManager {

    private static AppCompatDialog loadingDialog = null;

    public static void showLoadingDialog(Activity activity) {
        showLoadingDialog(activity, null);
    }

    public static void showLoadingDialog(final Activity activity, final String msg) {
        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingDialog = null;
                    if (msg == null) {
                        loadingDialog = DialogManager.createLoadingDialog(activity);
                    } else {
                        loadingDialog = DialogManager.createLoadingDialog(activity, msg);
                    }
                    loadingDialog.show();
                    loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void hideLoadingDialog(Activity activity) {
        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                        loadingDialog = null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ProgressView progressView;

    public static void showProgress(final Activity activity, final String progress) {
        progressView = null;
        progressView = new ProgressView(activity, R.style.LoadingDialogTheme);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressView.show();
                progressView.updateProgress(progress);
            }
        });

    }

    public static void hideProgress(Activity activity) {
        if (progressView != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressView.dismiss();
                    progressView = null;
                }
            });
        }
    }

    public static void updateProgress(final Activity activity, final String progress) {
        if (progressView != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressView.updateProgress(progress);
                }
            });
        }
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public static void OnDismissListener(Activity activity, final OnDismissListener listener) {
        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (listener != null) {
                                    listener.onDismiss();
                                }
                            }
                        });
                    }
                    if (progressView !=null && progressView.isShowing()){
                        progressView.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (listener != null) {
                                    listener.onDismiss();
                                }
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
