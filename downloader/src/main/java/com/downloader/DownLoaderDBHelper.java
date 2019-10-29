package com.downloader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.baselibrary.BaseApplication;

public class DownLoaderDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "download.db";
    public static final int DATABASE_VERSION = 1;
    private static volatile DownLoaderDBHelper mInstance;

    public synchronized static DownLoaderDBHelper getInstance() {
        if (mInstance == null) {
            synchronized (DownLoaderDBHelper.class) {
                if (mInstance == null) {
                    mInstance = new DownLoaderDBHelper(BaseApplication.context);
                }
            }
        }
        return mInstance;
    }

    public DownLoaderDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        Map<String, String> allMap = new HashMap<>();
//        allMap.put(TasksManagerModel.ID, "INTEGER PRIMARY KEY");
//        allMap.put(TasksManagerModel.NAME, "VARCHAR");
//        allMap.put(TasksManagerModel.URL, "VARCHAR");
//        allMap.put(TasksManagerModel.PATH, "VARCHAR");
//
//        StringBuffer stringBuffer = new StringBuffer();
//        stringBuffer.append("CREATE TABLE IF NOT EXISTS " + TasksManagerModel.TABLE_NAME);
//        stringBuffer.append("(");
//        int index = 0;
//        int max = allMap.size();
//        for (Map.Entry<String, String> entry : allMap.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue();
//            index++;
//            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
//                continue;
//            }
//            stringBuffer.append(key);
//            stringBuffer.append(" ");
//            stringBuffer.append(value);
//            if (index != max) {
//                stringBuffer.append(",");
//            }
//        }
//        stringBuffer.append(")");
//        Log.i(TAG, "onCreate: "+stringBuffer.toString());
//        db.execSQL(stringBuffer.toString());

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + TasksManagerModel.TABLE_NAME
                + String.format(
                "("
                        + "%s INTEGER PRIMARY KEY, " // id, download id
                        + "%s VARCHAR, " // name
                        + "%s VARCHAR, " // url
                        + "%s VARCHAR " // path
                        + ")"
                , TasksManagerModel.ID
                , TasksManagerModel.NAME
                , TasksManagerModel.URL
                , TasksManagerModel.PATH

        ));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
