package com.steve.runintest.upload;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.steve.runintest.R;
import com.steve.runintest.RunInResultActivity;
import com.steve.runintest.upload.tools.DetectEnvironment;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends AppCompatActivity {

    private TextView ssidInfo;
    private TextView macInfo;
    private WifiAdmin wifiAdmin;
    private Thread thread;
    private Timer timer;
    private MyTimerTask timerTask;
    private boolean timeOver;
    private Toast mToast = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x123) {
                Log.d("1111111", wifiAdmin.getSSID());
                if (wifiAdmin.getSSID().equals("\"D-Link_DIR-809\"")) {
                    ssidInfo.setText(wifiAdmin.getSSID());
                    String routerMac = wifiAdmin.getRouterMac();
                    String localMac = wifiAdmin.getLocalMac();
                    macInfo.setText(routerMac);
                    showMsg("Connect wifi success");
                    Intent intent = new Intent(StartActivity.this.getApplicationContext(), MainActivity.class);
                    intent.putExtra("mac", handleMac(routerMac) + "#" + handleMac(localMac));
                    startActivity(intent);
                    StartActivity.this.finish();
                }else {
                    fileToServerFail();
                }
            } else if (msg.what == 0x234) {
                Log.d("ffffffffffffff","llllllllllllllll");
                showMsg("Connect wifi failed");
                fileToServerFail();
            }
        }
    };

    //上传服务器出错
    private void fileToServerFail() {
        SharedPreferences.Editor editor = getSharedPreferences("server_result", MODE_PRIVATE).edit();
        editor.putBoolean("server", false);
        editor.commit();
        Intent intent = new Intent(StartActivity.this.getApplicationContext(), RunInResultActivity.class);
        startActivity(intent);
        StartActivity.this.finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常亮
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences.Editor editor = getSharedPreferences("server_result", MODE_PRIVATE).edit();
        editor.clear().commit();

        setContentView(R.layout.activity_start);
        ssidInfo = (TextView) findViewById(R.id.wifi_ssid_info);
        macInfo = (TextView) findViewById(R.id.wifi_mac_info);

        wifiAdmin = new WifiAdmin(getApplicationContext());

//        if (DetectEnvironment.isNetworkAvailable(this)) {
////            Log.e("ssid",wifiAdmin.getSSID()+";Local MAC: "+wifiAdmin.getLocalMac() + "; Net MAC: " + wifiAdmin.getRouterMac());
//            if (wifiAdmin.getSSID().equals("\"Tenda_24AE00\"")) {
//                ssidInfo.setText(wifiAdmin.getSSID());
//                macInfo.setText(wifiAdmin.getRouterMac());
//            } else {
//                wifiAdmin.connect("Tenda_24AE00", "12345678", WifiCipherType.WIFICIPHER_WPA);
//
//            }
//        } else {
////            wifiAdmin.connect("Tenda_24AE00", "12345678", WifiCipherType.WIFICIPHER_WPA);
//            wifiAdmin.openWifi();
//        }
//      wac.connect("TP-LINK-TL-WR740", "2017666888", WifiAdmin.WifiCipherType.WIFICIPHER_WPA);

    }

    public class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                //网络不可用
                if (!DetectEnvironment.isNetworkAvailable(StartActivity.this)) {
                    timeOver = true;
                    handler.sendEmptyMessage(0x234);
                    Log.e("Connect", System.currentTimeMillis() + ": wifi connection is time-out");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public class WorkThread extends Thread {

        public WorkThread() {
            super();
        }

        public WorkThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            //网络不可用
            while (!DetectEnvironment.isNetworkAvailable(StartActivity.this.getApplicationContext())) {
                try {
                    sleep(100);
                    if (timeOver) {
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            if (!timeOver) {
                try {
                    sleep(8000);
                    handler.sendEmptyMessage(0x123);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public String handleMac(String mac) {
        if (mac != null) {
            StringBuilder stringBuilder = new StringBuilder();
            String[] strs = mac.split(":");
            for (int i = 0; i < strs.length; i++) {
                stringBuilder.append(strs[i]);
            }
            return stringBuilder.toString().toUpperCase();
        } else {
            return "";
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("StartActivity.onStart");
        if (timer == null) {
            timer = new Timer();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("StartActivity.onResume");
        clear();
        wifiAdmin.connect("D-Link_DIR-809", "Aa123456", WifiCipherType.WIFICIPHER_WPA);//连接指定wifi，上传数据到服务器
        timeOver = false;
        if (thread == null) {
            thread = new WorkThread("NetWork");
            thread.start();
        }

        if (timerTask == null) {
            timerTask = new MyTimerTask();
            timer.schedule(timerTask, 1000 * 10);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("StartActivity.onPause");
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("StartActivity.onStop");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
    }

    private void clear() {
        ssidInfo.setText("");
        macInfo.setText("");
    }

    private void showMsg(String msg) {
        mToast = (mToast == null) ? Toast.makeText(StartActivity.this.getApplicationContext(), msg, Toast.LENGTH_LONG) : mToast;
        mToast.setText(msg);
        mToast.show();
    }
}
