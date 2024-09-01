package com.deepscience.example.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.deepscience.example.utils.Def;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MyService extends Service {
    private static String TAG = "MyService";
    TcpDumpUtil tcpDumpUtil = null;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        tcpDumpUtil = new TcpDumpUtil();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onDestroy");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Message event) {
        // 在主线程中接收和处理事件
        Toast.makeText(this, "Received message: " + event.what, Toast.LENGTH_SHORT).show();
        switch (event.what) {
            case Def.START:
                tcpDumpUtil.runTcpDump();
                break;
            case Def.STOP:
                tcpDumpUtil.stopTcpDump();
                break;
            case Def.PARSE:
                tcpDumpUtil.parse();
            case Def.PARSEDISPLAY:
                String url = (String) event.obj;
                Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
                break;
        }
    }

}