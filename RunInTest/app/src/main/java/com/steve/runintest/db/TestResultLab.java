package com.steve.runintest.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 17-11-20.
 */

public class TestResultLab {
    private static final String TAG = "TestResultLab";
    private static TestResultLab sTestResultLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static TestResultLab get(Context context) {
        Log.d("111111111","11111111");
        if (sTestResultLab == null) {
            sTestResultLab = new TestResultLab(context);
        }
        return sTestResultLab;
    }

    private TestResultLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new ResultToDbHelper(mContext).getWritableDatabase();
    }

    //添加新crime
    public void addTestResult(TestResult result) {
        ContentValues values = new ContentValues();
        Log.d(TAG,result.getTestItemResult()+":"+result.getTestItemName());
        values.put("testItemName", result.getTestItemName());
        values.put("testItemResult", result.getTestItemResult());
        mDatabase.insert("testItems", null, values);
    }

    //修改crime信息
    public void updateResult(TestResult result) {
        ContentValues values = new ContentValues();
        values.put("testItemResult", result.getTestItemResult());
        Log.d(TAG,result.getTestItemResult()+":"+result.getTestItemName());
        //mDatabase.execSQL("update testItems set testItemResult=? where testItemName=?",new String[]{result.getTestItemResult(),result.getTestItemName()});
        mDatabase.update("testItems", values, "testItemName=?", new String[]{result.getTestItemName()});
    }

    public void deleteResult(){
        mDatabase.delete("testItems",null,null);
    }

    //获取Result集
    public List<TestResult> getResults() {
        List<TestResult> testResults = new ArrayList<>();
        TestResult tr;
        Cursor cursor = mDatabase.query("testItems", null, null, null, null, null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                tr=new TestResult(cursor.getString(cursor.getColumnIndex("testItemName")),cursor.getString(cursor.getColumnIndex("testItemResult")));
                testResults.add(tr);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return testResults;
    }

    //查找测试项是否存在
    public boolean isTestItem(String testItemName) {
        boolean flag=false;
        Cursor cursor= mDatabase.query("testItems",null,"testItemName=?",new String[]{testItemName},null,null,null);
        Log.d(TAG,cursor.getCount()+"");
        if (cursor.getCount()>0){
            cursor.moveToFirst();
            Log.d(cursor.getColumnName(0),cursor.getColumnName(1));
            flag=true;
        }
        return flag;
    }

    //最终结果
    public boolean finalResult(){
        List<TestResult> results=getResults();
        Log.d(TAG,results.size()+"");
        boolean flag=true;
        for (int i=0;i<results.size();i++){
            TestResult tr=results.get(i);
            Log.d(tr.getTestItemName(),tr.getTestItemResult());
            if (tr.getTestItemResult()=="fail"){
                flag=false;
                return flag;
            }
        }
        return flag;
    }
}
