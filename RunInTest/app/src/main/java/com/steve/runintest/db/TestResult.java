package com.steve.runintest.db;

/**
 * Created by steve on 17-11-20.
 */

public class TestResult {
    private String mTestItemName;
    private String mTestItemResult;

    public TestResult(String testItemName, String testItemResult) {
        mTestItemName = testItemName;
        mTestItemResult = testItemResult;
    }

    public String getTestItemName() {
        return mTestItemName;
    }

    public String getTestItemResult() {
        return mTestItemResult;
    }
}
