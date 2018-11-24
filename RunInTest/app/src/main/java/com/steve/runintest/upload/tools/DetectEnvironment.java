package com.steve.runintest.upload.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by derik on 17-2-15.
 */

public class DetectEnvironment {

    /**
     * 功能：检测url是否接收请求
     * 描述：先检测网络是否可用，再和指定的url建立连接，依据不同结果发送消息给主线程处理
     *
     * @param ctx     上下文
     * @param url     指定url
     * @param handler 主线程handler
     */
    public static void detect(Context ctx, String url, Handler handler) {
        //url="http://192.168.0.100:8080/file_server/upload";
        if (isNetworkAvailable(ctx)) {
            Log.d("检测请求","Test");
            connect(handler, url);
        } else {
            MsgToast.show(ctx, "Fail, please check the wifi connection");
        }

    }

    //判断网络是否可用
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    Log.d("当前网络是可以使用的","Test");
                    return true;
                }
            }
        }
        return false;
    }

    //连接url
    private synchronized static void connect(final Handler handler, final String urlStr) {

        if (handler == null || urlStr == null || urlStr.equals("")) {
            return;
        }

        new Thread() {
            int state = -1;
            int counts = 0;
            HttpURLConnection httpUrlConnection;

            @Override
            public void run() {

                while (counts < 3) {
                    try {
                        Log.d("URLStr:",urlStr);
                        URL url = new URL(urlStr);
                        httpUrlConnection = (HttpURLConnection) url.openConnection();
                        httpUrlConnection.setConnectTimeout(3 * 1000);
                        state = httpUrlConnection.getResponseCode();
                        Log.d("state check:",new Integer(state).toString());
                        if (state == 200) {
                            Log.d("aqaa","state is 200");
                            Message msg = Message.obtain();
                            msg.what = 0x001;
                            msg.obj = urlStr;
                            handler.sendMessage(msg);
                            break;
                        }

                    } catch (Exception ex) {
                        counts++;
                        Log.e("Url test", "url is unavailable, retry:" + counts);

                    } finally {
                        Log.i("State", "" + state);
                        httpUrlConnection.disconnect();
                    }

                }

                if (state != 200) {
                    handler.sendEmptyMessage(0x002);
                }
            }

        }.start();
    }

}
