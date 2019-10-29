package com.cjt2325.cameralibrary;

import android.app.Activity;
import android.app.Dialog;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;



/**
 * Created by Administrator on 2017/9/15.
 */

public class DialogManager {

    private static final String TAG = "DialogManager";

    public static Dialog createLoadingDialog(Activity act) {
        return createLoadingDialog(act, "");
    }

    public static Dialog createLoadingDialog(Activity act, String content) {
        if (null != act) {
            Dialog appCompatDialog = new Dialog(act, R.style.LoadingDialogTheme);
            View view = act.getLayoutInflater().inflate(R.layout.layout_loading, null);
            appCompatDialog.setContentView(view);
            TextView title = (TextView) view.findViewById(R.id.content);
            ImageView progress = (ImageView) view.findViewById(R.id.progress);
            if (!TextUtils.isEmpty(content)) {
                title.setText("" + content);
            }
            Animation antv = AnimationUtils.loadAnimation(act, R.anim.loading_progressbar);
            LinearInterpolator lin = new LinearInterpolator();
            antv.setInterpolator(lin);
            antv.setRepeatCount(-1);
            progress.startAnimation(antv);
            appCompatDialog.setCanceledOnTouchOutside(false);
            return appCompatDialog;
        } else {
            return null;
        }
    }

}
