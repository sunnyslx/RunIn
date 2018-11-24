package com.steve.runintest.upload;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class WifiAdmin {
    private WifiManager wifiManager;

//    /**
//     * 向UI发送消息
//     *
//     * @param info 消息
//     */
//    public void sendMsg(int code, String info) {
//        if (mHandler != null) {
//            Message msg = Message.obtain();
//            msg.what = code;
//            msg.obj = info;
//            mHandler.sendMessage(msg);// 向Handler发送消息
//        } else {
//            Log.e("wifi", info);
//        }
//    }

    // 构造函数
    public WifiAdmin(Context context) {
        wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
    }

    // 提供一个外部接口，传入要连接的无线网
    public void connect(String ssid, String password, WifiCipherType type) {
        Thread thread = new Thread(new ConnectRunnable(ssid, password, type));
        thread.start();
    }

    // 返回以前配置过的所有无线网络信息列表
    private List<WifiConfiguration> wifiList() {
         return wifiManager.getConfiguredNetworks();
    }

    // 创建一个网络平配置
    private WifiConfiguration createWifiInfo(String SSID, String Password,
                                             WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // nopass
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(KeyMgmt.NONE);
        }
        // wep
        if (Type == WifiCipherType.WIFICIPHER_WEP) {
            if (!TextUtils.isEmpty(Password)) {
                if (isHexWepKey(Password)) {
                    config.wepKeys[0] = Password;
                } else {
                    config.wepKeys[0] = "\"" + Password + "\"";
                }
            }
            config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        // wpa
        if (Type == WifiCipherType.WIFICIPHER_WPA) {
//            config.preSharedKey = "\"" + Password + "\"";
//            config.hiddenSSID = false;
//            config.allowedAuthAlgorithms
//                    .set(AuthAlgorithm.OPEN);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//            config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
//            config.allowedPairwiseCiphers
//                    .set(WifiConfiguration.PairwiseCipher.TKIP);
//            // 此处需要修改否则不能自动重联
//            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//            config.allowedPairwiseCiphers
//                    .set(WifiConfiguration.PairwiseCipher.CCMP);
//            config.status = WifiConfiguration.Status.ENABLED;

            Log.d("此消息说明：","正在进行指定wifi的连接校验");
            config.preSharedKey = "\""+Password+"\"";       //WPA-PSK使用的预共享密钥
            config.hiddenSSID = true;           //设置该项会使一个网络不广播其SSID，因此这种特定的SSID只能用于浏览
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);//该配置支持的身份验证协议集合     公认的IEEE 802.11认证算法
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);//该配置所支持的组密码集合       公认的组密码
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);//该配置所支持的密钥管理集合      公认的密钥管理方案
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);//该配置所支持的WPA配对密码集合     公认的WPA配对密码
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);//该配置所支持的组密码集合       公认的组密码
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);       //该配置所支持的WPA配对密码集合      公认的WPA配对密码
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    class ConnectRunnable implements Runnable {
        private String ssid;

        private String password;

        private WifiCipherType type;

        public ConnectRunnable(String ssid, String password, WifiCipherType type) {
            this.ssid = ssid;
            this.password = password;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                // 打开wifi
                openWifi();
                Log.d("此消息说明：", "wifi被打开了");

                Thread.sleep(200);

                // 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
                while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                    Log.d("此消息说明:","wifi处于开启中");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                    }
                }

                WifiConfiguration wifiConfig = createWifiInfo(ssid, password,
                        type);

                Log.d("指定wifi连接后的配置属性的SSID：",wifiConfig.SSID);
                Log.d("指定wifi连接后的配置属性的所有属性：",wifiConfig.toString());
                if (wifiConfig == null) {
                    Log.i("wifi cfg", "wifiConfig is null!");
                    return;
                }

                //清空wifi列表
                List<WifiConfiguration> list = wifiList();
                if(list!=null){
                    if(list.size()>0) {
                        for (WifiConfiguration wf : list) {
                            wifiManager.removeNetwork(wf.networkId);
                        }
                    }
                }

                //添加现在的wifi
                int netID = wifiManager.addNetwork(wifiConfig);
                boolean enabled = wifiManager.enableNetwork(netID, true);

                Log.i("isEnable", "" + enabled);
                boolean connected = wifiManager.reconnect();
                Log.i("isReconnect", "" + connected);
                Log.i("success", "连接成功!");

            } catch (Exception e) {
                // TODO: handle exception
                Log.e("wifi",  "connect error: " +e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();

        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }

        return isHex(wepKey);
    }

    private static boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            final char c = key.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f')) {
                return false;
            }
        }

        return true;
    }



    // 打开wifi功能
    synchronized public boolean openWifi() {
        boolean result = true;
        if (!wifiManager.isWifiEnabled()) {
            result = wifiManager.setWifiEnabled(true);
        }
        return result;
    }

    // 检查WIFI功能的当前状态（0：正关闭，1：已关闭， 2：正打开， 3：已打开， 4：未知）
    public int checkState(){
        return  wifiManager.getWifiState();
    }

    // 关闭WIFI
    synchronized public boolean closeWifi(){
        boolean result = true;
        if (wifiManager.isWifiEnabled()) {
            result = wifiManager.setWifiEnabled(false);
        }
        return result;
    }

    public String getLocalMac() {
        WifiInfo mWifiInfo = wifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    public String getSSID(){
        WifiInfo mWifiInfo = wifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
    }

    public String getRouterMac(){
        WifiInfo mWifiInfo = wifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }


}