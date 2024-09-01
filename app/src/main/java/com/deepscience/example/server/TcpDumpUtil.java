package com.deepscience.example.server;

import android.os.Message;
import android.util.Log;

import com.deepscience.example.parse.Parse;
import com.deepscience.example.utils.Def;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class TcpDumpUtil {
    public static final String COMMAND_SU = "su";
    public static final String COMMAND_LINE_END = "\n";
    public static final String COMMAND_EXIT = "exit\n";
    public static String TAG = "TcpDumpUtil ";
    private ProcessCmd processCmd = null;
    private Parser parser = null;

    public TcpDumpUtil() {
        processCmd = new ProcessCmd();
        parser = new Parser();
    }

    public void runTcpDump() {
        Thread thread = new Thread(processCmd);
        thread.start();
    }

    public void stopTcpDump() {
        try {
            processCmd.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void parse() {
        Thread parserThread = new Thread(parser);
        parserThread.start();
    }


    class ProcessCmd implements Runnable {
        private Runtime runtime;
        private Process process;
        public final String[] tpdump = {
                "cd /sdcard/tmp/",
                "tcpdump -i eth0 -p -s 0 -w capture.pcap",
        };
//        public final String[] tpdump = {
//                "cd /sdcard/tmp/",
//                "tcpdump -i eth0 -XX -vvv -nn",
//        };
//        public CmdUtils cmdUtils = null;

//        public ProcessCmd() {
//            cmdUtils = new CmdUtils();
//        }

        public void stop() throws IOException, InterruptedException {
            if (process.isAlive()) {
                int pid = getProcessId();
                Process p = Runtime.getRuntime().exec(COMMAND_SU);
                DataOutputStream output = new DataOutputStream(p.getOutputStream());
                output.write(("kill -9 " + pid).getBytes());
                output.writeBytes(COMMAND_LINE_END);
                output.flush();
                process.destroyForcibly();
            }
            process.getInputStream().close();
            process.getOutputStream().close();
            process.getErrorStream().close();
            if (process.isAlive()) {
                Log.d(TAG, "dump进程关闭失败");
            }
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "runTcpDump:");
                runtime = Runtime.getRuntime();
                process = runtime.exec(COMMAND_SU);
                DataOutputStream output = new DataOutputStream(process.getOutputStream());
                for (String command : tpdump) {
                    if (command == null) {
                        continue;
                    }
                    output.write(command.getBytes());
                    output.writeBytes(COMMAND_LINE_END);
                    output.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 方法来获取进程的 PID
        private int getProcessId() {
            int pid = -1;
            try {
                // 通过 ps 命令获取所有进程信息
                Process psProcess = Runtime.getRuntime().exec("su");
                DataOutputStream output = new DataOutputStream(psProcess.getOutputStream());
                output.write("ps -ef\n".getBytes());
                output.flush();
                int parentPid = android.os.Process.myPid();
                BufferedReader reader = new BufferedReader(new InputStreamReader(psProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("tcpdump")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length > 2) {
                            pid = Integer.parseInt(parts[1]);
                            break;
                        }
                    }
                    Log.d(TAG, "getProcessId: " + line);
                }

                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return pid;
        }
    }

    class Parser implements Runnable {

        @Override
        public void run() {
            parsePcapFile("/sdcard/tmp/capture.pcap");
        }

        public void parsePcapFile(String pcapFilePath) {
            String url = Parse.parse(pcapFilePath);
            postMessage(Def.PARSEDISPLAY, url);
        }

        public void postMessage(int what, String url) {
            Message message = new Message();
            message.what = what;
            message.obj = url;
            // 发布消息事件
            EventBus.getDefault().post(message);
        }
    }
}
