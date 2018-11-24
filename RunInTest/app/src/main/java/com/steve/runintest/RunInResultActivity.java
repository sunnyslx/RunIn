package com.steve.runintest;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.steve.runintest.db.TestResultLab;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by steve on 17-11-4.
 */

public class RunInResultActivity extends AppCompatActivity {

    private static final String TAG = "RunInResultActivity";
    @BindView(R.id.run_in_test_result)
    TextView showResultText;
    private TestResultLab mTestResultLab;
    @BindView(R.id.run_in_test_item_result) TextView mShowTestItemResult;
    @BindView(R.id.run_in_test_server_result) TextView mShowServerResult;
    private Boolean mServerResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_result_run_in);
        ButterKnife.bind(this);

        mTestResultLab=TestResultLab.get(getApplicationContext());

        SharedPreferences sp=getSharedPreferences("server_result",MODE_PRIVATE);
        mServerResult=sp.getBoolean("server",true);

        if (mTestResultLab.finalResult()&&mServerResult){
            showResultText.setText("pass");
            showResultText.setTextColor(Color.GREEN);
        } else {
            showResultText.setText("fail");
            showResultText.setTextColor(Color.RED);
            if (!mTestResultLab.finalResult()){
                mShowTestItemResult.setVisibility(View.VISIBLE);
                mShowTestItemResult.setText("test item is fail,if you want to see the detail please click the back button");
                mShowTestItemResult.setTextColor(Color.RED);
            }
            if (!mServerResult){
                mShowServerResult.setVisibility(View.VISIBLE);
                mShowServerResult.setText("upload or download to server is fail");
                mShowServerResult.setTextColor(Color.RED);
            }
        }
    }
}