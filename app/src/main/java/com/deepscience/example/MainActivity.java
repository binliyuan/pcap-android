package com.deepscience.example;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String TAG = "MainActivity";

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/armpcap/";
    TcpDumpUtil tcpDumpUtil = null;

    TextView info;

    EditText index;

    EditText port;

    EditText protocol;

    EditText num;

    EditText filter;

    CheckBox isFilter;

    Button start;

    Button stop;
    Button parse;

    boolean Fisrt = true;

    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case 1:
                    Bundle bundle = message.getData();
                    String successMsg = bundle.getString("successMsg");
                    Log.d(TAG, "handleMessage: " + successMsg);
                    info.setText(successMsg);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        int uid = android.os.Process.myUid();
        verifyStoragePermissions(this);
        Toast.makeText(this, "running uid is  " + uid, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreate: " + "runTcpDump");
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the u
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    private void init(){
        info = findViewById(R.id.info);
        index = findViewById(R.id.index);
        port = findViewById(R.id.port);
        protocol = findViewById(R.id.protocol);
        num = findViewById(R.id.num);
        filter = findViewById(R.id.filter);
        isFilter = findViewById(R.id.is_filter);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        parse = findViewById(R.id.parse);
        tcpDumpUtil = new TcpDumpUtil();
        tcpDumpUtil.setHandler(handler);

//        NDKTools.pcapPrint();
//        NDKTools.pcapInit();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "开始抓包", Toast.LENGTH_SHORT).show();
                postMessage(Def.START);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"已停止抓包",Toast.LENGTH_SHORT).show();
                postMessage(Def.STOP);
            }
        });


        parse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"开始解析",Toast.LENGTH_SHORT).show();
                postMessage(Def.PARSE);
            }
        });


        isFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true){
                    filter.setEnabled(true);
                    port.setText("");
                    protocol.setText("");
                    port.setEnabled(false);
                    protocol.setEnabled(false);
                } else {
                    filter.setText("");
                    filter.setEnabled(false);
                    port.setEnabled(true);
                    protocol.setEnabled(true);
                }
            }
        });
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    public void postMessage(int what) {
        Message message = new Message();
        message.what = what;
        // 发布消息事件
        EventBus.getDefault().post(message);
    }

}

