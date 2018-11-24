package com.steve.runintest.item;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.steve.runintest.R;
import com.steve.runintest.Result;
import com.steve.runintest.RunInItemActivity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by steve on 17-11-4.
 */

public class DDRFragment extends Fragment {

    private static final String TAG = "DDRFragment";
    public static final int MSG_ONE = 1;
    public static final int MSG_TWO = 2;

    private double mSpeed1, mSpeed2;
    private String mString= "AAAA,BBBB,CCCC,DDDD," +
            "EEEE,FFFF,GGGG,HHHH,IIII,JJJJ,KKKK,LLLL," +
            "MMMM,NNNN,OOOO,PPPP,QQQQ,RRRR,SSSS,TTTT," +
            "UUUU,VVVV,WWWW,XXXX,YYYY,ZZZZ,";
    private Unbinder mUnbinder;
    @BindView(R.id.all_ddr)
    TextView mAllDdr;
    @BindView(R.id.used_ddr)
    TextView mUsedDdr;
    @BindView(R.id.usage_ddr)
    TextView mUsageDdr;
    @BindView(R.id.tv_writeddr)
    TextView mWriteDdr;
    @BindView(R.id.tv_readddr)
    TextView mReadDdr;
    @BindView(R.id.write_progress) SeekBar mWriteProgress;
    @BindView(R.id.read_progress) SeekBar mReadProgress;
    //private SeekBar mWriteProgress;
   // private SeekBar mReadProgress;
    private int mCurrentProgress = 0, mCurrentProgress2 = 0;
    private int mWrite = 0;
    private int mRead = 0;
    private boolean flag = true;
    private int write_max = 13130;
    private int read_max = 2626;
    private static int testTime = 0;
    private String testItem = "DDR";
    private Date startTime;
    private Date endTime;
    private String resultData;
    private Handler mHandler;
    private Handler handler;
    private Thread ddrThread;
    private boolean contorl = true;
    private boolean state;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        RunInItemActivity itemActivity = (RunInItemActivity) getActivity();
        mHandler = itemActivity.mHandler;
        initData();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_ONE:
                        mAllDdr.setText("\u3000"+getTotalMemory() + "MB");
                        mUsedDdr.setText("\u3000"+getAvailMemory() + "MB");
                        mUsageDdr.setText("\u3000"+getUsedPercentValue());
                        mWriteDdr.setText(translatePercent(mSpeed1));
                        mWriteProgress.setMax(write_max);
                        mCurrentProgress = (int) msg.obj;
                        mWriteProgress.setProgress(mCurrentProgress);
                        break;
                    case MSG_TWO:
                        mReadDdr.setText(translatePercent(mSpeed2));
                        mReadProgress.setMax(read_max);
                        mCurrentProgress2 = (int) msg.obj;
                        mReadProgress.setProgress(mCurrentProgress2);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ddr, container, false);
        Log.d(TAG, "onCreateView: ");
        mUnbinder = ButterKnife.bind(this, view);

  //      mReadProgress = (SeekBar) view.findViewById(R.id.read_progress);

  //      mWriteProgress = (SeekBar) view.findViewById(R.id.write_progress);

        //全屏
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //SeekBar不可触摸
        mWriteProgress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        //SeekBar不可触摸
        mReadProgress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        initData();
        return view;
    }

    private void initData() {
        mRead = 0;
        mWrite = 0;
        mCurrentProgress = 0;
        mCurrentProgress2 = 0;
        contorl = true;
        state = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        flag=true;

        if (mWrite == 0) {
            mWriteProgress.setProgress(0);
            mReadProgress.setProgress(0);
        }
        startTime = new Date();

        if(state) {
            ddrThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    StringBuffer mBuffer = new StringBuffer();
                    while (flag && contorl) {
                        try {
                            while (mWrite < write_max) {
                                mBuffer.append(mString);
                                mWrite = mBuffer.length();
                                mSpeed1 = (double) mWrite / (double) write_max;
                                Message msg = new Message();
                                Thread.sleep(100);
                                msg.obj = mWrite;
                                msg.what = MSG_ONE;
                                handler.sendMessage(msg);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        flag = false;
                    }
                    try {
                        String[] strArray = mBuffer.toString().split(",");
                        for (int i = 0; i < strArray.length; i++) {
                            if (strArray[i].length() != 4) {
//                            Looper.prepare();
                                //如果发现EMMC读取信息不一致，关闭测试并显示弹窗信息.
                                //Toast.makeText(getActivity(),"If you see this message,There will throw a EMMC problem!",Toast.LENGTH_SHORT).show();
//                            Looper.loop();
                                getActivity().finish();
                            }
                        }
                        while (mRead < strArray.length && contorl) {
                            //每次发26长度的字符串过去，发了101次，因为mBuffer分割后长度变2626了。
                            mRead += 26;
                            mSpeed2 = (double) mRead / (double) strArray.length;
                            Message msg = new Message();
                            msg.what = MSG_TWO;
                            msg.obj = mRead;
                            Thread.sleep(100);
                            Log.d("mRead:", new Integer(mRead).toString());
                            handler.sendMessage(msg);
                        }
                        if (mRead == read_max) {
                            endTime = new Date();
                            resultData = "pass";
                            testTime++;
                            Message msg = new Message();
                            msg.what = 105;
                            Result result = new Result(testTime, testItem, getTime(startTime), getTime(endTime), getTestTime(startTime, endTime), resultData);
                            msg.obj = result;
                            if (mHandler!=null) {
                                mHandler.sendMessage(msg);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
            ddrThread.start();
        }
    }

    //格式化时间
    private String getTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    //测试时间
    private String getTestTime(Date start, Date end) {
        return String.valueOf(end.getTime() / 1000 - start.getTime() / 1000);
    }

    public String translatePercent(double num) {
        NumberFormat num1 = NumberFormat.getPercentInstance();
        return num1.format(num);
    }

    //获取当前可用内存
    public String getAvailMemory() {
        ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return String.valueOf(mi.availMem / 1024 / 1024);
    }

    //获取DDR总容量
    public String getTotalMemory() {
        long mTotal;
        String path = "/proc/meminfo";
        String content = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path), 8);
            String line;
            if ((line = br.readLine()) != null) {
                content = line;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        int begin = content.indexOf(":");
        int end = content.indexOf("k");
        content = content.substring(begin + 1, end).trim();
        mTotal = Integer.parseInt(content) / 1024;
        return String.valueOf(mTotal);
    }

    //计算已用DDR容量百分比
    public String getUsedPercentValue() {
        long totalMemorySize = Long.parseLong(getTotalMemory());
        long availableSize = Long.parseLong(getAvailMemory());
        int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
        return percent + "%";
    }

    //关线程
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        contorl = false;
        if (ddrThread!=null) {
            ddrThread.interrupt();
            ddrThread=null;
        }
        quitFullScreen();
    }

    //退出全屏
    private void quitFullScreen() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().setAttributes(lp);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    public void onPause() {
        super.onPause();
        state = false;
    }
}
