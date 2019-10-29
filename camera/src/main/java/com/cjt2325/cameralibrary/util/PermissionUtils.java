package com.cjt2325.cameralibrary.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

/**
 * Created by Administrator on 2017/9/1.
 */

public class PermissionUtils {

    public static String CAMERA = "CAMERA";

    public static String[] camera = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    /**
     * 检查是否有相应的权限
     *
     * @param context
     * @return
     */
    public static boolean checkPermissionAllGranted(Context context, String type) {

        if (type.equals(CAMERA)) {
            for (String permission : camera) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 请求权限
     *
     * @param activity
     * @param code
     */
    public static void requestPermissions(Activity activity, String type, int code) {

        if (type.equals(CAMERA)) {
            ActivityCompat.requestPermissions(activity, camera, code);
        }

    }

    public static boolean checkPermissionGranted(Activity activity, String type) {

        if (type.equals(CAMERA)) {
            for (String permission : camera) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    // 只要有一个权限没有被授予, 则直接返回 false
                    requestPermission(activity, permission);
                    return false;
                }
            }
        }

        return true;
    }

    public static void requestPermission(Activity activity, String permission) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
    }

    public interface OnClickListener {

        void onConfirmClicked();

        void onCancelClicked();

    }

    private static OnClickListener onClickListener = null;

    public void setOnClickListener(OnClickListener l) {
        this.onClickListener = l;
    }

    /**
     * 打开 APP 的详情设置
     */
    public static void openAppDetails(final Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("\nApp需要您的" + message + "权限，您可以到 “应用信息”>“权限管理”中配置权限。");
        builder.setPositiveButton("授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    /**
     * 打开 APP 的详情设置
     */
    public static void openAppDetailsInit(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("权限申请");
        builder.setMessage("储存权限、手机信息权限为必选项，全部开通才可以正常使用APP \n\n您可以到 “应用信息”>“权限管理”中配置权限。");
        builder.setPositiveButton("授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                activity.startActivity(intent);
                onClickListener.onConfirmClicked();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onClickListener.onCancelClicked();
            }
        });
        builder.show();
    }

}
