package com.steve.runintest;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by steve on 17-11-15.
 */

public class TestResultToFile {
    private static final String TAG = "TestResultToFile";
    private static final String TEST_ITEM = "Test item";
    private static final String TEST_TIME = "Testing frequency";
    private static final String TEST_START = "Starting time";
    private static final String TEST_END = "End time";
    private static final String TEST_DURATION = "Test duration";
    private static final String TEST_RESULT = "Test Results";
    private static final String FILE_NAME="fcl_upload/";
    public static String SN;

    public static String getFileName(){
        return SN+"_test.txt";
    }

    public static void writeFile(String item,
                                 String time, String start, String end,
                                 String duration, String result) {
        try {
            String filesDir = Environment.getExternalStorageDirectory().getPath();
            File file=new File(filesDir,"fcl_upload");
            if (!file.exists()){
                file.mkdir();
            }
            File f = new File(file, getFileName());
            //String path = "/data/data/com.steve.runintest/test.txt";
            if (!f.exists()){
                f.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(f,true);
            String data ="/**********TestItem**********/"+"\r\n"+
                    TEST_ITEM + ":" + item + "\r\n" +
                            TEST_TIME + ":" + time + "\r\n" +
                            TEST_START + ":" + start + "\r\n" +
                            TEST_END + ":" + end + "\r\n" +
                            TEST_DURATION + ":" + duration + "\r\n" +
                            TEST_RESULT + ":" + result+"\r\n";
            fos.write(data.getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile() {
        String filesDir=Environment.getExternalStorageDirectory().getPath();
        File file=new File(filesDir,"fcl_upload/"+getFileName());
        if (file.exists()){
            file.delete();
            Log.d(TAG, "delete file " + FILE_NAME + " sucessfully!");
        }
    }
}
