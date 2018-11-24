package com.steve.runintest.item;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.steve.runintest.R;
import com.steve.runintest.Result;
import com.steve.runintest.RunInItemActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by steve on 17-11-4.
 */

public class TwoDFragment extends Fragment {
    public static final String TAG="TwoDFragment";

    @BindView(R.id.t_d_img_one) ImageView circle1;
    @BindView(R.id.t_d_img_two) ImageView circle2;
    @BindView(R.id.t_d_img_three) ImageView circle3;
    @BindView(R.id.t_d_img_four) ImageView circle4;

    private AnimatorSet animatorSet;
    private LinearInterpolator lir = new LinearInterpolator();

    //测试结果需要数据
    private static int testTime = 0;
    private String testItem = "2D";
    private Date startTime;
    private Date endTime;
    private String resultData;
    private Handler mHandler;

    //计时器
    private Timer timer;
    private TimerTask timerTask;
    private long timer_counting;//定义运行30s

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunInItemActivity itemActivity= (RunInItemActivity) getActivity();
        mHandler=itemActivity.mHandler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        full(true);//全屏
        timer_counting = 30*1000;
        View view =  inflater.inflate(R.layout.fragment_d_two, container, false);
        ButterKnife.bind(this,view);
        WindowManager wm = this.getActivity().getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        Log.i(TAG, "onCreateView: width"+width+",height:"+height);
        //红色小球横向位移
        ObjectAnimator translationXAnim = ObjectAnimator.ofFloat(circle1, "translationX", 0f,width-200f,0f);
        translationXAnim.setDuration(6000);
        translationXAnim.setInterpolator(lir);
        translationXAnim.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        translationXAnim.setRepeatMode(ValueAnimator.RESTART);//

        //设置红球旋转
        ObjectAnimator animator = ObjectAnimator.ofFloat(circle1,"rotation",0,-180,-359);
        animator.setDuration(6000);
        animator.setInterpolator(lir);
        animator.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        animator.setRepeatMode(ValueAnimator.RESTART);//

        //绿色小球横向位移
        ObjectAnimator translationXAnim2 = ObjectAnimator.ofFloat(circle2, "translationX", 0f,width-200f,0f);
        translationXAnim2.setDuration(6000);
        translationXAnim2.setInterpolator(lir);
        translationXAnim2.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        translationXAnim2.setRepeatMode(ValueAnimator.RESTART);//

        //绿色小球纵向位移
        ObjectAnimator translationYAnim2 = ObjectAnimator.ofFloat(circle2, "translationY",0f,150f-height,0f,150f-height,0f,150f-height,0f);
        translationYAnim2.setDuration(6000);
        translationYAnim2.setInterpolator(lir);
        translationYAnim2.setRepeatCount(ValueAnimator.INFINITE);
        translationYAnim2.setRepeatMode(ValueAnimator.RESTART);

        //设置绿球旋转
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(circle2,"rotation",0,180,359);
        animator2.setDuration(6000);
        animator2.setInterpolator(lir);
        animator2.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        animator2.setRepeatMode(ValueAnimator.RESTART);//

        //紫色小球横向位移
        ObjectAnimator translationXAnim3 = ObjectAnimator.ofFloat(circle3, "translationX", 0f,200f-width,0f);
        translationXAnim3.setDuration(6000);
        translationXAnim3.setInterpolator(lir);
        translationXAnim3.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        translationXAnim3.setRepeatMode(ValueAnimator.RESTART);//

        //紫色小球纵向位移
        ObjectAnimator translationYAnim3 = ObjectAnimator.ofFloat(circle3, "translationY",0f,75f-height/2,0f,height/2-75f,0f);
        translationYAnim3.setDuration(6000);
        translationYAnim3.setInterpolator(lir);
        translationYAnim3.setRepeatCount(ValueAnimator.INFINITE);
        translationYAnim3.setRepeatMode(ValueAnimator.RESTART);

        //设置紫球旋转
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(circle3,"rotation",0,-180,-359);
        animator3.setDuration(6000);
        animator3.setInterpolator(lir);
        animator3.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        animator3.setRepeatMode(ValueAnimator.RESTART);

        //蓝色小球横向位移
        ObjectAnimator translationXAnim4 = ObjectAnimator.ofFloat(circle4, "translationX", 0f,200f-width,0f);
        translationXAnim4.setDuration(10000);
        translationXAnim4.setInterpolator(lir);
        translationXAnim4.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        translationXAnim4.setRepeatMode(ValueAnimator.RESTART);

        //蓝色小球垂直下落
        ObjectAnimator translationYYAnim4 = ObjectAnimator.ofFloat(circle4, "translationY", 0f,150f-height,0f);
        translationYYAnim4.setDuration(6000);
        translationYYAnim4.setInterpolator(new BounceInterpolator());
        translationYYAnim4.setRepeatCount(ValueAnimator.INFINITE);
        translationYYAnim4.setRepeatMode(ValueAnimator.RESTART);

        //设置蓝球旋转
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(circle4,"rotation",0,180,359);
        animator4.setDuration(6000);
        animator4.setInterpolator(lir);
        animator4.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        animator4.setRepeatMode(ValueAnimator.RESTART);


        animatorSet = new AnimatorSet();
        animatorSet.playTogether(translationXAnim,animator,//红球动作
                translationXAnim2,translationYAnim2,animator2,//绿球动作
                translationXAnim3,translationYAnim3,animator3,//紫球动作
                translationXAnim4,translationYYAnim4,animator4//蓝球动作
        );
        Log.i(TAG, "onCreateView: ");
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
                msg.what = 101;
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
        Log.i(TAG, "onResume: ");
        if (animatorSet.isPaused()){
            animatorSet.resume();
        }else{
            animatorSet.start();
        }
        if(timer==null){
            Log.i(TAG, "onResume: 定时器为空");
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
    }

    //是否全屏
    private void full(boolean enable) {
        if (enable) {
            Log.i(TAG, "full: 2D设置为全屏");
            WindowManager.LayoutParams lp =  getActivity().getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getActivity().getWindow().setAttributes(lp);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            Log.i(TAG, "full: 2D设置为非全屏");
            WindowManager.LayoutParams attr = getActivity().getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().getWindow().setAttributes(attr);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override//动画暂停，关闭计时器
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
        if(animatorSet.isRunning()) {
            animatorSet.pause();
        }
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
}
