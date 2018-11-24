package com.steve.runintest;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.steve.runintest.db.TestResult;
import com.steve.runintest.db.TestResultLab;
import com.steve.runintest.item.BatteryFragment;
import com.steve.runintest.item.BtFragment;
import com.steve.runintest.item.CPUFragment;
import com.steve.runintest.item.DDRFragment;
import com.steve.runintest.item.EMMCFragment;
import com.steve.runintest.item.LCDFragment;
import com.steve.runintest.item.ThreeDFragment;
import com.steve.runintest.item.TwoDFragment;
import com.steve.runintest.item.VedioFragment;
import com.steve.runintest.item.WifiFragment;
import com.steve.runintest.upload.StartActivity;
import com.steve.runintest.utils.AutoConnectWifi;
import com.steve.runintest.utils.TimeTools;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by steve on 17-10-23.
 */

public class RunInItemActivity extends FragmentActivity {

    private static final String TAG = "RunInItemActivity";
    private static final int WHAT = 123;

    //浮动窗参数
    private View view;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    //替换fragment相关参数
    private int index = 0;
    private int id;//单项测试需要

    //测试时间
    private long testTotalTime;//总时间
    private TextView downTime;
    private List<Fragment> mFragments;
    private long startTime;//开始
    private TestResultLab mTestResultLab;

