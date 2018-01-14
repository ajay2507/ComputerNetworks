/**
 * 
 */
//package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * @author Ajay
 *
 */
public class GoBackNReceiver {

	/**
	 * @param args
	 */
	
	private DatagramSocket receiverSocket;
	private int windowSize;
	private int segmentSize;
	private String protocolName;
	private int noOfPackets;
	public static final double LOST_PACKET_PROBABILITY = 0.1;
	
	
	public GoBackNReceiver(String protocol, DatagramSocket receiverSocket
			){
		 this.protocolName = protocol;
		 this.receiverSocket = receiverSocket;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	public void sendInitialAck(DatagramPacket receivePacket, InetAddress IPAddress, int portNo) throws IOException {
		
		ACK intialAck = new ACK(0);
		byte[] initialAckBytes = Serializer.covertToBytes(intialAck);
		
    	DatagramPacket replyPacket = new DatagramPacket(initialAckBytes, initialAckBytes.length,receivePacket.getAddress(),receivePacket.getPort());
    	receiverSocket.send(replyPacket);
    	System.out.println("----- Initial sending back from Go Back N receiver--- "+initialAckBytes);
	}
	
	
	public void receiveFile(DatagramPacket receivePacket) throws IOException,ClassNotFoundException{
		 
		byte[] receiverData = new byte[1024];
		ArrayList<Packet> packetReceived = new ArrayList<Packet>();
		int expectedSeqNo = 0;
		Boolean flag = true;
		Boolean checkSum = true;
		while(flag) {
			DatagramPacket incomingPacket = new DatagramPacket(receiverData, receiverData.length);
			receiverSocket.receive(incomingPacket);
			//System.out.println("***********"+incomingPacket.getData());
        	Packet packet = (Packet) Serializer.bytesToObject(incomingPacket.getData());
        	//System.out.println("Incoming squence number  "+new String(packet.getData())+ "is last flag: "+packet.isLast());
        	System.out.println("=======Packet received =======> "+packet.getSeqNo());
        	if(packet.getCheckSum() !=  CheckSum(packet.getData())) {
        		System.out.println("Check sum not equal");
        		System.out.println("********Error Occurred in the packet********");
        		//packet.setSeqNo(-1);
        		checkSum = false;
        		
        	}
        	
        	if (expectedSeqNo == packet.getSeqNo() && packet.isLast() && checkSum) {

        		System.out.println("last packet"+packet.isLast());
        		expectedSeqNo++;
        		packetReceived.add(packet);
        		flag = false;

			}else if(expectedSeqNo == packet.getSeqNo() && !packet.isLast() && checkSum) {
        		
        		packetReceived.add(packet);
        		expectedSeqNo++;
        		System.out.println("inside if ");
         	}
        	else {
        		System.out.println("No conditions satisfied");
        		packet.setSeqNo(-1);
        	}
        	ACK ack = new ACK(expectedSeqNo);
			byte[] ackBytes = Serializer.covertToBytes(ack);
			
			if (Math.random() > LOST_PACKET_PROBABILITY && checkSum && packet.getSeqNo() != -1) {
				
				System.out.println("sending ACK back!!!!! with packet number =====>  "+(expectedSeqNo-1));
				DatagramPacket replyPacket = new DatagramPacket(ackBytes, ackBytes.length, receivePacket.getAddress(), receivePacket.getPort());
				receiverSocket.send(replyPacket);
				
			}else {
				
				if(checkSum && packet.getSeqNo() != -1) {
					int length = packetReceived.size();
					System.out.println("========== Packet Lost =============="+(expectedSeqNo-1));
					if(length > 0) {
						packetReceived.remove(length - 1);	
					}
					
					expectedSeqNo--;
					if(expectedSeqNo < 0) {
						expectedSeqNo = 0;
					}
					flag = true;
					
				}
				
			}
			
			
			
			
        	
		}
		
		System.out.print("*******Printing data**************");
		
		for(Packet p : packetReceived){
		  for(byte b: p.getData()){
			   System.out.print((char) b);
			}
		}
	  
	}
	
	private static int CheckSum(byte[] message) {
        int checksum = 0;
        for (int i = 0; i < message.length; i++) {
            checksum += message[i];
        }
        return checksum;
    }
 

}
