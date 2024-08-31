package com.deepscience.example;

import android.icu.util.LocaleData;
import android.util.Log;

import android.os.Handler;

import com.deepscience.example.pcapanalyzer.service.PCAPService;

import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;

import java.io.File;
import java.util.Arrays;

public class TcpDumpUtil {
    public static String TAG = "TcpDumpUtil ";
    public ProcessCmd processCmd = null;
    public Parser parser = null;
    public TcpDumpUtil() {
        processCmd = new ProcessCmd();
        parser = new Parser();
    }

    public void runTcpDump() {
//        Thread thread = new Thread(processCmd);
//        thread.start();
        Thread parserThread = new Thread(parser);
        parserThread.start();
    }

    public void stopTcpDump() {
        processCmd.stop();
    }

    public void parse() {
        Thread parserThread = new Thread(parser);
        parserThread.start();
    }

    public String getMessage() {
        return processCmd.getMessage();
    }

    public void setHandler(Handler handler) {
        processCmd.setHandler(handler);
    }

    class ProcessCmd implements Runnable {
        public final String[] tpdump = {
                "cd /sdcard/tmp/",
                "tcpdump -i eth0 -p -s 0 -w capture.pcap",
        };
//        public final String[] tpdump = {
//                "cd /sdcard/tmp/",
//                "tcpdump -i eth0 -XX -vvv -nn",
//        };
        public CmdUtils cmdUtils = null;
        public ProcessCmd() {
            cmdUtils = new CmdUtils();
        }

        public void stop() {
            cmdUtils.setStatus(false);
        }

        public String getMessage() {
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

    class Parser implements Runnable {

        @Override
        public void run() {
            parsePcapFile("/sdcard/tmp/capture.pcap");
        }

        public void parsePcapFile(String pcapFilePath) {
//            PCAPService pcapService = new PCAPService();
//            File pcapFile = new File(pcapFilePath);
//            pcapService.parsePcap(pcapFile);
//            PcapRTCPParser.Parser(pcapFilePath);
            Parse.parse(pcapFilePath);
        }
    }


}
