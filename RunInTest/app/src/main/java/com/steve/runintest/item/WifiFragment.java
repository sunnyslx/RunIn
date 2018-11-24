package com.steve.runintest.item;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.steve.runintest.R;
import com.steve.runintest.Result;
import com.steve.runintest.RunInItemActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by steve on 17-10-25.
 */

public class WifiFragment extends Fragment {
    private static final String TAG = "WifiFragment";
    private static final int WIFI_ENABLE = 0;
    private static final int WIFI_DISABLE = 1;

    @BindView(R.id.show_wifi_state)
    TextView mState;
    @BindView(R.id.show_wifi_mac)
    TextView mMac;
    @BindView(R.id.show_wifi_timer)
    TextView mShowDownTimer;
    @BindView(R.id.list)
    ListView mListView;
    private Unbinder unbinder;

    private WifiManager mWifiManager;
    private List<ScanResult> mList;//wifi扫描结果（周围wifi）
    private boolean state;

    private boolean mThreadTag;
    private Thread mThread = null;
    //wifi开关次数
    private int mWifiOpenCount = 0;
    private int mWifiCloseCount = 0;

    //测试结果数据
    private static int testTime = 0;
    private String testItem = "WIFI";
    private Date startTime;
    private Date endTime;
    private String resultData;

    //往测试界面发消息
    private Handler mHandler;

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
                Log.d(TAG, "run: "+curTime);
                if (curTime == 0) {
                    endTime = new Date();
                    if (mWifiCloseCount > 0 && mWifiOpenCount > 0) {
                        resultData = "pass";
                        sendTestResult();
                    }else {
                        resultData = "fail";
                        sendTestResult();
                    }
                } else {
                    curTime -= 1000;//计时器，每次减五秒。
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

    //开关wifi更新界面
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
                switch (msg.what) {
                    case WIFI_ENABLE:
                        mState.setText(R.string.open);
                        if(mWifiManager!=null) {
                            try{
                            mMac.setText(mWifiManager.getConnectionInfo().getMacAddress());
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }
                        mListView.setVisibility(View.VISIBLE);
                        break;
                    case WIFI_DISABLE:
                        mState.setText(R.string.close);
                        mMac.setText("");
                        mListView.setVisibility(View.GONE);
                        break;
                }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取测试activity中handler
        RunInItemActivity itemActivity = (RunInItemActivity) getActivity();
        mHandler = itemActivity.mHandler;

        startTime = new Date();
        curTime=30*1000;
        state = true;

        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(getActivity().WIFI_SERVICE);
        if (mWifiManager == null) {
            Log.d(TAG,"测试结束，发消息！");
            endTime = new Date();
            resultData = "fail";

            Log.d(TAG, "do not support wifi");
            //测试失败,系统不支持wifi功能
            sendTestResult();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi, container, false);
        unbinder = ButterKnife.bind(this, view);
//        mList = mWifiManager.getScanResults();
//        MyAdapter adapter = new MyAdapter(getActivity(), mList);
//        mListView.setAdapter(adapter);

        //全屏
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return view;
    }

    @Override
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
        if(mWifiManager!=null){
            try {
                mList = mWifiManager.getScanResults();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        MyAdapter adapter = new MyAdapter(getActivity(), mList);
        mListView.setAdapter(adapter);
        mThreadTag = true;
        if(state) {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mThreadTag) {
                        try {
                            if (mWifiManager != null) {
                                try {
                                    if (!mWifiManager.isWifiEnabled()) {
                                        mWifiManager.setWifiEnabled(true);
                                        mWifiOpenCount++;
                                        sendMsg(WIFI_ENABLE);
                                        Thread.sleep(10000);
                                    }
                                    if (mWifiManager.isWifiEnabled()) {
                                        mWifiManager.setWifiEnabled(false);
                                        mWifiCloseCount++;
                                        sendMsg(WIFI_DISABLE);
                                        Thread.sleep(5000);
                                    }
                                }catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            mThread.start();
        }
    }

    //发消息到当前，用于更新UI
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
        msg.what = 107;
        Result result = new Result(testTime, testItem, getTime(startTime), getTime(endTime), getTestTime(startTime, endTime), resultData);
        msg.obj = result;
        if (mHandler!=null) {
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mTimer!=null){
            mTimer.cancel();
            mTimer = null;
            mTimerTask = null;
        }
        state = false;
    }

    //格式化日期
    private String getTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    //测试时间
    private String getTestTime(Date start, Date end) {
        return String.valueOf(end.getTime() / 1000 - start.getTime() / 1000);
    }

    //取消计时，关闭线程
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyTimer();
        if (handler!=null){
            handler=null;
        }
//        unbinder.unbind();
        Log.d(TAG,"WWWWWWWWIIIIIIFFFFFIIII");
        close();
        quitFullScreen();
    }

    //退出全屏
    private void quitFullScreen() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().setAttributes(lp);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    //关闭线程
    private void close() {
        if (mThread != null) {
            mThreadTag = false;
            mThread.interrupt();
            mThread = null;
        }

        if (mWifiManager != null) {
            if (!mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(true);
            }
        }

        if (mWifiManager != null) {
            mWifiManager = null;
        }
        state = false;
    }
}

//listview的adapter周边wifi
class MyAdapter extends BaseAdapter {
    LayoutInflater inflater;
    List<ScanResult> list;

    public MyAdapter(Context context, List<ScanResult> list) {
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View view1 = null;
        if (view == null) {
            view1 = inflater.inflate(R.layout.fragment_wifi_item, null);
        } else {
            view1 = view;
        }
        ScanResult scanResult = list.get(i);
        TextView textView = view1.findViewById(R.id.show_wifi_name);
        textView.setText(scanResult.SSID);
        return view1;
    }
}
