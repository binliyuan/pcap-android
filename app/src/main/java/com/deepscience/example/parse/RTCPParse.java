package com.deepscience.example.parse;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class RTCPParse {
    private static String TAG = "PcapRTCPParser";
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

    private static Rtcp RtcpUnmarshal(byte[] data) {
        Log.d(TAG, "RtcpUnmarshal: ");
        if (data.length < 13) {
            Log.d(TAG, "RtcpUnmarshal: " + "not is rtcp" );
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
}
