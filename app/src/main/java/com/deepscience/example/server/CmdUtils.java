package com.deepscience.example.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class CmdUtils {
    public static final String COMMAND_SU = "su";
    public static final String COMMAND_LINE_END = "\n"; 
    public static final String COMMAND_EXIT = "exit\n";
    private boolean status = false; // true running false exit
    public String successMsg = null;
    public StringBuffer errorMsg = null;
    public Handler mainHandler = null;
    private Runtime runtime = null;
    Process process = null;
    public Result execute(String[] commands) {
        status = true;
        runtime = Runtime.getRuntime();
        DataOutputStream output = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        try {
            process = runtime.exec(COMMAND_SU);

            output = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                output.write(command.getBytes());
                output.writeBytes(COMMAND_LINE_END);
                output.flush();
            }
            successMsg = new String();
            errorMsg = new StringBuffer();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while (true) {
                if (status == false)
                    break;
//                s = successResult.readLine();
//                successMsg = s + "\n";
//                Message message = new Message();
//                message.what = 1;
//                Bundle bundle = new Bundle();
//                bundle.putString("successMsg", successMsg.toString());
//                message.setData(bundle);
//                mainHandler.sendMessage(message);
            }
//            while ( (s = errorResult.readLine()) != null) {
//                if (status == false)
//                    break;
//                errorMsg.append(s).append("\n");
//            }
            output.writeBytes(COMMAND_EXIT);
            output.flush();
            process.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
                try {
                    if (output != null) {
                        output.writeBytes(COMMAND_EXIT);
                        if (output != null)
                            output.close();
                    }
                    if (successResult != null) {
                        successResult.close();
                    }
                    if (errorResult != null) {
                        errorResult.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (process != null) {
                        process.destroyForcibly();
                    }
                }
        }
        return new Result(successMsg == null ? null : successMsg.toString()
                , errorMsg == null ? null : errorMsg.toString());
    }

    public void setStatus(boolean status) {
        this.status = status;
        if (process != null) {
            process.destroy();
        }
//        if (runtime != null)
//            runtime.exit(0);
    }

    public static class Result {
        public String successMsg; 
        public String errorMsg; 
        
        public Result(String successMsg, String errorMsg) {
            super();
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}
