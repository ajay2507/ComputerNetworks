//package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GBNSRReceiver {
    
	private static DatagramSocket receiverSocket;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
	byte[] receiverData = new byte[1024];
	receiverSocket = new DatagramSocket(5000);
	System.out.println("***********Receiver - Accepting the packets****************");
	if(true) {
    	//portNo = Integer.parseInt(args[1]);
    	DatagramPacket receivePacket=new DatagramPacket(receiverData,receiverData.length);
    	//int portNo = Integer.parseInt(args[0]);
    	int portNo = 5000;
    	receiverSocket.receive(receivePacket);
    	System.out.println("***********"+receivePacket.getData());
    	Packet packet = (Packet) Serializer.bytesToObject(receivePacket.getData());
    	InetAddress IPAddress = InetAddress.getByName("localhost");
    	System.out.println("Packet with sequence number " + packet.getSeq() + " initial data " + packet.isInitialAck());
    	if(packet.getProtocol().equalsIgnoreCase("Gbn")) {
    		System.out.println("*********** GO BACK N Receiver **************");
    		GoBackNReceiver gbrReceiver = new GoBackNReceiver(packet.getProtocol(),receiverSocket);
    		gbrReceiver.sendInitialAck(receivePacket, IPAddress, portNo);
    		gbrReceiver.receiveFile(receivePacket);
	    }else {
	    	System.out.println("*********** SR Receiver **************");
	    	SelectiveRepeatReceiver SRR = new SelectiveRepeatReceiver(packet.getProtocol(),receiverSocket);
	    	 SRR.sendInitialAck(receivePacket, IPAddress, portNo);
	    	 SRR.receiveFile(receivePacket);
	      }
    	}
	}
}