    //计时器
    private TimerTask mTimerTask;
    private long curTime;
    private Timer mTimer;
    private boolean isPause = true;


    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == WHAT) {
                long sRecLen = (long) msg.obj;
                //toClock()———— 毫秒换成00:00:00格式的方式。
                if (sRecLen>0) {
                    downTime.setText(TimeTools.toClock(sRecLen));
                }
            }

            if (msg != null) {
                switch (msg.what) {
                    case 100:
                        handleMsg(msg);
                        break;
                    case 101:
                        handleMsg(msg);
                        break;
                    case 102:
                        handleMsg(msg);
                        break;
                    case 103:
                        handleMsg(msg);
                        break;
                    case 104:
                        handleMsg(msg);
                        break;
                    case 105:
                        handleMsg(msg);
                        break;
                    case 106:
                        handleMsg(msg);
                        break;
                    case 107:
                        handleMsg(msg);
                        break;
                    case 108:
                        handleMsg(msg);
                        break;
                    case 109:
                        handleMsg(msg);
                        break;
                    case 0x001:
                        Toast.makeText(getApplicationContext(), "文件校验中...", Toast.LENGTH_LONG).show();
                        break;
                    case 0x002:
                        Toast.makeText(getApplicationContext(), "文件校验成功", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x003:
                        Toast.makeText(getApplicationContext(), "文件校验失败", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    //接收测试项结果，并保存，之后进行下一条测试项
    private void handleMsg(Message msg) {
        Log.d(TAG, msg.toString());
        Result result = (Result) msg.obj;

        if (!mTestResultLab.isTestItem(result.getTestItem())) {//将测试项的结果保存至数据库，用于回显
            Log.d(TAG, "insert");
            mTestResultLab.addTestResult(new TestResult(result.getTestItem(), result.getResultData()));
        } else {
            Log.d(TAG, "update");
            mTestResultLab.updateResult(new TestResult(result.getTestItem(), result.getResultData()));
        }

        //保存测试数据到文件
        TestResultToFile.writeFile(result.getTestItem(),
                result.getTestTime() + "", result.getStartTime(), result.getEndTime(),
                result.getRunTime(), result.getResultData());

        nextTestItem();//下一项测试判断
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //监控开始
//        LogcatHelper.getInstance(getApplicationContext()).start();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常亮
        setContentView(R.layout.activity_item_run_in);

        try {
            AutoConnectWifi autoConnectWifi = new AutoConnectWifi(RunInItemActivity.this.getApplicationContext());
            autoConnectWifi.openWifi();
            autoConnectWifi.addNetwork(autoConnectWifi.createWifiInfo("D-Link_DIR-809", "Aa123456", 3));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        createFloatWindow();//加载浮动窗
        //获取测试总时间
        Intent intent = getIntent();
        testTotalTime = intent.getIntExtra(RunInActivity.TEST_ITEM_TIME, 0);//秒
//        testTotalTime = 180;
        id = intent.getIntExtra("id", 0);
        Log.d(TAG, testTotalTime + "");
        curTime = testTotalTime * 1000;
//        curTime = 2 * 60 * 1000;
        startTime = 0;
        //       curTime=45*1000;
        mTestResultLab = TestResultLab.get(getApplicationContext());//将测试项名称和结果保存至数据库的封装类对象
    }

    //初始化timer,每隔1秒发一次消息，更新计时消息
    public void initTimer() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (curTime <= 0) {
                    curTime = 0;
                    try {
                        windowManager.removeView(view);
                    }catch (IllegalArgumentException e){
                        e.printStackTrace();
                    }catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                    //停止log
//                    LogcatHelper.getInstance(getApplicationContext()).stop();
                    //结束
                    Intent intent = new Intent(RunInItemActivity.this, StartActivity.class);//跳至服务器上传文件界面
                    startActivity(intent);
                    RunInItemActivity.this.finish();
                } else {
                    curTime -= 1000;//计时器，每次减一秒。
                    Message message = new Message();
                    message.what = WHAT;
                    message.obj = curTime;
                    if (mHandler!=null) {
                        mHandler.sendMessage(message);
                    }
                }
                startTime += 1;//进行时计时器，每次+1秒
            }
        };
        mTimer = new Timer();
    }

    @Override//定时加替换fragment
    protected void onResume() {
        super.onResume();

        if (curTime != 0 && isPause) {
            destroyTimer();
            initTimer();
            mTimer.schedule(mTimerTask, 0, 1000);
            isPause = false;
        }

        mFragments = RunInActivity.getFragments();
//        Log.d(TAG, mFragments.size() + "");
//        Log.d(TAG, mFragments.get(index).toString());
        if (mFragments.size() > 0) {
            replaceFragment(mFragments.get(index));
        }
    }

    //进入下个测试项判断条件
    private void nextTestItem() {
        if (mFragments.size() > 0) {
            Log.d(startTime + ":", testTotalTime + "");
            if (mFragments.size() == 1) {
                if (startTime < testTotalTime) {
                    replaceFragment(createFragment(id));
                }
            } else {
                if (startTime < testTotalTime) {
                    index++;
                    // If have no more item then skip result view
                    if (index >= mFragments.size()) {
                        index = 0;
                        replaceFragment(mFragments.get(index));
                    } else {
                        replaceFragment(mFragments.get(index));
                    }
                }
            }
        }
    }

    //替换fragment
    private void replaceFragment(Fragment newFragment) {
        try {
            Log.d(TAG, newFragment.toString());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_replace, newFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);//简单地淡入或淡出
            ft.commit();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    //创建浮动窗
    private void createFloatWindow() {
        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.float_window, null);
        downTime = view.findViewById(R.id.remain_time);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.alpha = 0.3f;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowManager.addView(view, layoutParams);
    }

    @Override//定时器暂停
    protected void onPause() {
        super.onPause();

//        windowManager.removeView(view);
        if (!isPause && mTimer != null) {
            isPause = true;
            mTimer.cancel();
        }
    }

    @Override//后退处理，关闭浮动窗，取消计时器，清空list，关闭当前activity
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        try {
            EMMCFragment fragment = (EMMCFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_replace);
            if(fragment!=null) {
               try {
                   fragment.change();
               }catch (NullPointerException e){
                   e.printStackTrace();
               }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
        windowManager.removeView(view);
        destroyTimer();
        mFragments.clear();
        this.finish();
    }

    @Override//清空list
    protected void onDestroy() {
        super.onDestroy();
        mFragments.clear();
    }

    //单项测试每次都返回新的fragment对象
    private Fragment createFragment(int index) {
        Fragment fragment;
        switch (index) {
            case 0:
                fragment = new VedioFragment();
                break;
            case 1:
                fragment = new TwoDFragment();
                break;
            case 2:
                fragment = new ThreeDFragment();
                break;
            case 3:
                fragment = new CPUFragment();
                break;
            case 4:
                fragment = new EMMCFragment();
                break;
            case 5:
                fragment = new DDRFragment();
                break;
            case 6:
                fragment = new LCDFragment();
                break;
            case 7:
                fragment = new WifiFragment();
                break;
            case 8:
                fragment = new BtFragment();
                break;
            default:
                fragment = new BatteryFragment();
                break;
        }
        return fragment;
    }
}
