package com.steve.runintest.item;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.steve.runintest.R;
import com.steve.runintest.Result;
import com.steve.runintest.RunInItemActivity;
import java.io.IOException;
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

public class VedioFragment extends Fragment {
    private static final String TAG = "VedioFragment";

    @BindView(R.id.surface_view)
    SurfaceView mSurfaceView;
    @BindView(R.id.seek_bar)
    SeekBar mSeekBar;
    @BindView(R.id.time_bar)
    TextView mText;
    private MediaPlayer mediaPlayer;
    private static String time;
    private SurfaceHolder surfaceHolder;
    public static final int MSG_ONE = 1;
    private static int testTime = 0;
    private String testItem = "VEDIO";
    private Date startTime;
    private Date endTime;
    private String resultData;
    private Handler mHandler;
    private Thread thread1, thread2;
    private int position;
    private Timer timer;
    private TimerTask timerTask;
    private long timer_counting;
    private Uri DataSourceuri;
    private boolean flag;
    //surface是否已经创建好
    private boolean isSurfaceCreated;
    //是否第一次进入
    private boolean state;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunInItemActivity itemActivity = (RunInItemActivity) getActivity();
        mHandler = itemActivity.mHandler;
    }

    /*
    * 视图周期
    * **/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        ButterKnife.bind(this, view);
        Log.d("VedioFragment的", "onCreateView()...方法执行");
        initPlayer();//初始化播放资源
        return view;
    }

    /**
     * 播放资源初始化
     * **/
    private void initPlayer(){
        DataSourceuri = Uri.parse("android.resource://com.steve.runintest/" + R.raw.aaa);
        mediaPlayer = new MediaPlayer();
        //设置视频流类型
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //设置播放时打开屏幕
        mSurfaceView.getHolder().setKeepScreenOn(true);
        //创建surfaceView
        CreateSurface();
        full(true);
        startTime = new Date();
        isSurfaceCreated = false;
        state = true;
        flag = true;
        position = 0;

    }

    /*
     *此周期完成重启以及初始化视频播放
     * **/
    @Override
    public void onResume() {
        //重启后如果surface被销毁就重新创建
        if(!state){
            if(!isSurfaceCreated)
            {
                CreateSurface();
            }
        }
        super.onResume();

        Log.d("VedioFragment的", "onResume()...方法执行");
        /*
         * 创建完毕页面后需要将播放操作延迟10ms防止因surface创建不及时导致播放失败
         */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (isSurfaceCreated) {
                    try {
                        play();
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }
        }, 10);
    }

    /**
     * 创建视频展示页面
     */
    private void CreateSurface() {
        surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //兼容4.0以下的版本
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                isSurfaceCreated = false;
                try {
                    if (mediaPlayer != null) {
                        try {
                            if (mediaPlayer.isPlaying()) {
                                position = mediaPlayer.getCurrentPosition();
                                mediaPlayer.stop();
                            }
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                }catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("surface被创建出来了", "xxxxxxxxxxxxxxxx");
                //将视频画面输出到SurfaceView
                isSurfaceCreated = true;
//                mediaPlayer.setDisplay(mSurfaceView.getHolder());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
            }
        });
    }

    //格式转换
    private String getTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    //获取单项测试运行时间
    private String getTestTime(Date start, Date end) {
        return String.valueOf(end.getTime() / 1000 - start.getTime() / 1000);
    }

    // 自定义的滑动条线程
    private class SeekBarThread implements Runnable {
        @Override
        public void run() {
            try {
                while (mediaPlayer != null && mediaPlayer.isPlaying() && flag) {
                    try {
                        // 将SeekBar位置设置到当前播放位置
                        mSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                        // 每100毫秒更新一次位置
                        Thread.sleep(100);
                        int musicTime = mediaPlayer.getCurrentPosition() / 1000;
                        String show = musicTime / 60 + ":" + musicTime % 60;
                    }catch (NullPointerException e){
                        e.printStackTrace();
                        flag = false;
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                flag = false;
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    //自定义的时间线程
    private class TimeThread implements Runnable {
        @Override
        public void run() {
            while (mediaPlayer != null && flag) {
                try {
                    if (mediaPlayer.isPlaying() & flag) {
                        // 每1秒更新一次时间
                        Thread.sleep(1000);
                        Message msg = new Message();
                        msg.what = MSG_ONE;
                        //发送
                        if (handler!=null) {
                            handler.sendMessage(msg);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    flag = false;
                    e.printStackTrace();
                }
            }
        }
    }

    //提示主线程UI进行时间更新操作
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //通过消息的内容msg.what分别更新ui
            switch (msg.what) {
                case MSG_ONE:
                    try {
                        int musicTime = 0;
                        //显示在textview上
                        if (mediaPlayer != null) {
                            try {
                                musicTime = mediaPlayer.getCurrentPosition() / 1000;
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }
                            DecimalFormat df = new DecimalFormat("00");
                            int minute = musicTime / 60;
                            String minuteStr = df.format(minute);
                            int seconds = musicTime % 60;
                            String secondsStr = df.format(seconds);
                            String show = minuteStr + ":" + secondsStr;
                            mText.setText(show + "/" + time);
                            break;
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                default:
                    break;
            }
        }
    };

    //设置全屏
    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
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

    //程序暂停处理
    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                //保存当前播放位置
                position = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
                stopTimer();//暂停计时
            }catch (NullPointerException e){
                e.printStackTrace();
            }
            state = false;
        }
    }

    //视频播放方法
    private void play() {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(getContext(), DataSourceuri);

            mediaPlayer.setDisplay(mSurfaceView.getHolder());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.seekTo(position);
                    mediaPlayer.start();//开始播放
                    // 设置seekbar的最大值
                    mSeekBar.setMax(mediaPlayer.getDuration());
                    // 创建进度条线程
                    thread1 = new Thread(new SeekBarThread());
                    //创建时间线程
                    thread2 = new Thread(new TimeThread());
                    // 启动线程
                    thread1.start();
                    thread2.start();
                    Log.d("进入视频播放并启动线程", "Test()...");
                    //定义总时间
                    int time_all = mediaPlayer.getDuration();
                    if(state) {
                        timer_counting = time_all;
                    }
                    //定时器开启
                    startTimer();
                    int musicTime = time_all / 1000;
                    DecimalFormat df = new DecimalFormat("00");
                    int minute = musicTime / 60;
                    String minuteStr = df.format(minute);
                    int seconds = musicTime % 60;
                    String secondsStr = df.format(seconds);
                    time = minuteStr + ":" + secondsStr;
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });
            mSeekBar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            // mediaPlayer添加完成事件的监听器
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer arg0) {
                    Toast.makeText(getActivity(), "播放完毕", Toast.LENGTH_SHORT).show();
//                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制为竖屏
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    //定时器启动
    private void startTimer() {
        timer = new Timer();
        timerTask = new MyTimerTask();
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    //定时器关闭
    private void stopTimer() {
        if (timer != null) {
            try {
                timer.cancel();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
            timer = null;
            timerTask = null;
        }
    }

    //视频倒计时任务
    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            timer_counting -= 1000;
            if (timer_counting < 0) {
                //向主线程发送消息
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

    //播放结束释放资源
    @Override
    public void onDestroyView() {
        Log.d("VedioFragment的", "onDestoryView()...方法执行");
        super.onDestroyView();
        if (handler!=null){
            handler=null;
        }
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        //终止计时
        stopTimer();
        full(false);
        flag = false;
    }
}