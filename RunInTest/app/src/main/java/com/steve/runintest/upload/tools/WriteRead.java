package com.steve.runintest.upload.tools;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by derik on 16-12-9.
 */
public class WriteRead {
    private static final int writeRepeatNum = 10;
    // C8，strRepeatNum不能高于250000，否则将导致内存溢出. 不同平台此值不同
    private static final int strRepeatNum = 10;
    private static final String strArray[] = {"QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm", "0123456789-=[]{};':\",|<>?/`!@#$%^&*()_+"};
    private static final String root = "sdcard/CPUTest";
    private int writeCount = 0;
    private int readCount = 0;
    // 默认路径
    private String fileDir = root;
    private String fileName = "test";
    private long filesCount = 0;
    private Context mContext;

    public WriteRead(Context ctx) {
        mContext = ctx;
    }

    public WriteRead(Context ctx, String fileDir) {
        mContext = ctx;
        this.fileDir = fileDir;
    }

    public WriteRead(Context ctx, String fileDir, String fileName) {
        mContext = ctx;
        this.fileDir = fileDir;
        this.fileName = fileName;
    }

    /**
     * @return Available: 1
     */
    public int sdcardState() {
        // TODO Auto-generated method stub
        int state = 0;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            state = 1;
            File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
            String str_dir = sdDir.toString();
            //Log.i("SDCard", str_dir);
        } else if (Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            state = 0;
            Log.e("", "MEDIA_MOUNTED_READ_ONLY");
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_NOFS)) {
            state = 0;
            Log.e("", "MEDIA_NOFS");
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_SHARED)) {
            state = 0;
            Log.e("", "MEDIA_SHARED");
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_CHECKING)) {
            state = 0;
            Log.e("", "MEDIA_CHECKING");
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_BAD_REMOVAL)) {
            state = 0;
            Log.e("", "MEDIA_BAD_REMOVAL");
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTABLE)) {
            state = 0;
            Log.e("", "MEDIA_UNMOUNTABLE");
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
            state = 0;
            Log.e("", "MEDIA_UNMOUNTED");
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)) {
            state = 0;
            Log.e("", "MEDIA_REMOVED");
        }
        return state;
    }

    /**
     * @return Default String
     */
    private String initStr() {
        StringBuilder str = new StringBuilder("");
        for (int i = 0; i < strArray.length; i++) {
            str.append(strArray[i]);
            str.append("\n");
        }
        return str.toString();
    }

    /**
     * @param arr, String array will be used as element of Text creating, can not be too long.
     * @return Default String
     */
    public String initStr(String[] arr) {
        if (arr != null && arr.length > 0) {
            StringBuilder str = new StringBuilder("");
            for (int i = 0; i < arr.length; i++) {
                str.append(arr[i]);
                str.append("\n");
            }
            return str.toString();
        } else {
            return initStr();
        }

    }

    /**
     * @return boolean
     */
    public boolean write() {
        if (sdcardState() == 0) {
            Log.e("SD state", "There is no sdcard");
            return false;
        }
//        filesCount++;
        String writeText = createText(null);
        File testDir = new File(fileDir);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }

//        File testFile = new File(testDir.getAbsolutePath(), fileName + filesCount + ".txt");
        File testFile = new File(testDir.getAbsolutePath(), fileName + ".txt");
        if (testFile.exists()) {
            testFile.delete();
        }

        try {
            testFile.createNewFile();
//            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(testFile));
            RandomAccessFile randomAccessFile = new RandomAccessFile(testFile, "rw");

//            bufferedWriter.write(writeText);
//            bufferedWriter.flush();
//            bufferedWriter.close();

            writeCount = 0;
            for (int i = 0; i < writeRepeatNum; i++) {
                if (writeCount > writeRepeatNum) {
                    writeCount = 0;
                }
                randomAccessFile.seek(testFile.length());
                randomAccessFile.write(writeText.getBytes());
                writeCount++;
            }
            randomAccessFile.close();
//            Log.i("writeCount", "" + writeCount);
//            Log.i("length", "" + testFile.length());
//            Log.i("write", "finished");
            return true;

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();

        } catch (IOException e2) {
            e2.printStackTrace();

        }
        return false;
    }

    /**
     * @param arr, String array will be used as element of Text creating, can not be too long.
     * @return Use default length create default String if arr is null or empty.
     */
    public String createText(String[] arr) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < strRepeatNum; i++) {
            stringBuilder.append(initStr(arr));
        }

        return stringBuilder.toString();
    }

    /**
     * @param strRepeatNum, the numbers of elements will be created.
     * @param arr,          String array will be used as element of Text creating, can not be too long.
     * @return Create default String if arr is null or empty.
     */
    public String createText(int strRepeatNum, String[] arr) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < strRepeatNum; i++) {
            stringBuilder.append(initStr(arr));
        }

        return stringBuilder.toString();
    }

    /**
     * Delete the files created by test.
     */
    synchronized public void testOver(String url) {
        Log.d("110", "testOver: "+url);
        deleteFolder(url);
    }

    public static void deleteFolder(String url) {
        Log.d("110:","执行文件删除操作");
        try {
            File file1 = new File(url);
            if (file1.exists()) {
                if (file1.isDirectory()) {
                    File[] list = file1.listFiles();
                    if (list != null && list.length > 0) {
                        for (File file : list) {
                            deleteFolder(file.getAbsolutePath());
                        }
                    }
                    file1.delete();
                } else {
                    file1.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
