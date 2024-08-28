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
    // PCAP 文件头的大小
    private static final int PCAP_HEADER_SIZE = 24;
    // Linux SLL 头部的大小
    private static final int SLL_HEADER_SIZE = 16;
    public static void Parser(String pathFile) {
        try {
            FileInputStream fis = new FileInputStream(pathFile);
            byte[] globalHeader = new byte[24];
            if (fis.read(globalHeader) != 24) {
                System.out.println("Failed to read global header.");
                return;
            }

            parseGlobalHeader(globalHeader);
//            parse(fis);
            parse1(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void parseRTCPPacket(byte[] rtcpData) {
        String url = new String(rtcpData);
        System.out.println("Extracted URL (if any): " + url);
        ByteBuffer buffer = ByteBuffer.wrap(rtcpData).order(ByteOrder.BIG_ENDIAN);

        // 解析 RTCP 报文头部
        int firstByte = buffer.get(0);
        int version = (firstByte >> 6) & 0x03;
        byte packetType = buffer.get(1);
        int packetTypeInt = packetType & 0xFF;

        // 检查是否是已知的RTCP包类型
        if (packetTypeInt < 200 || packetTypeInt > 204) {
            return; // 这是一个RTCP数据包
        }
//        int length = buffer.getShort() & 0xFFFF;
//        int ssrc = buffer.getInt();

        System.out.println("RTCP Packet Details:");
        System.out.println("Version: " + version);
//        System.out.println("Padding: " + padding);
//        System.out.println("Reception Report Count: " + rc);
//        System.out.println("Payload Type: " + payloadType);
//        System.out.println("Length: " + length);
//        System.out.println("SSRC: " + Integer.toHexString(ssrc));

//        // 解析报告块（如果有的话）
//        for (int i = 0; i < rc; i++) {
//            if (buffer.remaining() < 8) {
//                System.out.println("Not enough data for report block.");
//                return;
//            }
//            ReportBlock block = parseReportBlock(buffer);
//            System.out.println("Report Block " + i + ": " + block);
//        }
//
//        // 假设数据包中可能有嵌入的数据，尝试提取网址
//        if (buffer.remaining() > 0) {
//            byte[] remainingData = new byte[buffer.remaining()];
//            buffer.get(remainingData);
//            String extractedUrl = extractUrlFromData(remainingData);
//            if (extractedUrl != null) {
//                System.out.println("Extracted URL: " + extractedUrl);
//            } else {
//                System.out.println("No URL found in the remaining data.");
//            }
//        }
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
        if (packetTypeInt != 204) {
            System.out.println("Not an RTCP APP packet. Packet Type: " + packetTypeInt);
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

    public static void parse(FileInputStream fis) {
        try {
            // 开始读取每个数据包
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
                parseFrameHeader(packetData);

                int ipHeaderLen = (packetData[14] - 64 ) * 4;
                byte[] ipHeaderBuffer = Arrays.copyOfRange(packetData, 14, 14 + ipHeaderLen);
                IPHeader ipHeader = parseIPHeader(ipHeaderBuffer);
                ipHeader.getProtocol();
                Log.d(TAG, "Parser: " + ipHeader.getProtocol());
                if (ipHeader.getProtocol() != IPHeader.PROTOCOL_UDP) {
                    System.out.println("This packet is not RTCP segment");
                    continue;
                }
                byte[] rtcp = Arrays.copyOfRange(packetData, 14 + ipHeaderLen, packetData.length);
                parseRTCPPacket(rtcp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void parse1(FileInputStream fis) {
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

                int ipHeaderLen = (packetData[14] & 0x0F) * 4;
                byte[] ipHeaderBuffer = Arrays.copyOfRange(packetData, 14, 14 + ipHeaderLen);
                IPHeader ipHeader = parseIPHeader(ipHeaderBuffer);

                if (ipHeader.getProtocol() != IPHeader.PROTOCOL_UDP) {
                    System.out.println("This packet is not a UDP segment");
                    continue;
                }

                // 假设数据包包含 RTCP 数据，解析它
                int udpHeaderStart = 14 + ipHeaderLen;
                byte[] udpHeader = Arrays.copyOfRange(packetData, udpHeaderStart, udpHeaderStart + 8);
                UDPHeader(udpHeader);
                int rtcpDataStart = udpHeaderStart + 8;
                byte[] rtcpData = Arrays.copyOfRange(packetData, rtcpDataStart, packetData.length);

//                parseRTCPPacket(rtcpData);
                parseRTCPAppPacket(rtcpData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

//    private static void parseRTCPPacket(byte[] packetData) {
//        if (packetData.length < 4) {
//            System.out.println("Invalid RTCP packet length.");
//            return;
//        }
//
//        ByteBuffer buffer = ByteBuffer.wrap(packetData);
//
//        // 解析包头
//        byte firstByte = buffer.get();
//        int version = (firstByte >> 6) & 0x03; // 前 2 位为版本号
//        boolean padding = ((firstByte >> 5) & 0x01) == 1; // 第 3 位为填充标志
//        int reportCount = firstByte & 0x1F; // 后 5 位为报告块计数
//
//        byte packetType = buffer.get(); // 第二个字节为包类型
//        int length = buffer.getShort() & 0xFFFF; // 长度字段
//
//        // 进一步解析不同类型的 RTCP 包
//        switch (packetType) {
//            case (byte) 200: // Sender Report (SR)
//                parseSenderReport(buffer);
//                break;
//            case (byte) 201: // Receiver Report (RR)
//                parseReceiverReport(buffer);
//                break;
//            case (byte) 202: // Source Description (SDES)
//                parseSourceDescription(buffer);
//                break;
//            case (byte) 203: // Goodbye (BYE)
//                parseGoodbye(buffer);
//                break;
//            case (byte) 204: // Application-defined (APP)
//                parseApplicationDefined(buffer);
//                break;
//            default:
//                System.out.println("Unknown RTCP Packet Type.");
//                break;
//        }
//    }

    private static void parseSenderReport(ByteBuffer buffer) {
        if (buffer.remaining() < 20) {
            System.out.println("Invalid Sender Report packet length.");
            return;
        }

        long ssrc = buffer.getInt() & 0xFFFFFFFFL;
        long ntpTimestamp = buffer.getLong();
        long rtpTimestamp = buffer.getInt() & 0xFFFFFFFFL;
        long packetCount = buffer.getInt() & 0xFFFFFFFFL;
        long octetCount = buffer.getInt() & 0xFFFFFFFFL;

        System.out.println("Sender Report - SSRC: " + ssrc);
        System.out.println("NTP Timestamp: " + ntpTimestamp);
        System.out.println("RTP Timestamp: " + rtpTimestamp);
        System.out.println("Packet Count: " + packetCount);
        System.out.println("Octet Count: " + octetCount);
    }

    private static void parseReceiverReport(ByteBuffer buffer) {
        if (buffer.remaining() < 24) {
            System.out.println("Invalid Receiver Report packet length.");
            return;
        }

        long ssrc = buffer.getInt() & 0xFFFFFFFFL;

        System.out.println("Receiver Report - SSRC: " + ssrc);
        // 进一步的报告块解析省略
    }

    private static void parseSourceDescription(ByteBuffer buffer) {
        System.out.println("Parsing Source Description (SDES)...");
        // 解析 SDES 包的内容
    }

    private static void parseGoodbye(ByteBuffer buffer) {
        System.out.println("Parsing Goodbye (BYE)...");
        // 解析 Goodbye 包的内容
    }

    private static void parseApplicationDefined(ByteBuffer buffer) {
        System.out.println("Parsing Application-defined (APP)...");
        // 解析 APP 包的内容
    }
}
