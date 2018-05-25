package com.bong.panpan.adbbugfix;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.OutputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @InjectView(R.id.textView)
    TextView textView;
    @InjectView(R.id.button)
    Button button;
    @InjectView(R.id.editText)
    EditText editText;
    @InjectView(R.id.textView2)
    TextView textView2;
    NetworkConnectChangedReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        try {
            set(editText.getText().toString());
            textView.setText("成功");
            String ip = getIp();
            if (ip != null)
                textView2.setText("adb connect " + ip);
            else
                textView2.setText("WiFi未连接");
        } catch (Exception e) {
            e.printStackTrace();
            textView2.setText("未获取root权限");
            textView.setText(e.getMessage());
        }
        mReceiver = new NetworkConnectChangedReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
        try {
            set(editText.getText().toString());
            textView.setText("成功");
        } catch (Exception e) {
            e.printStackTrace();
            textView.setText(e.getMessage());
        }
    }

    protected static int set(String paramInt) throws Exception {
        Process localProcess = Runtime.getRuntime().exec("su");
        DataOutputStream localDataOutputStream = new DataOutputStream(
                (OutputStream) localProcess.getOutputStream());
        localDataOutputStream.writeBytes("setprop service.adb.tcp.port "
                + paramInt + "\n");
        localDataOutputStream.flush();
        localDataOutputStream.writeBytes("stop adbd\n");
        localDataOutputStream.flush();
        localDataOutputStream.writeBytes("start adbd\n");
        localDataOutputStream.flush();
        localDataOutputStream.writeBytes("exit\n");
        localDataOutputStream.flush();
        localProcess.waitFor();
        return localProcess.exitValue();
    }

    private String getIp() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //检查Wifi状态
        if (!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true);
            return null;
        }
        WifiInfo wi = wm.getConnectionInfo();
        //获取32位整型IP地址
        int ipAdd = wi.getIpAddress();
        //把整型地址转换成“*.*.*.*”地址
        String ip = intToIp(ipAdd);
        return ip;
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    public class NetworkConnectChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
// 监听WIFI的连接状态即是否连上了一个有效无线路由。
// 当上边广播的状态是WifiManager.WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播(因为WIFI功能未打开，所以不可能连接上有效WIFI)。
// 当上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，当然刚打开WIFI肯定还没有连接到有效的无线
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;
                    // WIFI已连接
                    if (isConnected) {
                        String ip = getIp();
                        if (ip != null)
                            textView2.setText("adb connect " + ip);
                        else
                            textView2.setText("WiFi未连接");
                    }
                    // WIFI未连接
                    else {
                        textView2.setText("WiFi未连接");
                    }
                }
            }
        }
    }
}
