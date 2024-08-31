package com.deepscience.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RTCPParse {
    private static String TAG = "PcapRTCPParser";

    public static void parse(byte[] data) {
        System.out.println(bytesToHex(data));
//        String hexData = "de149aa052f196fed7018cd60800450002584ce940004011a6f30a103b8f7d4081d9e27a04520244470e81cc008eab8b7308505542000200000003011a617274633a2f2f6c69766563612d727463707573682e74616f62616f2e636f6d2f6d65646961706c6174666f726d2f65316639636239612d323038662d346439372d383061632d3531646332383061373966663f617574685f6b65793d313732373636373638362d302d302d3036616135353265353838656465623531613464636666636237343838646164266772746e5f7477696e5f72746d703d6f6e2663616c6c6261636b3d65794a6a61474675626d5673535751694f694a6d4f57526b4e4467325a47497a4f446c6b4d6a5930597a51774e444d775a5463324f5445795a6a6c6a5a694973496d70545a5845694f6a4173496e42545a5845694f6a4173496e4e75496a6f6964474676596d467658327870646d5569665104009402003e0100017202000972747320617564696f03000493089b60040006010000bb80010500042b0988000700025400090006707297de7e480c000600400040004003003b0100016202000972747320766964656f0300041e671e900400010205000254100700067162b20035f10e000700138803e813881000050438078014040012010008727473206461746102000407129a010c00010117002a030004000410410700200100010102000e040112070202080b01050a0e0b0603000402000100040001801e00420101000a3332393736353139353131020020663964643438366462333839643236346334303433306537363931326639636603000e547274634c69766553747265616dffff";
//
//        // 将十六进制字符串转换为字节数组
//        byte[] data = hexStringToByteArray(hexData);
        // 解析以太网头（14字节）
        byte[] destMac = new byte[6];
        byte[] srcMac = new byte[6];
        System.arraycopy(data, 0, destMac, 0, 6);
        System.arraycopy(data, 6, srcMac, 0, 6);
        int etherType = ((data[12] & 0xFF) << 8) | (data[13] & 0xFF);

//        System.out.println("Destination MAC: " + bytesToHex(destMac));
//        System.out.println("Source MAC: " + bytesToHex(srcMac));
//        System.out.println("EtherType: " + String.format("0x%04X", etherType));

        // 解析 IP 头（假设以太网类型是 IPv4，IP 头长度为 20 字节）
        int ipHeaderStart = 14;
        int ipHeaderLength = (data[ipHeaderStart] & 0x0F) * 4;
        byte[] srcIp = new byte[4];
        byte[] destIp = new byte[4];
        System.arraycopy(data, ipHeaderStart + 12, srcIp, 0, 4);
        System.arraycopy(data, ipHeaderStart + 16, destIp, 0, 4);

//        System.out.println("Source IP: " + bytesToIp(srcIp));
//        System.out.println("Destination IP: " + bytesToIp(destIp));

        // 解析 UDP 头（假设协议是 UDP，UDP 头长度为 8 字节）
        int udpHeaderStart = ipHeaderStart + ipHeaderLength;
        byte[] udpHeader = Arrays.copyOfRange(data, udpHeaderStart, udpHeaderStart + 8);
        UDPHeader(udpHeader);
        int srcPort = ((data[udpHeaderStart] & 0xFF) << 8) | (data[udpHeaderStart + 1] & 0xFF);
        int destPort = ((data[udpHeaderStart + 2] & 0xFF) << 8) | (data[udpHeaderStart + 3] & 0xFF);

//        System.out.println("Source Port: " + srcPort);
//        System.out.println("Destination Port: " + destPort);

        // 提取有效负载并将其转换为字符串
        int payloadStart = udpHeaderStart + 8;
        byte[] payload = new byte[data.length - payloadStart];
        System.out.println(parseTaoBaoLiveUrl(payload));

    }

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


    public static String parseTaoBaoLiveUrl(byte[] udpPayload) {
        Rtcp rp = RtcpUnmarshal(udpPayload);
        if (rp != null) {
            String name1 = "PUB\0";
            if (rp.getName().equalsIgnoreCase(name1) && rp.getAppData().length > 10) {
                byte[] b1 = new byte[]{0x02, 0x00, 0x00, 0x00, 0x03};
                byte[] b2 = Arrays.copyOfRange(rp.getAppData(), 0, 5);
                if (Arrays.equals(b1, b2)) {
                    ByteBuffer buffer = ByteBuffer.wrap(rp.getAppData(), 5, 2);
                    buffer.order(ByteOrder.BIG_ENDIAN);
                    short size = buffer.getShort();
                    byte[] urlBytes = Arrays.copyOfRange(rp.getAppData(), 7, 7 + size);
                    return new String(urlBytes);
                }
            }
        }
        return "";
    }

    // Implementation of RtcpUnmarshal method
    private static Rtcp RtcpUnmarshal(byte[] data) {
        if (data.length < 13) {
            return null; // Not enough data to be a valid RTCP packet
        }

        Rtcp r = new Rtcp();
        byte b = data[0];
        r.setVer((byte) (b >> 6));
        r.setSubType((byte) (b & 0x1f));
        r.setPayloadType(data[1]);
        ByteBuffer lengthBuffer = ByteBuffer.wrap(data, 2, 2);
        lengthBuffer.order(ByteOrder.BIG_ENDIAN);
        r.setLength(lengthBuffer.getShort());

        r.setSsrc(bytesToHex(Arrays.copyOfRange(data, 4, 8)));
        r.setName(new String(Arrays.copyOfRange(data, 8, 12)));
        r.setAppData(Arrays.copyOfRange(data, 12, data.length));

        return r;
    }

    // Utility method to convert bytes to a hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Class to represent the RTCP Packet structure
    static class Rtcp {
        private byte ver;
        private byte subType;
        private byte payloadType;
        private short length;
        private String ssrc;
        private String name;
        private byte[] appData;

        public byte getVer() {
            return ver;
        }

        public void setVer(byte ver) {
            this.ver = ver;
        }

        public byte getSubType() {
            return subType;
        }

        public void setSubType(byte subType) {
            this.subType = subType;
        }

        public byte getPayloadType() {
            return payloadType;
        }

        public void setPayloadType(byte payloadType) {
            this.payloadType = payloadType;
        }

        public short getLength() {
            return length;
        }

        public void setLength(short length) {
            this.length = length;
        }

        public String getSsrc() {
            return ssrc;
        }

        public void setSsrc(String ssrc) {
            this.ssrc = ssrc;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public byte[] getAppData() {
            return appData;
        }

        public void setAppData(byte[] appData) {
            this.appData = appData;
        }
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


    // 辅助方法：将字节数组转换为 IP 地址字符串
    private static String bytesToIp(byte[] bytes) {
        return (bytes[0] & 0xFF) + "." + (bytes[1] & 0xFF) + "." + (bytes[2] & 0xFF) + "." + (bytes[3] & 0xFF);
    }
}
