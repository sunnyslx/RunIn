package com.steve.runintest.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by steve on 17-12-1.
 */

public class AutoConnectWifi {
    private static final String TAG = "WifiAdmin";
    private WifiManager mWifiManager;

    // 构造器
    public AutoConnectWifi(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);// 取得WifiManager对象
    }

    public WifiManager getWifiManager() {
        return mWifiManager;
    }

    // 打开WIFI
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    // 添加一个网络并连接
    public void addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b =  mWifiManager.enableNetwork(wcgID, true);
        Log.d(TAG,wcg.toString());
        Log.d(TAG,wcgID+":"+b);
    }

    //然后是一个实际应用方法，只验证过没有密码的情况：
    // 一种是WEP，一种是WPA，还有没有密码的情况
    public WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();       //该配置支持的身份验证协议集合
        config.allowedGroupCiphers.clear();     //该配置所支持的组密码集合
        config.allowedKeyManagement.clear();        //该配置所支持的密钥管理集合
        config.allowedPairwiseCiphers.clear();      //该配置所支持的WPA配对密码集合
        config.allowedProtocols.clear();        //该配置所支持的安全协议集合
        config.SSID = "\"" + SSID + "\"";       //Wifi网络名称

        WifiConfiguration tempConfig = this.isExsits(SSID);
        if(tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if(Type == 1) //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if(Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;           //设置该项会使一个网络不广播其SSID，因此这种特定的SSID只能用于浏览
            config.wepKeys[0]= "\""+Password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if(Type == 3) //WIFICIPHER_WPA
        {
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

    //判断该网络是否存在
    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if(existingConfigs!=null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }
}
