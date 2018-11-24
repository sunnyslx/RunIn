package com.steve.runintest;



/**
 * Created by steve on 17-11-21.
 */

public class Result {
    private int mTestTime;
    private String mTestItem;
    private String mStartTime;
    private String mEndTime;
    private String mRunTime;
    private String mResultData;

    public Result(int testTime, String testItem, String startTime, String endTime, String runTime, String resultData) {
        mTestTime = testTime;
        mTestItem = testItem;
        mStartTime = startTime;
        mEndTime = endTime;
        mRunTime = runTime;
        mResultData = resultData;
    }

    public int getTestTime() {
        return mTestTime;
    }

    public String getTestItem() {
        return mTestItem;
    }

    public String  getStartTime() {
        return mStartTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public String getRunTime() {
        return mRunTime;
    }

    public String getResultData() {
        return mResultData;
    }
}
