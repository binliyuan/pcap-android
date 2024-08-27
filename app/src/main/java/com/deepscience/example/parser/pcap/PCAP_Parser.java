package com.deepscience.example.parser.pcap;

import android.util.Log;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

public class PCAP_Parser {
    private static final String TAG = "PCAP_Parser";
    public static void parser(byte[] data, String protocol_name) {
        // init the pcap parser class
        DisplayFilter display = new DisplayFilter(data);
        // if valid header
        if (display.pcapValide()) {
            try {
                display.protocol = protocol_name;
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.d(TAG, "parser: " + "Error: No protocol specified !");
            }
            display.print();
        } else {
            System.err.println("Error: Decoder failure");
        }
    }
}
