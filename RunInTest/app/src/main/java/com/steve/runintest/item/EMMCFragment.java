package com.steve.runintest.item;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.steve.runintest.R;
import com.steve.runintest.Result;
import com.steve.runintest.RunInItemActivity;
import com.steve.runintest.upload.tools.FileOperate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by steve on 17-11-4.
 */

public class EMMCFragment extends Fragment {

    private static final String TAG = "EMMCActivity";
    public String filename = "sdcard.txt";
    private Unbinder mUnbinder;
    @BindView(R.id.tv_writeemmc)
    TextView mWriteEmmc;
    @BindView(R.id.tv_reademmc)
    TextView mReadEmmc;
    @BindView(R.id.write_progress)
    SeekBar mWriteProgress;
    @BindView(R.id.read_progress)
    SeekBar mReadProgress;
    public static final int MSG_ONE = 1;
    public static final int MSG_TWO = 2;
    private static double max;
    private LinearLayout linearLayout;
    double speed1 = 0, speed2 = 0;
    double current_progress = 0, current_progress1 = 0;
    double write = 0;
    double read = 0;
    String strElement;
    private boolean flag;
    private static int testTime = 0;
    private String testItem = "EMMC";
    private Date startTime;
    private Date endTime;
    private String resultData;
    private Handler mHandler;
    private Thread mThread;
    private static boolean control;
    private boolean state;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunInItemActivity itemActivity = (RunInItemActivity) getActivity();
        mHandler = itemActivity.mHandler;

        Log.d(TAG, "onCreate()" + "hgjkgl" + testTime);
        startTime = new Date();
        strElement = "";

        deleteFile(getContext());

        for (int i = 0; i < 50; i++) {
            strElement += "进行UI跟新进行UI跟新进行UI跟新进行UI跟新进行UI跟新进行UI跟新进行UI跟新进行UI跟新进行UI跟新进行UI跟新";
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emmc, container, false);
        Log.d(TAG, "onCreateView Create()...");
        mUnbinder = ButterKnife.bind(this, view);

