package com.deepscience.example.parse;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Parse {

    private static final String TAG = "Parse";
    static int count = 0;
    public static String parse(String pcapFilePath) {
        try {
            FileInputStream fis = new FileInputStream(pcapFilePath);
            // 跳过全局头部（24 字节）
            fis.skip(24);
            while (fis.available() > 0) {
                count++;
//                Log.d(TAG, "parse: " + count);
                // 读取数据包头部（16 字节）
                byte[] packetHeader = new byte[16];
                fis.read(packetHeader);
                // 解析捕获长度（字节偏移量 8 到 11）
                int capturedLength = (packetHeader[8] & 0xFF) | ((packetHeader[9] & 0xFF) << 8)
                        | ((packetHeader[10] & 0xFF) << 16) | ((packetHeader[11] & 0xFF) << 24);
                // 读取数据包内容
                byte[] packetData = new byte[capturedLength];
                fis.read(packetData);
                // 解析以太网头部（14 字节）
                int ethernetHeaderLength = 14;
                byte[] frameHeaderBuffer = Arrays.copyOfRange(packetData, 0, 14);
                FrameHeader frameHeader = parseFrameHeader(frameHeaderBuffer);
                if (frameHeader.getProtocol() != FrameHeader.PROTOCOL_IP) {
                    System.out.println("This packet is not IP packet!");
                    continue;
                }

                // 解析 IP 头部长度
                int ipHeaderStart = ethernetHeaderLength;

                int ipHeaderLength = (packetData[14] & 0x0F) * 4;

//                System.out.println("ipHeaderLength  " + ipHeaderLength);
                byte[] ipHeaderBuffer = Arrays.copyOfRange(packetData, 14, 14 + ipHeaderLength);

                IPHeader ipHeader = parseIPHeader(ipHeaderBuffer);
                if (ipHeader.getProtocol() != IPHeader.PROTOCOL_UDP) {
                    continue;
                }

                // 解析 UDP/TCP 头部（假设 UDP 头部长度固定为 8 字节）
                int transportHeaderLength = 8;
                byte[] udpHeader = Arrays.copyOfRange(packetData, 14 + ipHeaderLength, 14 + ipHeaderLength + 8);
                int lenght = UDPHeader(udpHeader);
                // 计算有效载荷的起始位置
                int payloadStart = ethernetHeaderLength + ipHeaderLength + transportHeaderLength;
                byte[] payload = Arrays.copyOfRange(packetData, payloadStart, lenght + payloadStart);
                String url =  RTCPParse.parseTaoBaoLiveUrl(payload);
                if (url != null && !url.equals("")) {
                    Log.d(TAG, "parse: " + url);
                    return url;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 将 byte[] 转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static FrameHeader parseFrameHeader(byte[] frameHeaderBuffer) {
        FrameHeader frameHeader = new FrameHeader();
        // 目的MAC地址、源MAC地址没用，越过
        byte[] protocolBuffer = Arrays.copyOfRange(frameHeaderBuffer, 12, 14);

        int protocol = DataUtils.byteArray2Int(protocolBuffer, 2);
        frameHeader.setProtocol(protocol);
        return frameHeader;
    }

    public static IPHeader parseIPHeader(byte[] ipHeaderBuffer) {
        IPHeader ipHeader = new IPHeader();
        int headerLen = ipHeaderBuffer.length;
        ipHeader.setHeaderLen(headerLen);
        // 首部和数据长度和
        byte[] totalLenBuffer = Arrays.copyOfRange(ipHeaderBuffer, 2, 4);
        int totalLen = DataUtils.byteArray2Int(totalLenBuffer, 2);
        ipHeader.setTotalLen(totalLen);

        // upper protocol
        // 6 represents tcp
        int protocol = DataUtils.byteToInt(ipHeaderBuffer[9]);
        ipHeader.setProtocol(protocol);

        // parse sip and dip
        byte[] srcIPBuffer = Arrays.copyOfRange(ipHeaderBuffer, 12, 16);
        byte[] dstIPBuffer = Arrays.copyOfRange(ipHeaderBuffer, 16, 20);
        int srcIP = DataUtils.byteArray2Int(srcIPBuffer, 4);
        int dstIP = DataUtils.byteArray2Int(dstIPBuffer, 4);
        ipHeader.setSrcIP(srcIP);
        ipHeader.setDstIP(dstIP);
        return ipHeader;
    }

    public static  int UDPHeader(byte[] udpHeaderBytes) {
        if (udpHeaderBytes.length != 8) {
            throw new IllegalArgumentException("UDP header must be 8 bytes long");
        }
        int sourcePort;       // 源端口号
        int destinationPort;  // 目标端口号
        int length;           // 数据报长度
        int checksum;
        // 解析源端口号
        sourcePort = ((udpHeaderBytes[0] & 0xFF) << 8) | (udpHeaderBytes[1] & 0xFF);
        // 解析目标端口号
        destinationPort = ((udpHeaderBytes[2] & 0xFF) << 8) | (udpHeaderBytes[3] & 0xFF);
        // 解析长度
        length = ((udpHeaderBytes[4] & 0xFF) << 8) | (udpHeaderBytes[5] & 0xFF);
        // 解析校验和
        checksum = ((udpHeaderBytes[6] & 0xFF) << 8) | (udpHeaderBytes[7] & 0xFF);
        return length;
    }

    public static void parseRTCPAppPacket(byte[] rtcpData) {
        if (rtcpData.length < 12) {
            System.out.println("RTCP APP packet too short to be valid.");
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(rtcpData);

        // 解析版本、填充、报告数 (1字节)
        byte firstByte = buffer.get();
        int version = (firstByte >> 6) & 0x03;
        // 解析包类型 (1字节)
        byte packetType = buffer.get();
        int packetTypeInt = packetType & 0xFF;
        System.out.println("packetTypeInt" + packetTypeInt);
        if (packetTypeInt != 204) {
            return;
        }

        // 解析长度字段 (2字节)
        short length = buffer.getShort();
        int dataLength = length * 4; // 32-bit words to bytes

        // 解析 SSRC/CSRC Identifier (4字节)
        long ssrc = buffer.getInt() & 0xFFFFFFFFL;
        System.out.println("SSRC: " + ssrc);

        // 解析应用程序名称 (4字节)
        byte[] nameBytes = new byte[4];
        buffer.get(nameBytes);
        String appName = new String(nameBytes, StandardCharsets.US_ASCII);
        System.out.println("Application Name: " + appName);

        // 解析应用程序特定的数据
        byte[] appSpecificData = new byte[dataLength - 8]; // Subtract 8 bytes (4 for SSRC and 4 for Name)
        buffer.get(appSpecificData);

        // 示例：假设应用程序特定的数据是 URL 字符串
        String url = new String(appSpecificData, StandardCharsets.UTF_8).trim();

        System.out.println("Parsed URL: " + url);
    }

}
