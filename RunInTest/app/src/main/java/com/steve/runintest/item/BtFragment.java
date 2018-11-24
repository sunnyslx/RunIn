package com.steve.runintest.item;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by steve on 17-10-23.
 */

public class BtFragment extends Fragment {
    private static final String TAG = "BtFragment";
    private static final int BLUETOOTH_ENABLE = 0;
    private static final int BLUETOOTH_DISABLE = 1;

    @BindView(R.id.show_bt_state)
    TextView mState;
    @BindView(R.id.show_bt_mac)
    TextView mMac;
    private Unbinder unbinder;

    private BluetoothAdapter mBluetoothAdapter = null;
    private int mBtOpenCount = 0;
    private int mBtCloseCount = 0;
    private boolean mThreadTag;
    private Thread mThread = null;
    private boolean state;


    private Handler mHandler;

    //测试结果数据
    private static int testTime = 0;
    private String testItem = "Bluetooth";
    private Date startTime;
    private Date endTime;
    private String resultData;

    //更新界面BT状态
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BLUETOOTH_ENABLE:
                    mState.setText(R.string.open);
                    if(mBluetoothAdapter!=null) {
                        try {
                            mMac.setText(mBluetoothAdapter.getAddress());
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                    break;
                case BLUETOOTH_DISABLE:
                    mState.setText(R.string.close);
                    mMac.setText("");
                    break;
            }
        }
    };

    //计时器
    private TimerTask mTimerTask;
    private long curTime;
    private Timer mTimer;
    private boolean isPause=true;

    //初始化timer
    public void initTimer() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (curTime == 0) {
                    endTime = new Date();
                    if (mBtCloseCount > 0 && mBtOpenCount > 0) {
                        resultData = "pass";
                        sendTestResult();
                    }else {
                        resultData = "fail";
                        sendTestResult();
                    }
                } else {
                    curTime -= 1000;//计时器，每次减1秒。
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunInItemActivity itemActivity = (RunInItemActivity) getActivity();
        mHandler = itemActivity.mHandler;

        startTime = new Date();
        curTime=30*1000;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG,"测试结束，发消息！");
            endTime = new Date();
            resultData = "fail";
            //测试失败,系统不支持蓝牙功能
            sendTestResult();
        }
    }

    //发消息更新BT状态
    private void sendMsg(int state) {
        Message msg = new Message();
        msg.what = state;
        if (handler!=null) {
            handler.sendMessage(msg);
        }
    }

    //发送测试结果到测试activity
    private void sendTestResult(){
        testTime++;
        Message msg = new Message();
        msg.what = 108;
        Result result = new Result(testTime, testItem, getTime(startTime), getTime(endTime), getTestTime(startTime, endTime), resultData);
        msg.obj = result;
        if (mHandler!=null) {
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bt, container, false);
        unbinder = ButterKnife.bind(this, view);
        state = true;
        //全屏
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return view;
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
        if (mTimer==null) {
            initTimer();
            mTimer.schedule(mTimerTask, 0, 1000);
            isPause = false;
        }

        mThreadTag = true;
        if (state) {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mThreadTag) {
                        try {
                            if (mBluetoothAdapter!=null) {
                                try {
                                    if (!mBluetoothAdapter.isEnabled()) {
                                        mBluetoothAdapter.enable();
                                        mBtOpenCount++;
                                        sendMsg(BLUETOOTH_ENABLE);
                                    } else {
                                        mBluetoothAdapter.disable();
                                        mBtCloseCount++;
                                        sendMsg(BLUETOOTH_DISABLE);
                                    }
                                }catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            mThread.start();
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

    @Override//取消计时器
    public void onPause() {
        super.onPause();
        if(mTimer!=null){
            mTimer.cancel();
            mTimer = null;
            mTimerTask = null;
        }
        state = false;
    }

    @Override//关闭计时器
    public void onDestroyView() {

        if (mThread != null) {
            state = false;
            mThreadTag = false;
            mThread.interrupt();
            mThread = null;
        }
        if (mBluetoothAdapter!=null){
            if (mBluetoothAdapter.isEnabled()){
                mBluetoothAdapter.disable();
            }
            mBluetoothAdapter=null;
        }

        if (handler!=null){
            handler=null;
        }

        if (unbinder!=null) {
            unbinder.unbind();
        }

        destroyTimer();
        super.onDestroyView();
        quitFullScreen();
    }

    //退出全屏
    private void quitFullScreen() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().setAttributes(lp);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
}
