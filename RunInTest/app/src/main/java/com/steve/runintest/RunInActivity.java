package com.steve.runintest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RunInActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RunInActivity";
    public static final String TEST_ITEM_TIME = "com.steve.runintest.time";

    @BindView(R.id.cb_video)
    CheckBox mVedioBox;
    @BindView(R.id.cb_two_d)
    CheckBox mTwoDBox;
    @BindView(R.id.cb_three_d)
    CheckBox mThreeDBox;
    @BindView(R.id.cb_cpu)
    CheckBox mCPUBox;
    @BindView(R.id.cb_emmc)
    CheckBox mEMMCBox;
    @BindView(R.id.cb_ddr)
    CheckBox mDDRBox;
    @BindView(R.id.cb_lcd)
    CheckBox mLCDBox;
    @BindView(R.id.cb_wifi)
    CheckBox mWIFIBox;
    @BindView(R.id.cb_bluetooth)
    CheckBox mBTBox;
    @BindView(R.id.cb_battery)
    CheckBox mBatteryBox;

    @BindView(R.id.tv_video_result)
    TextView mVedioResult;
    @BindView(R.id.tv_two_d_result)
    TextView mTwoDResult;
    @BindView(R.id.tv_three_d_result)
    TextView mThreeDResult;
    @BindView(R.id.tv_cpu_result)
    TextView mCPUResult;
    @BindView(R.id.tv_emmc_result)
    TextView mEMMCResult;
    @BindView(R.id.tv_ddr_result)
    TextView mDDRResult;
    @BindView(R.id.tv_lcd_result)
    TextView mLCDResult;
    @BindView(R.id.tv_wifi_result)
    TextView mWIFIResult;
    @BindView(R.id.tv_bluetooth_result)
    TextView mBTResult;
    @BindView(R.id.tv_battery_result)
    TextView mBatteryResult;


    @BindView(R.id.checkbox_all)
    CheckBox mCheckBoxAll;//全选
    @BindView(R.id.start_test_all)
    ImageButton mStart;//开始按钮
    @BindView(R.id.choose_time)
    Spinner mSpinner;//测试时间下拉列表

    private CheckBox[] mCheckBoxes;
    private static List<Fragment> mFragments = new ArrayList<>();
    private int mTime;//测试总时间

    private TestResultLab mTestResultLab;
    private List<TestResult> mTestResults;
    private int id;//针对单项测试

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常亮
        setContentView(R.layout.activity_run_in);

        ButterKnife.bind(this);
        mCheckBoxes = new CheckBox[]{mVedioBox, mTwoDBox, mThreeDBox, mCPUBox, mEMMCBox, mDDRBox, mLCDBox, mWIFIBox, mBTBox,mBatteryBox};
        //删除数据库中保存的测结果（回显数据）
        mTestResultLab = TestResultLab.get(getApplicationContext());
        mTestResultLab.deleteResult();
        //获取测试机台的SN号
        TestResultToFile.SN = android.os.Build.SERIAL;
        Log.d(TAG,TestResultToFile.SN);
        //监听复选框，改变全选复选框的状态
        for (int i=0;i<mCheckBoxes.length;i++){
            mCheckBoxes[i].setOnClickListener(RunInActivity.this);
        }
        //全选
        mCheckBoxAll.setOnClickListener(new View.OnClickListener() {//全选
            @Override
            public void onClick(View view) {
                if (mCheckBoxAll.isChecked()) {
                    for (int i = 0; i < mCheckBoxes.length; i++) {
                        if (!mCheckBoxes[i].isChecked()) {
                            mCheckBoxes[i].setChecked(true);
                        }
                    }
                } else {
                    for (int i = 0; i < mCheckBoxes.length; i++) {
                        if (mCheckBoxes[i].isChecked()) {
                            mCheckBoxes[i].setChecked(false);
                        }
                    }
                }
            }
        });
        //处理测试总时间
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//测试时间
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String mSpinnerTime = (String) mSpinner.getSelectedItem();
                String str = mSpinnerTime.substring(0, mSpinnerTime.length() - 1);
                float time = Float.parseFloat(str);
                Log.d(TAG, time + "");
                mTime = (int) (time * 60 * 60);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    //测试结束后将结果显示到界面
    @Override
    protected void onResume() {
        super.onResume();
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initTestItem();
                for (int i = 0, j = 0; i < mCheckBoxes.length; i++, j++) {
                    if (!mCheckBoxes[i].isChecked()) {
                        mFragments.remove(j);
                        j--;
                    }else{
                        id=i;
                    }
                }
                TestResultToFile.deleteFile();
                if (mFragments.size() == 1) {
                    Intent intent = new Intent(RunInActivity.this, RunInItemActivity.class);
                    intent.putExtra(TEST_ITEM_TIME, mTime);
                    intent.putExtra("id",id);
                    startActivity(intent);
                }else if (mFragments.size()>0){
                    Intent intent = new Intent(RunInActivity.this, RunInItemActivity.class);
                    intent.putExtra(TEST_ITEM_TIME, mTime);
                    startActivity(intent);
                }
            }
        });


        mTestResults = mTestResultLab.getResults();
        if (mTestResults.size() > 0) {
            for (int i = 0; i < mTestResults.size(); i++) {
                if (mTestResults.get(i).getTestItemName().equalsIgnoreCase("vedio")) {
                    mVedioResult.setText(mTestResults.get(i).getTestItemResult());
                }
                if (mTestResults.get(i).getTestItemName().equalsIgnoreCase("2d")) {
                    mTwoDResult.setText(mTestResults.get(i).getTestItemResult());
                }
                if (mTestResults.get(i).getTestItemName().equalsIgnoreCase("3d")) {
                    mThreeDResult.setText(mTestResults.get(i).getTestItemResult());
                }
                if (mTestResults.get(i).getTestItemName().equalsIgnoreCase("cpu")) {
                    mCPUResult.setText(mTestResults.get(i).getTestItemResult());
                }
                if (mTestResults.get(i).getTestItemName().equalsIgnoreCase("emmc")) {
                    mEMMCResult.setText(mTestResults.get(i).getTestItemResult());
                }
                if (mTestResults.get(i).getTestItemName().equalsIgnoreCase("ddr")) {
                    mDDRResult.setText(mTestResults.get(i).getTestItemResult());
                }
                if (mTestResults.get(i).getTestItemName().equalsIgnoreCase("lcd")) {
                    mLCDResult.setText(mTestResults.get(i).getTestItemResult());
                }
                if (mTestResults.get(i).getTestItemName().equalsIgnoreCase("wifi")) {
                    mWIFIResult.setText(mTestResults.get(i).getTestItemResult());
                }
                if (mTestResults.get(i).getTestItemName().equalsIgnoreCase("bluetooth")) {
                    mBTResult.setText(mTestResults.get(i).getTestItemResult());
                }
                if (mTestResults.get(i).getTestItemName().equalsIgnoreCase("battery")) {
                    mBatteryResult.setText(mTestResults.get(i).getTestItemResult());
                }
            }
        }
    }

    //测试项默认全选
    private void initTestItem() {
        mFragments.add(new VedioFragment());
        mFragments.add(new TwoDFragment());
        mFragments.add(new ThreeDFragment());
        mFragments.add(new CPUFragment());
        mFragments.add(new EMMCFragment());
        mFragments.add(new DDRFragment());
        mFragments.add(new LCDFragment());
        mFragments.add(new WifiFragment());
        mFragments.add(new BtFragment());
        mFragments.add(new BatteryFragment());
    }

    //提供接口获取测试项
    public static List<Fragment> getFragments() {
        return mFragments;
    }

    //动态改变全选复选框状态
    @Override
    public void onClick(View view) {
        if (mVedioBox.isChecked()&&mTwoDBox.isChecked()&&mThreeDBox.isChecked()
                &&mCPUBox.isChecked()&&mDDRBox.isChecked()&&mEMMCBox.isChecked()
                &&mLCDBox.isChecked()&&mWIFIBox.isChecked()&&mBTBox.isChecked()
                &&mBatteryBox.isChecked()){
            mCheckBoxAll.setChecked(true);
        }else {
            mCheckBoxAll.setChecked(false);
        }
    }
}