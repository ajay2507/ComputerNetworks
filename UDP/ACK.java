//package UDP;

import java.io.Serializable;

public class ACK implements Serializable{
	
	int ackNo;
	
	public ACK(int packetNo) {
		super();
		this.ackNo = packetNo;
	}

	public int getAckNo() {
		return ackNo;
	}

	public void setAckNo(int ackNo) {
		this.ackNo = ackNo;
	}
}
