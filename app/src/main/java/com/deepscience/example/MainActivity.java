package com.deepscience.example;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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

import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;
import com.jaredrummler.android.shell.ShellNotFoundException;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.jaredrummler.android.shell.Shell.SU.run;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String TAG = "MainActivity";

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/armpcap/";

    TextView info;

    EditText index;

    EditText port;

    EditText protocol;

    EditText num;

    EditText filter;

    CheckBox isFilter;

    Button start;

    Button stop;

    Shell.Console console;

    boolean Fisrt = true;

    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case 1:
                    Bundle bundle = message.getData();
                    String info_str = "";
                    List<String> liststr = bundle.getStringArrayList("data");
                    for (String str : liststr){
                        info_str+=str+"\n";
                    }
                    info.setText(info_str);
                    if(Fisrt){
                        Fisrt = false;
                    }
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
//        moveFile();
        verifyStoragePermissions(this);
//        createDir();
        Toast.makeText(this, "run...", Toast.LENGTH_SHORT).show();
//        if (Fisrt) {
//            capture();
//        }
    }

    private void moveFile(){
//        Resources myResources = getResources();
//        InputStream myFile = myResources.openRawResource(R.raw.armpcap);
//        String pathTemp = path+"armpcap";
//        Log.d(TAG, "moveFile: " + pathTemp);
//        File file = new File(path);
//        OutputStream os = null;
//        try {
//            os = new FileOutputStream(file);
//            int bytesRead = 0;
//            byte[] buffer = new byte[8192];
//            while ((bytesRead = myFile.read(buffer, 0, 8192)) != -1) {
//                os.write(buffer, 0, bytesRead);
//            }
//            os.close();
//            myFile.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            Shell.Console.Builder builder = new Shell.Console.Builder();
//            Shell.Console console1 = builder.useSU().build();
//            console1.run("cd /sdcard/armpcap/");
//            console1.run("mv armpcap /data/local/");
//            console1.run("cd /data/local/");
//            console1.run("chmod 777 armpcap");
//            console1.close();
//        } catch (ShellNotFoundException e) {
//            e.printStackTrace();
//        }

    }

    private void capture(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                NDKTools.startCapture();
//                String filter_str = "";
//                String port_str = "";
//                String protocol_str = "";
//                String index_str = "";
//                String num_str = "";
//                if (!Fisrt){
//                    if (isFilter.isChecked()){
//                        num_str = "-1";
//                        index_str = index.getText().toString();
//                        if(!num.getText().toString().equals("")){
//                            num_str = num.getText().toString();
//                        }
//                        filter_str = "\""+filter.getText().toString()+"\"";
//                    } else {
//                        num_str = "-1";
//                        index_str = index.getText().toString();
//                        if(!num.getText().toString().equals("")){
//                            num_str = num.getText().toString();
//                        }
//                        protocol_str = protocol.getText().toString();
//                        port_str = port.getText().toString();
//                        if (port_str.equals("")){
//                            filter_str = "\""+protocol_str+"\"";
//                        }else{
//                            filter_str = "\""+protocol_str+" port "+port_str+"\"";
//                        }
//                    }
//                }
//                if (Shell.SU.available()){
//                    try {
//                        console = Shell.SU.getConsole();
//                        console.run("cd /data/local");
//                        CommandResult result = console.run("./armpcap "+index_str+" "+filter_str+" "+num_str);
//                        Message message = new Message();
//                        message.what = 1;
//                        Bundle data = new Bundle();
//                        ArrayList<String> liststr = new ArrayList<>();
//                        for (String str : result.stdout){
//                            liststr.add(str);
//                        }
//                        if (!result.isSuccessful())
//                            liststr.add("未指定抓包数量，强行中断抓包！！！！");
//                        data.putStringArrayList("data", liststr);
//                        message.setData(data);
//                        handler.sendMessage(message);
//                        if (Fisrt){
//                            console.close();
//                        }
//                    } catch (ShellNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }).start();
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
        if (isDeviceRooted()) {
            Log.d(TAG, "init: " + "is Device Rooted");
            runAsRoot();
        } else {
            Log.d(TAG, "init: " + "is not Device Rooted");
        }
        NDKTools.pcapPrint();
        NDKTools.pcapInit();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"开始抓包",Toast.LENGTH_SHORT).show();
                capture();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                    Shell.Console.Builder builder = new Shell.Console.Builder();
//                    Shell.Console console1 = builder.useSU().build();
//                    console1.run("pkill armpcap");
//                    console1.close();
//                } catch (ShellNotFoundException e) {
//                    e.printStackTrace();
//                }
//                if(!console.isClosed()){
                    NDKTools.stopCapture();
//                    console.close();
                    Toast.makeText(MainActivity.this,"已停止抓包",Toast.LENGTH_SHORT).show();
//                }
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
    }

    public void createDir () {
        File destDir = new File(path);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }

    public boolean isDeviceRooted() {
        String[] paths = {"/system/xbin/su", "/system/bin/su", "/system/app/Superuser.apk", "/sbin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    public void runAsRoot() {
        try {
            // 试图以 root 权限执行命令
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            // 执行需要 root 权限的命令
            os.writeBytes("echo hello from root\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();

            // 检查命令执行结果
            if (process.exitValue() == 0) {
                Log.d("RootCheck", "Command executed successfully");
            } else {
                Log.d("RootCheck", "Failed to execute command");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

