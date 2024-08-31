package com.deepscience.example;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.deepscience.example.pcapanalyzer.entity.format.FrameHeader;
import com.deepscience.example.pcapanalyzer.entity.format.IPHeader;
import com.deepscience.example.pcapanalyzer.entity.format.PacketHeader;
import com.deepscience.example.pcapanalyzer.utils.DataUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PcapRTCPParser {
    private static String TAG = "PcapRTCPParser";
    public static void Parser(String pathFile) {
        try {
            FileInputStream fis = new FileInputStream(pathFile);
            byte[] globalHeader = new byte[24];
            if (fis.read(globalHeader) != 24) {
                System.out.println("Failed to read global header.");
                return;
            }

            parseGlobalHeader(globalHeader);
            parse(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void parseRTCPAppPacket(byte[] rtcpData) {
        if (rtcpData.length < 12) {
            System.out.println("RTCP APP packet too short to be valid.");
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(rtcpData);

        // 解析版本、填充、报告数 (1字节)
        byte firstByte = buffer.get();
        int version = (firstByte >> 6) & 0x03;
        if (version != 2) {
            System.out.println("Unsupported RTCP version: " + version);
            return;
        }

        // 解析包类型 (1字节)
        byte packetType = buffer.get();
        int packetTypeInt = packetType & 0xFF;
        Log.d(TAG, "parseRTCPAppPacket: packetTypeInt: " + packetTypeInt);
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
        Log.d(TAG, "parseRTCPAppPacket: url " + url);
        System.out.println("Parsed URL: " + url);
    }

    public static void parse(FileInputStream fis) {
        try {
            while (fis.available() > 0) {
                byte[] packetHeader = new byte[16];
                if (fis.read(packetHeader) != 16) {
                    System.out.println("Failed to read packet header.");
                    break;
                }

                PacketHeader packetHeader1 = parsePacketHeader(packetHeader);
                // 读取数据包数据
                byte[] packetData = new byte[packetHeader1.getCapLen()];
                if (fis.read(packetData) != packetHeader1.getCapLen()) {
                    System.out.println("Failed to read packet data.");
                    break;
                }
                byte[] mergedArray = mergeTwoByteArrays(packetHeader, packetData);
                RTCPParse.parse(packetData);
//                int ihl = (packetData[14] & 0x0F);
//                Log.d(TAG, "parse: ihl" + ihl);
//                Log.d(TAG, "parse: packetData" +  packetData.length);
//                int ipHeaderLen = ihl * 4;
//                byte[] ipHeaderBuffer = Arrays.copyOfRange(packetData, 14, 14 + ipHeaderLen);
//                if (ipHeaderBuffer.length < 20)
//                    return;
//                IPHeader ipHeader = parseIPHeader(ipHeaderBuffer);
//
//                if (ipHeader.getProtocol() != IPHeader.PROTOCOL_UDP) {
//                    System.out.println("This packet is not a UDP segment");
//                    continue;
//                }
//
//                // 假设数据包包含 RTCP 数据，解析它
//                int udpHeaderStart = 14 + ipHeaderLen;
//                byte[] udpHeader = Arrays.copyOfRange(packetData, udpHeaderStart, udpHeaderStart + 8);
//                UDPHeader(udpHeader);
//                int rtcpDataStart = udpHeaderStart + 8;
//                byte[] rtcpData = Arrays.copyOfRange(packetData, rtcpDataStart, packetData.length);
//
////                parseRTCPPacket(rtcpData);
//                parseRTCPAppPacket(rtcpData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static byte[] mergeTwoByteArrays(byte[] array1, byte[] array2) {
        byte[] mergedArray = new byte[array1.length + array2.length];

        System.arraycopy(array1, 0, mergedArray, 0, array1.length);
        System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);

        return mergedArray;
    }
//
//    public static void parse1(FileInputStream fis) {
//        // 将十六进制字符串转换为字节数组
//        byte[] data = hexStringToByteArray(hexData);
//
//        // 解析以太网头（14字节）
//        byte[] destMac = new byte[6];
//        byte[] srcMac = new byte[6];
//        System.arraycopy(data, 0, destMac, 0, 6);
//        System.arraycopy(data, 6, srcMac, 0, 6);
//        int etherType = ((data[12] & 0xFF) << 8) | (data[13] & 0xFF);
//
//        System.out.println("Destination MAC: " + bytesToHex(destMac));
//        System.out.println("Source MAC: " + bytesToHex(srcMac));
//        System.out.println("EtherType: " + String.format("0x%04X", etherType));
//
//        // 解析 IP 头（假设以太网类型是 IPv4，IP 头长度为 20 字节）
//        int ipHeaderStart = 14;
//        int ipHeaderLength = (data[ipHeaderStart] & 0x0F) * 4;
//        byte[] srcIp = new byte[4];
//        byte[] destIp = new byte[4];
//        System.arraycopy(data, ipHeaderStart + 12, srcIp, 0, 4);
//        System.arraycopy(data, ipHeaderStart + 16, destIp, 0, 4);
//
//        System.out.println("Source IP: " + bytesToIp(srcIp));
//        System.out.println("Destination IP: " + bytesToIp(destIp));
//
//        // 解析 UDP 头（假设协议是 UDP，UDP 头长度为 8 字节）
//        int udpHeaderStart = ipHeaderStart + ipHeaderLength;
//        int srcPort = ((data[udpHeaderStart] & 0xFF) << 8) | (data[udpHeaderStart + 1] & 0xFF);
//        int destPort = ((data[udpHeaderStart + 2] & 0xFF) << 8) | (data[udpHeaderStart + 3] & 0xFF);
//
//        System.out.println("Source Port: " + srcPort);
//        System.out.println("Destination Port: " + destPort);
//
//        // 提取有效负载并将其转换为字符串
//        int payloadStart = udpHeaderStart + 8;
//        byte[] payload = new byte[data.length - payloadStart];
//        System.arraycopy(data, payloadStart, payload, 0, payload.length);
//        String payloadString = new String(payload, StandardCharsets.UTF_8);
//
//        System.out.println("Payload: " + payloadString);
//    }


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

    public static FrameHeader parseFrameHeader(byte[] frameHeaderBuffer) {
        FrameHeader frameHeader = new FrameHeader();
        // 目的MAC地址、源MAC地址没用，越过
        byte[] protocolBuffer = Arrays.copyOfRange(frameHeaderBuffer, 12, 14);

        int protocol = DataUtils.byteArray2Int(protocolBuffer, 2);
        frameHeader.setProtocol(protocol);
        return frameHeader;
    }



    private static void parseGlobalHeader(byte[] header) {
        System.out.println("Parsing Global Header...");
        int magicNumber = ByteBuffer.wrap(header, 0, 4).getInt();
        int versionMajor = ByteBuffer.wrap(header, 4, 2).getShort();
        int versionMinor = ByteBuffer.wrap(header, 6, 2).getShort();
        int thisZone = ByteBuffer.wrap(header, 8, 4).getInt();
        int sigFigs = ByteBuffer.wrap(header, 12, 4).getInt();
        int snapLen = ByteBuffer.wrap(header, 16, 4).getInt();
        int network = ByteBuffer.wrap(header, 20, 4).getInt();

        System.out.println("Magic Number: " + Integer.toHexString(magicNumber));
        System.out.println("Version: " + versionMajor + "." + versionMinor);
        System.out.println("Time Zone: " + thisZone);
        System.out.println("Sig Figs: " + sigFigs);
        System.out.println("Snap Length: " + snapLen);
        System.out.println("Network: " + network);
    }

    private static PacketHeader parsePacketHeader(byte[] dataHeaderBuffer){

        byte[] timeSBuffer = Arrays.copyOfRange(dataHeaderBuffer, 0, 4);
        byte[] timeMsBuffer = Arrays.copyOfRange(dataHeaderBuffer, 4, 8);
        byte[] capLenBuffer = Arrays.copyOfRange(dataHeaderBuffer, 8, 12);
        byte[] lenBuffer = Arrays.copyOfRange(dataHeaderBuffer, 12, 16);

        PacketHeader packetHeader = new PacketHeader();

        DataUtils.reverseByteArray(timeSBuffer);
        DataUtils.reverseByteArray(timeMsBuffer);
        DataUtils.reverseByteArray(capLenBuffer);
        DataUtils.reverseByteArray(lenBuffer);

        int timeS = DataUtils.byteArray2Int(timeSBuffer, 4);
        int timeMs = DataUtils.byteArray2Int(timeMsBuffer, 4);
        int capLen = DataUtils.byteArray2Int(capLenBuffer, 4);
        int len = DataUtils.byteArray2Int(lenBuffer, 4);

        packetHeader.setTimeS(timeS);
        packetHeader.setTimeMs(timeMs);
        packetHeader.setCapLen(capLen);
        packetHeader.setLen(len);

        return packetHeader;
    }

    // 构造函数，接受一个8字节的数组作为输入
    public static  void UDPHeader (byte[] udpHeaderBytes) {
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
    }

    // 辅助方法：将十六进制字符串转换为字节数组
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    // 辅助方法：将字节数组转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X:", b));
        }
        return sb.substring(0, sb.length() - 1);
    }

    // 辅助方法：将字节数组转换为 IP 地址字符串
    private static String bytesToIp(byte[] bytes) {
        return (bytes[0] & 0xFF) + "." + (bytes[1] & 0xFF) + "." + (bytes[2] & 0xFF) + "." + (bytes[3] & 0xFF);
    }
}
