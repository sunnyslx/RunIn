package com.steve.runintest.item;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.steve.runintest.R;
import com.steve.runintest.Result;
import com.steve.runintest.RunInItemActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by steve on 17-11-4.
 */

public class BatteryFragment extends Fragment {

    private int BatteryN;       //目前电量
    private int BatteryV;       //电池电压
    private double BatteryT;        //电池温度
    private String BatteryStatus;   //电池状态
    private String BatteryTemp;     //电池使用情况
    private String BatteryType;     //电池类型
    private String BatteryCharging; //充电类型
    @BindView(R.id.health)
    TextView text_health;
    @BindView(R.id.state)
    TextView text_state;
    @BindView(R.id.voltage)
    TextView text_voltage;
    @BindView(R.id.level)
    TextView text_level;
    @BindView(R.id.temperature)
    TextView text_temperature;
    @BindView(R.id.plugged)
    TextView text_plugged;
    @BindView(R.id.technology)
    TextView text_technology;
    @BindView(R.id.pgb_battery)
    ProgressBar progressBar;
    @BindView(R.id.img_lighting)
    ImageView imageView;
    public static final String TAG="BatteryFragment";
    protected static final int MSG_ONE = 1;
    private int progress;

    //测试结果需要数据
    private static int testTime = 0;
    private String testItem = "Battery";
    private Date startTime;
    private Date endTime;
    private String resultData;
    private Handler mHandler;
    private Handler handler;

    //计时器
    private Timer timer;
    private TimerTask timerTask;
    private long timer_counting;//定义运行30s

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunInItemActivity itemActivity= (RunInItemActivity) getActivity();
        mHandler=itemActivity.mHandler;
        progress = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        full(true);//全屏
        Log.d("Test Demo:","准备注册");
        timer_counting = 30*1000;
        View view =  inflater.inflate(R.layout.activity_battery, container, false);
        ButterKnife.bind(this,view);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Log.d("Test Demo","接收消息");
                if(msg.what==MSG_ONE){
                    //设置电量进度
                    progressBar.setProgress(progress);
                    int i = progressBar.getProgress();
                    Log.d("当前进度为：",i+"");
                    if(BatteryStatus.equals("charging")){
                        //显示闪电
                        imageView.setVisibility(View.VISIBLE);
                    } else{
                        //不显示闪电
                        imageView.setVisibility(View.GONE);
                    }
                    //电池摄氏温度，默认获取的非摄氏温度值，需做一下运算转换
                    text_health.setText("health:"+BatteryTemp);//健康状态
                    text_state.setText("status:"+BatteryStatus);//充电状态
                    text_voltage.setText("voltage:"+zhuanhuan((double) BatteryV/1000)+"V");//电压保留一位
                    text_level.setText("level:"+BatteryN+"%");//电池电量
                    text_temperature.setText("temperature:"+(BatteryT/10.0)+"℃");//电池温度
                    if(BatteryStatus.equals("charging")){
                        text_plugged.setText("plugged:"+BatteryCharging);//电池充电类型
                    }else{
                        text_plugged.setText("plugged:"+"null");//否则为null
                    }
                    text_technology.setText("technology:"+BatteryType);//电池类型
                }
            }
        };
        Log.d("Test Demo:","准备注册");
         //注册一个系统 BroadcastReceiver，作为访问电池信息之用，这个不能直接在AndroidManifest.xml中注册
        getActivity().registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        //开启计时器
        startTimer();
        startTime=new Date();
        return view;
    }

    private class MyTimerTask extends TimerTask{
        @Override
        public void run() {
            timer_counting -=1000;
            if(timer_counting<0){
                //向主线程发送消息
                Log.d(new Long(timer_counting).toString(),"向主线程发送消息");
                endTime = new Date();
                resultData = "pass";
                testTime++;
                Message msg = new Message();
                msg.what = 109;
                Result result = new Result(testTime, testItem, getTime(startTime), getTime(endTime), getTestTime(startTime, endTime), resultData);
                msg.obj = result;
                if (mHandler!=null) {
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Battery:","onResume()...方法执行");
        if(timer==null){
            Log.d("Test:","此时的定时器时空的");
            startTimer();
        }
    }

    //格式化日期
    private String getTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    //测试时间
    private String getTestTime(Date start, Date end) {
        return String.valueOf(end.getTime() / 1000 - start.getTime() / 1000);
    }

    @Override//退出全屏，关闭计时器
    public void onDestroyView() {
        super.onDestroyView();
        full(false);
        stopTimer();
        //解绑广播
        getActivity().unregisterReceiver(mBatInfoReceiver);
    }

    //是否全屏
    private void full(boolean enable) {
        if (enable) {
            Log.d("进入Battery设置方法","设置为全屏");
            WindowManager.LayoutParams lp =  getActivity().getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getActivity().getWindow().setAttributes(lp);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            Log.d("进入Battery设置方法","设置为非全屏");
            WindowManager.LayoutParams attr = getActivity().getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().getWindow().setAttributes(attr);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override//暂停，关闭计时器
    public void onPause() {
        super.onPause();
        Log.d("Battery:","onPause()...方法执行");
        stopTimer();
    }
    //定时器启动
    private void startTimer(){
        timer = new Timer();
        timerTask = new MyTimerTask();
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }
    //定时器关闭
    private void stopTimer(){
        if(timer!=null){
            timer.cancel();
            timer = null;
            timerTask = null;
        }
    }
    /* 创建广播接收器 */
    public BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Test Demo:","进入广播");
            String action = intent.getAction();

            //如果捕捉到的action是ACTION_BATTERY_CHANGED， 就运行onBatteryInfoReceiver()

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                Log.d("Test Demo:","进入判断");
                BatteryN = intent.getIntExtra("level", 0);    //目前电量（0~100）
                //电池图片进度获取
                progress = BatteryN;
                BatteryV = intent.getIntExtra("voltage", 0);  //电池电压(mv)
                BatteryT = intent.getIntExtra("temperature", 0);  //电池温度(数值)
                BatteryType = intent.getStringExtra("technology");//电池类型
                Log.d("Test Demo:",BatteryT+"");
                switch (intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        BatteryStatus = "charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        BatteryStatus = "discharging";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        BatteryStatus = "not charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        BatteryStatus = "full";
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        BatteryStatus = "unknown";
                        break;
                }
                switch (intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        BatteryTemp = "unknown";
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        BatteryTemp = "good";
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        BatteryTemp = "dead";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        BatteryTemp = "over_voltage";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        BatteryTemp = "overheat";
                        break;
                }
                switch (intent.getIntExtra("plugged",0)){
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        BatteryCharging = "AC Charging";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        BatteryCharging = "USB Charging";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                        BatteryCharging = "wifi Charging";
                        break;
                }
                Log.d("发送消息","Test Demo");
                Message message = new Message();
                message.what = MSG_ONE;
                Log.d("此时的prograss值为：",progress+"");
                if (handler!=null) {
                    handler.sendMessage(message);
                }
            }
        }
    };

    private String zhuanhuan(double batteryv) {
        DecimalFormat decimalFormat = new DecimalFormat("#.0");
        return decimalFormat.format(batteryv);
    }
}
