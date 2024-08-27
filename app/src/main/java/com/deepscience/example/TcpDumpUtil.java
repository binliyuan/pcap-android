package com.deepscience.example;

import android.util.Log;

import android.os.Handler;

public class TcpDumpUtil {
    public static String TAG = "TcpDumpUtil ";
    public ProcessCmd processCmd = null;

    public TcpDumpUtil() {
        processCmd = new ProcessCmd();
    }

    public void runTcpDump() {
        Thread thread = new Thread(processCmd);
        thread.start();
    }

    public void stopTcpDump() {
        processCmd.stop();
    }

    public StringBuffer getMessage() {
        return processCmd.getMessage();
    }

    public void setHandler(Handler handler) {
        processCmd.setHandler(handler);
    }
    class ProcessCmd implements Runnable {
        public final String[] tpdump = {
                "cd /sdcard/tmp/",
                "tcpdump -i any -p -s 0",
        };
        public CmdUtils cmdUtils = null;
        public ProcessCmd() {
            cmdUtils = new CmdUtils();
        }

        public void stop() {
            cmdUtils.setStatus(false);
        }

        public StringBuffer getMessage() {
            return cmdUtils.successMsg;
        }

        public void setHandler(Handler handler) {
            cmdUtils.setMainHandler(handler);
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "runTcpDump: 1");
                CmdUtils.Result result = cmdUtils.execute(tpdump);
                String successMsg = result.successMsg;
                String errorMsg = result.errorMsg;
                Log.d(TAG, "run: " + successMsg);
                Log.d(TAG, "run: " + errorMsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
