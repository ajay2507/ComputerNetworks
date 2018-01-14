/**
 * 
 */
//package UDP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Ajay
 *
 */
public class GoBackNSender {

	/**
	 * @param args
	 */
	private int noOfBits;
	private int noOfPackets;
	private int windowSize;
	private int timeout;
	private int segmentSize;
	private long startTime;
	private long stopTime;
	private String protocolName;
	private DatagramSocket senderSocket;
	private int sf = 0;
	private int sn = 0;
	private int lastSeqNo = 0;
	private int checkSum = 0;
	private byte[] byteArray;
	private byte[] initialSenderArray;
	private byte[] initialAckByteArray;
	private InetAddress ipAddress;
	int portNo;
	private Boolean isLast;
	public static final double LOST_ACK_PROBAB = 0.05;
    public static final double BIT_ERROR_PROB = 0.1;
	
	public GoBackNSender(String protocol, int timeout, int windowSize, int segmentSize, int portNo ){

		this.protocolName = protocol;
		this.windowSize = windowSize;
		this.timeout = timeout;
		this.windowSize = windowSize;
		this.segmentSize = segmentSize;
		this.portNo = portNo;
		this.initialAckByteArray = new byte[1024];
		//this.noOfPackets = noOfPackets;
	}
	
	public GoBackNSender() {
		
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		 
		 /*GoBackNSender
		 sender.processFile();
		*/
	}
	
	public void processFile() throws IOException, ClassNotFoundException{
		
		//System.out.println("inside process method");
		this.senderSocket = new DatagramSocket();
	    //System.out.println("inside process file");
		// Reading the input data from a file
		//System.getProperty("user.dir")+"\\"
		File file = new File(System.getProperty("user.dir")+"\\InputFile.txt");
		FileInputStream fsStream = new FileInputStream(file);
		StringBuilder sb=new StringBuilder();
		BufferedReader br=new BufferedReader(new InputStreamReader(fsStream,"UTF-8"));
		String line = "";
		String inputFileData = "";
		while((line=br.readLine())!=null)
		{
			sb.append(line);
			
		}
		inputFileData=sb.toString();
		
		byteArray = new byte[(int) inputFileData.getBytes().length];
		byteArray = inputFileData.getBytes();
		//System.out.println("input data "+new String(byteArray));
        noOfPackets = byteArray.length/this.segmentSize; 
        
        ipAddress = InetAddress.getByName("localhost");
        
        // convert packet object to byte array
        Packet initialPacketObject = new Packet(protocolName, 0,initialSenderArray,false,true,checkSum);
        byte[] initialSenderArray = Serializer.covertToBytes(initialPacketObject);
        
        // sending initial request
        DatagramPacket sendPacket=new DatagramPacket(initialSenderArray,initialSenderArray.length,ipAddress,portNo);
        System.out.println("!-----------Sending initial packet------------!");
        senderSocket.send(sendPacket);
        //calculate the last squence number
        
        
        lastSeqNo = (int)Math.ceil( (double) byteArray.length / segmentSize );
        // getting initial Ack
        byte[] receiveData=new byte[1024];
        DatagramPacket initialResp = new DatagramPacket(receiveData, receiveData.length);
		senderSocket.receive(initialResp);
		System.out.println("!---Getting initial Ack from server---!");
		ArrayList<Packet> packetSent = new ArrayList<Packet>();
		ACK initialAck = (ACK) Serializer.bytesToObject(initialResp.getData());
		//System.out.println("initial Response----> "+initialAck.getAckNo() + " last seq no"+lastSeqNo);
	    
		if(initialAck.getAckNo() == 0) {
			while(true) {
				while(sn - sf < windowSize && sn < lastSeqNo) {
					System.out.println("Go Back N Sender side ==> SF =>"+sf+" SN => "+sn);
					byte[] packetsToBeTransmit = new byte[segmentSize];
					//segmenting the input array
					packetsToBeTransmit = Arrays.copyOfRange(byteArray, sn*segmentSize , sn*segmentSize  + segmentSize );
					
					isLast = sn == lastSeqNo-1? true:false;
					checkSum = CheckSum(packetsToBeTransmit);
					System.out.println("Sender side checksum ====> "+checkSum);
					Packet packetObject = new Packet(protocolName, sn, packetsToBeTransmit,isLast, false,checkSum);
					
					// serialize the input object
					byte[] sendByteArray = Serializer.covertToBytes(packetObject);
					DatagramPacket packet = new DatagramPacket(sendByteArray, sendByteArray.length, ipAddress, portNo );
					//packetSent.add(packetObject);
					System.out.println("====== sending packet =========> "+ packetObject.getSeqNo());
					packetSent.add(packetObject);
					//senderSocket.setSoTimeout(timeout);
					senderSocket.send(packet);
					sn = sn+1;
			   }
				
			try {
				senderSocket.setSoTimeout(timeout);
				// receiving ack from receiver
				DatagramPacket receivingAck = new DatagramPacket(receiveData, receiveData.length);
				
				senderSocket.receive(receivingAck);
				
				ACK ack = (ACK) Serializer.bytesToObject(receivingAck.getData());
				
				if (Math.random() > LOST_ACK_PROBAB) {
					System.out.println("====== Received Ack for packet number =======>"+ (ack.getAckNo()-1));
					// move the window based on the ack
					sf = Math.max(sf, ack.getAckNo());
					
				}else {
					System.out.println("======Ack lost for packet number =======>"+ (ack.getAckNo()-1));
				}
				
				
				if(ack.getAckNo() == lastSeqNo) {
					break;
					//System.out.println();
				}
		} catch (SocketTimeoutException e) {
			
			 System.out.println("*** Timeout *******");
			  String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			  for(int i = sf; i < sn; i++) {
				  Packet packetData = packetSent.get(i);
				  checkSum = CheckSum(packetData.getData());
				  System.out.println("*******Generating random characters******");
				  byte[] packetsToBeTransmit = packetData.getData();
				   if (Math.random() <= BIT_ERROR_PROB) {
					   
					   StringBuilder salt = new StringBuilder();
					   Random rnd = new Random();
					   while (salt.length() < 5) { 
						    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
						     salt.append(SALTCHARS.charAt(index));
						}
						        
						packetData.setData(salt.toString().getBytes());
						System.out.println("*******Bit error prob******"+salt.toString());
				   }
					Packet packetObject = new Packet(protocolName, packetData.getSeqNo(), packetsToBeTransmit,isLast, false,checkSum);
					byte[] sendByteArray = Serializer.covertToBytes(packetObject);
					DatagramPacket packet = new DatagramPacket(sendByteArray, sendByteArray.length, ipAddress, portNo );
					packetSent.add(packetObject);
					senderSocket.send(packet);
						
					System.out.println("=====Resending Packet for packet no ========>"+packetData.getSeqNo()+" Sf==>"+sf);
			
		}

	
	}
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
