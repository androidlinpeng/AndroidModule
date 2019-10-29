package com.apicloud;

import android.os.Environment;

import java.io.File;

/**
 * Created by daniel on 15-6-16.
 */
public class FileUtils {

    public static String getTempPath(){
        return Environment.getExternalStorageDirectory() +
                File.separator +
                "amsgcopy" +
                File.separator +
                "codeImage" +
                File.separator;
    }

    public static File createTmpFile(String fileName) {
        if (!isBlank(fileName)) {
            try{
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    File file = new File(getTempPath() + fileName);
                    file.getParentFile().mkdirs();
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    return file;
                }else{
                    return null;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    public static boolean isBlank(String s){
        return (s==null || s.equals("") || s.equals("null"));
    }

    public static void clearTmpFile(){
        File file = new File(getTempPath());
        delete(file);
    }

    private static void delete(File file){
        if (file.isFile()) {
            file.delete();
            return;
        }

        if(file.isDirectory()){
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }
}
