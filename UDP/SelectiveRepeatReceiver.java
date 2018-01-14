/**
 * 
 */
//package UDP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;


/**
 * @author Ajay
 *
 */
public class SelectiveRepeatReceiver {

	/**
	 * @param args
	 */
	
	private DatagramSocket receiverSocket;
	private int windowSize;
	private int segmentSize;
	private String protocolName;
	private int noOfPackets;
	public static final double LOST_PACKET_PROBABILITY = 0.1;
	
	public SelectiveRepeatReceiver(String protocol, DatagramSocket receiverSocket){
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
    	
	}
	
	public void receiveFile(DatagramPacket receivePacket) throws IOException,ClassNotFoundException{
		 
		byte[] receiverData = new byte[1024];
		ArrayList<Packet> packetReceived = new ArrayList<Packet>();
		ArrayList<Packet> store = new ArrayList<>();
		int expectedSeqNo = 0;
		Boolean flag = true;
		
		while(flag) {
			Boolean checkSumFlag = true;
			DatagramPacket incomingPacket = new DatagramPacket(receiverData, receiverData.length);
			receiverSocket.receive(incomingPacket);
			
			
        	Packet packet = (Packet) Serializer.bytesToObject(incomingPacket.getData());
        	//System.out.println("*******incoming data****"+packet.getSeqNo());
        	//System.out.println(new String(packet.getData()));
        	//SelectiveRepeatReceiver receiver = new SelectiveRepeatReceiver();
        	//System.out.println("before check sume ");
        	if(packet.getData() == null) {
        		System.out.println("Data is empty");
        	}else {
        	if(packet.getCheckSum() !=  CheckSum(packet.getData())) {
        		checkSumFlag = false;
        		System.out.println("Check sum not equal");
        		System.out.println("********Error Occurred in the packet********");
        	}
        	
        	if (expectedSeqNo == packet.getSeqNo() && packet.isLast() && checkSumFlag ) {

        		System.out.println("**** Inside first if ******");
        		expectedSeqNo++;
        		packetReceived.add(packet);
        		int newValue = sendAck(receivePacket, packet.getSeqNo(), expectedSeqNo, receivePacket.getAddress(),receivePacket.getPort(),true,checkSumFlag);
                if(newValue < expectedSeqNo) {
                	int length = packetReceived.size();
                	expectedSeqNo = newValue;
					System.out.println("========= Packet Lost ==============> "+expectedSeqNo);
					if(length > 1) {
					packetReceived.remove(length - 1);}
					flag = true;
                	
                }else {
                	flag = false;
                	System.out.println("last packet======================>");
                }

			}
        	else if(expectedSeqNo == packet.getSeqNo() && !packet.isLast() && checkSumFlag && store.size() == 0) {
        		System.out.println("**** Inside first if ******");
        		expectedSeqNo++;
        		packetReceived.add(packet);
        		int newValue = sendAck(receivePacket, packet.getSeqNo(), expectedSeqNo, receivePacket.getAddress(),receivePacket.getPort(),true,checkSumFlag);
                if(newValue < expectedSeqNo) {
                	int length = packetReceived.size();
                	expectedSeqNo = newValue;
					System.out.println("========= Packet Lost ==============> "+expectedSeqNo);
					if(length > 1) {
					packetReceived.remove(length - 1);}
                	
                }
        	
        	}else if (expectedSeqNo == packet.getSeqNo() && checkSumFlag && store.size() > 0) {
                System.out.println("Second if");
        		packetReceived.add(packet);
        		expectedSeqNo++;
				int newValue = sendAck(receivePacket, packet.getSeqNo(), expectedSeqNo, receivePacket.getAddress(),receivePacket.getPort(),false,checkSumFlag);
				if (newValue < expectedSeqNo) {
					int length = packetReceived.size();
					System.out.println("========= Packet Lost ==============> "+expectedSeqNo);
					if(length > 1) {
					packetReceived.remove(length - 1);}
                	expectedSeqNo = newValue;

				} else {
					ArrayList<Packet> tempArray = new ArrayList<>();
					tempArray.addAll(store);
					int count = 0;
					for (int i = 0; i < tempArray.size(); i++) {
						if (!(expectedSeqNo == tempArray.get(i).getSeqNo())) {

							break;

						} else {
							System.out.println("=======Packet from buffer ========>" + tempArray.get(i).getSeqNo());
							expectedSeqNo++;
							count++;
							
						}
					}
					store = new ArrayList<>();
					for (int j = 0; j < tempArray.size(); j++) {
						if (j < count) {
							continue;
						}
						store.add(tempArray.get(j));
					}
					if (packet.isLast()) {
						flag = false;
					}

				}

			}
        	else if(expectedSeqNo > packet.getSeqNo() && checkSumFlag) {
        		//packet which is already received is handled here
        		System.out.println("**** Inside third if ******");
        		sendAck(receivePacket, packet.getSeqNo(), expectedSeqNo, receivePacket.getAddress(),receivePacket.getPort(),true,checkSumFlag);
        		System.out.println("==========Packet already received============");
        	}else if(expectedSeqNo < packet.getSeqNo() && checkSumFlag) {
        		// receiving packets greater than expteced
        		System.out.println("**** Inside fourth if ******"+packet.getSeqNo());
        		sendAck(receivePacket, packet.getSeqNo(), expectedSeqNo, receivePacket.getAddress(),receivePacket.getPort(),true,checkSumFlag);
				store.add(packet);
        	}else {
        		
        	}}
        	
        	//if
        	
        	
        	
        	
        	/*if(expectedSeqNo == packet.getSeqNo() && !packet.isLast() && checkSumFlag) {
        		
        		packetReceived.add(packet);
        		expectedSeqNo++;
        		System.out.println("inside if ");
         	}
        	else if(packet.isLast() && expectedSeqNo == packet.getSeqNo() && checkSumFlag) {
        		// check packet is last one or not
        		System.out.println("last packet");
        		expectedSeqNo++;
        		packetReceived.add(packet);
        		flag = false;
        	}else if (packet.getSeqNo() < expectedSeqNo && checkSumFlag) {
				
        		System.out.println("******* Sending duplicate Ack *********");
        		sendAck(receivePacket, packet.getSeqNo(), expectedSeqNo, receivePacket.getAddress(),receivePacket.getPort(),checkSumFlag);
				

			}*/
        	
		}
		
		System.out.print("*******Printing data**************");
		
		for(Packet p : packetReceived){
		  for(byte b: p.getData()){
			   System.out.print((char) b);
			}
		}
	  
	}
	
	public int sendAck(DatagramPacket receivePacket, int seqNo, int expectedSeqNo, InetAddress iPAddress,int port, Boolean flag, Boolean checkSumFlag) throws IOException {

		ACK ack = new ACK(seqNo+1);
		byte[] ackBytes = Serializer.covertToBytes(ack);
		DatagramPacket replyPacket = new DatagramPacket(ackBytes, ackBytes.length, receivePacket.getAddress(), receivePacket.getPort());
		//receiverSocket.send(replyPacket);

		if ((Math.random() > LOST_PACKET_PROBABILITY)) {
			if(checkSumFlag) {
			System.out.println("===========Sending Ack for packet===========> "+(ack.getAckNo() - 1));
			receiverSocket.send(replyPacket);
		} 
		}else if (flag) {
			
			expectedSeqNo--;
			
		}
		return expectedSeqNo;
	}
	
	private static int CheckSum(byte[] message) {
        int checksum = 0;
        System.out.println("checksum "+message);
        for (int i = 0; i < message.length; i++) {
            checksum += message[i];
        }
        return checksum;
    }
 
 

}
