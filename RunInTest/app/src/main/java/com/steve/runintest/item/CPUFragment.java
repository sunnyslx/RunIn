package com.steve.runintest.item;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
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
import com.steve.runintest.upload.tools.WriteRead;
import com.steve.runintest.utils.CPUTestManger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by steve on 17-11-4.
 */

public class CPUFragment extends Fragment {
    protected static final int MSG_ONE = 1;
    private static final String TAG = "CPUFragment";
    int preMax = 0, preAvg, sum = 0, preMin = 100;
    int count = 0;
    private String s;
    @BindView(R.id.cpu_Minusage)
    TextView mCpuMinusage;
    @BindView(R.id.cpu_Maxusage)
    TextView mCpuMaxusage;
    @BindView(R.id.cpu_Avgusage)
    TextView mCpuAvgusage;
    @BindView(R.id.tv_cpusage)
    TextView mCpuUsage;
    @BindView(R.id.tv_maxFreq)
    TextView mMaxFreq;
    @BindView(R.id.tv_minFreq)
    TextView mMinFreq;
    @BindView(R.id.tv_nowFreq)
    TextView mNowFreq;
    @BindView(R.id.tv_cpuTemp)
    TextView mCpuTemp;
    //Cpu温度文件
    File cputempfile;
    //Cpu温度文件路径
    String[] cpuTemp = {"/system/bin/cat", "sys/class/thermal/thermal_zone0/temp"};
    //存储路径根文件
    File file = null;
    //Cpu频率
    CPUTestManger manger = new CPUTestManger();
    //更新UI并且查询利用率的线程
    private CpuThread mCpuThread;
    //计时器
    private Timer timer;
    private TimerTask timerTask;
    private long timer_counting;//定义运行30s
    //结果相关数据
    private static int testTime = 0;
    private String testItem = "cpu";
    private Date startTime;
    private Date endTime;
    private String resultData;
    private Handler mHandler;
    //循环关闭条件
    private boolean writeFlag, nullFlag,cpu_flag;
    private int rate = 0;
    //开个线程池
    ExecutorService executorService;
    private int thread_count;

