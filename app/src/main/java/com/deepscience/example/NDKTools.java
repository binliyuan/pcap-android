package com.deepscience.example;

public class NDKTools {

//    static {
//        System.loadLibrary("pcap");
//        System.loadLibrary("ndkpcap-jni");
//    }


    public static native void pcapPrint();

    public static native void pcapInit();

    public static native void startCapture();

    public static native void stopCapture();

}