        //全屏
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        linearLayout = view.findViewById(R.id.check_message);
        //SeekBar不可触摸
        mWriteProgress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        //SeekBar不可触摸
        mReadProgress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        //初始化相关数据
        initData();
        return view;
    }

    private Handler handler;

    //数据初始化
    private void initData() {
        write = 0;
        read = 0;
        speed1 = 0;
        speed2 = 0;
        current_progress = 0;
        current_progress1 = 0;
        max = 50;
        control = true;
        flag = true;
        state = true;
    }

    @Override
    public void onResume() {

        super.onResume();
        Log.d(TAG, "onResume() Create....");

        control = true;
        if (write == 0) {
            mWriteProgress.setProgress(0);
            mReadProgress.setProgress(0);
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //通过消息内容msg.what进行UI跟新
                switch (msg.what) {
                    case MSG_ONE:
                        current_progress = (double) msg.obj;
                        mWriteProgress.setMax(50);
                        int new_progress = (int) current_progress;
                        mWriteProgress.setProgress(new_progress);
                        mWriteEmmc.setText(TranslatePre(speed1));
                        break;
                    case MSG_TWO:
                        mReadProgress.setMax(50);
                        current_progress1 = (double) msg.obj;
                        int new_progress1 = (int) current_progress1;
                        mReadProgress.setProgress(new_progress1);
                        mReadEmmc.setText((TranslatePre(speed2)));
                        break;
                    case 3:
                        linearLayout.setVisibility(View.VISIBLE);
                    default:
                        break;
                }
            }
        };
        Log.d("control:", control + "");
        //防止多个线程混乱操作
        if (state) {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: " + mThread.toString());
                    try {
                        while (flag && control) {
                            // 判断是否有挂载sdcard
                            if (Environment.getExternalStorageState().equals(
                                    Environment.MEDIA_MOUNTED)) {
                                // 得到sdcar文件目录
                                File dir = Environment.getExternalStorageDirectory();
                                File file = new File(dir, filename);
                                FileOutputStream fos = new FileOutputStream(file);
                                FileInputStream fis = new FileInputStream(file);
                                while (write < max && control) {
                                    fos.write(strElement.getBytes());
                                    write = fis.available() / 1024 / 1024;
                                    speed1 = write / max;
                                    Message msg = new Message();
                                    msg.obj = write;
                                    msg.what = MSG_ONE;
                                    //发送
                                    handler.sendMessage(msg);
                                }
                                flag = false;
                                fos.close();
                                fis.close();
                            }
                        }
                        while (!flag && control) {
                            File dir = Environment.getExternalStorageDirectory();
                            File src = new File(dir, filename);
                            if (!src.exists()) {
                                break;
                            }
                            FileInputStream fis = new FileInputStream(src);
                            while (read < max && control) {
                                byte[] bys = new byte[1024];
                                int l = fis.read(bys);
                                read += l / 1024;
                                speed2 = read / max;
                                Thread.sleep(100);
                                Message msg = new Message();
                                msg.obj = read;
                                msg.what = MSG_TWO;
                                //发送
                                handler.sendMessage(msg);
                            }
                            fis.close();

                            //校验开始
                            //显示信息
                            Message message = new Message();
                            message.what = 3;
                            handler.sendMessage(message);
                            FileInputStream fis1 = new FileInputStream(src);
                            //处理校验
                            //将读到的内容存入文件中
                            File director = new File(dir, "read.txt");
                            if (!director.exists()) {
                                director.createNewFile();
                            }
                            FileOutputStream fos = new FileOutputStream(director);
                            byte[] bys1 = new byte[1024];
                            int len = 0;
                            while ((len = fis1.read(bys1)) != -1) {
                                fos.write(bys1, 0, len);
                            }
                            if (FileOperate.compare(src, director)) {

                                //向主发信息
                                Message result_check = new Message();
                                result_check.what = 0x002;
                                if (mHandler!=null) {
                                    mHandler.sendMessage(result_check);
                                }
                                Log.d("文件校验成功","123456");
                                //校验完毕后删除对应文件
                                if(director.exists()){
                                    director.delete();
                                }
                            } else {
                                Log.d("文件校验失败","789456");
                                //向主发信息
                                Message bad_check = new Message();
                                bad_check.what = 0x003;
                                if (mHandler!=null) {
                                    mHandler.sendMessage(bad_check);
                                }
                                endTime = new Date();
                                resultData = "fail";
                                testTime++;
                                Message msg = new Message();
                                msg.what = 104;
                                Result result = new Result(testTime, testItem, getTime(startTime), getTime(endTime), getTestTime(startTime, endTime), resultData);
                                msg.obj = result;
                                if (mHandler!=null) {
                                    mHandler.sendMessage(msg);
                                }
                            }
                            fis1.close();
                            fos.close();
                            flag = true;
                            Log.d(TAG, "测试结束，发消息！");
                            endTime = new Date();
                            resultData = "pass";
                            testTime++;
                            Message msg = new Message();
                            msg.what = 104;
                            Result result = new Result(testTime, testItem, getTime(startTime), getTime(endTime), getTestTime(startTime, endTime), resultData);
                            msg.obj = result;
                            if (mHandler!=null) {
                                mHandler.sendMessage(msg);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mThread.start();
        }
    }

    private String getTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    private String getTestTime(Date start, Date end) {
        return String.valueOf(end.getTime() / 1000 - start.getTime() / 1000);
    }

    public String TranslatePre(double num) {
        NumberFormat num1 = NumberFormat.getPercentInstance();
        return num1.format(num);
    }

    public void deleteFile(Context context) {
        String filesDir = Environment.getExternalStorageDirectory().getPath();
        File f = new File(filesDir, "sdcard.txt");
        if (f.isFile() && f.exists()) {
            f.delete();
            Log.d(TAG, "delete file " + "path/sdcard.txt" + " sucessfully!");
        } else {
            Log.d(TAG, "delete file " + "path/sdcard.txt" + " fail");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        state = false;
        Log.d(TAG, "onPause: create()...");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("onstop():", "Onstop()...");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: " + "已销毁");
        if (mUnbinder != null) {
            mUnbinder = null;
        }
        if (mThread != null) {
            state = false;
            control = false;
            mThread.interrupt();
            mThread = null;
        }
        if (handler != null) {
            handler = null;
        }
        deleteFile(getContext());
        quitFullScreen();
    }

    //退出全屏
    private void quitFullScreen() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().setAttributes(lp);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: " + "已销毁");
    }

    public void change() {
        control = false;
    }
}