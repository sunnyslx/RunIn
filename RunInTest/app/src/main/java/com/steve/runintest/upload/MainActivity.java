package com.steve.runintest.upload;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.steve.runintest.R;
import com.steve.runintest.RunInResultActivity;
import com.steve.runintest.upload.tools.DetectEnvironment;
import com.steve.runintest.upload.tools.FileOperate;
import com.steve.runintest.upload.tools.MsgDialog;
import com.steve.runintest.upload.tools.MsgToast;
import com.steve.runintest.upload.tools.SDCardTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    //文件目录
    private static final String DIR_DOWNLOAD = "sdcard/fcl_download";
    private static final String DIR_UPLOAD = "sdcard/fcl_upload";

    //服务器端地址
    private static final String URL_UPLOAD = "http://192.168.0.100:8080/file_server/upload";
    private static final String URL_LIST = "http://192.168.0.100:8080/file_server/list";
    //此地址需要带参filename, 否则返回错误值500
    private static final String URL_DOWNLOAD = "http://192.168.0.100:8080/file_server/download";
    //此地址需要带参
    private static final String URL_DELETE = "http://192.168.0.100:8080/file_server/delete";
    //文件名分隔符
    private static final String sign = "@";

    //文件上传最大限制
    private static final long maxLenPerFile = 104857600; //100MB
    private static final long maxLenAllFiles = 209715200; //200MB

    private static Gson GSON = new Gson();
    private static final String[] flag = {"true"};
    private int downloadNum = 0;
    private long downloadTime = 0;
    private long downloadLength = 0;

    private TextView stateInfo;
    private TextView uploadRateInfo;
    private TextView downloadRateInfo;
    private TextView compareResult;
    private ListView listViewUpload;
    private ListView listViewDownload;

    private List<String> filesToUploadList; //文件名
    private List<String> filesDownloadList; //文件名
    private List<String> itemNameList;
    private List<String> itemRealNameList;

    private long totalSize;
    private String mac;
    private Context mContext;

    private HandleInfo handler;

    class HandleInfo extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x001:
                    String url = (String) msg.obj;
                    if (url.equals(URL_UPLOAD)) {
                        // 关闭点击上传，打开自动上传
                        Log.d("自动上传开启","Test");
                        listUploadFiles(DIR_UPLOAD, false, true);

                    } else if (url.equals(URL_LIST)) {
                        Log.d("自动下载开启","Test");
                        //主要作用：点击update时，刷新listView
                        listDownloadFiles(URL_LIST, false, false);
                    }
                    break;

                case 0x002:
//Todo:server handler fail
                    SharedPreferences.Editor editor=getSharedPreferences("server_result",MODE_PRIVATE).edit();
                    editor.putBoolean("server",false);
                    editor.commit();
                    Intent intentFail = new Intent(MainActivity.this.getApplicationContext(), RunInResultActivity.class);
                    startActivity(intentFail);
                    finish();

                    stateInfo.setText("url is unavailable");
                    Toast.makeText(mContext, "Url is unavailable，please check the server", Toast.LENGTH_LONG).show();


                    break;

                case 0x100:
                    //上传速率更新
                    String upRate = msg.obj + " kb/s";
                    uploadRateInfo.setText(upRate);
                    //刷新符合本机MAC的文件清单
                    //关闭可点击下载，开启自动下载符合本机MAC的所有文件功能
                    listDownloadFiles(URL_LIST, true, true);
                    break;

                case 0x101:
                    clearInfoUp();
                    break;

                case 0x200:
                    String downRate = msg.obj + " kb/s";
                    // 更新下载速率后，自动开始校验文件的MD5值
                    downloadRateInfo.setText(downRate);
                    compareAuto(DIR_UPLOAD, DIR_DOWNLOAD);

                    break;

                case 0x201:
                    clearInfoDown();
                    break;

                case 0x300:
                    //状态更新
                    String state = (String) msg.obj;
                    stateInfo.setText(state);
                    break;

                case 0x400:
                    boolean cmpResult = (boolean) msg.obj;
                    if (cmpResult) {
                        compareResult.setText("PASS");
                        compareResult.setTextColor(Color.BLUE);
                        listDownloadFiles(URL_LIST, true, false);
                    } else {
                        compareResult.setText("FAIL");
                        compareResult.setTextColor(Color.RED);
                    }


