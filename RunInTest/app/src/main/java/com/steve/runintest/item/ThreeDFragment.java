package com.steve.runintest.item;

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

import com.steve.runintest.Result;
import com.steve.runintest.RunInItemActivity;
import com.steve.runintest.utils.MySurfaceView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by steve on 17-11-4.
 */

public class ThreeDFragment extends Fragment {
    public static final String TAG = "ThreeDFragment";

    private static int testTime = 0;
    private String testItem = "3D";
    private Date startTime;
    private Date endTime;
    private String resultData;
    private Handler mHandler;
    private MySurfaceView mGLSurfaceView;

    private Timer timer;
    private TimerTask timerTask;
    private long timer_counting;//定义运行30s

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunInItemActivity itemActivity= (RunInItemActivity) getActivity();
        mHandler=itemActivity.mHandler;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //全屏
        full(true);
        timer_counting = 30*1000;
        mGLSurfaceView = new MySurfaceView(getActivity());
        mGLSurfaceView.requestFocus();
        mGLSurfaceView.setFocusableInTouchMode(true);
        startTimer();
        return mGLSurfaceView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        mGLSurfaceView.onResume();
        startTime = new Date();

        if (timer==null) {
            startTimer();
        }
    }

    private String getTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    private String getTestTime(Date start, Date end) {
        return String.valueOf(end.getTime() / 1000 - start.getTime() / 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
        mGLSurfaceView.onPause();
        stopTimer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");
        full(false);
        stopTimer();
    }

    //设置全屏
    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp =  getActivity().getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getActivity().getWindow().setAttributes(lp);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getActivity().getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().getWindow().setAttributes(attr);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void startTimer(){
        timer = new Timer();
        timerTask = new MyTimerTask();
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private void stopTimer(){
        if(timer!=null){
            timer.cancel();
            timer = null;
            timerTask = null;
        }
    }

    private class MyTimerTask extends TimerTask{
        @Override
        public void run() {
            timer_counting -=1000;
            if(timer_counting<0){
                //向主线程发送消息
                Log.i(TAG, "run: 向主线程发送消息");
                endTime = new Date();
                resultData = "pass";
                testTime++;
                Message msg = new Message();
                msg.what = 102;
                Result result = new Result(testTime, testItem, getTime(startTime), getTime(endTime), getTestTime(startTime, endTime), resultData);
                msg.obj = result;
                if (mHandler!=null) {
                    mHandler.sendMessage(msg);
                }
            }
        }
    }
}