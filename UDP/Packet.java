//package UDP;

import java.io.Serializable;
import java.util.Arrays;

public class Packet implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public String protocol;
    
    public int seqNo;
	
	public byte[] data;
	
	public boolean isLast;
	
	public boolean isInitialAck;
	
	public int windowSize;
	
	public int checkSum;

	
	public Packet(String protocol, int seqNo, byte[] data, boolean isLast, boolean isInitialAck, int checkSum) {
		this.protocol = protocol;
		this.seqNo = seqNo;
		this.data = data;
		this.isLast = isLast;
		this.isInitialAck = isInitialAck;
		this.checkSum = checkSum;
	}
	
	public int getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(int checkSum) {
		this.checkSum = checkSum;
	}

	
	
	public int getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}

	public boolean isInitialAck() {
		return isInitialAck;
	}

	public void setInitialAck(boolean isInitialAck) {
		this.isInitialAck = isInitialAck;
	}


	public int getSeq() {
		return seqNo;
	}

	public void setSeq(int seqNo) {
		this.seqNo = seqNo;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public boolean isLast() {
		return isLast;
	}

	public void setLast(boolean isLast) {
		this.isLast = isLast;
	}

	@Override
	public String toString() {
		return "Data [protocol=" + protocol + ",seqNo=" + seqNo + ",checkSum=" + checkSum  +", data=" + Arrays.toString(data)
				+ ", isLast=" + isLast + ",isInitial=" + isInitialAck + "]";
	}
}
