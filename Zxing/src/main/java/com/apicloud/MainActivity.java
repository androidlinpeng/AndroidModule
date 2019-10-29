package com.apicloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.apicloud.code.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    static final int ACTIVITY_REQUEST_CODE_A = 100;
    private static final int RESULT_CAMERA = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissionsAll(PermissionUtils.CAMERA, RESULT_CAMERA)) {
                    Intent intent = new Intent(MainActivity.this, CommonScanActivity.class);
                    intent.putExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_ALL_MODE);
                    startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
                }
            }
        });
        findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissionsAll(PermissionUtils.CAMERA, RESULT_CAMERA)) {
                    Intent intent = new Intent(MainActivity.this, CreateCodeActivity.class);
                    intent.putExtra("gittext", "gittext");
                    startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == ACTIVITY_REQUEST_CODE_A) {
            String result = data.getStringExtra("result");
            if (null != result) {
                Toast.makeText(getApplication(), "" + result, Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean checkPermissionsAll(String type, int code) {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean isAllGranted = PermissionUtils.checkPermissionAllGranted(MainActivity.this, type);
            if (!isAllGranted) {
                PermissionUtils.requestPermissions(MainActivity.this, type, code);
                return false;
            }
        }
        return true;
    }


}