    //此handler用于在此Fragment中更新相关UI的操作
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ONE) {
                Log.i(TAG, "handleMessage:CpuFragment获取到了此handler发过来的消息");
                mCpuUsage.setText(rate + "%");
                //获取利用率的最大值及显示
                preMax = cpuMaxUsage(rate);
                mCpuMaxusage.setText(preMax + "%");
                //利用率的平均值及显示
                mCpuAvgusage.setText(cpuAvgUsage(rate) + "%");
                //利用率的最小值及显示
                preMin = cpuMinUsage(rate);
                mCpuMinusage.setText(preMin + "%");
                //Cpu频率的设置
                mMinFreq.setText(manger.getMinCpuFreq() + "KHz");
                mMaxFreq.setText(manger.getMaxCpuFreq() + "KHz");
                mNowFreq.setText(manger.getCurCpuFreq() + "KHz");
                //设置Cpu温度值
                if (cputempfile.exists()) {
                    mCpuTemp.setText(manger.formatTemp(manger.ReadCPU0(cpuTemp)) + "°C");
                } else {
                    mCpuTemp.setText(manger.formatTemp(s) + "°C");
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        RunInItemActivity itemActivity = (RunInItemActivity) getActivity();
        mHandler = itemActivity.mHandler;
        timer_counting = 5 * 60 * 1000;
        //来定义5个活动线程
//        fixedThreadPool = Executors.newFixedThreadPool(5);
        executorService= Executors.newCachedThreadPool();
        //初始化相关数据
        initData();
        //CPU温度文件路径
        cputempfile = new File("sys/class/thermal/thermal_zone0/temp");
        //广播获取电池温度
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        this.getActivity().registerReceiver(mBroadcastReceiver, filter);
        startTime = new Date();
        //声明根目录
        file = new File(Environment.getExternalStorageDirectory().getPath()+"/CPUTest");
        if(file.exists()){
            deletAllFiles(file);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cpu, container, false);
        ButterKnife.bind(this, view);
        //全屏
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Log.i(TAG, "onCreateView: ");
        //开启线程,给handler发消息更新UI
        mCpuThread = new CpuThread();
        mCpuThread.start();
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
        //开启定时器
        startTimer();
    }

    //初始化数据
    private void initData() {
        preMax = 0;
        sum = 0;
        preMin = 100;
        preAvg = 0;
        count = 0;
        writeFlag = true;
        nullFlag = true;
        cpu_flag = true;
        Log.i(TAG, "initData: 数据初始化");
    }

    //定时器启动
    private void startTimer() {
        timer = new Timer();
        timerTask = new MyTimerTask();
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
        Log.i(TAG, "startTimer: 定时器开启");
    }

    //定时器通知主handler进行跳转
    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            timer_counting -= 1000;
            if (timer_counting < 0) {
                //向主线程发送消息
                Log.d(new Long(timer_counting).toString(), "向主线程发送消息");
                endTime = new Date();
                resultData = "pass";
                testTime++;
                Message msg = new Message();
                msg.what = 103;
                Result result = new Result(testTime, testItem, getTime(startTime), getTime(endTime), getTestTime(startTime, endTime), resultData);
                msg.obj = result;
                if (mHandler != null){
                    mHandler.sendMessage(msg);
                }
                timer.cancel();
            }else{
                if(rate>0 && rate<=100) {
                    Message mm = new Message();
                    mm.what = MSG_ONE;
                    handler.sendMessage(mm);
                }
            }
        }
    }

    //用来查询利用率的线程
    public class CpuThread extends Thread {
        @Override
        public void run() {
            while(cpu_flag) {
                Log.i(TAG, "run: CpuThread线程被开启");
                rate = getCpuUsageCur();
                Log.d("555555", "" + rate);
                try {
                    if (rate < 85) {
                        //开启循环耗时
                        executorService.execute(new NullRunnable());
                        thread_count++;
//                        executorService.execute(new WriteRunnable());
                        executorService.execute(new WriteRunnable1("/sdcard/CPUTest" + "/Dir" + thread_count));
                        Log.d(" thread_count:","" + thread_count);
                        Log.d("11111", "1个写线程加入了线程池");
                    } else if (85 < rate && rate < 95) {

                    } else if (rate > 95) {
                        nullFlag = false;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
            if(executorService!=null){
                executorService = null;
            }
        }
    }

    private int getCpuUsageCur() {
        String Result;
        String[] resultArr;
        String user, system;
        int usevalue = 0;
        int systemvalue = 0;
        try {
            Process process = Runtime.getRuntime().exec("top -n 1");
            InputStreamReader isReader = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(isReader);
            while ((Result = br.readLine()) != null) {
                if (Result.trim().length() < 1) {
                    continue;
                } else {
                    resultArr = Result.split(" ");
                    user = resultArr[1];
                    system = resultArr[3];
                    String user1 = user.replace("%,", "");
                    String system1 = system.replace("%,", "");
                    usevalue = Integer.parseInt(user1);
                    systemvalue = Integer.parseInt(system1);
                    break;
                }
            }
            br.close();
            isReader.close();
//            if (process != null) {
//                process.destroy();
//            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //Log.i("Message", "user_cpu:" + usevalue + "% ---------" + "system_cpu:" + systemvalue + "%");
        return (usevalue + systemvalue);
    }

    //此线程用来开启耗时操作进行Cpu的消耗(可以加25%左右的压力)
    private class NullRunnable implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "run: Thread1被启动了");
            int i = 0;
            while (nullFlag) {
                for (; i < 56000000; i++) ;
                try {
                    Thread.sleep(50);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                i = 0;
            }
        }
    }

    //此线程用来执行文件的写操作
    class WriteRunnable implements Runnable{
        @Override
        public void run() {
            WriteRead writeRead = null;
            writeRead = new WriteRead(getContext(), "/sdcard/CPUTest" + "/Dir" + thread_count);
            Log.d("88888", "此时线程名称：" + thread_count);
            while (writeFlag) {
////            writeToFile();
                writeRead.write();
            }
        }
    }
    private class WriteRunnable1 implements Runnable {
        private String fileDir;

        WriteRunnable1(String fileDir) {
            this.fileDir = fileDir;
        }

        @Override
        public void run() {
            WriteRead writeRead = new WriteRead(getContext(), fileDir);
            while (writeFlag) {
                writeRead.write();
            }
            writeRead.testOver(fileDir);
        }
    }
    //用来创建文件的方法
//    private void createFile() {
//        file2 = new File(file1, Thread.currentThread().getName());
//        if (!file2.exists()) {
//            file2.mkdir();
//        }
//        if(file2.exists()) {
//            file3 = new File(file2, Thread.currentThread().getName() + ".txt");
//            if (!file3.exists()) {
//                try {
//                    file3.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    //用来进行文件写操作的方法
//    private void writeToFile() {
//        createFile();
//        try {
//            FileOutputStream fs = new FileOutputStream(file3);
//            BufferedOutputStream bos = new BufferedOutputStream(fs);
//            try {
//                while (writeFlag) {
//                    bos.write(string.getBytes());  //write()方法可以写入byte数组、int
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                bos.close();
//                fs.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }


    //递归删除文件目录及文件
//    public void deletAllFiles(File file) {
//        Log.d("执行了删除文件夹的方法", "执行了删除文件夹的方法");
//        //文件目录存在？（包括文件及文件夹）
//        if (file.exists()) {
//            //是文件？
//            if (file.isFile()) {
//                Log.d("文件路径", file.getAbsolutePath());
//                file.delete();
//            }
//            //是文件夹？
//            else if (file.isDirectory()) {
//
//                //接收文件夹目录下所有的文件实例
//                File[] listFiles = file.listFiles();
//                if (listFiles == null) {
//                    return;
//                }
//                for (File file4 : listFiles) {
////                    Log.d("遍历删除","遍历删除");
//                    //foreach遍历删除文件 递归
//                    deletAllFiles(file4);
//                }
//                //递归跳出来的时候删除空文件夹
//                file.delete();
//            }
//        }
//    }

    //资源回收
    @Override
    public void onPause() {
        Log.d(TAG, "onDestroy:onPause");
        super.onPause();
        //关闭定时器
        stopTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroy:onDestoryView");
        super.onDestroyView();
        nullFlag = false;
        writeFlag = false;
        cpu_flag = false;
        quitFullScreen();
        stopTimer();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy:onDestory");
        //广播解绑
        this.getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
        //结束线程资源以及进程
        if(mCpuThread!=null){
            mCpuThread.interrupt();
            mCpuThread= null;
        }
        //文件删除
        deletAllFiles(file);
    }

    //退出全屏
    private void quitFullScreen() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().setAttributes(lp);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    //此广播用来进行电池温度获取
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        private int temperature;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                temperature = intent.getIntExtra("temperature", 0);
                s = " " + String.valueOf(temperature) + "\n";
            }
        }
    };

    //定时器关闭
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            timerTask = null;
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

    //计算Cpu利用率最大值
    public int cpuMaxUsage(int num) {
        return Math.max(preMax, num);
    }

    //计算Cpu利用率最小值
    public int cpuMinUsage(int num2) {
        return Math.min(preMin, num2);
    }

    //计算Cpu利用率平均值
    public int cpuAvgUsage(int num) {
        if (timer != null) {
            sum += num;
            count++;
//            Log.d("sum",sum+"");
            preAvg = sum / count;
        }
        return preAvg;
    }

    //递归删除文件目录及文件
    public void deletAllFiles(File file) {
        Log.d("执行了删除文件夹的方法", "执行了删除文件夹的方法");
        Log.d("110：",""+file);
        //文件目录存在？（包括文件及文件夹）
        if (file.exists()) {
            //是文件？
            if (file.isFile()) {
                Log.d("文件路径", file.getAbsolutePath());
                file.delete();
            }
            //是文件夹？
            else if (file.isDirectory()) {

                //接收文件夹目录下所有的文件实例
                File[] listFiles = file.listFiles();
                if (listFiles == null) {
                    return;
                }
                for (File file4 : listFiles) {
//                    Log.d("遍历删除","遍历删除");
                    //foreach遍历删除文件 递归
                    deletAllFiles(file4);
                }
                //递归跳出来的时候删除空文件夹
                file.delete();
            }
        }
    }

}