//Todo:server handler complete
                    Intent intentPass=new Intent(MainActivity.this, RunInResultActivity.class);
                    startActivity(intentPass);

                   // MainActivity.this.finish();
                    finish();
                    break;

                case 0x500:
//Todo:server handler fail
                    SharedPreferences.Editor editor2=getSharedPreferences("server_result",MODE_PRIVATE).edit();
                    editor2.putBoolean("server",false);
                    editor2.commit();
                    Intent intentFail2 = new Intent(MainActivity.this.getApplicationContext(), RunInResultActivity.class);
                    startActivity(intentFail2);

                    String errorStr = (String) msg.obj;
                    Log.i("File size", errorStr);
                    MsgToast.show(mContext, errorStr);


                    finish();
                    break;
                default:
                    break;
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        SharedPreferences.Editor editor = getSharedPreferences("server_result", MODE_PRIVATE).edit();
        editor.clear().commit();

        Button btnFilesUp;
        Button btnFilesDown;

        handler = new HandleInfo();
        mContext = this;
        mac = getIntent().getStringExtra("mac");

        stateInfo =  findViewById(R.id.state_info);
        uploadRateInfo =  findViewById(R.id.rate_upload_info);
        downloadRateInfo =  findViewById(R.id.rate_download_info);
        compareResult =  findViewById(R.id.compare_result);

        btnFilesUp = findViewById(R.id.btn_upload_files);
        listViewUpload = findViewById(R.id.listView_upload_files);

        btnFilesDown = findViewById(R.id.btn_download_files);
        listViewDownload = findViewById(R.id.listView_download_files);
        Log.d("监测进入Main的AC","接下来开始启动。。");
        // 检测Url，并启动上传
        detectUrl(URL_UPLOAD);
        clearAll();

        btnFilesUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAll();
                detectUrl(URL_UPLOAD);

            }
        });

        btnFilesDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                detectUrl(URL_LIST);

            }
        });

    }

    /**
     * 功能：显示指定上传目录中可上传的文件
     *
     * @param dir         上传文件所在目录
     * @param clickEnable 是否可点击Item选项上传指定文件
     * @param autoEnable  是否开启自动上传
     */
    private void listUploadFiles(final String dir, final boolean clickEnable, final boolean autoEnable) {
        initUploadListViewData(dir, clickEnable, autoEnable);
    }

    /**
     * 功能：依据本地指定目录中的文件清单，初始化ListView数据
     *
     * @param filesDir    上传文件所在目录
     * @param clickEnable 是否可点击上传
     * @param autoEnable  是否自动上传
     */
    private void initUploadListViewData(final String filesDir, final boolean clickEnable, final boolean autoEnable) {
        Log.d("filePath",filesDir);
        try {
            File dirs = new File(filesDir);
            // 如果没有指定目录，自动创建
            if (!dirs.exists()) {
                if (dirs.mkdirs()) {
                    Log.i("Upload dir", "Create success");
                } else {
                    Log.e("Upload dir", "Create failed");
                }
            }

            final ArrayList<String> filesLocalList = (ArrayList<String>) FileOperate.getFiles(dirs, false);
            int listSize = 0;
            if (filesLocalList != null) {
                listSize = filesLocalList.size();
                Log.i("filesLocalList", "" + filesLocalList.size());
            }

            ArrayAdapter<String> adapterUp = new ArrayAdapter<>(mContext, R.layout.layout_listview_line, R.id.listView_line, filesLocalList);
            listViewUpload.setAdapter(adapterUp);

            if (listSize > 0) {
                // 实现点击上传单个文件
                if (clickEnable) {
                    listViewUpload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            final int itemNum = position;
                            MsgDialog msgDialog = new MsgDialog(mContext, "Upload file: " + filesLocalList.get(itemNum) + "？", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    uploadFile(URL_UPLOAD, new String[]{filesLocalList.get(itemNum)});

                                }
                            });
                            msgDialog.show();
                        }
                    });
                }

                // 实现自动上传所有文件
                if (autoEnable) {
                    String[] files = new String[filesLocalList.size()];
                    filesLocalList.toArray(files);
                    uploadFile(URL_UPLOAD, files);
                }

            } else {
                MsgToast.show(mContext, "The directory of files to upload is empty");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能：上传文件
     *
     * @param url   文件上传的服务器地址
     * @param files 待上传文件路径名数组
     */
    public void uploadFile(final String url, final String[] files) {

        if (SDCardTest.sdcardState() != 1) {
            MsgToast.show(this, "SDCARD is unavailable");
            return;
        }

        //  该类通常用在android应用程序中创建异步GET, POST, PUT和DELETE HTTP请求
        // 请求参数通过RequestParams实例创建
        // 响应通过重写匿名内部类 ResponseHandlerInterface的方法处理
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams param = new RequestParams();//用于创建AsyncHttpClient实例中的请求参数(包括字符串或者文件)的集合

        filesToUploadList = new ArrayList<>();
        try {
            File[] fileArray = new File[files.length];
            totalSize = 0;
            long len;
            String filePath;
            for (int i = 0; i < files.length; i++) {
                fileArray[i] = new File(files[i]);
                len = fileArray[i].length();//文件大小
                filePath = fileArray[i].getAbsolutePath();
                totalSize += len;
                //检测单个文件是否超出限制
                if (!checkFile(len, maxLenPerFile)) {
                    sendMsg(0x500, "The size can not be larger than 100MB. File: " + filePath);
                    return;
                }
                //保存上传文件名
                filesToUploadList.add(fileArray[i].getName());
            }

            //检测所有文件大小是否超出限制
            if (!checkFile(totalSize, maxLenAllFiles)) {
                sendMsg(0x500, "The total size can not be larger than 200MB");
                return;
            }

            param.put("file", fileArray);
            if (mac != null && !mac.equals("")) {
                param.put("mac", mac);
            } else {
                param.put("mac", "Mac value is empty");
            }

            httpClient.post(url, param, new AsyncHttpResponseHandler() {
                //用于拦截和处理由AsyncHttpClient创建的请求。
                // 在匿名类AsyncHttpResponseHandler中的重写 onSuccess(int, org.apache.http.Header[], byte[])方法用于处理响应成功的请求。
                // 此外，你也可以重写 onFailure(int, org.apache.http.Header[], byte[], Throwable), onStart(), onFinish(),
                // onRetry() 和onProgress(int, int)方法
                long startTime;
                long endTime;

                @Override
                public void onStart() {
                    super.onStart();
                    startTime = System.currentTimeMillis();
                    //先清空和上传相关的UI
                    sendMsg(0x101, "");
                    Log.d("文件传递","post内容");
                    Log.d("服务器地址",url);
                    //更新状态
                    sendMsg(0x300, "Upload...");
                }

                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                    endTime = System.currentTimeMillis();
                    long useTime = endTime - startTime;

                    Log.i("Upload success>", new String(bytes));
                    //更新状态
                    sendMsg(0x300, "Upload success");
                    //更新上传速率
                    sendMsg(0x100, rate(totalSize, useTime).toString());
                }

                @Override
                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                    if (bytes != null) {
                        Log.i("Upload failure>", new String(bytes));
                    } else {
                        Log.d("Upload failure>", "onFailure: ");
                    }

                    sendMsg(0x300, "Upload fail");
                }

            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            MsgToast.show(MainActivity.this, "File to upload does not exist！");
        } catch (Exception e) {
            sendMsg(0x300, "Upload fail");
            e.printStackTrace();
        }
    }

    /**
     * 功能：显示服务器上可下载的文件
     * @param url         服务器获取文件清单请求地址， 返回json数据
     * @param clickEnable 是否可点击
     * @param autoEnable  是否自动下载
     */
    private void listDownloadFiles(String url, final boolean clickEnable, final boolean autoEnable) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams param = new RequestParams();
        try {
            httpClient.post(url, param, new AsyncHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                    LinkedTreeMap<String, String> map = (LinkedTreeMap) GSON.fromJson(new String(bytes), Map.class);

                    itemNameList = new ArrayList<>();
                    itemRealNameList = new ArrayList<>();

                    if (map.size() > 0) {
                        for (String key : map.keySet()) {
                            Log.i("List success, key>", key);
                            Log.i("List success. value>", map.get(key));

                            if (mac != null && !mac.equals("") && key.startsWith(mac)) {
                                // 带MAC文件名
                                itemNameList.add(key);
                                // 不带MAC的原文件名
                                itemRealNameList.add(map.get(key));
                                if (itemNameList.size() == 0) {
                                    MsgToast.show(mContext, "No files signed with the device's mac");

                                }
                            }

                        }

                    } else {
                        MsgToast.show(mContext, "No files can be download on server");
                    }

                    // 初始化可下载的listView列表
                    initDownloadListViewData(clickEnable, autoEnable);

                }

                @Override
                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                    Log.i("List failure>", new String(bytes));
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取服务器中符合本机的文件清单，并初始化ListView的数据
     *
     * @param clickEnable 是否可单击下载
     * @param autoEnable  是否自动下载
     */
    private void initDownloadListViewData(final boolean clickEnable, final boolean autoEnable) {

        BaseAdapter adapterDown;
        adapterDown = new BaseAdapter() {
            @Override
            public int getCount() {
                return itemNameList.size();
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup viewGroup) {
                System.out.println("getView " + position + " " + convertView);
                LinearLayout layout;
                Holder holder;
                if (convertView == null) {
                    layout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_listview_line, null);
                    TextView textView = (TextView) layout.findViewById(R.id.listView_line);
                    textView.setText(itemNameList.get(position));
                    holder = new Holder();
                    holder.textView = textView;
                    layout.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                    holder.textView.setText(itemNameList.get(position));
                    return convertView;
                }

                return layout;
            }
        };

        listViewDownload.setAdapter(adapterDown);

        if (itemNameList != null && itemNameList.size() > 0) {
            if (clickEnable) {
                listViewDownload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        final int itemNum = position;

                        MsgDialog msgDialog = new MsgDialog(mContext, "Download file: " + itemNameList.get(itemNum) + "？", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                downloadNum = 1;
                                downloadLength = 0;
                                downloadTime = 0;
                                //下载指定的文件
                                downloadFile(URL_DOWNLOAD, itemNameList.get(itemNum), DIR_DOWNLOAD, itemRealNameList.get(itemNum));
                            }
                        });
                        msgDialog.show();

                    }
                });

            }

            if (autoEnable) {
                //自动下载刚上传的所有文件
                downloadNum = filesToUploadList.size();
                downloadLength = 0;
                downloadTime = 0;

                for (int i = 0; i < filesToUploadList.size(); i++) {
                    downloadFile(URL_DOWNLOAD, mac + sign + filesToUploadList.get(i), DIR_DOWNLOAD, filesToUploadList.get(i));
                    Log.i("Auto download", i + "");

                }


            }
        }
    }

    class Holder {
        TextView textView;
    }

    /**
     * 功能：下载文件
     *
     * @param urlStr   文件下载服务器地址
     * @param fileName 需下载的文件全名(含MAC)，
     * @param savePath 保存的目录路径
     * @param saveName 移除MAC后的原名，亦即保存文件名
     */
    public void downloadFile(final String urlStr, final String fileName, final String savePath, final String saveName) {
        filesDownloadList = new ArrayList<>();
        new Thread() {
            @Override
            public void run() {

                PrintWriter out = null;
                InputStream is = null;
                FileOutputStream fos = null;

                try {
                    URL url = new URL(urlStr);
                    // 使用URLConnection 下载
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setRequestProperty("accept", "*/*");
                    urlConnection.setRequestProperty("connection", "Keep-Alive");
                    urlConnection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);

                    out = new PrintWriter(urlConnection.getOutputStream());
                    out.print("filename=" + fileName);
                    out.flush();

                    try {
                        // 保存到文件
                        is = urlConnection.getInputStream();
                        File file;
                        if ((file = FileOperate.prepareFile(savePath, saveName)) != null) {
                            fos = new FileOutputStream(file);
                            byte[] buffer = new byte[2048];
                            int hasRead;

                            if (downloadNum != 0) {
                                //先清空和下载有关的UI信息
                                sendMsg(0x201, "");
                                //开始下载
                                sendMsg(0x300, "Download...");
                            }

                            long startTime = System.currentTimeMillis();
                            while ((hasRead = is.read(buffer)) > 0) {
                                fos.write(buffer, 0, hasRead);
                            }
                            long endTime = System.currentTimeMillis();
                            long useTime = endTime - startTime;

                            //同一时间只允许一个线程操作此部分
                            synchronized (flag) {
                                downloadNum--;
                                downloadTime += useTime;
                                downloadLength += file.length();
                                filesDownloadList.add(file.getName());
                                if (downloadNum == 0) {
                                    Log.i("Download", "Success");
                                    Log.i("Download", "Num:" + downloadNum + ", Time:" + downloadTime + ", Len:" + downloadLength);

                                    //发送消息，更新下载速率
                                    sendMsg(0x200, rate(downloadLength, downloadTime).toString());
                                    //发送完成消息，更新状态
                                    sendMsg(0x300, "Download success");

                                }
                            }

                        }

                    } catch (Exception e1) {
                        sendMsg(0x300, "Download fail");
                        e1.printStackTrace();
                    }

                } catch (IOException e2) {
                    sendMsg(0x300, "Download fail");
                    e2.printStackTrace();

                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }

                }

            }
        }.start();

    }

    /**
     * 功能：自动比较下载后的全部文件
     *
     * @param uploadDir   上传文件目录
     * @param downloadDir 下载文件目录
     */
    private void compareAuto(final String uploadDir, final String downloadDir) {
        new Thread() {
            public void run() {
                boolean result = false;

                if (filesDownloadList != null && filesDownloadList.size() > 0) {

                    for (String name : filesDownloadList) {
                        File tarFile = new File(downloadDir, name);
                        File localFile = new File(uploadDir, name);
                        if (tarFile.exists()) {
                            if (localFile.exists()) {
                                if (FileOperate.compare(localFile, tarFile)) {
                                    result = true;
                                    Log.i("File", tarFile.getAbsolutePath() + " compare pass");
                                } else {
                                    result = false;
                                    Log.e("File", tarFile.getAbsolutePath() + " compare fail");
                                    break;
                                }
                            } else {
                                result = false;
                                Log.e("File", localFile.getAbsolutePath() + " does not exist");
                                break;
                            }

                        } else {
                            result = false;
                            Log.e("File", tarFile.getAbsolutePath() + " does not exist");
                            break;
                        }
                    }

                }

                sendMsg(0x400, result);
            }
        }.start();

    }

    /**
     * 功能：清除服务器上文件
     *
     * @param url 删除资源请求地址
     */
    public void deleteFileRequest(String url) {

        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams param = new RequestParams();

        try {

            for (int i = 0; i < filesDownloadList.size(); i++) {
                param.add("file" + i, mac + sign + filesDownloadList.get(i));
            }

            httpClient.post(url, param, new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {

                    Log.i("Delete success>", new String(bytes));

                }

                @Override
                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {

                    Log.i("Delete failure>", new String(bytes));

                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能：检测url是否可用，并会启动上传或下载
     *
     * @param url 检测地址
     */
    private void detectUrl(String url) {
        Log.d("检测地址","Test");
        DetectEnvironment.detect(this, url, handler);
    }

    private void clearAll() {
        stateInfo.setText("");
        uploadRateInfo.setText("");
        downloadRateInfo.setText("");
        compareResult.setText("");
    }

    private void clearInfoUp() {
        stateInfo.setText("");
        uploadRateInfo.setText("");
    }

    private void clearInfoDown() {
        stateInfo.setText("");
        downloadRateInfo.setText("");
        compareResult.setText("");
    }

    /**
     * @param len 文件大小
     * @param maxLen 限制大小
     * @return 超出限制，返回false
     */
    private boolean checkFile(long len, long maxLen) {
        if (len > maxLen) {
            return false;
        }
        return true;
    }

    // KB/s
    public BigDecimal rate(long size, long time) {
        BigDecimal rate;
        Log.i("Size", size + " bytes");
        Log.i("Use time", time + " ms");
        if (size == 0 || time == 0) {
            rate = new BigDecimal(0);
        } else {
            rate = new BigDecimal(size * 1000).divide(new BigDecimal(time * 1024), 2, BigDecimal.ROUND_UP);

        }
        return rate;
    }

    /**
     * 功能：发送消息
     *
     * @param code   消息代码
     * @param msgStr 消息内容
     */
    private void sendMsg(int code, Object msgStr) {
        Message msg = Message.obtain();
        msg.what = code;
        msg.obj = msgStr;
        handler.sendMessage(msg);
    }

}
