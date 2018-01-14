/**
 * 
 */
//package UDP;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;


/**
 * @author Ajay
 *
 */
public class SelectiveRepeatSender {

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
	
	public SelectiveRepeatSender(String protocol, int timeout, int windowSize, int segmentSize, int portNo ){

		this.protocolName = protocol;
		this.windowSize = windowSize;
		this.timeout = timeout;
		this.windowSize = windowSize;
		this.segmentSize = segmentSize;
		this.portNo = portNo;
		this.initialAckByteArray = new byte[1024];
		//this.noOfPackets = noOfPackets;
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
		
		FileInputStream fsStream;
		try {
		// reading the input commands from the given file
			System.out.println(System.getProperty("user.dir")+"\\");
		File file = new File("C:\\Users\\Ajay\\Documents\\Java\\UDP_Protocols\\src\\UDP\\InputCommand.txt");
	    fsStream = new FileInputStream(file);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(fsStream, "UTF-8"));
		String line;
		ArrayList<String> list = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			
		    list.add(line);
		}
		String[] stringArray = list.get(1).split(" ");
		int windowSize = Integer.parseInt(stringArray[1]);
		//System.out.println("----size + "+windowSize);
		
		
		SelectiveRepeatSender srSender = new SelectiveRepeatSender(list.get(0),Integer.parseInt(list.get(2)),windowSize,
				Integer.parseInt(list.get(3)),5000);
		
		srSender.processFile();
	}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
	}
	
	public void processFile() throws IOException, ClassNotFoundException {
		
		this.senderSocket = new DatagramSocket();
	    System.out.println("inside process file");
		// Reading the input data from a file
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
		
       // noOfPackets = byteArray.length/this.segmentSize; 
        
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
      //  System.out.println("bytearray lenth: "+byteArray.length+"last seq no "+lastSeqNo);
        /*if(noOfPackets < lastSeqNo) {
        	lastSeqNo = noOfPackets;
        }*/
        // getting initial Ack
        byte[] receiveData=new byte[1024];
        DatagramPacket initialResp = new DatagramPacket(receiveData, receiveData.length);
		senderSocket.receive(initialResp);
		System.out.println("!---Getting initial Ack from server---!");
		ACK initialAck = (ACK) Serializer.bytesToObject(initialResp.getData());
		System.out.println("initial Response----> "+initialAck.getAckNo() + " last seq no"+lastSeqNo);
		ArrayList<Packet> packetSent = new ArrayList<Packet>();
		HashSet<Integer> visitedList = new HashSet<>();
		if(initialAck.getAckNo() == 0) {
			
			
			while(true) {
				
				while(sn - sf < windowSize && sn < lastSeqNo) {
					System.out.println("Sender side ==> SF =>"+sf+" SN=>"+sn);
					byte[] packetsToBeTransmit = new byte[segmentSize];
					//copy the transmitting packets
					packetsToBeTransmit = Arrays.copyOfRange(byteArray, sn*segmentSize , sn*segmentSize  + segmentSize );
				
					isLast = sn == lastSeqNo-1? true:false;
					System.out.println("Is Last flag: "+isLast);
					checkSum = CheckSum(packetsToBeTransmit);
					System.out.println("Sender side checksum ====> "+checkSum);
					Packet packetObject = new Packet(protocolName, sn, packetsToBeTransmit,isLast, false,checkSum);
					
					// serialize the input object
					byte[] sendByteArray = Serializer.covertToBytes(packetObject);
					DatagramPacket packet = new DatagramPacket(sendByteArray, sendByteArray.length, ipAddress, portNo );
					packetSent.add(packetObject);
					System.out.println("!==== Sending packets with packet number ====> " +sn);
					//System.out.println(new String(packetsToBeTransmit));
					//senderSocket.setSoTimeout(timeout);
					senderSocket.send(packet);
					sn = sn+1;
				} 
				try {
				// set the timer
				senderSocket.setSoTimeout(timeout);
				// receiving ack from receiver
				DatagramPacket receivingAck = new DatagramPacket(receiveData, receiveData.length);
				
				senderSocket.receive(receivingAck);
				
				ACK ack = (ACK) Serializer.bytesToObject(receivingAck.getData());
				
				if (Math.random() > LOST_ACK_PROBAB) {
					System.out.println("======Received Ack for packet number =======>"+ (ack.getAckNo()-1));
					//check to move the window
					if ((ack.getAckNo() - sf) == 1) {
						sf = sf+1;
						
						for(int i = sf; i<sn; i++) {
							if(visitedList.contains(i)) {
								sf++;
								visitedList.remove(i);
							}else {
								break;
							}
						}
						
					}else {
						visitedList.add(ack.getAckNo()-1);
					}
				
				}else {
					System.out.println("======Ack lost for packet number =======>"+ (ack.getAckNo()-1));
				}
				
				
		        
				// check the ack is for last packet or not
				System.out.println("sf ==>"+sf+"last ==>"+lastSeqNo);
				if(sf == lastSeqNo && visitedList.isEmpty()){
					break;
				}
				
				}catch(SocketTimeoutException e) {
					  System.out.println("*** Timeout *******");
					  String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
					  for(int i = sf; i < sn; i++) {
						  Packet packetData = packetSent.get(i);
							if (!(visitedList.contains(packetData.getSeqNo()))) {
                                
								checkSum = CheckSum(packetData.getData());
								System.out.println("*******Generating random characters******"+checkSum);
								byte[] packetsToBeTransmit = packetData.getData();
								if (Math.random() <= BIT_ERROR_PROB) {
									    StringBuilder salt = new StringBuilder();
								        Random rnd = new Random();
								        while (salt.length() < 5) { 
								            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
								            salt.append(SALTCHARS.charAt(index));
								        }
								        
								        packetData.setData(salt.toString().getBytes());
								        System.out.println("bit error"+"---"+salt.toString());

								}
								System.out.println("packets to be transmitted"+packetsToBeTransmit.toString());
							
								Packet packetObject = new Packet(protocolName, packetData.getSeqNo(), packetsToBeTransmit,isLast, false,checkSum);
								byte[] sendByteArray = Serializer.covertToBytes(packetObject);
								DatagramPacket packet = new DatagramPacket(sendByteArray, sendByteArray.length, ipAddress, portNo );
								packetSent.add(packetObject);
								senderSocket.send(packet);
								
								System.out.println("=====Resending Packet for packet no ========>"+packetData.getSeqNo());
					  }
					
				}
				
				
				
				
			}
			
		}
		
     
		
	}else {
		System.out.println("******Initial Connection Failed********");
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
