package com.neekin.wifidemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "ggg";
    private Button btn1, btn2;
    private WifiManager wifiManager;
    private List<ScanResult> results;//扫描结果集合
    private List<WifiConfiguration> configurations;//已连接过的IFI集合
    private int networkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //申请权限
        requestPermiss();
    }

    /**
     * 申请权限
     */
    private void requestPermiss() {//申请位置信息可以使扫描WIFI可以得到的集合不为0，反之为0无结果
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Log.i(TAG, "requestPermissions  0: ");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // 显示给用户的解释
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                //TODO 利用share保存的参数来判断是否要询问权限
                return;
            }
        }
    }

    private void initView() {
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        configurations = wifiManager.getConfiguredNetworks();

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);//打开WIFI
            Log.i(TAG, "onCreate: 打开WIFI  没办法");
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//WIFI打开状态
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//WIFI扫描结果
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//网络状态改变
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);//热点连接结果通知广播
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                            WifiManager.WIFI_STATE_DISABLED);
                    switch (wifistate) {
                        case WifiManager.WIFI_STATE_DISABLED:
                            //wifi已关闭
                            Log.i(TAG, "onReceive: wifi已关闭");
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            //wifi已打开
                            Log.i(TAG, "onReceive: wifi已打开");
                            break;
                        case WifiManager.WIFI_STATE_ENABLING:
                            //wifi正在打开
                            Log.i(TAG, "onReceive: wifi正在打开");
                            break;
                        case WifiManager.WIFI_STATE_DISABLING:
                            Log.i(TAG, "onReceive: wifi正在关闭");
                            break;
                        case WifiManager.WIFI_STATE_UNKNOWN:
                            Log.i(TAG, "onReceive: wifi未知");
                            break;
                    }
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    boolean isScanned = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true);
                    if (isScanned) {
                        results = wifiManager.getScanResults();
                        for (int i = 0; i < results.size(); i++) {
                            for (int j = 0; j < configurations.size(); j++) {
                                if (configurations.get(j).SSID.equals("\"NILLKIN\"") && results.get(i).SSID.equals("NILLKIN")) {
                                    //wifiManager.enableNetwork(configurations.get(j).networkId, true);//连接指定已经连接过的网络
                                    return;
                                }
                            }
                        }
                    }
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    Log.i(TAG, "onReceive:千百次 " + info);
                    break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);
                    Log.i(TAG, "onReceive:不知道成功与否 " + error);
                    if (WifiManager.ERROR_AUTHENTICATING == error) {
                        //boolean isRemoved = wifiManager.removeNetwork(networkId);
                        Log.e(TAG, "onReceive: 密码错误,认证失败 ");
                    }
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
//                wifiManager.startScan();//周围热点扫描
                Log.i(TAG, "onReceive周围热点扫描: " + isNetworkConnected()+"   ");
                isNetworkOnline();
                //Toast.makeText(MainActivity.this,isNetworkOnline()+"",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn2:
                Log.i(TAG, "onClick: 连接指定热点");
                conn();
                break;
        }
    }

    private void conn() {
        AccessPoint ap = new AccessPoint();
        ap.setSsid("LieBaoWiFi704");
        ap.setPassword("09090909yy");//09090909yy
        ap.setEncryptionType("wpa");
        WifiConfiguration config = createConfiguration(ap);//创建一个配置：WifiConfiguration
        networkId = wifiManager.addNetwork(config);//生成一个networkId
        wifiManager.enableNetwork(networkId, true);//开始连接
    }

    /**
     * 检测网络是否连接
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    /***
     * 检测网络连通性
     */
    public void isNetworkOnline() {
        AsyncTask<String,String,Boolean> asyncTask=new AsyncTask<String, String, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                Runtime runtime = Runtime.getRuntime();
                try {
                    Process ipProcess = runtime.exec("ping -c 3 -w 100 www.baidu.com");
                    int exitValue = ipProcess.waitFor();
                    Log.i("ggg", "Process:"+exitValue);//exitValue总是返回2的问题，检查下有没有添加上网的权限
                    return (exitValue == 0);
                } catch (IOException | InterruptedException e) {
                    Log.i(TAG, "isNetworkOnline: "+e);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                Log.i(TAG, "onPostExecute: "+aBoolean);
                Toast.makeText(MainActivity.this,aBoolean+"",Toast.LENGTH_SHORT).show();
            }
        };
        asyncTask.execute();

    }

    public WifiConfiguration createConfiguration(AccessPoint ap) {
        String SSID =ap.getSsid() ;//WIFI名称
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + SSID + "\"";

        String encryptionType = ap.getEncryptionType();//WIFI的加密类型，如wpa,wpa2,wep等
        String password = ap.getPassword();//WIFI的密码
        if (encryptionType.contains("wep")) {
            /**
             * special handling according to password length is a must for wep
             */
            int i = password.length();
            if (((i == 10 || (i == 26) || (i == 58))) && (password.matches("[0-9A-Fa-f]*"))) {
                config.wepKeys[0] = password;
            } else {
                config.wepKeys[0] = "\"" + password + "\"";
            }
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (encryptionType.contains("wpa")) {
            config.preSharedKey = "\"" + password + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        return config;
    }
}
