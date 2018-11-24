package com.steve.runintest.item;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.steve.runintest.R;
import com.steve.runintest.Result;
import com.steve.runintest.RunInItemActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LCDFragment extends Fragment {
    private static final String TAG = "LCDFragment";
    private static final int SCREEN_BRIGHT_LOW = 0;
    private static final int SCREEN_BRIGHT_MIDDLE = 1;
    private static final int SCREEN_BRIGHT_HIGH = 2;
    private static final int SCREEN_BRIGHT_CURRENT = 4;
    private static final int SCREEN_RG = 5;

    @BindView(R.id.change_color) TextView changeColor;
    private Unbinder unbinder;

    //颜色，亮度相关参数
    private int[] colors;
    private int mCurrentBright;
    private int mState;
    private int mCurrentIndex;

    //测试结果数据
    private static int testTime = 0;
    private String testItem = "LCD";
    private Date startTime;
    private Date endTime;
    private String resultData;
    private Handler mHandler;

    //计时器
    private TimerTask mTimerTask;
    private long curTime;
    private Timer mTimer;
    private boolean isPause=true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        RunInItemActivity itemActivity = (RunInItemActivity) getActivity();
        mHandler = itemActivity.mHandler;

        //setRetainInstance(true);

        startTime = new Date();

        colors = new int[]{Color.RED, Color.GREEN, Color.BLUE,Color.BLACK,Color.GRAY};
        mCurrentBright=0;
        mState=0;
        mCurrentIndex=0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lcd, container, false);
        unbinder = ButterKnife.bind(this, view);

        curTime=9*1000;
        //全屏
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return view;
    }

    //初始化timer，定时发消息
    public void initTimer() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (curTime == 0) {
                    Log.d(TAG,"测试结束，发消息！");
                    endTime = new Date();
                    resultData = "pass";
                    testTime++;
                    Message msg = new Message();
                    msg.what = 106;
                    Result result = new Result(testTime, testItem, getTime(startTime), getTime(endTime), getTestTime(startTime, endTime), resultData);
                    msg.obj = result;
                    if (mHandler!=null) {
                        mHandler.sendMessage(msg);
                    }
                } else {
                    curTime -= 1000;//计时器，每次减一秒。
                    if (mState == 0) {
                        Log.d(TAG,mCurrentIndex+"当前颜色索引");
                        Message msg = new Message();
                        msg.what = SCREEN_RG;
                        handler.sendMessage(msg);
                    } else if (mState == 1) {
                        Log.d(TAG,mCurrentBright+"当前亮度索引");
                        if (mCurrentBright == 0) {
                            sendMsg(SCREEN_BRIGHT_LOW, 85);
                        } else if (mCurrentBright == 85) {
                            sendMsg(SCREEN_BRIGHT_MIDDLE, 170);
                        } else if (mCurrentBright == 170) {
                            sendMsg(SCREEN_BRIGHT_HIGH, 255);
                        } else if (mCurrentBright == 255) {
                            sendMsg(SCREEN_BRIGHT_CURRENT, 0);
                        }
                    }
                }
            }
        };
        mTimer = new Timer();
    }

    //销毁计时器
    public void destroyTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    @Override//开始计时
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (curTime != 0 && isPause) {
            destroyTimer();
            initTimer();
            mTimer.schedule(mTimerTask, 0, 1000);
            isPause = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if (mTimer==null) {
            initTimer();
            mTimer.schedule(mTimerTask, 0, 1000);
            isPause = false;
        }
    }

    //格式化时间
    private String getTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    //运行时间
    private String getTestTime(Date start, Date end) {
        return String.valueOf(end.getTime() / 1000 - start.getTime() / 1000);
    }

    //发消息改变屏幕亮度
    private void sendMsg(int state, int a) {
        Message msg = new Message();
        msg.what = state;
        msg.arg1 = a;
        handler.sendMessage(msg);
    }

    //根据接收消息更新界面
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCREEN_RG:
                    changeColor.setBackgroundColor(colors[mCurrentIndex++]);
                    if (mCurrentIndex > 4) {
                        mState = 1;
                        mCurrentIndex = 0;
                    }
                    break;
                case SCREEN_BRIGHT_LOW:
                    changeScreen(msg, 0);
                    changeColor.setBackgroundColor(Color.WHITE);
                    break;
                case SCREEN_BRIGHT_MIDDLE:
                    changeScreen(msg, 85);
                    break;
                case SCREEN_BRIGHT_HIGH:
                    changeScreen(msg, 170);
                    break;
                case SCREEN_BRIGHT_CURRENT:
                    changeScreen(msg, 255);
                    mState = 0;
                    break;
            }
        }
    };

    @Override//取消定时
    public void onPause() {
        super.onPause();
        if (mTimer!=null){
            mTimer.cancel();
            mTimer = null;
            mTimerTask = null;
        }
    }

    //handler屏幕亮度处理逻辑
    private void changeScreen(Message msg, int bright) {
        mCurrentBright = msg.arg1;
        changeAppBrightness(bright);
    }

    @Override//结束退屏及关闭计时器
    public void onDestroyView() {
        super.onDestroyView();
//        unbinder.unbind();
        quitFullScreen();
        destroyTimer();
    }

    //退出全屏
    private void quitFullScreen() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().setAttributes(lp);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    //屏幕亮度调节
    public void changeAppBrightness(int brightness) {
        Window window = getActivity().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
        }
        window.setAttributes(lp);
    }
}