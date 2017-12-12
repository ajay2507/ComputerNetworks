import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;


/**
 * @author Ajay
 *
 */



class DVRSender extends Thread {
	private DatagramSocket outSocket;
	private DVRRouter tableSent;
	private int PORT;
	DatagramPacket outPacket = null;

	public DVRSender(DatagramSocket updateSocket, DVRRouter tableSent, int portNumber) {
		this.outSocket = updateSocket;
		this.tableSent = tableSent;
		this.PORT = portNumber;
	}

	public void run() {
		int updateCount = 0;
		try {
			while(true) {
				Thread.sleep(15000);
				tableSent.readCostChange();
				InetAddress address = InetAddress.getByName("224.0.0.0");
		        ByteArrayOutputStream boutStream = new ByteArrayOutputStream();
		  	    ObjectOutputStream ooutStream = new ObjectOutputStream(boutStream);
		  	    ooutStream.writeObject(tableSent);
	           
		  	    byte[] buff = boutStream.toByteArray();
	            outPacket = new DatagramPacket(buff, buff.length, address, PORT);
		        outSocket.send(outPacket);
		        System.out.println("Output Number "+ ++updateCount+":");
		        tableSent.printTable(tableSent.getTable());
		        System.out.println();
		        try {
		          Thread.sleep(500);
		        } catch (InterruptedException ie) {
		        }
			}
		}
		catch (IOException ioe) {
		      System.out.println(ioe);
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}



public class DVRStarter {

	public static void main(String[] args) throws SocketException, ClassNotFoundException {
		
		System.out.println("----------Distance Vector Routing Protocol---------");
		System.out.println("File Name "+args[1]+"port No: "+args[0]);
		String path = System.getProperty("user.dir")+"\\"+args[1];
        ByteArrayInputStream bInStream;
		ObjectInputStream oInStream;
		MulticastSocket socket = null;
		System.setProperty("java.net.preferIPv4Stack", "true");
		DatagramSocket outSocket = new DatagramSocket();
		InetAddress groupAddress;
		byte[] inBuf = new byte[10240];
		DatagramPacket inPacket = new DatagramPacket(inBuf, inBuf.length);;

		try {
			socket = new MulticastSocket(Integer.parseInt(args[0]));
			groupAddress = InetAddress.getByName("224.0.0.0");
			socket.joinGroup(groupAddress);
            DVRRouter router = new DVRRouter();
            router.createRoutingTable(path);
			new DVRSender(outSocket, router, Integer.parseInt(args[0])).start();

			while(true) {
				// reveiveing the UDP packet
				socket.receive(inPacket);
				byte[] data = inPacket.getData();
				bInStream = new ByteArrayInputStream(data);
				oInStream = new ObjectInputStream(bInStream);
				DVRRouter objectReceived = (DVRRouter) oInStream.readObject();
				ArrayList<String> neigh = new ArrayList<String>(router.getImmediateNeighbors());
				//System.out.println(router.getRouterName());
				neigh.remove(router.getRouterName());
				if(neigh.contains(objectReceived.getRouterName())) {
					router.updateRouterTable(router, objectReceived);
				}
			}
		}
		catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}


