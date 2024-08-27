package com.deepscience.example.pcapanalyzer.entity.format;

/**
 * TCP ��ͷ��20 �ֽ�
 */
public class TCPHeader {
	
	/**
	 * Դ�˿ڣ�2 �ֽڣ�
	 */
	private int srcPort;
	
	/**
	 * Ŀ�Ķ˿ڣ�2 �ֽڣ�
	 */
	private int dstPort;
	
	/**
	 * ���ݱ�ͷ�ĳ���(4 bit) + ����(4 bit) = 1 byte
	 */
	private int headerLen;

	public int getSrcPort() {
		return srcPort;
	}

	public void setSrcPort(int srcPort) {
		this.srcPort = srcPort;
	}

	public int getDstPort() {
		return dstPort;
	}

	public void setDstPort(int dstPort) {
		this.dstPort = dstPort;
	}

	public int getHeaderLen() {
		return headerLen;
	}

	public void setHeaderLen(int headerLen) {
		this.headerLen = headerLen;
	}

	public TCPHeader() {}

	@Override
	public String toString() {
		return "TCPHeader [srcPort=" + srcPort
				+ ", dstPort=" + dstPort
				+ ", headerLen=" + headerLen
				+ "]";
	}

}
